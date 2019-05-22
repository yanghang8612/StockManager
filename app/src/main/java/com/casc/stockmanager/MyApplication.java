package com.casc.stockmanager;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import com.casc.stockmanager.backend.impl.BLEReaderImpl;
import com.casc.stockmanager.backend.impl.USBReaderImpl;
import com.casc.stockmanager.bean.ApiConfig;
import com.casc.stockmanager.bean.Config;
import com.casc.stockmanager.helper.NetHelper;
import com.casc.stockmanager.helper.SpHelper;
import com.casc.stockmanager.helper.net.param.Reply;
import com.casc.stockmanager.message.ConfigUpdatedMessage;
import com.casc.stockmanager.message.ParamsChangedMessage;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();

    private static MyApplication mInstance;

    public static MyApplication getInstance() {
        return mInstance;
    }

    private ConnectivityManager mConnectivityManager;

    @Subscribe
    public void onMessageEvent(ParamsChangedMessage msg) {
        MyVars.executor.execute(new UpdateConfigTask());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);

        // 初始化相关字段
        mInstance = this;
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // 初始化读写器实例(USB、蓝牙)
        MyVars.usbReader = new USBReaderImpl(this);
        MyVars.bleReader = new BLEReaderImpl(this);

        MyVars.executor.scheduleWithFixedDelay(new UpdateConfigTask(), 2000 , MyParams.CONFIG_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
        MyVars.executor.scheduleWithFixedDelay(new InternetStatusCheckTask(), 2000, MyParams.INTERNET_STATUS_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
        MyVars.executor.scheduleWithFixedDelay(new PlatformStatusCheckTask(), 2000, MyParams.PLATFORM_STATUS_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private class UpdateConfigTask implements Runnable {

        @Override
        public void run() {
            try {
                Reply apiReply = NetHelper.getInstance().getApiConfig().execute().body();
                if (apiReply != null && apiReply.getCode() == 200) {
                    SpHelper.setParam(MyParams.S_API_JSON, apiReply.getContent().toString());
                    MyVars.api = new Gson().fromJson(apiReply.getContent().toString(), ApiConfig.class);

                    Reply configReply = NetHelper.getInstance().getConfig().execute().body();
                    if (configReply != null && configReply.getCode() == 200) {
                        String content = configReply.getContent().toString();
                        if (!SpHelper.getString(MyParams.S_CONFIG_JSON).equals(content)) {
                            SpHelper.setParam(MyParams.S_CONFIG_JSON, content);
                            MyVars.config = new Gson().fromJson(content, Config.class);
                            EventBus.getDefault().post(new ConfigUpdatedMessage());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class InternetStatusCheckTask implements Runnable {

        @Override
        public void run() {
            NetworkCapabilities nc = mConnectivityManager.getNetworkCapabilities(
                    mConnectivityManager.getActiveNetwork());
            MyVars.status.setReaderStatus(MyVars.getReader().isConnected());
            MyVars.status.setNetworkStatus(nc != null &&
                    nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
            EventBus.getDefault().post(MyVars.status);
        }
    }

    private class PlatformStatusCheckTask implements Runnable {

        @Override
        public void run() {
            try {
                Reply reply = NetHelper.getInstance().sendHeartbeat().execute().body();
                MyVars.status.setPlatformStatus(reply != null && reply.getCode() == 200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
