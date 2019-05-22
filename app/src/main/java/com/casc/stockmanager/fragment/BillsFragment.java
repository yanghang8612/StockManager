package com.casc.stockmanager.fragment;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.casc.stockmanager.MyVars;
import com.casc.stockmanager.R;
import com.casc.stockmanager.activity.ActivityCollector;
import com.casc.stockmanager.activity.DeliveryActivity;
import com.casc.stockmanager.activity.MainActivity;
import com.casc.stockmanager.adapter.BillAdapter;
import com.casc.stockmanager.bean.DeliveryBill;
import com.casc.stockmanager.helper.NetHelper;
import com.casc.stockmanager.helper.net.param.Reply;
import com.casc.stockmanager.message.BillUploadedMessage;
import com.casc.stockmanager.message.PollingResultMessage;
import com.casc.stockmanager.utils.CommonUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;

public class BillsFragment extends BaseFragment {

    private static final String TAG = BillsFragment.class.getSimpleName();

    private static final int MSG_UPDATE = 0;

    private List<DeliveryBill> mBills = new ArrayList<>();

    private Map<String, DeliveryBill> mBillsMap = new HashMap<>();

    private BillAdapter mAdapter= new BillAdapter(mBills);

    private Handler mHandler = new InnerHandler(this);

    @BindView(R.id.rv_bill_list) RecyclerView mBillListRv;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PollingResultMessage msg) {
        if (!isVisible() || ActivityCollector.topNotOf(MainActivity.class)) return;
        if (msg.isRead) {
            String epcStr = CommonUtils.bytesToHex(msg.epc);
            switch (CommonUtils.validEPC(msg.epc)) {
                case CARD_DELIVERY:
                    if (!mBillsMap.containsKey(DeliveryBill.getCardIDFromEPC(msg.epc))) {
                        DeliveryBill bill = new DeliveryBill(msg.epc);
                        mBills.add(0, bill);
                        mBillsMap.put(bill.getBillID(), bill);
                        Message.obtain(mHandler, MSG_UPDATE).sendToTarget();
                    }
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BillUploadedMessage msg) {
        mBills.remove(MyVars.billToShow);
        mBillsMap.remove(MyVars.billToShow.getBillID());
        Message.obtain(mHandler, MSG_UPDATE).sendToTarget();
    }

    @Override
    protected void initFragment() {
        EventBus.getDefault().register(this);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Log.i(TAG, String.valueOf(position));
                MyVars.billToShow = mBills.get(position);
                DeliveryActivity.actionStart(mContext);
            }
        });
        mBillListRv.setLayoutManager(new LinearLayoutManager(mContext));
        mBillListRv.setAdapter(mAdapter);

        MyVars.executor.scheduleWithFixedDelay(new DeliveryBillQueryTask(), 3, 10, TimeUnit.SECONDS);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_delivery;
    }

    @Override
    public void onResume() {
        super.onResume();
        Message.obtain(mHandler, MSG_UPDATE).sendToTarget();
    }

    private static class InnerHandler extends Handler {

        private WeakReference<BillsFragment> mOuter;

        InnerHandler(BillsFragment fragment) {
            this.mOuter = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            BillsFragment outer = mOuter.get();
            switch (msg.what) {
                case MSG_UPDATE:
                    outer.mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    private class DeliveryBillQueryTask implements Runnable {

        private class Form {

            @SerializedName("form_id")
            private String id;

            @SerializedName("client_name")
            private String client;

            @SerializedName("driver_name")
            private String driver;

            @SerializedName("product_list")
            private List<Product> products;
        }

        private class Product {

            @SerializedName("code")
            private int code;

            @SerializedName("quantity")
            private int quantity;
        }

        @Override
        public void run() {
            try {
                Reply reply = NetHelper.getInstance().queryDeliveryBill().execute().body();
                if (reply != null && reply.getCode() == 200) {
                    List<Form> forms = new Gson().fromJson(reply.getContent(),
                            new TypeToken<List<Form>>(){}.getType());
                    if (forms == null) return;
                    for (Form form : forms) {
                        if (!mBillsMap.containsKey(form.id) && form.id.startsWith(MyVars.config.getCompanySymbol())) {
                            DeliveryBill bill = new DeliveryBill(form.id, form.client, form.driver);
                            for (Product product : form.products) {
                                bill.addGoods(product.code, product.quantity);
                            }
                            mBills.add(0, bill);
                            mBillsMap.put(form.id, bill);
                        }
                    }
                    Iterator<Map.Entry<String, DeliveryBill>> i = mBillsMap.entrySet().iterator();
                    while (i.hasNext()) {
                        Map.Entry<String, DeliveryBill> e = i.next();
                        if (!e.getValue().isFromCard()) {
                            boolean isFormsContained = false;
                            for (Form form : forms) {
                                if (form.id.equals(e.getKey())) {
                                    isFormsContained = true;
                                }
                            }
                            if (!isFormsContained) {
                                mBills.remove(e.getValue());
                                i.remove();
                            }
                        }
                    }
                    Message.obtain(mHandler, MSG_UPDATE).sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
