package com.alphawallet.app.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.webkit.URLUtil;

import androidx.core.content.ContextCompat;

import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.web3j.StructuredDataEncoder;
import com.alphawallet.token.entity.ProviderTypedData;
import com.alphawallet.token.entity.Signable;

import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alphawallet.ethereum.EthereumNetworkBase.ARTIS_SIGMA1_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.ARTIS_TAU1_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.AVALANCHE_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.BINANCE_MAIN_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.BINANCE_TEST_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.CLASSIC_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.FANTOM_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.FANTOM_TEST_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.FUJI_TEST_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.GOERLI_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.HECO_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.HECO_TEST_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.KOVAN_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.MAINNET_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.MATIC_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.MATIC_TEST_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.POA_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.RINKEBY_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.ROPSTEN_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.SOKOL_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.XDAI_ID;

public class Utils {

    private static final String ISOLATE_NUMERIC = "(0?x?[0-9a-fA-F]+)";
    private static final String ICON_REPO_ADDRESS_TOKEN = "[TOKEN]";
    private static final String CHAIN_REPO_ADDRESS_TOKEN = "[CHAIN]";
    public  static final String ALPHAWALLET_REPO_NAME = "alphawallet/iconassets";
    private static final String TRUST_ICON_REPO = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/" + CHAIN_REPO_ADDRESS_TOKEN + "/assets/" + ICON_REPO_ADDRESS_TOKEN + "/logo.png";
    private static final String ALPHAWALLET_ICON_REPO = "https://raw.githubusercontent.com/" + ALPHAWALLET_REPO_NAME + "/master/" + ICON_REPO_ADDRESS_TOKEN + "/logo.png";

    public static int dp2px(Context context, int dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }

