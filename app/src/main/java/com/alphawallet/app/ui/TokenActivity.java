package com.alphawallet.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.alphawallet.app.BuildConfig;
import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.ConfirmationType;
import com.alphawallet.app.entity.StandardFunctionInterface;
import com.alphawallet.app.entity.Transaction;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.entity.tokenscript.TokenScriptRenderCallback;
import com.alphawallet.app.entity.tokenscript.WebCompletionCallback;
import com.alphawallet.app.repository.EventResult;
import com.alphawallet.app.repository.TransactionsRealmCache;
import com.alphawallet.app.repository.entity.RealmAuxData;
import com.alphawallet.app.repository.entity.RealmTransaction;
import com.alphawallet.app.ui.widget.entity.TokenTransferData;
import com.alphawallet.app.util.BalanceUtils;
import com.alphawallet.app.util.KeyboardUtils;
import com.alphawallet.app.util.Utils;
import com.alphawallet.app.viewmodel.TokenFunctionViewModel;
import com.alphawallet.app.viewmodel.TokenFunctionViewModelFactory;
import com.alphawallet.app.web3.OnSetValuesListener;
import com.alphawallet.app.web3.Web3TokenView;
import com.alphawallet.app.web3.entity.Address;
import com.alphawallet.app.web3.entity.PageReadyCallback;
import com.alphawallet.app.widget.ChainName;
import com.alphawallet.app.widget.EventDetailWidget;
import com.alphawallet.app.widget.FunctionButtonBar;
import com.alphawallet.app.widget.SystemView;
import com.alphawallet.app.widget.TokenIcon;
import com.alphawallet.token.entity.TSActivityView;
import com.alphawallet.token.entity.TSTokenView;
import com.alphawallet.token.entity.TokenScriptResult;
import com.alphawallet.token.tools.Numeric;
import com.alphawallet.token.tools.TokenDefinition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

import static com.alphawallet.app.C.ETH_SYMBOL;
import static com.alphawallet.app.entity.TransactionDecoder.FUNCTION_LENGTH;
import static com.alphawallet.app.service.AssetDefinitionService.ASSET_DETAIL_VIEW_NAME;
import static com.alphawallet.app.ui.widget.holder.TransactionHolder.TRANSACTION_BALANCE_PRECISION;
import static com.alphawallet.ethereum.EthereumNetworkBase.MAINNET_ID;


/**
 * Created by JB on 6/08/2020.
 */
