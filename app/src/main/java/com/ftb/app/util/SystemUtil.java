package com.ftb.app.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.text.TextUtils;

import com.ftb.app.FtbApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import devliving.online.securedpreferencestore.DefaultRecoveryHandler;
import devliving.online.securedpreferencestore.SecuredPreferenceStore;

/**
 * Created by mangoo on 2017/9/2.
 */

public class SystemUtil {

    public static final String PREFER_TAG = "FTBPreference";

    public static final String USER_TAG = "USER_TAG";

    public static final String USER_WALLET_LIST = "USER_WALLET_LIST";

    public static ProgressDialog initProgressDialog(Activity activity) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在加载中...");
        progressDialog.setCancelable(false);//false不能取消显示，true可以取消显示
        return progressDialog;
    }

    public static String getPreferValueBy(String name) {
        try {
            SecuredPreferenceStore prefStore = SecuredPreferenceStore.getSharedInstance();
        } catch (IllegalStateException ex) {
            initSecurePreference();
        }

        SecuredPreferenceStore prefStore = SecuredPreferenceStore.getSharedInstance();
        return prefStore.getString(name, "");
    }

    public static void setPreferValueBy(String name, String value) {
        try {
            SecuredPreferenceStore prefStore = SecuredPreferenceStore.getSharedInstance();
        } catch (IllegalStateException ex) {
            initSecurePreference();
        }

        SecuredPreferenceStore prefStore = SecuredPreferenceStore.getSharedInstance();
        prefStore.edit().putString(name, value).apply();
    }

    public static JSONObject storeWallet(String wallet) throws JSONException {
        if (SystemUtil.isNotBlank(wallet)) {
            JSONObject jsonObject = new JSONObject(wallet);
            String privateKey = jsonObject.getString("privateKey");
            String address = jsonObject.getString("address");
            String seed = jsonObject.getString("seed");

            //clear seed after backup
            jsonObject.put("seed", "");

            if (SystemUtil.isNotBlank(address) && SystemUtil.isNotBlank(privateKey)) {
                String json = SystemUtil.getPreferValueBy(SystemUtil.USER_WALLET_LIST);

                JSONArray array = new JSONArray();
                array.put(jsonObject);

                if (SystemUtil.isNotBlank(json)) {
                    JSONArray arrayOld = new JSONArray(json);
                    for (int i = 0; i < arrayOld.length(); i++) {
                        array.put(arrayOld.get(i));
                    }
                }
                SystemUtil.setPreferValueBy(SystemUtil.USER_WALLET_LIST, array.toString());
                return jsonObject;
            }
        }
        return null;
    }

    public static void initSecurePreference() {
        //check if SecuredPreferenceStore was initialized.
        try {
            SecuredPreferenceStore prefStore = SecuredPreferenceStore.getSharedInstance();
            return;
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
        try {
            SecuredPreferenceStore.init(FtbApplication.getInstance(), new DefaultRecoveryHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isLogin() {
        String json = getPreferValueBy(USER_TAG);
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.getString("userId").length() > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getWalletAddr() {
        String json = getPreferValueBy(USER_WALLET_LIST);
        try {
            JSONArray jsonArray = new JSONArray(json);
            if (jsonArray != null && jsonArray.length() > 0) {
                return jsonArray.getJSONObject(0).getString("address");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static JSONObject getWallet() {
        String json = getPreferValueBy(USER_WALLET_LIST);
        try {
            JSONArray jsonArray = new JSONArray(json);
            if (jsonArray != null && jsonArray.length() > 0) {
                return jsonArray.getJSONObject(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 大陆号码或香港号码均可
     */
    public static boolean isPhoneLegal(String str) throws PatternSyntaxException {
        return isChinaPhoneLegal(str) || isHKPhoneLegal(str);
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        return string;
    }

    /**
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数
     * 此方法中前三位格式有：
     * 13+任意数
     * 15+除4的任意数
     * 18+除1和4的任意数
     * 17+除9的任意数
     * 147
     */
    public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {
        String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 香港手机号码8位数，5|6|8|9开头+7位任意数
     */
    public static boolean isHKPhoneLegal(String str) throws PatternSyntaxException {
        String regExp = "^(5|6|8|9)\\d{7}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static boolean isBlank(String v) {
        if (v == null || v.trim().length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isEmpty(String v) {
        return isBlank(v);
    }

    public static boolean isNotBlank(String v) {
        return !isBlank(v);
    }

}
