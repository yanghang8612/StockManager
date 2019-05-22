package com.casc.stockmanager.helper.net.param;

import com.casc.stockmanager.MyParams;
import com.casc.stockmanager.helper.SpHelper;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MsgReturn {

    private String stage;

    @SerializedName("reader_id")
    private String readerID;

    private double longitude;

    private double latitude;

    private double height;

    @SerializedName("confirm_time")
    private long confirmTime;

    @SerializedName("dealer_name")
    private String dealer;

    @SerializedName("driver_name")
    private String driver;

    @SerializedName("bucket_list")
    private List<Bucket> buckets = new ArrayList<>();

    public MsgReturn(String dealer, String driver) {
        this.stage = "08";
        this.readerID = SpHelper.getString(MyParams.S_READER_ID);
        this.longitude = Double.valueOf(SpHelper.getString(MyParams.S_LONGITUDE));
        this.latitude = Double.valueOf(SpHelper.getString(MyParams.S_LATITUDE));
        this.height = Double.valueOf(SpHelper.getString(MyParams.S_HEIGHT));
        this.confirmTime = System.currentTimeMillis();
        this.dealer = dealer;
        this.driver = driver;
    }

    public void addBucket(String epc, long time) {
        buckets.add(new Bucket(epc, time));
    }

    private class Bucket {

        private String epc;

        private long time;

        private Bucket(String epc, long time) {
            this.epc = epc;
            this.time = time;
        }
    }
}
