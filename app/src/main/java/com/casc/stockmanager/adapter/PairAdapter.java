package com.casc.stockmanager.adapter;

import android.support.annotation.Nullable;

import com.casc.stockmanager.R;
import com.casc.stockmanager.bean.IntStrPair;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class PairAdapter extends BaseQuickAdapter<IntStrPair, BaseViewHolder> {

    public PairAdapter(@Nullable List<IntStrPair> data) {
        super(R.layout.item_pair, data);
    }

    public PairAdapter(int layoutResId, @Nullable List<IntStrPair> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, IntStrPair item) {
        helper.setText(R.id.tv_str_value, item.getStr())
                .setText(R.id.tv_int_value, String.valueOf(item.getInt()));
    }
}
