package com.casc.stockmanager.activity;

import android.content.Context;
import android.content.Intent;

import com.casc.stockmanager.MyVars;
import com.casc.stockmanager.R;
import com.casc.stockmanager.message.BillConfirmedMessage;

import org.angmarch.views.NiceSpinner;
import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

public class BillConfirmActivity extends BaseActivity {

    private static final String TAG = BillConfirmActivity.class.getSimpleName();
    private static final String IS_RETURN = "is_return";

    public static void actionStart(Context context, boolean isReturn) {
        if (ActivityCollector.topNotOf(BillConfirmActivity.class)) {
            Intent intent = new Intent(context, BillConfirmActivity.class);
            intent.putExtra(IS_RETURN, isReturn);
            context.startActivity(intent);
        }
    }

    private boolean isReturn;

    @BindView(R.id.nsp_dealer) NiceSpinner mDealerSp;
    @BindView(R.id.nsp_driver) NiceSpinner mDriverSp;

    @OnClick(R.id.btn_back) void onBackButtonClicked() {
        finish();
    }

    @OnClick(R.id.btn_confirm) void onConfirmButtonClicked() {
        String dealer = mDealerSp.getText().toString();
        String driver = mDriverSp.getText().toString();
        EventBus.getDefault().post(new BillConfirmedMessage(dealer, driver, isReturn));
        finish();
    }

    @Override
    protected void initActivity() {
        isReturn = getIntent().getBooleanExtra(IS_RETURN, false);
        mDealerSp.attachDataSource(MyVars.config.getDealers());
        mDriverSp.attachDataSource(MyVars.config.getDrivers());
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_bill_confirm;
    }

}
