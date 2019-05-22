package com.casc.stockmanager.bean;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Config {

    @SerializedName("epc_version")
    private int version;

    @SerializedName("industry_symbol")
    private String industrySymbol;

    @SerializedName("company_symbol")
    private String companySymbol;

    @SerializedName("dealer_list")
    private List<String> dealers = new ArrayList<>();

    @SerializedName("driver_list")
    private List<String> drivers = new ArrayList<>();

    @SerializedName("abnormal_reasons")
    private List<String> reasons = new ArrayList<>();

    @SerializedName("product_list")
    private List<IntStrPair> products = new ArrayList<>();

    @SerializedName("disable_list")
    private List<IntStrPair> disables = new ArrayList<>();

    public int getVersion() {
        return version;
    }

    public String getHeader() {
        return industrySymbol + companySymbol;
    }

    public String getIndustrySymbol() {
        return industrySymbol;
    }

    public String getCompanySymbol() {
        return companySymbol;
    }

    public List<String> getDealers() {
        return dealers == null ? dealers = new ArrayList<>() : dealers;
    }

    public List<String> getDrivers() {
        return drivers == null ? drivers = new ArrayList<>() : drivers;
    }

    public List<String> getReasons() {
        return reasons == null ? reasons = new ArrayList<>() : reasons;
    }

    public List<IntStrPair> getProducts() {
        return products == null ? products = new ArrayList<>() : products;
    }

    public List<IntStrPair> getDisables() {
        return disables == null ? disables = new ArrayList<>() : disables;
    }

    public IntStrPair getProductByCode(int code) {
        for (IntStrPair pair : products) {
            if (pair.getInt() == code) {
                return pair;
            }
        }
        Log.i("Error:", code + " is not exist.");
        return null;
    }

    public IntStrPair getProductByName(String name) {
        for (IntStrPair pair : products) {
            if (pair.getStr().equals(name)) {
                return pair;
            }
        }
        return null;
    }

    public IntStrPair getDisableByCode(int code) {
        for (IntStrPair pair : disables) {
            if (pair.getInt() == code) {
                return pair;
            }
        }
        return null;
    }

    public IntStrPair getDisableByWord(String word) {
        for (IntStrPair pair : disables) {
            if (pair.getStr().equals(word)) {
                return pair;
            }
        }
        return null;
    }
}
