package com.casc.stockmanager.bean;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class IntStrPair {

    @SerializedName(value = "name", alternate = {"reason"})
    private String strValue;

    @SerializedName(value = "quantity", alternate = {"code"})
    private int intValue;

    public IntStrPair(String strValue, int intValue) {
        this.strValue = strValue;
        this.intValue = intValue;
    }

    public String getStr() {
        return strValue;
    }

    public int getInt() {
        return intValue;
    }

    public void increaseInt() {
        intValue += 1;
    }

    @Override
    public String toString() {
        return strValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntStrPair pair = (IntStrPair) o;
        return Objects.equals(strValue, pair.strValue);
    }
}
