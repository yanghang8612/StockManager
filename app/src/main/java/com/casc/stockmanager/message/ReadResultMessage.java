package com.casc.stockmanager.message;

import java.util.Arrays;

public class ReadResultMessage {

    public byte[] epc;

    public byte[] data;

    public ReadResultMessage(byte[] ins) {
        this.epc = Arrays.copyOfRange(ins, 8, 6 + ins[5]);
        this.data = Arrays.copyOfRange(ins, 6 + ins[5], ins.length - 2);;
    }
}
