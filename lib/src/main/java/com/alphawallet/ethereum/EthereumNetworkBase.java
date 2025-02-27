package com.alphawallet.ethereum;

/* Weiwu 12 Jan 2020: This class eventually will replace the EthereumNetworkBase class in :app
 * one all inteface methods are implemented.
 */

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class EthereumNetworkBase { // implements EthereumNetworkRepositoryType
    public static final int MAINNET_ID = 1;
    public static final int CLASSIC_ID = 61;
    public static final int POA_ID = 99;
    public static final int KOVAN_ID = 42;
    public static final int ROPSTEN_ID = 3;
    public static final int SOKOL_ID = 77;
    public static final int RINKEBY_ID = 4;
    public static final int XDAI_ID = 100;
    public static final int GOERLI_ID = 5;
    public static final int ARTIS_SIGMA1_ID = 246529;
    public static final int ARTIS_TAU1_ID = 246785;
    public static final int BINANCE_TEST_ID = 97;
    public static final int BINANCE_MAIN_ID = 56;
    public static final int HECO_ID = 128;
    public static final int HECO_TEST_ID = 256;
    public static final int FANTOM_ID = 250;
    public static final int FANTOM_TEST_ID = 4002;
    public static final int AVALANCHE_ID = 43114;
    public static final int FUJI_TEST_ID = 43113;
    public static final int MATIC_ID = 137;
    public static final int MATIC_TEST_ID = 80001;

    public static final String MAINNET_RPC_URL = "https://mainnet.infura.io/v3/da3717f25f824cc1baa32d812386d93f";
    public static final String CLASSIC_RPC_URL = "https://www.ethercluster.com/etc";
    public static final String XDAI_RPC_URL = "https://dai.poa.network";
    public static final String POA_RPC_URL = "https://core.poa.network/";
    public static final String ROPSTEN_RPC_URL = "https://ropsten.infura.io/v3/da3717f25f824cc1baa32d812386d93f";
    public static final String RINKEBY_RPC_URL = "https://rinkeby.infura.io/v3/da3717f25f824cc1baa32d812386d93f";
    public static final String KOVAN_RPC_URL = "https://kovan.infura.io/v3/da3717f25f824cc1baa32d812386d93f";
    public static final String SOKOL_RPC_URL = "https://sokol.poa.network";
    public static final String GOERLI_RPC_URL = "https://goerli.infura.io/v3/da3717f25f824cc1baa32d812386d93f";
    public static final String ARTIS_SIGMA1_RPC_URL = "https://rpc.sigma1.artis.network";
    public static final String ARTIS_TAU1_RPC_URL = "https://rpc.tau1.artis.network";
    public static final String BINANCE_TEST_RPC_URL = "https://data-seed-prebsc-1-s3.binance.org:8545";
    public static final String BINANCE_MAIN_RPC_URL = "https://bsc-dataseed1.binance.org:443";
    public static final String HECO_RPC_URL = "https://http-mainnet-node.huobichain.com";
    public static final String HECO_TEST_RPC_URL = "https://http-testnet.hecochain.com";
    public static final String AVALANCHE_RPC_URL = "https://api.avax.network/ext/bc/C/rpc";
    public static final String FUJI_TEST_RPC_URL = "https://api.avax-test.network/ext/bc/C/rpc";
    public static final String FANTOM_RPC_URL = "https://rpcapi.fantom.network";
    public static final String FANTOM_TEST_RPC_URL = "https://rpc.testnet.fantom.network/";
    public static final String MATIC_RPC_URL = "https://rpc-mainnet.maticvigil.com/";
    public static final String MUMBAI_TEST_RPC_URL = "https://rpc-mumbai.maticvigil.com/";

    static Map<Integer, NetworkInfo> networkMap = new LinkedHashMap<Integer, NetworkInfo>() {
        {
            put(MAINNET_ID, new NetworkInfo("Ethereum", "ETH", MAINNET_RPC_URL, "https://etherscan.io/tx/",
                    MAINNET_ID));
            put(CLASSIC_ID, new NetworkInfo("Ethereum Classic", "ETC", CLASSIC_RPC_URL, "https://blockscout.com/etc/mainnet/tx/",
                    CLASSIC_ID));
            put(XDAI_ID, new NetworkInfo("xDAI", "xDAI", XDAI_RPC_URL, "https://blockscout.com/poa/dai/tx/",
                    XDAI_ID));
            put(POA_ID, new NetworkInfo("POA", "POA", POA_RPC_URL, "https://blockscout.com/poa/core/tx/",
                    POA_ID));
            put(ARTIS_SIGMA1_ID, new NetworkInfo("ARTIS sigma1", "ATS", ARTIS_SIGMA1_RPC_URL, "https://explorer.sigma1.artis.network/tx/",
                    ARTIS_SIGMA1_ID));
            put(KOVAN_ID, new NetworkInfo("Kovan (Test)", "ETH", KOVAN_RPC_URL, "https://kovan.etherscan.io/tx/",
                    KOVAN_ID));
            put(ROPSTEN_ID, new NetworkInfo("Ropsten (Test)", "ETH", ROPSTEN_RPC_URL, "https://ropsten.etherscan.io/tx/",
                    ROPSTEN_ID));
            put(SOKOL_ID, new NetworkInfo("Sokol (Test)", "POA", SOKOL_RPC_URL, "https://blockscout.com/poa/sokol/tx/",
                    SOKOL_ID));
            put(RINKEBY_ID, new NetworkInfo("Rinkeby (Test)", "ETH", RINKEBY_RPC_URL, "https://rinkeby.etherscan.io/tx/",
                    RINKEBY_ID));
            put(GOERLI_ID, new NetworkInfo("Görli (Test)", "GÖETH", GOERLI_RPC_URL, "https://goerli.etherscan.io/tx/",
                    GOERLI_ID));
            put(ARTIS_TAU1_ID, new NetworkInfo("ARTIS tau1 (Test)", "ATS", ARTIS_TAU1_RPC_URL, "https://explorer.tau1.artis.network/tx/",
                    ARTIS_TAU1_ID));
            put(BINANCE_TEST_ID, new NetworkInfo("BSC TestNet (Test)", "BNB", BINANCE_TEST_RPC_URL, "https://explorer.binance.org/smart-testnet/tx/",
                    BINANCE_MAIN_ID));
            put(BINANCE_MAIN_ID, new NetworkInfo("Binance", "BNB", BINANCE_MAIN_RPC_URL, "https://explorer.binance.org/smart/tx/",
                    BINANCE_TEST_ID));
            put(HECO_ID, new NetworkInfo("Heco", "HT", HECO_RPC_URL, "https://hecoinfo.com/tx/",
                    HECO_ID));
            put(HECO_TEST_ID, new NetworkInfo("Heco (Test)", "HT", HECO_TEST_RPC_URL, "https://testnet.hecoinfo.com/tx/",
                    HECO_TEST_ID));

            put(AVALANCHE_ID, new NetworkInfo("Avalanche Mainnet C-Chain", "AVAX", AVALANCHE_RPC_URL, "https://cchain.explorer.avax.network/tx/",
                    AVALANCHE_ID));
            put(FUJI_TEST_ID, new NetworkInfo("Avalanche FUJI C-Chain (Test)", "AVAX", FUJI_TEST_RPC_URL, "https://cchain.explorer.avax-test.network/tx/",
                    FUJI_TEST_ID));

            put(FANTOM_ID, new NetworkInfo("Fantom Opera", "FTM", FANTOM_RPC_URL, "https://ftmscan.com/tx/",
                    FANTOM_ID));
            put(FANTOM_TEST_ID, new NetworkInfo("Fantom (Test)", "FTM", FANTOM_TEST_RPC_URL, "https://explorer.testnet.fantom.network/tx/",
                    FANTOM_TEST_ID));

            put(MATIC_ID, new NetworkInfo("Polygon", "POLY", MATIC_RPC_URL, "https://explorer.matic.network/tx/",
                    MATIC_ID));
            put(MATIC_TEST_ID, new NetworkInfo("Mumbai (Test)", "POLY", MUMBAI_TEST_RPC_URL, "https://mumbai-explorer.matic.today/tx/",
                    MATIC_TEST_ID));
        }
    };

    public static NetworkInfo getNetworkByChain(int chainId) {
        return networkMap.get(chainId);
    }

    public static String getShortChainName(int chainId)
    {
        NetworkInfo info = networkMap.get(chainId);
        if (info != null)
        {
            String shortName = info.name;
            int index = shortName.indexOf(" (Test)");
            if (index > 0) shortName = info.name.substring(0, index);
            if (shortName.length() > networkMap.get(CLASSIC_ID).name.length()) //shave off the last word
            {
                shortName = shortName.substring(0, shortName.lastIndexOf(" "));
            }
            return shortName;
        }
        else
        {
            return networkMap.get(MAINNET_ID).name;
        }
    }

    public static String getChainSymbol(int chainId)
    {
        NetworkInfo info = networkMap.get(chainId);
        if (info != null)
        {
            return info.symbol;
        }
        else
        {
            return networkMap.get(MAINNET_ID).symbol;
        }
    }
}
