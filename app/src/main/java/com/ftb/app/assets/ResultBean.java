package com.wallet.crypto.ftb.assets;

import org.json.JSONObject;

/**
 * Created by zhanghesong on 2018/3/18.
 */

public class ResultBean {
    private JSONObject jsonObject;
    private int tag;

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public ResultBean(JSONObject jsonObject, int tag) {
        this.jsonObject = jsonObject;
        this.tag = tag;
    }
}
