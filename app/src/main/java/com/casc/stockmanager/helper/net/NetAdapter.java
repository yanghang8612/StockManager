package com.casc.stockmanager.helper.net;

import android.support.annotation.NonNull;
import android.util.Log;

import com.casc.stockmanager.helper.net.param.Reply;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class NetAdapter implements Callback<Reply> {

    public abstract void onSuccess(Reply reply);

    public abstract void onFail(String msg);

    @Override
    public void onResponse(@NonNull Call<Reply> call, @NonNull Response<Reply> response) {
        Reply reply = response.body();
        if (response.isSuccessful() && reply != null) {
            if (reply.getCode() == 200) {
                onSuccess(reply);
            } else {
                Log.i("Success", reply.toString());
                onFail(reply.getMessage() + ",请检查后重试");
            }
        } else {
            ResponseBody error = response.errorBody();
            try {
                Log.i("NetError", error == null ? "Null" : error.string());
            } catch (IOException e) {
                e.printStackTrace();
            }
            onFail(("平台内部错误" + response.code() + ",请联系运维人员"));
        }
    }

    @Override
    public void onFailure(@NonNull Call<Reply> call, @NonNull Throwable t) {
        Log.i("NetFail", t.toString());
        if (t instanceof ConnectException) {
            onFail("服务器不可用,请检查网络");
        }
        else if (t instanceof SocketTimeoutException) {
            onFail("网络连接超时,请稍后重试");
        } else {
            onFail("网络连接失败(" + t.getMessage() + ")");
        }
    }
}
