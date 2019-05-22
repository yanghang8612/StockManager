package com.casc.stockmanager.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.casc.stockmanager.MyVars;
import com.casc.stockmanager.R;
import com.casc.stockmanager.adapter.GoodsAdapter;
import com.casc.stockmanager.adapter.StackAdapter;
import com.casc.stockmanager.bean.DeliveryBill;
import com.casc.stockmanager.bean.Stack;
import com.casc.stockmanager.helper.NetHelper;
import com.casc.stockmanager.helper.net.NetAdapter;
import com.casc.stockmanager.helper.net.param.MsgDelivery;
import com.casc.stockmanager.helper.net.param.Reply;
import com.casc.stockmanager.message.BillConfirmedMessage;
import com.casc.stockmanager.message.BillUploadedMessage;
import com.casc.stockmanager.message.MultiStatusMessage;
import com.casc.stockmanager.message.PollingResultMessage;
import com.casc.stockmanager.utils.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Response;

public class DeliveryActivity extends BaseActivity {

    private static final String TAG = DeliveryActivity.class.getSimpleName();

    private static final int MSG_UPDATE = 0;

    public static void actionStart(Context context) {
        if (ActivityCollector.topNotOf(DeliveryActivity.class)) {
            Intent intent = new Intent(context, DeliveryActivity.class);
            context.startActivity(intent);
        }
    }

    private boolean mIsBackMode;

    private DeliveryBill mBill;

    private GoodsAdapter mGoodsAdapter;

    private StackAdapter mStackAdapter;

    private List<String> mCache = new ArrayList<>();

    private final Object mLock = new Object();

    private Handler mHandler = new InnerHandler(this);

    @BindView(R.id.iv_reader_status) ImageView mReaderStatusIv;
    @BindView(R.id.iv_platform_status) ImageView mPlatformStatusIv;
    @BindView(R.id.tv_bill_id) TextView mBillIDTv;
    @BindView(R.id.tv_dealer) TextView mDealerTv;
    @BindView(R.id.tv_driver) TextView mDriverTv;
    @BindView(R.id.rv_goods_list) RecyclerView mGoodsListRv;
    @BindView(R.id.rv_stack_list) RecyclerView mStackListRv;

    @OnClick(R.id.btn_delivery_revoke) void onRevokeButtonClicked() {
        if (mBill.getStacks().size() == 1) {
            showToast("散货撤销请走退库流程");
        } else if (mBill.getStacks().size() != 0) {
            showDialog("确认要撤销上一垛出库吗？", new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mBill.removeLastStack();
                    Message.obtain(mHandler, MSG_UPDATE).sendToTarget();
                }
            });
        }
    }

    @OnClick(R.id.btn_delivery_mode) void onModeButtonClicked(Button btn) {
        if (mIsBackMode) {
            mIsBackMode = false;
            btn.setText("正常模式");
            btn.getBackground().setTint(getColor(R.color.light_gray));
        } else {
            mIsBackMode = true;
            btn.setText("退库模式");
            btn.getBackground().setTint(getColor(R.color.red));
        }
    }

    @OnClick(R.id.btn_delivery_commit) void onCommitButtonClicked() {
        BillConfirmActivity.actionStart(this, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MultiStatusMessage msg) {
        mReaderStatusIv.setImageResource(msg.readerStatus ?
                R.drawable.ic_connection_normal : R.drawable.ic_connection_abnormal);
        mPlatformStatusIv.setImageResource(msg.platformStatus ?
                R.drawable.ic_connection_normal : R.drawable.ic_connection_abnormal);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PollingResultMessage msg) {
        if (msg.isRead) {
            String epcStr = CommonUtils.bytesToHex(msg.epc);
            switch (CommonUtils.validEPC(msg.epc)) {
                case BUCKET:
                    synchronized (mLock) {
                        if (mIsBackMode) {
                            mCache.remove(epcStr);
                            mBill.removeBucket(epcStr);
                            Message.obtain(mHandler, MSG_UPDATE).sendToTarget();
                        }
                        else if (!mCache.contains(epcStr) && !mBill.getBuckets().containsKey(epcStr)) {
                            mCache.add(epcStr);
                        }
                    }
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BillConfirmedMessage msg) {
        if (!msg.isReturn) {
            MsgDelivery delivery = new MsgDelivery(
                    mBill.isFromCard() ? "0000000000" : mBill.getBillID(),
                    mBill.isFromCard() ? 2 : mBill.checkBill() ?  0 : 1,
                    msg.dealer, msg.driver);
            for (Map.Entry<String, Long> bucket : mBill.getBuckets().entrySet()) {
                delivery.addBucket(bucket.getKey(), bucket.getValue(), "0");
            }
            for (Map.Entry<String, Long> bucket : mBill.getRemoves().entrySet()) {
                delivery.addBucket(bucket.getKey(), bucket.getValue(), "2");
            }
            NetHelper.getInstance().uploadDeliveryMsg(delivery).enqueue(new NetAdapter() {
                @Override
                public void onSuccess(Reply reply) {
                    showToast("提交成功");
                    EventBus.getDefault().post(new BillUploadedMessage());
                    finish();
                }

                @Override
                public void onFail(String msg) {
                    showToast(msg);
                }
            });
        }
    }

    @Override
    protected void initActivity() {
        EventBus.getDefault().register(this);

        mBill = MyVars.billToShow;
        mBillIDTv.setText(mBill.getBillID());
        mDealerTv.setText(mBill.getDealer());
        mDriverTv.setText(mBill.getDriver());

        mGoodsAdapter = new GoodsAdapter(mBill.getGoods());
        mGoodsListRv.setLayoutManager(new LinearLayoutManager(this));
        mGoodsListRv.setAdapter(mGoodsAdapter);
        mStackAdapter = new StackAdapter(mBill.getStacks());
        mStackListRv.setLayoutManager(new GridLayoutManager(this, 3));
        mStackListRv.setAdapter(mStackAdapter);

        MyVars.executor.schedule(new IdentifyBucketTask(), 3, TimeUnit.SECONDS);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_delivery;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private static class InnerHandler extends Handler {

        private WeakReference<DeliveryActivity> mOuter;

        InnerHandler(DeliveryActivity activity) {
            this.mOuter = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DeliveryActivity outer = mOuter.get();
            switch (msg.what) {
                case MSG_UPDATE:
                    outer.mGoodsAdapter.notifyDataSetChanged();
                    outer.mStackAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    private class IdentifyBucketTask implements Runnable {

        private class StackInfo {

            @SerializedName("id")
            private int id;

            @SerializedName("flag")
            private String flag;

            @SerializedName("bucket_list")
            private List<String> buckets;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (mLock) {
                        if (!mCache.isEmpty() && MyVars.status.platformStatus) {
                            Reply reply = NetHelper.getInstance()
                                    .checkStackOrSingle(mCache.get(0)).execute().body();
                            if (reply != null && reply.getCode() == 200) {
                                StackInfo info = new Gson().fromJson(reply.getContent(), StackInfo.class);
                                if ("0".equals(info.flag) || "2".equals(info.flag)) { // 散货
                                    mBill.addBulk(mCache.remove(0));
                                } else { // 整垛
                                    mCache.removeAll(info.buckets);
                                    mBill.addStack(new Stack(info.id, info.buckets));
                                }
                                Message.obtain(mHandler, MSG_UPDATE).sendToTarget();
                            }
                        }
                    }
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