public class TokenActivity extends BaseActivity implements PageReadyCallback, StandardFunctionInterface,
                                                            TokenScriptRenderCallback, WebCompletionCallback, OnSetValuesListener
{
    @Inject
    protected TokenFunctionViewModelFactory tokenFunctionViewModelFactory;
    private TokenFunctionViewModel viewModel;

    private TokenIcon icon;
    private FunctionButtonBar functionBar;
    private String eventKey;
    private String transactionHash;
    private Token token;
    private StringBuilder attrs;
    private Web3TokenView tokenView;
    private EventDetailWidget eventDetail;
    private int parsePass;
    private final Map<String, String> args = new HashMap<>();
    private TSTokenView scriptViewData;
    private BigInteger tokenId;
    private Realm realm;
    private final Handler handler = new Handler();
    private boolean isFromTokenHistory = false;
    private long pendingStart = 0;
    private TokenTransferData transferData;

    @Nullable
    private Disposable pendingTxUpdate = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_activity);

        eventKey = getIntent().getStringExtra(C.EXTRA_ACTION_NAME);
        transactionHash = getIntent().getStringExtra(C.EXTRA_TXHASH);
        isFromTokenHistory = getIntent().getBooleanExtra(C.EXTRA_STATE, false);
        transferData = getIntent().getParcelableExtra(C.EXTRA_TOKEN_ID);
        //TODO: Send event details
        icon = findViewById(R.id.token_icon);

        SystemView systemView = findViewById(R.id.system_view);
        systemView.hide();
        toolbar();
        setTitle(getString(R.string.activity_label));

        findViewById(R.id.layout_select_ticket).setVisibility(View.GONE);

        tokenId = BigInteger.ZERO;
        eventDetail = findViewById(R.id.event_detail);
    }

    private void setupViewModel()
    {
        viewModel = new ViewModelProvider(this, tokenFunctionViewModelFactory)
                .get(TokenFunctionViewModel.class);
        viewModel.walletUpdate().observe(this, this::onWallet);
    }

    private void initViews()
    {
        tokenView = findViewById(R.id.web3_tokenview);
        functionBar = findViewById(R.id.layoutButtons);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        initViews();
        if (viewModel == null)
        {
            setupViewModel();
        }

        viewModel.getCurrentWallet();
        viewModel.restartServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_view_transaction_details)
        {
            viewModel.showTransactionDetail(this, transactionHash, token.tokenInfo.chainId);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) realm.close();
        stopPendingUpdate();
    }

    private void onWallet(Wallet wallet)
    {
        if (!TextUtils.isEmpty(eventKey))
        {
            handleEvent(wallet);
        }
        else
        {
            handleTransaction(wallet);
        }

        setupFunctions();
    }

    private void setupFunctions()
    {
        if (token != null)
        {
            functionBar.revealButtons();
            List<Integer> functions = new ArrayList<>();
            if (pendingTxUpdate != null)
            {
                functions.add(R.string.speedup_transaction);
                functions.add(R.string.go_to_token);
                functions.add(R.string.cancel_transaction);
            }
            else
            {
                functions.add(R.string.go_to_token);
            }

            functionBar.setupFunctions(this, functions);
        }
    }

    private void handleTransaction(Wallet wallet)
    {
        Transaction transaction = viewModel.fetchTransaction(transactionHash);
        if (transaction == null) return;

        TextView eventTime = findViewById(R.id.event_time);
        TextView eventAmount = findViewById(R.id.event_amount);
        TextView eventAction = findViewById(R.id.event_action);
        TextView eventActionSymbol = findViewById(R.id.event_action_symbol);
        //date
        eventTime.setText(Utils.localiseUnixTime(getApplicationContext(), transaction.timeStamp));
        //icon
        token = getOperationToken(transaction);
        String sym = token != null ? token.tokenInfo.symbol : ETH_SYMBOL;
        icon.bindData(token, viewModel.getAssetDefinitionService());
        //status
        if (token != null) icon.setStatusIcon(token.getTxStatus(transaction));

        String operationName = token.getOperationName(transaction, this);

        transaction.getDestination(token);
        eventAction.setText(operationName);
        eventActionSymbol.setText(sym);
        //amount
        String transactionValue = token.getTransactionResultValue(transaction, TRANSACTION_BALANCE_PRECISION);

        if (!token.shouldShowSymbol(transaction) && transaction.input.length() >= FUNCTION_LENGTH)
        {
            eventAmount.setText(transaction.input.substring(0, FUNCTION_LENGTH));
            eventActionSymbol.setText(getString(R.string.sent_to, token.getFullName()));
        }
        else if (TextUtils.isEmpty(transactionValue))
        {
            eventAmount.setVisibility(View.GONE);
        }
        else
        {
            eventAmount.setText(transactionValue);
        }

        if (transaction.isPending())
        {
            //listen for token completion
            setupPendingListener(wallet);
            pendingStart = transaction.timeStamp;
            icon.startPendingSpinner(transaction.timeStamp, viewModel.fetchExpectedTxTime(transactionHash)/1000);
        }

        String supplementalTxt = transaction.getSupplementalInfo(token.getWallet(), viewModel.getTokensService().getNetworkName(token.tokenInfo.chainId));
        if (!TextUtils.isEmpty(supplementalTxt))
        {
            eventDetail.setupTransactionView(transaction, token, viewModel.getAssetDefinitionService(), supplementalTxt);
        }
        else if (token.isERC721())
        {
            setupERC721TokenDetail(transaction);
        }

        setChainName(transaction);
    }

    private void setupPendingListener(Wallet wallet)
    {
        realm = viewModel.getRealmInstance(wallet);
        startPendingUpdate();
    }

    private void startPendingUpdate()
    {
        //now set up the transaction pending time
        LinearLayout txPending = findViewById(R.id.pending_time_layout);
        txPending.setVisibility(View.VISIBLE);

        pendingTxUpdate = Observable.interval(0, 1, TimeUnit.SECONDS)
                .doOnNext(l -> {
                    runOnUiThread(() -> {
                        long pendingTimeInSeconds = (System.currentTimeMillis() / 1000) - pendingStart;
                        TextView pendingText = findViewById(R.id.pending_time);
                        if (pendingText != null) pendingText.setText(getString(R.string.transaction_pending_for, Utils.convertTimePeriodInSeconds(pendingTimeInSeconds, this)));
                        checkForUpdate();
                    });
                }).subscribe();
    }

    private void checkForUpdate()
    {
        RealmTransaction realmTransaction = realm.where(RealmTransaction.class)
                .equalTo("hash", transactionHash)
                .findFirst();

        if (realmTransaction != null && !realmTransaction.isPending())
        {
            Transaction tx = TransactionsRealmCache.convert(realmTransaction);
            //tx written, update icon
            handler.post(() -> {
                icon.setStatusIcon(token.getTxStatus(tx));
            });
            stopPendingUpdate();
        }
    }

    private void stopPendingUpdate()
    {
        //now set up the transaction pending time
        LinearLayout txPending = findViewById(R.id.pending_time_layout);
        if (txPending != null) txPending.setVisibility(View.GONE);

        if (pendingTxUpdate != null && !pendingTxUpdate.isDisposed()) pendingTxUpdate.dispose();
        pendingTxUpdate = null;
    }

    private Token getOperationToken(Transaction tx)
    {
        String operationAddress = tx.getOperationTokenAddress();
        Token operationToken = viewModel.getTokensService().getToken(tx.chainId, operationAddress);
        if (operationToken == null && transferData != null)
        {
            operationToken = viewModel.getTokensService().getToken(tx.chainId, transferData.tokenAddress);
        }

        if (operationToken == null)
        {
            operationToken = viewModel.getCurrency(tx.chainId);
        }

        return operationToken;
    }

    private void handleEvent(Wallet wallet)
    {
        RealmAuxData item = viewModel.getTransactionsInteract().fetchEvent(wallet.address, eventKey);
        if (item != null)
        {
            transactionHash = item.getTransactionHash();
            Transaction transaction = viewModel.fetchTransaction(transactionHash);
            TextView eventTime = findViewById(R.id.event_time);
            TextView eventAmount = findViewById(R.id.event_amount);
            TextView eventAction = findViewById(R.id.event_action);
            TextView eventActionSymbol = findViewById(R.id.event_action_symbol);
            //handle info
            //date
            eventTime.setText(Utils.localiseUnixTime(getApplicationContext(), item.getResultTime()));
            //icon
            token = viewModel.getToken(item.getChainId(), item.getTokenAddress());
            tokenId = determineTokenId(item);

            if (transaction == null || token == null)
            {
                return; //shouldn't get here.
            }

            String sym = token != null ? token.tokenInfo.symbol : ETH_SYMBOL;
            icon.bindData(token, viewModel.getAssetDefinitionService());
            //status
            icon.setStatusIcon(item.getEventStatusType());
            //amount
            String transactionValue = getEventAmount(item, transaction, true);
            if (TextUtils.isEmpty(transactionValue))
            {
                eventAmount.setVisibility(View.GONE);
            }
            else
            {
                eventAmount.setText(getString(R.string.valueSymbol, transactionValue, sym));
            }
            //action
            eventAction.setText(item.getTitle(getApplicationContext()));
            eventActionSymbol.setText(sym);

            //Is the token an NFT and does the event hold tokenId data?
            if (token != null)
            {
                populateActivityInfo(item, getEventAmount(item, transaction, false));
            }

            setChainName(transaction);
        }
    }

    private BigInteger determineTokenId(RealmAuxData item)
    {
        if (token != null && token.isNonFungible() && item.getEventResultMap().containsKey("tokenId"))
        {
            String tokenIdResult = item.getEventResultMap().get("tokenId").value;
            return tokenIdResult.startsWith("0x") ? Numeric.toBigInt(tokenIdResult) : new BigInteger(tokenIdResult);
        }
        else
        {
            return BigInteger.ZERO;
        }
    }

    private void populateActivityInfo(RealmAuxData item, String transactionValue)
    {
        //check for TokenScript
        TokenDefinition def = viewModel.getAssetDefinitionService().getAssetDefinition(item.getChainId(), item.getTokenAddress());

        //corresponding view for this activity?
        String cardName = item.getFunctionId();

        if (def != null)
        {
            //look up in activities
            TSActivityView view = def.getActivityCards().get(cardName);
            if (view != null)
            {
                scriptViewData = view.getView(ASSET_DETAIL_VIEW_NAME);
            }
        }

        if (scriptViewData == null)
        {
            renderDefaultView(item, transactionValue);
            return;
        }

        tokenView.setChainId(token.tokenInfo.chainId);
        tokenView.setWalletAddress(new Address(token.getWallet()));
        tokenView.setRpcUrl(token.tokenInfo.chainId);
        tokenView.setOnReadyCallback(this);
        tokenView.setOnSetValuesListener(this);
        tokenView.setKeyboardListenerCallback(this);
        parsePass = 1;
        viewModel.getAssetDefinitionService().clearResultMap();
        args.clear();

        //corresponding view. Populate the view
        getAttrs(item);
    }

    private void renderDefaultView(RealmAuxData item, String transactionValue)
    {
        eventDetail.setupView(item, token, viewModel.getAssetDefinitionService(), transactionValue);
    }

    private String getEventAmount(RealmAuxData eventData, Transaction tx, boolean addSign)
    {
        Map<String, EventResult> resultMap = eventData.getEventResultMap();
        int decimals = token != null ? token.tokenInfo.decimals : C.ETHER_DECIMALS;
        String value = "";
        switch (eventData.getFunctionId())
        {
            case "received":
                if (addSign) value += "+ ";
            case "sent":
                if (value.length() == 0 && addSign) value += "- ";
                if (resultMap.containsKey("amount"))
                {
                    value += BalanceUtils.getScaledValueFixed(new BigDecimal(resultMap.get("amount").value),
                            decimals, 4);
                    return value;
                }
                break;
            case "approvalObtained":
            case "ownerApproved":
                if (resultMap.containsKey("value"))
                {
                    value = BalanceUtils.getScaledValueFixed(new BigDecimal(resultMap.get("value").value),
                            decimals, 4);
                    return value;
                }
                break;
            default:
                break;
        }

        if (token != null && tokenId.compareTo(BigInteger.ZERO) > 0)
        {
            value = "1";
        }
        else if (token != null && tx != null)
        {
            value = token.isEthereum() ? token.getTransactionValue(tx, 4) : tx.getOperationResult(token, 4);
        }

        return value;
    }

    private void getAttrs(RealmAuxData eventData)
    {
        findViewById(R.id.layout_select_ticket).setVisibility(View.VISIBLE);
        try
        {
            attrs = viewModel.getAssetDefinitionService().getTokenAttrs(token, tokenId, 1);
            //add result map values
            if (eventData != null)
            {
                Map<String, EventResult> resultMap = eventData.getEventResultMap();
                for (String resultKey : resultMap.keySet())
                {
                    TokenScriptResult.addPair(attrs, resultKey, resultMap.get(resultKey).value);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        viewModel.getAssetDefinitionService().resolveAttrs(token, new ArrayList<>(Collections.singletonList(tokenId)), null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onAttr, this::onError, () -> displayFunction(attrs.toString()))
                .isDisposed();
    }

    private void onError(Throwable throwable)
    {
        throwable.printStackTrace();
        displayFunction(attrs.toString());
    }

    private void onAttr(TokenScriptResult.Attribute attribute)
    {
        //is the attr incomplete?
        if (BuildConfig.DEBUG) System.out.println("ATTR/FA: " + attribute.id + " (" + attribute.name + ")" + " : " + attribute.text);
        TokenScriptResult.addPair(attrs, attribute.id, attribute.text);
    }

    private void displayFunction(String tokenAttrs)
    {
        try
        {
            String magicValues = viewModel.getAssetDefinitionService().getMagicValuesForInjection(token.tokenInfo.chainId);

            String injectedView = tokenView.injectWeb3TokenInit(scriptViewData.tokenView, tokenAttrs, tokenId);
            injectedView = tokenView.injectJSAtEnd(injectedView, magicValues);
            injectedView = tokenView.injectStyleAndWrapper(injectedView, scriptViewData.style);

            String base64 = Base64.encodeToString(injectedView.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
            tokenView.loadData(base64, "text/html; charset=utf-8", "base64");
        }
        catch (Exception e)
        {
            fillEmpty();
        }
    }

    private void fillEmpty()
    {
        findViewById(R.id.layout_webwrapper).setVisibility(View.VISIBLE);
        tokenView.loadData("<html><body>No Data</body></html>", "text/html", "utf-8");
    }

    private void setChainName(Transaction transaction)
    {
        ChainName chainName = findViewById(R.id.chain_name);
        if (transaction.chainId != MAINNET_ID)
        {
            chainName.setVisibility(View.VISIBLE);
            chainName.setChainID(transaction.chainId);
        }
        else
        {
            chainName.setVisibility(View.GONE);
        }
    }

    @Override
    public void callToJSComplete(String function, String result)
    {

    }

    @Override
    public void enterKeyPressed()
    {
        KeyboardUtils.hideKeyboard(getCurrentFocus());
    }

    @Override
    public void onPageLoaded(WebView view)
    {
        tokenView.callToJS("refresh()");
    }

    @Override
    public void onPageRendered(WebView view)
    {
        findViewById(R.id.layout_webwrapper).setVisibility(View.VISIBLE);
        if (parsePass == 1)
        {
            tokenView.reload();
        }

        parsePass++;
    }

    @Override
    public void setValues(Map<String, String> updates)
    {
        boolean newValues = false;
        //called when values update
        for (String key : updates.keySet())
        {
            String value = updates.get(key);
            String old = args.put(key, updates.get(key));
            if (!value.equals(old)) newValues = true;
        }

        if (newValues)
        {
            viewModel.getAssetDefinitionService().addLocalRefs(args);
            //rebuild the view
            getAttrs(null);
        }
    }

    @Override
    public void handleClick(String action, int actionId)
    {
        if (actionId == R.string.speedup_transaction)
        {
            //resend the transaction to speedup
            viewModel.reSendTransaction(transactionHash, this, token, ConfirmationType.RESEND);
        }
        else if (actionId == R.string.cancel_transaction)
        {
            //cancel the transaction
            viewModel.reSendTransaction(transactionHash, this, token, ConfirmationType.CANCEL_TX);
        }
        else
        {
            //go to the token
            if (isFromTokenHistory)
            {
                //go back to token - we arrived here from the token view
                finish();
            }
            else
            {
                //same as if you clicked on it in the wallet view
                token.clickReact(viewModel, this);
            }
        }
    }

    private void setupERC721TokenDetail(Transaction transaction)
    {
        if (transferData != null)
        {
            eventDetail.setupTransferData(transaction, token, transferData);
        }
        else if (transaction.hasInput() && transaction.transactionInput.isSendOrReceive(transaction))
        {
            eventDetail.setupERC721TokenView(token, token.getTransferValueRaw(transaction.transactionInput).toString(), true);
        }
    }
}
