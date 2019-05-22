package com.casc.stockmanager.activity;

import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

import com.casc.stockmanager.MyParams;
import com.casc.stockmanager.R;
import com.casc.stockmanager.helper.SpHelper;
import com.casc.stockmanager.message.ParamsChangedMessage;

import org.angmarch.views.NiceSpinner;
import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;

public class ReaderSettingsActivity extends BaseActivity {

    private static final String TAG = ReaderSettingsActivity.class.getSimpleName();

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, ReaderSettingsActivity.class);
        context.startActivity(intent);
    }

    @BindView(R.id.tv_title) TextView mTitleTv;
    @BindView(R.id.nsp_reader_power) NiceSpinner mReaderPowerSp;
    @BindView(R.id.nsp_reader_q_value) NiceSpinner mReaderQValueSp;
    @BindView(R.id.et_reader_id) EditText mReaderIDEt;
    @BindView(R.id.et_device_addr) EditText mDeviceAddrEt;
    @BindArray(R.array.reader_power) String[] mPowerValues;
    @BindArray(R.array.reader_q_value) String[] mQValues;

    @OnClick(R.id.btn_back) void onBackButtonClicked() {
        finish();
    }

    @OnClick(R.id.btn_save) void onSaveButtonClicked() {
        SpHelper.setParam(MyParams.S_POWER, mReaderPowerSp.getText().toString());
        SpHelper.setParam(MyParams.S_Q_VALUE, mReaderQValueSp.getText().toString());
        SpHelper.setParam(MyParams.S_READER_ID, mReaderIDEt.getText().toString());
        SpHelper.setParam(MyParams.S_DEVICE_ADDR, mDeviceAddrEt.getText().toString());
        EventBus.getDefault().post(new ParamsChangedMessage());
        finish();
    }

    @Override
    protected void initActivity() {
        mReaderPowerSp.attachDataSource(Arrays.asList(mPowerValues));
        mReaderQValueSp.attachDataSource(Arrays.asList(mQValues));

        mReaderPowerSp.setText(SpHelper.getString(MyParams.S_POWER));
        mReaderQValueSp.setText(SpHelper.getString(MyParams.S_Q_VALUE));
        mReaderIDEt.setText(SpHelper.getString(MyParams.S_READER_ID));
        mDeviceAddrEt.setText(SpHelper.getString(MyParams.S_DEVICE_ADDR));
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_reader_settings;
    }
}
