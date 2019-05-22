package com.casc.stockmanager.bean;

import com.google.gson.annotations.SerializedName;

public class ApiConfig {

    @SerializedName("company_symbol")
    private String companySymbol;

    @SerializedName("main_api_url")
    private String dataCloudAddr;

    @SerializedName("standby_api_url")
    private String webCloudAddr;

    public String getCompanySymbol() {
        return companySymbol;
    }

    public String getDataCloudAddr() {
        return dataCloudAddr == null ? "" : dataCloudAddr;
    }

    public String getWebCloudAddr() {
        return webCloudAddr == null ? "" : webCloudAddr;
    }
}
