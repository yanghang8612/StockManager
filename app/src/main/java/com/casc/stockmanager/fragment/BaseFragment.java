package com.casc.stockmanager.fragment;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.casc.stockmanager.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();

    // Fragment所属Activity上下文
    protected Context mContext;

    // 播放实例
    private SoundPool mSoundPool =
            new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build())
                    .build();

    // 资源ID
    private int mSoundID;

    private Toast mToast;

    // 系统震动辅助类
    protected Vibrator mVibrator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mSoundID = mSoundPool.load(mContext, R.raw.timer, 1);
        mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
//        LinearLayout layout = (LinearLayout) mToast.getView();
//        TextView tv = (TextView) layout.getChildAt(0);
//        tv.setTextSize(24);
//        mToast.setGravity(Gravity.CENTER, 0, 0);
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initFragment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mToast.cancel();
        EventBus.getDefault().unregister(this);
    }

    // 派生类必须重写该abstract方法，以实现自己的Fragment初始化逻辑
    protected abstract void initFragment();

    // 派生类所加载的LayoutID
    protected abstract int getLayout();

    protected void playSound() {
        mSoundPool.play(mSoundID, 1, 1, 10, 0, 1.0F);
    }

    protected void increaseCount(TextView view) {
        int count = Integer.valueOf(view.getText().toString());
        view.setText(String.valueOf(++count));
    }

    protected void decreaseCount(TextView view) {
        int count = Integer.valueOf(view.getText().toString());
        view.setText(String.valueOf(--count));
    }

    protected void showToast(String content) {
        mToast.setText(content);
        mToast.show();
    }

    protected void showToast(@StringRes int contentRes) {
        mToast.setText(contentRes);
        mToast.show();
    }

    protected void showDialog(String content, MaterialDialog.SingleButtonCallback callback) {
        new MaterialDialog.Builder(mContext)
                .content(content)
                .positiveText("确认")
                .positiveColorRes(R.color.white)
                .btnSelector(R.drawable.md_btn_postive, DialogAction.POSITIVE)
                .negativeText("取消")
                .negativeColorRes(R.color.gray)
                .onPositive(callback)
                .show();
    }
}
