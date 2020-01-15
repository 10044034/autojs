package org.autojs.autojs.bean;

import java.util.List;

public class SubmitDataBean {

    /**
     * trading_time : 09:12;今天
     * order_money : -4.00
     * transfer_type : {"mSpanCount":0,"mSpanData":[],"mSpans":[],"mText":"扫收钱码付款-给侠"}
     * reciprocal_name :
     */

    private String trading_time;
    private String order_money;
    private TransferTypeBean transfer_type;
    private String reciprocal_name;

    public String getTrading_time() {
        return trading_time;
    }

    public void setTrading_time(String trading_time) {
        this.trading_time = trading_time;
    }

    public String getOrder_money() {
        return order_money;
    }

    public void setOrder_money(String order_money) {
        this.order_money = order_money;
    }

    public TransferTypeBean getTransfer_type() {
        return transfer_type;
    }

    public void setTransfer_type(TransferTypeBean transfer_type) {
        this.transfer_type = transfer_type;
    }

    public String getReciprocal_name() {
        return reciprocal_name;
    }

    public void setReciprocal_name(String reciprocal_name) {
        this.reciprocal_name = reciprocal_name;
    }

    public static class TransferTypeBean {
        /**
         * mSpanCount : 0
         * mSpanData : []
         * mSpans : []
         * mText : 扫收钱码付款-给侠
         */

        private int mSpanCount;
        private String mText;
        private List<?> mSpanData;
        private List<?> mSpans;

        public int getMSpanCount() {
            return mSpanCount;
        }

        public void setMSpanCount(int mSpanCount) {
            this.mSpanCount = mSpanCount;
        }

        public String getMText() {
            return mText;
        }

        public void setMText(String mText) {
            this.mText = mText;
        }

        public List<?> getMSpanData() {
            return mSpanData;
        }

        public void setMSpanData(List<?> mSpanData) {
            this.mSpanData = mSpanData;
        }

        public List<?> getMSpans() {
            return mSpans;
        }

        public void setMSpans(List<?> mSpans) {
            this.mSpans = mSpans;
        }
    }
}
