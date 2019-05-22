package com.casc.stockmanager.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.casc.stockmanager.R;
import com.casc.stockmanager.activity.BillConfirmActivity;
import com.casc.stockmanager.activity.MainActivity;
import com.casc.stockmanager.adapter.BucketAdapter;
import com.casc.stockmanager.adapter.PairAdapter;
import com.casc.stockmanager.bean.Bucket;
import com.casc.stockmanager.bean.IntStrPair;
import com.casc.stockmanager.helper.NetHelper;
import com.casc.stockmanager.helper.net.NetAdapter;
import com.casc.stockmanager.helper.net.param.MsgReturn;
import com.casc.stockmanager.helper.net.param.Reply;
import com.casc.stockmanager.message.BillConfirmedMessage;
import com.casc.stockmanager.message.PollingResultMessage;
import com.casc.stockmanager.activity.ActivityCollector;
import com.casc.stockmanager.utils.CommonUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ReturnFragment extends BaseFragment {

    private static final String TAG = ReturnFragment.class.getSimpleName();

    private static final int MSG_UPDATE = 0;
    private static final int MSG_CLEAR = 1;

    private List<Bucket> mBuckets = new ArrayList<>();

    private BucketAdapter mBucketAdapter = new BucketAdapter(mBuckets);

    private List<IntStrPair> mBrandList = new ArrayList<>();

    private PairAdapter mBrandAdapter = new PairAdapter(mBrandList);

    private Handler mHandler = new InnerHandler(this);

    @BindView(R.id.rv_return_list) RecyclerView mReturnListRv;
    @BindView(R.id.rv_brand_list) RecyclerView mBrandListRv;

    @OnClick(R.id.btn_clear)
    void onClearButtonClicked() {
        showDialog("确认清空区域内容吗？", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Message.obtain(mHandler, MSG_CLEAR).sendToTarget();
            }
        });
    }

    @OnClick(R.id.btn_commit)
    void onCommitButtonClicked() {
        BillConfirmActivity.actionStart(mContext, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PollingResultMessage msg) {
        if (!isVisible() || ActivityCollector.topNotOf(MainActivity.class)) return;
        if (msg.isRead) {
            String epcStr = CommonUtils.bytesToHex(msg.epc);
            Bucket bucket = new Bucket(epcStr);
            switch (CommonUtils.validEPC(msg.epc)) {
                case BUCKET:
                    if (!mBuckets.contains(bucket)) {
                        playSound();
                        mBuckets.add(bucket);
                        String product = CommonUtils.getProduct(epcStr).getStr();
                        IntStrPair brand = null;
                        for (IntStrPair pair : mBrandList) {
                            if (pair.getStr().equals(product)) {
                                brand = pair;
                                break;
                            }
                        }
                        if (brand == null) {
                            brand = new IntStrPair(product, 0);
                            mBrandList.add(mBrandList.size() - 1, brand);
                        }
                        brand.increaseInt();
                        mBrandList.get(mBrandList.size() - 1).increaseInt();
                        Message.obtain(mHandler, MSG_UPDATE).sendToTarget();
                    }
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BillConfirmedMessage msg) {
        if (msg.isReturn) {
            MsgReturn msgReturn = new MsgReturn(msg.dealer, msg.driver);
            for (Bucket bucket : mBuckets) {
                msgReturn.addBucket(bucket.getEPC(), bucket.getScannedTime());
            }
            NetHelper.getInstance().uploadReturnMsg(msgReturn).enqueue(new NetAdapter() {
                @Override
                public void onSuccess(Reply reply) {
                    showToast("提交成功");
                    Message.obtain(mHandler, MSG_CLEAR).sendToTarget();
                }

                @Override
                public void onFail(String msg) {
                    showToast(msg);
                }
            });
        }
    }

    @Override
    protected void initFragment() {
        EventBus.getDefault().register(this);
        mReturnListRv.setLayoutManager(new GridLayoutManager(mContext, 3));
        mReturnListRv.setAdapter(mBucketAdapter);

        mBrandList.add(new IntStrPair("总数量", 0));
        mBrandListRv.setLayoutManager(new LinearLayoutManager(mContext));
        mBrandListRv.setAdapter(mBrandAdapter);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_return;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach");
    }

    private static class InnerHandler extends Handler {

        private WeakReference<ReturnFragment> mOuter;

        InnerHandler(ReturnFragment fragment) {
            this.mOuter = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            ReturnFragment outer = mOuter.get();
            switch (msg.what) {
                case MSG_CLEAR:
                    outer.mBuckets.clear();
                    outer.mBrandList.clear();
                    outer.mBrandList.add(new IntStrPair("总数量", 0));
                case MSG_UPDATE:
                    outer.mBucketAdapter.notifyDataSetChanged();
                    outer.mBrandAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }
}
