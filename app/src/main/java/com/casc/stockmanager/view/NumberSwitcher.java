package com.casc.stockmanager.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.casc.stockmanager.R;

public class NumberSwitcher extends TextSwitcher {

    private static final String TAG = NumberSwitcher.class.getSimpleName();

    private Context mContext;

    private int mTextColor;

    public NumberSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mTextColor = Color.BLACK;
        setFactory(new ViewFactory() {
            @Override
            public View makeView() {
                TextView tv = new TextView(mContext);
                //tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
                tv.setTextColor(mTextColor);
                //tv.getPaint().setFakeBoldText(true);
                tv.setGravity(Gravity.END);
                return tv;
            }
        });
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberSwitcher);
//        if (a.getBoolean(R.styleable.NumberSwitcher_ns_reverseAnim, false)) {
//            setOutAnimation(context, R.anim.number_bottom_out);
//            setInAnimation(context, R.anim.number_top_in);
//        } else {
//            setOutAnimation(context, R.anim.number_top_out);
//            setInAnimation(context, R.anim.number_bottom_in);
//        }
//        a.recycle();
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        TextView curTv = (TextView) getCurrentView();
        curTv.setTextColor(textColor);
    }

    public void setNumber(int newNumber) {
        String oldText = ((TextView) getCurrentView()).getText().toString();
        int oldNumber = TextUtils.isEmpty(oldText) ? -1 : Integer.valueOf(oldText);
        if (oldNumber >= 0 && Math.abs(newNumber - oldNumber) > 0) {
            if (newNumber > oldNumber) {
                setOutAnimation(mContext, R.anim.number_top_out);
                setInAnimation(mContext, R.anim.number_bottom_in);
            } else {
                clearAnimation();
                setOutAnimation(mContext, R.anim.number_bottom_out);
                setInAnimation(mContext, R.anim.number_top_in);
            }
            super.setText(String.valueOf(newNumber));
        } else {
            super.setCurrentText(String.valueOf(newNumber));
        }
    }

    public int getNumber() {
        String curText = ((TextView) getCurrentView()).getText().toString();
        return TextUtils.isEmpty(curText) ? -1 : Integer.valueOf(curText);
    }

    public void increaseNumber() {
        String oldText = ((TextView) getCurrentView()).getText().toString();
        int oldNumber = TextUtils.isEmpty(oldText) ? -1 : Integer.valueOf(oldText);
        if (oldNumber >= 0) {
            setNumber(oldNumber + 1);
        }
    }

    public void decreaseNumber() {
        String oldText = ((TextView) getCurrentView()).getText().toString();
        int oldNumber = TextUtils.isEmpty(oldText) ? -1 : Integer.valueOf(oldText);
        if (oldNumber > 0) {
            setNumber(oldNumber - 1);
        }
    }
}
