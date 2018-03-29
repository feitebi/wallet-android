package com.ftb.app.util;

import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;

import org.bitcoinj.crypto.EncryptedData;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.wallet.Protos;
import org.spongycastle.crypto.params.KeyParameter;

/**
 * Created by mangoo on 2017/9/6.
 */

public class KeyCrypto {

    /**
     * The results format is = N,R,P,salt,encryptekey,encryptedSalt
     *
     * @param wif      the private key of WIF formate of bitcoin wallet
     * @param password the password user typed to encrypt private key
     * @return results = N,R,P,salt,encryptekey,encryptedSalt
     * @throws KeyCrypterException if it cannot produce encrypted data with the parameters.
     */
    public static String encryptPrivateKey(String wif, String password) throws KeyCrypterException {
        if (SystemUtil.isBlank(wif) || SystemUtil.isBlank(password)) {
            return "";
        }

        KeyCrypterScrypt crypterScrypt = new KeyCrypterScrypt();

        Protos.ScryptParameters params = crypterScrypt.getScryptParameters();

        String NRPSalt = params.getN() + "," + params.getR() + "," + params.getP() + "," + BaseEncoding.base64Url().encode(params.getSalt().toByteArray());

        KeyParameter aesKey = crypterScrypt.deriveKey(password);

        EncryptedData data = crypterScrypt.encrypt(BaseEncoding.base64Url().decode(wif), aesKey);
        String ecKey64 = BaseEncoding.base64Url().encode(data.encryptedBytes);
        String salt = BaseEncoding.base64Url().encode(data.initialisationVector);

        return NRPSalt + "," + ecKey64 + "," + salt;
    }

    /**
     * Decrypt private from password and encrypted data, which format is = N,R,P,salt,encryptekey,encryptedSalt
     *
     * @param encrypteData = N,R,P,salt,encryptekey,encryptedSalt
     * @param password     the orignal password to decrypt private key encrypted data.
     * @return the WIF format private key for bitcoin wallet
     * @throws KeyCrypterException if password not match the one of encrypted time or encrypteData is not matched as last time created.
     */
    public static String decryptPrivatekey(String encrypteData, String password) throws KeyCrypterException {
        if (SystemUtil.isBlank(encrypteData) || SystemUtil.isBlank(password) || encrypteData.split(",").length != 6) {
            return "";
        }

        String[] paramerts = encrypteData.split(",");

        EncryptedData data2 = new EncryptedData(BaseEncoding.base64Url().decode(paramerts[5]),
                BaseEncoding.base64Url().decode(paramerts[4]));

        Protos.ScryptParameters.Builder scryptParametersBuilder = Protos.ScryptParameters.newBuilder()
                .setN(Long.parseLong(paramerts[0])).setR(Integer.parseInt(paramerts[1]))
                .setP(Integer.parseInt(paramerts[2]))
                .setSalt(ByteString.copyFrom(BaseEncoding.base64Url().decode(paramerts[3])));

        KeyCrypterScrypt crypterScrypt = new KeyCrypterScrypt(scryptParametersBuilder.build());
        KeyParameter aesKey2 = crypterScrypt.deriveKey(password);
        byte[] pkData = crypterScrypt.decrypt(data2, aesKey2);

        return BaseEncoding.base64Url().encode(pkData);
    }

}
