package com.easefun.polyv.livecommon.module.modules.interact.info;

public class PLVInteractInfo {

    private LotteryData lotteryData;

    public LotteryData getLotteryData() {
        return lotteryData;
    }

    public void setLotteryData(LotteryData lotteryData) {
        this.lotteryData = lotteryData;
    }

   public static class LotteryData {
        private String lotteryTextCN;
        private String lotteryTextEN;

        public String getLotteryTextCN() {
            return lotteryTextCN;
        }

        public void setLotteryTextCN(String lotteryTextCN) {
            this.lotteryTextCN = lotteryTextCN;
        }

        public String getLotteryTextEN() {
            return lotteryTextEN;
        }

        public void setLotteryTextEN(String lotteryTextEN) {
            this.lotteryTextEN = lotteryTextEN;
        }
    }


}

