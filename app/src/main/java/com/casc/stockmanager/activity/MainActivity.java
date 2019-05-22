package com.casc.stockmanager.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.casc.stockmanager.R;
import com.casc.stockmanager.fragment.FragmentFactory;
import com.casc.stockmanager.message.MultiStatusMessage;
import com.casc.stockmanager.message.PollingResultMessage;
import com.casc.stockmanager.utils.CommonUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    private int mPreFragmentIndex = 0;

    @BindView(R.id.toolBar) Toolbar mToolbar;
    @BindView(R.id.iv_reader_status) ImageView mReaderStatusIv;
    @BindView(R.id.iv_platform_status) ImageView mPlatformStatusIv;
    @BindView(R.id.bnv_main) BottomNavigationView mMainBnv;

    @OnLongClick(R.id.ll_reader_status) boolean onReaderStatusLlClicked() {
        ReaderSettingsActivity.actionStart(this);
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MultiStatusMessage message) {
        mReaderStatusIv.setImageResource(message.readerStatus ?
                R.drawable.ic_connection_normal : R.drawable.ic_connection_abnormal);
        mPlatformStatusIv.setImageResource(message.platformStatus ?
                R.drawable.ic_connection_normal : R.drawable.ic_connection_abnormal);
    }

    @Override
    protected void initActivity() {
        EventBus.getDefault().register(this);

        mMainBnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_delivery:
                        switchFragment(0);
                        return true;
                    case R.id.navigation_return:
                        switchFragment(1);
                        return true;
                }
                return false;
            }
        });
        mMainBnv.inflateMenu(R.menu.navigation);

        initFragment();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    private void initFragment() {
        Fragment defaultFragment = FragmentFactory.getInstanceByIndex(0);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fl_main, defaultFragment);
        transaction.commit();
    }

    public void switchFragment(int toIndex) {
        Fragment from = FragmentFactory.getInstanceByIndex(mPreFragmentIndex);
        Fragment to = FragmentFactory.getInstanceByIndex(toIndex);
        if (from != null && to != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (!to.isAdded()) {
                transaction.hide(from).add(R.id.fl_main, to).commit();
            }
            else {
                transaction.hide(from).show(to).commit();
            }
            mPreFragmentIndex = toIndex;
        }
    }
}
