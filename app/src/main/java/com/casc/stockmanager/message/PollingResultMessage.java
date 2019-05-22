package com.casc.stockmanager.message;

import com.casc.stockmanager.MyParams;
import com.casc.stockmanager.helper.SpHelper;
import com.casc.stockmanager.utils.CommonUtils;

public class PollingResultMessage {

    public boolean isRead;

    public int rssi;

    public byte[] epc;

    public PollingResultMessage() {
        this.isRead = false;
    }

    public PollingResultMessage(String epc) {
        this(CommonUtils.hexToBytes(epc));
    }

    public PollingResultMessage(byte[] epc) {
        this((byte) 0, epc);
    }

    public PollingResultMessage(byte rssi, byte[] epc) {
        this.isRead = true;
        this.rssi = (int) rssi + SpHelper.getInt(MyParams.S_POWER);
        this.epc = epc;
    }

}
