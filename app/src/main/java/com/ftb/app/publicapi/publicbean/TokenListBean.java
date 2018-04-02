package com.wallet.crypto.ftb.publicapi.publicbean;

import java.util.List;

/**
 * Created by zhanghesong on 2018/3/18.
 */

public class TokenListBean {
    public List<DataList> dataList;

    public List<DataList> getDataList() {
        return dataList;
    }

    public void setDataList(List<DataList> dataList) {
        this.dataList = dataList;
    }

    public static class DataList{
        public String id;
        public String symbol;
        public String drawFlag;
        public String withdrawStart;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getDrawFlag() {
            return drawFlag;
        }

        public void setDrawFlag(String drawFlag) {
            this.drawFlag = drawFlag;
        }

        public String getWithdrawStart() {
            return withdrawStart;
        }

        public void setWithdrawStart(String withdrawStart) {
            this.withdrawStart = withdrawStart;
        }
    }
}
