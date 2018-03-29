package com.ftb.app.util;

import com.ftb.app.net.JSONRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mangoo on 2017/9/7.
 */

public class EtherTokenClient {

    private static String ETHERSCANIO_URL = "https://api.etherscan.io/api?module=account&action=txlist&startblock=0&" +
            "endblock=999999999&page=1&offset=1000&sort=asc&apikey=BMTKE1ANJN5YN7I8E67FNGM7Z6CQN777WY&address=";

    private static String TOKEN_BALANCE_URI = "https://api.tokenbalance.com/token/";

    public static List<String> fetchAllContracts(String addr) throws Exception {
        List<String> arr = new ArrayList<String>();
        String json = JSONRequest.get(ETHERSCANIO_URL + addr);
        JSONObject jsonObject = new JSONObject(json);
        JSONArray transArray = jsonObject.getJSONArray("result");
        for (int i = 0; i < transArray.length(); i++) {
            arr.add(transArray.getJSONObject(i).getString("contractAddress"));
        }
        return arr;
    }

    public static JSONObject tokenForAddr(String addr, String contractAddr) throws Exception {
        List<String> arr = new ArrayList<String>();
        String json = JSONRequest.get(TOKEN_BALANCE_URI + contractAddr + "/" + addr);
        return new JSONObject(json);
    }

}
