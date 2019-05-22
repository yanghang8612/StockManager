package com.casc.stockmanager.helper.net.param;

import com.casc.stockmanager.MyParams;
import com.casc.stockmanager.helper.SpHelper;
import com.google.gson.annotations.SerializedName;

public class MsgLog {

    @SerializedName("reader_id")
    private String readerID;

    @SerializedName("log_time")
    private long time;

    @SerializedName("log_level")
    private int level;

    @SerializedName("log_content")
    private String content;

    public MsgLog(int level, String content) {
        this.readerID = SpHelper.getString(MyParams.S_READER_ID);
        this.time = System.currentTimeMillis();
        this.level = level;
        this.content = content;
    }
}
