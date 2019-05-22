package com.casc.stockmanager.message;

public class BillConfirmedMessage {

    public String dealer;

    public String driver;

    public boolean isReturn;

    public BillConfirmedMessage(String dealer, String driver, boolean isReturn) {
        this.dealer = dealer;
        this.driver = driver;
        this.isReturn = isReturn;
    }
}
