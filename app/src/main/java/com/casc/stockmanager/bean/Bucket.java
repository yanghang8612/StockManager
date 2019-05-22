package com.casc.stockmanager.bean;

import java.util.Objects;

public class Bucket {

    private String epc;

    private long scannedTime;

    public Bucket(String epc) {
        this.epc = epc;
        this.scannedTime = System.currentTimeMillis();
    }

    public String getEPC() {
        return epc;
    }

    public long getScannedTime() {
        return scannedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bucket bucket = (Bucket) o;
        return Objects.equals(epc, bucket.epc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epc);
    }
}
