package com.casc.stockmanager.helper.net.param;

import com.casc.stockmanager.MyParams;
import com.casc.stockmanager.helper.SpHelper;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MsgDelivery {

    private String stage;

    @SerializedName("reader_id")
    private String readerID;

    private double longitude;

    private double latitude;

    private double height;

    @SerializedName("confirm_time")
    private long confirmTime;

    @SerializedName("form_id")
    private String formID;

    @SerializedName("accordance_flag")
    private int accordanceFlag;

    @SerializedName("dealer_name")
    private String dealer;

    @SerializedName("driver_name")
    private String driver;

    @SerializedName("bucket_list")
    private List<Bucket> buckets = new ArrayList<>();

    public MsgDelivery(String formID, int accordanceFlag, String dealer, String driver) {
        this.stage = "06";
        this.readerID = SpHelper.getString(MyParams.S_READER_ID);
        this.longitude = Double.valueOf(SpHelper.getString(MyParams.S_LONGITUDE));
        this.latitude = Double.valueOf(SpHelper.getString(MyParams.S_LATITUDE));
        this.height = Double.valueOf(SpHelper.getString(MyParams.S_HEIGHT));
        this.confirmTime = System.currentTimeMillis();
        this.formID = formID;
        this.accordanceFlag = accordanceFlag;
        this.dealer = dealer;
        this.driver = driver;
    }

    public void addBucket(String epc, long time, String flag) {
        buckets.add(new Bucket(epc, time, flag));
    }

    private class Bucket {

        private String epc;

        private long time;

        private String flag;

        private Bucket(String epc, long time, String flag) {
            this.epc = epc;
            this.time = time;
            this.flag = flag;
        }
    }
}
