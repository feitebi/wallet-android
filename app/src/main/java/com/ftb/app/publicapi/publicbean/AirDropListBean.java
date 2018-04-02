package com.wallet.crypto.ftb.publicapi.publicbean;

import java.util.List;

/**
 * Created by zhanghesong on 2018/3/18.
 */

public class AirDropListBean {
    public List<WaitingAirdropList> waitingAirdropList;
    public List<WithdrawAirdropList> withdrawAirdropList;

    public List<WaitingAirdropList> getWaitingAirdropList() {
        return waitingAirdropList;
    }

    public void setWaitingAirdropList(List<WaitingAirdropList> waitingAirdropList) {
        this.waitingAirdropList = waitingAirdropList;
    }

    public List<WithdrawAirdropList> getWithdrawAirdropList() {
        return withdrawAirdropList;
    }

    public void setWithdrawAirdropList(List<WithdrawAirdropList> withdrawAirdropList) {
        this.withdrawAirdropList = withdrawAirdropList;
    }

    public static class WaitingAirdropList{
        public String tokenId;
        public   String tokenAmount;

        public String getTokenId() {
            return tokenId;
        }

        public void setTokenId(String tokenId) {
            this.tokenId = tokenId;
        }

        public String getTokenAmount() {
            return tokenAmount;
        }

        public void setTokenAmount(String tokenAmount) {
            this.tokenAmount = tokenAmount;
        }
    }
    public static class WithdrawAirdropList{
        public String tokenId;
        public   String tokenAmount;

        public String getTokenId() {
            return tokenId;
        }

        public void setTokenId(String tokenId) {
            this.tokenId = tokenId;
        }

        public String getTokenAmount() {
            return tokenAmount;
        }

        public void setTokenAmount(String tokenAmount) {
            this.tokenAmount = tokenAmount;
        }
    }
}