    public static String formatUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return url;
        } else {
            if (isValidUrl(url)) {
                return C.HTTPS_PREFIX + url;
            } else {
                return C.INTERNET_SEARCH_PREFIX + url;
            }
        }
    }

    public static boolean isValidUrl(String url) {
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(url.toLowerCase());
        return m.matches();
    }

    public static boolean isAlNum(String testStr)
    {
        boolean result = false;
        if (testStr != null && testStr.length() > 0)
        {
            result = true;
            for (int i = 0; i < testStr.length(); i++)
            {
                char c = testStr.charAt(i);
                if (!Character.isIdeographic(c) && !Character.isLetterOrDigit(c) && !Character.isWhitespace(c) && (c < 32 || c > 126))
                {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    public static boolean isValidValue(String testStr)
    {
        boolean result = false;
        if (testStr != null && testStr.length() > 0)
        {
            result = true;
            for (int i = 0; i < testStr.length(); i++)
            {
                char c = testStr.charAt(i);
                if (!Character.isDigit(c) && !(c == '.' || c == ','))
                {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    private static String getFirstWord(String text) {
        if (TextUtils.isEmpty(text)) return "";
        text = text.trim();
        int index;
        for (index = 0; index < text.length(); index++)
        {
            if (!Character.isLetterOrDigit(text.charAt(index))) break;
        }

        return text.substring(0, index).trim();
    }

    public static String getIconisedText(String text)
    {
        if (TextUtils.isEmpty(text)) return "";
        String firstWord = getFirstWord(text);
        if (!TextUtils.isEmpty(firstWord))
        {
            return firstWord.substring(0, Math.min(firstWord.length(), 4)).toUpperCase();
        }
        else
        {
            return "";
        }
    }

    public static String getShortSymbol(String text)
    {
        if (TextUtils.isEmpty(text)) return "";
        String firstWord = getFirstWord(text);
        if (!TextUtils.isEmpty(firstWord))
        {
            return firstWord.substring(0, Math.min(firstWord.length(), 5)).toUpperCase();
        }
        else
        {
            return "";
        }
    }

    public static int getChainColour(int chainId)
    {
        switch (chainId)
        {
            case MAINNET_ID:
                return R.color.mainnet;
            case CLASSIC_ID:
                return R.color.classic;
            case POA_ID:
                return R.color.poa;
            case KOVAN_ID:
                return R.color.kovan;
            case ROPSTEN_ID:
                return R.color.ropsten;
            case SOKOL_ID:
                return R.color.sokol;
            case RINKEBY_ID:
                return R.color.rinkeby;
            case GOERLI_ID:
                return R.color.goerli;
            case XDAI_ID:
                return R.color.xdai;
            case ARTIS_SIGMA1_ID:
                return R.color.artis_sigma1;
            case ARTIS_TAU1_ID:
                return R.color.artis_tau1;
            case BINANCE_MAIN_ID:
                return R.color.binance_main;
            case BINANCE_TEST_ID:
                return R.color.binance_test;
            case HECO_ID:
                return R.color.heco_main;
            case HECO_TEST_ID:
                return R.color.heco_test;
            case FANTOM_ID:
                return R.color.fantom_main;
            case FANTOM_TEST_ID:
                return R.color.fantom_test;
            case AVALANCHE_ID:
                return R.color.avalanche_main;
            case FUJI_TEST_ID:
                return R.color.avalanche_test;
            case MATIC_ID:
                return R.color.polygon_main;
            case MATIC_TEST_ID:
                return R.color.polygon_test;
            default:
                return R.color.mine;
        }
    }

    public static void setChainColour(View view, int chainId)
    {
        view.getBackground().setTint(ContextCompat.getColor(view.getContext(), getChainColour(chainId)));
    }

    /**
     * This is here rather than in the Signable class because Signable is cross platform not Android specific
     *
     * @param signable
     * @return
     */
    public static int getSigningTitle(Signable signable)
    {
        switch (signable.getMessageType())
        {
            default:
            case SIGN_MESSAGE:
                return R.string.dialog_title_sign_message;
            case SIGN_PERSONAL_MESSAGE:
                return R.string.dialog_title_sign_personal_message;
            case SIGN_TYPED_DATA:
            case SIGN_TYPED_DATA_V3:
            case SIGN_TYPES_DATA_V4:
                return R.string.dialog_title_sign_typed_message;
        }
    }

    public static CharSequence formatTypedMessage(ProviderTypedData[] rawData)
    {
        //produce readable text to display in the signing prompt
        StyledStringBuilder sb = new StyledStringBuilder();
        boolean firstVal = true;
        for (ProviderTypedData data : rawData)
        {
            if (!firstVal) sb.append("\n");
            sb.startStyleGroup().append(data.name).append(":");
            sb.setStyle(new StyleSpan(Typeface.BOLD));
            sb.append("\n  ").append(data.value.toString());
            firstVal = false;
        }

        sb.applyStyles();

        return sb;
    }

    public static CharSequence formatEIP721Message(StructuredDataEncoder messageData)
    {
        HashMap<String, Object> messageMap = (HashMap<String, Object>) messageData.jsonMessageObject.getMessage();
        StyledStringBuilder sb = new StyledStringBuilder();
        for (String entry : messageMap.keySet())
        {
            sb.startStyleGroup().append(entry).append(":").append("\n");
            sb.setStyle(new StyleSpan(Typeface.BOLD));
            Object v = messageMap.get(entry);
            if (v instanceof LinkedHashMap)
            {
                HashMap<String, Object> valueMap = (HashMap<String, Object>) messageMap.get(entry);
                for (String paramName : valueMap.keySet())
                {
                    String value = valueMap.get(paramName).toString();
                    sb.startStyleGroup().append(" ").append(paramName).append(": ");
                    sb.setStyle(new StyleSpan(Typeface.BOLD));
                    sb.append(value).append("\n");
                }
            }
            else
            {
                sb.append(" ").append(v.toString()).append("\n");
            }
        }

        sb.applyStyles();

        return sb;
    }

    public static CharSequence createFormattedValue(Context ctx, String operationName, Token token)
    {
        String symbol = token != null ? token.getSymbolOrShortName() : "";
        boolean needsBreak = false;

        if ((symbol.length() + operationName.length()) > 16 && symbol.length() > 0)
        {
            int spaceIndex = operationName.lastIndexOf(' ');
            if (spaceIndex > 0)
            {
                operationName = operationName.substring(0, spaceIndex) + '\n' + operationName.substring(spaceIndex+1);
            }
            else
            {
                needsBreak = true;
            }
        }

        StyledStringBuilder sb = new StyledStringBuilder();
        sb.startStyleGroup().append(operationName);
        sb.setStyle(new StyleSpan(Typeface.NORMAL));

        if (needsBreak)
        {
            sb.append("\n");
        }
        else
        {
            sb.append(" ");
        }

        sb.startStyleGroup().append(symbol);
        sb.setStyle(new StyleSpan(Typeface.BOLD));

        sb.applyStyles();

        return sb;
    }

    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static boolean copyFile(String source, String dest)
    {
        try
        {
            FileChannel s = new FileInputStream(source).getChannel();
            FileChannel d = new FileOutputStream(dest).getChannel();
            d.transferFrom(s, 0, s.size());
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isAddressValid(String address)
    {
        return address != null && address.length() > 0 && WalletUtils.isValidAddress(address);
    }

    public static String intArrayToString(int[] values)
    {
        StringBuilder store = new StringBuilder();
        boolean firstValue = true;
        for (int network : values)
        {
            if (!firstValue) store.append(",");
            store.append(network);
            firstValue = false;
        }

        return store.toString();
    }

    public static List<Integer> intListToArray(String list)
    {
        List<Integer> idList = new ArrayList<>();
        //convert to array
        String[] split = list.split(",");
        for (String s : split)
        {
            Integer value;
            try
            {
                value = Integer.valueOf(s);
                idList.add(value);
            }
            catch (NumberFormatException e)
            {
                //empty
            }
        }

        return idList;
    }

    public static int[] bigIntegerListToIntList(List<BigInteger> ticketSendIndexList)
    {
        int[] indexList = new int[ticketSendIndexList.size()];
        for (int i = 0; i < ticketSendIndexList.size(); i++) indexList[i] = ticketSendIndexList.get(i).intValue();
        return indexList;
    }


    /**
     * Produce a string CSV of integer IDs given an input list of values
     * @param idList
     * @param keepZeros
     * @return
     */
    public static String bigIntListToString(List<BigInteger> idList, boolean keepZeros)
    {
        if (idList == null) return "";
        String displayIDs = "";
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (BigInteger id : idList)
        {
            if (!keepZeros && id.compareTo(BigInteger.ZERO) == 0) continue;
            if (!first)
            {
                sb.append(",");
            }
            first = false;

            sb.append(Numeric.toHexStringNoPrefix(id));
            displayIDs = sb.toString();
        }

        return displayIDs;
    }

    public static List<Integer> stringIntsToIntegerList(String userList)
    {
        List<Integer> idList = new ArrayList<>();

        try
        {
            String[] ids = userList.split(",");

            for (String id : ids)
            {
                //remove whitespace
                String trim = id.trim();
                Integer intId = Integer.parseInt(trim);
                idList.add(intId);
            }
        }
        catch (Exception e)
        {
            idList = new ArrayList<>();
        }

        return idList;
    }

    public static String integerListToString(List<Integer> intList, boolean keepZeros)
    {
        if (intList == null) return "";
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (Integer id : intList)
        {
            if (!keepZeros && id == 0) continue;
            if (!first)sb.append(",");
            sb.append(String.valueOf(id));
            first = false;
        }

        return sb.toString();
    }

    public static boolean isHex(String hexStr)
    {
        if (hexStr == null) return false;
        for (Character c : hexStr.toCharArray())
        {
            if ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))
            {
                return true;
            }
        }

        return false;
    }

    public static String isolateNumeric(String valueFromInput)
    {
        try
        {
            Matcher regexResult = Pattern.compile(ISOLATE_NUMERIC).matcher(valueFromInput);
            if (regexResult.find())
            {
                if (regexResult.groupCount() >= 1)
                {
                    valueFromInput = regexResult.group(0);
                }
            }
        }
        catch (Exception e)
        {
            // Silent fail - no action; just return input; this function is only to clean junk from a number
        }

        return valueFromInput;
    }

    public static String formatAddress(String address) {
        address = Keys.toChecksumAddress(address);
        String result = "";
        String firstSix = address.substring(0, 6);
        String lastSix = address.substring(address.length()-4);
        StringBuilder formatted = new StringBuilder(result);
        return formatted.append(firstSix).append("...").append(lastSix).toString().toLowerCase();
    }

    /**
     * Just enough for diagnosis of most errors
     * @param s String to be HTML escaped
     * @return escaped string
     */
    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c)
            {
                case '"':
                    out.append("&quot;");
                    break;
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    public static String convertTimePeriodInSeconds(long pendingTimeInSeconds, Context ctx)
    {
        long days = pendingTimeInSeconds/(60*60*24);
        pendingTimeInSeconds -= (days*60*60*24);
        long hours = pendingTimeInSeconds/(60*60);
        pendingTimeInSeconds -= (hours*60*60);
        long minutes = pendingTimeInSeconds/60;
        long seconds = pendingTimeInSeconds%60;

        StringBuilder sb = new StringBuilder();
        int timePoints = 0;

        if (days > 0)
        {
            timePoints = 2;
            if (days == 1)
            {
                sb.append(ctx.getString(R.string.day_single));
            }
            else
            {
                sb.append(ctx.getString(R.string.day_plural, String.valueOf(days)));
            }
        }

        if (hours > 0)
        {
            if (timePoints == 0)
            {
                timePoints = 1;
            }
            else
            {
                sb.append(", ");
            }

            if (hours == 1)
            {
                sb.append(ctx.getString(R.string.hour_single));
            }
            else
            {
                sb.append(ctx.getString(R.string.hour_plural, String.valueOf(hours)));
            }
        }

        if (minutes > 0 && timePoints < 2)
        {
            if (timePoints != 0)
            {
                sb.append(", ");
            }
            timePoints++;
            if (minutes == 1)
            {
                sb.append(ctx.getString(R.string.minute_single));
            }
            else
            {
                sb.append(ctx.getString(R.string.minute_plural, String.valueOf(minutes)));
            }
        }

        if (seconds > 0 && timePoints < 2)
        {
            if (timePoints != 0)
            {
                sb.append(", ");
            }
            if (seconds == 1)
            {
                sb.append(ctx.getString(R.string.second_single));
            }
            else
            {
                sb.append(ctx.getString(R.string.second_plural, String.valueOf(seconds)));
            }
        }

        return sb.toString();
    }

    public static String shortConvertTimePeriodInSeconds(long pendingTimeInSeconds, Context ctx)
    {
        long days = pendingTimeInSeconds/(60*60*24);
        pendingTimeInSeconds -= (days*60*60*24);
        long hours = pendingTimeInSeconds/(60*60);
        pendingTimeInSeconds -= (hours*60*60);
        long minutes = pendingTimeInSeconds/60;
        long seconds = pendingTimeInSeconds%60;

        String timeStr;

        if (pendingTimeInSeconds == -1)
        {
            timeStr = ctx.getString(R.string.never);
        }
        else if (days > 0)
        {
            timeStr = ctx.getString(R.string.day_single);
        }
        else if (hours > 0)
        {
            if (hours == 1 && minutes == 0)
            {
                timeStr = ctx.getString(R.string.hour_single);
            }
            else
            {
                BigDecimal hourStr = BigDecimal.valueOf(hours + (double)minutes/60.0)
                        .setScale(1, RoundingMode.HALF_DOWN); //to 1 dp
                timeStr = ctx.getString(R.string.hour_plural, hourStr.toString());
            }
        }
        else if (minutes > 0)
        {
            if (minutes == 1 && seconds == 0)
            {
                timeStr = ctx.getString(R.string.minute_single);
            }
            else
            {
                BigDecimal minsStr = BigDecimal.valueOf(minutes + (double)seconds/60.0)
                        .setScale(1, RoundingMode.HALF_DOWN); //to 1 dp
                timeStr = ctx.getString(R.string.minute_plural, minsStr.toString());
            }
        }
        else
        {
            if (seconds == 1)
            {
                timeStr = ctx.getString(R.string.second_single);
            }
            else
            {
                timeStr = ctx.getString(R.string.second_plural, String.valueOf(seconds));
            }
        }

        return timeStr;
    }

    public static String localiseUnixTime(Context ctx, long timeStampInSec)
    {
        Date date = new java.util.Date(timeStampInSec * DateUtils.SECOND_IN_MILLIS);
        DateFormat timeFormat = java.text.DateFormat.getTimeInstance(DateFormat.SHORT, LocaleUtils.getDeviceLocale(ctx));
        return timeFormat.format(date);
    }

    public static String localiseUnixDate(Context ctx, long timeStampInSec)
    {
        Date date = new java.util.Date(timeStampInSec * DateUtils.SECOND_IN_MILLIS);
        DateFormat timeFormat = java.text.DateFormat.getTimeInstance(DateFormat.SHORT, LocaleUtils.getDeviceLocale(ctx));
        DateFormat dateFormat = java.text.DateFormat.getDateInstance(DateFormat.MEDIUM, LocaleUtils.getDeviceLocale(ctx));
        return timeFormat.format(date) + " | " + dateFormat.format(date);
    }

    public static long randomId() {
        return new Date().getTime();
    }

    public static String getDomainName(String url)
    {
        try
        {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        }
        catch (Exception e)
        {
            return url != null ? url : "";
        }
    }

    public static String getTokenImageUrl(int chainId, String address)
    {
        String tURL = TRUST_ICON_REPO;
        String repoChain;
        switch (chainId)
        {
            case CLASSIC_ID:
                repoChain = "classic";
                break;
            case XDAI_ID:
                repoChain = "xdai";
                break;
            case POA_ID:
                repoChain = "poa";
                break;
            case BINANCE_MAIN_ID:
                repoChain = "binance";
                break;
            case AVALANCHE_ID:
                repoChain = "avalanche";
                break;
            case MATIC_ID:
                repoChain = "polygon";
                break;
            case KOVAN_ID:
            case RINKEBY_ID:
            case SOKOL_ID:
            case ROPSTEN_ID:
            case ARTIS_SIGMA1_ID:
            case ARTIS_TAU1_ID:
                tURL = ALPHAWALLET_ICON_REPO;
                repoChain = "";
                break;
            default:
                repoChain = "ethereum";
                break;
        }
        tURL = tURL.replace(ICON_REPO_ADDRESS_TOKEN, address).replace(CHAIN_REPO_ADDRESS_TOKEN, repoChain);

        return tURL;
    }

    public static String getAWIconRepo(String address)
    {
        return ALPHAWALLET_ICON_REPO.replace(ICON_REPO_ADDRESS_TOKEN, Keys.toChecksumAddress(address));
    }

    public static boolean isContractCall(Context context, String operationName)
    {
        return !TextUtils.isEmpty(operationName) && context.getString(R.string.contract_call).equals(operationName);
    }

    public static String parseIPFS(String URL)
    {
        if (TextUtils.isEmpty(URL)) return URL;
        String parsed = URL;
        int ipfsIndex = URL.lastIndexOf("/ipfs/");
        if (ipfsIndex >= 0)
        {
            parsed = "https://ipfs.io" + URL.substring(ipfsIndex);
        }

        return parsed;
    }
}
