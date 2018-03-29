package com.ftb.app.util;

import com.google.common.base.Joiner;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

/**
 * Created by mangoo on 2017/9/4.
 */

public class BitcoinWalletUtil {

    public static String createWalletByPwd(String pwd) {
        if (SystemUtil.isBlank(pwd)) {
            return "";
        }

        try {
            return imprtWalletBy("", pwd);
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String imprtWalletBy(String seedCode, String pwd) throws UnreadableWalletException {
        if (SystemUtil.isBlank(pwd)) {
            return "";
        }

        String nnemonic = seedCode;
        NetworkParameters params = TestNet3Params.get();
        Wallet wallet = new Wallet(params);

        String addr = "";
        String privateKey = "";

        DeterministicSeed seed;
        if (seedCode != null && seedCode.length() > 0) {
            if (seedCode.split(" ").length == 12) { //nnemonic
                Long creationtime = System.currentTimeMillis() / 1000;

                seed = new DeterministicSeed(nnemonic, null, "", creationtime);

                // The wallet class provides a easy fromSeed() function that loads a new wallet
                // from a given seed.
                wallet = Wallet.fromSeed(params, seed);

                ECKey key = wallet.currentReceiveKey();

                addr = key.toAddress(params).toString();
                privateKey = key.getPrivateKeyAsWiF(params);

            } else if (seedCode.length() == 51 || seedCode.length() == 52) {//private key
                ECKey key;

                DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, seedCode);
                key = dumpedPrivateKey.getKey();
                wallet.importKey(key);
                nnemonic = "";
                privateKey = key.getPrivateKeyAsWiF(params);
                addr = key.toAddress(params).toString();
            }
        } else {
            seed = wallet.getKeyChainSeed();

            final Joiner SPACE_JOINER = Joiner.on(" ");

            nnemonic = SPACE_JOINER.join(seed.getMnemonicCode());

            ECKey key = wallet.currentReceiveKey();

            addr = key.toAddress(params).toString();
            privateKey = key.getPrivateKeyAsWiF(params);
        }

        try {
            privateKey = KeyCrypto.encryptPrivateKey(privateKey, pwd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "{\"address\":\"" + addr + "\",\"privateKey\":\"" + privateKey + "\",\"seed\":\"" + nnemonic + "\"}";
    }
}
