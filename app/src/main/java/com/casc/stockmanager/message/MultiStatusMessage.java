package com.casc.stockmanager.message;

public class MultiStatusMessage {

    public boolean readerStatus;

    public boolean networkStatus;

    public boolean platformStatus;

    public MultiStatusMessage setReaderStatus(boolean readerStatus) {
        this.readerStatus = readerStatus;
        return this;
    }

    public MultiStatusMessage setNetworkStatus(boolean networkStatus) {
        this.networkStatus = networkStatus;
        return this;
    }

    public MultiStatusMessage setPlatformStatus(boolean platformStatus) {
        this.platformStatus = platformStatus;
        return this;
    }

    public boolean canSendRequest() {
        return networkStatus && platformStatus;
    }
}
