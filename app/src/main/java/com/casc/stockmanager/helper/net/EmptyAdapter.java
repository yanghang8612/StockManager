package com.casc.stockmanager.helper.net;

import com.casc.stockmanager.helper.net.param.Reply;

public class EmptyAdapter extends NetAdapter {

    @Override
    public void onSuccess(Reply reply) {}

    @Override
    public void onFail(String msg) {}
}
