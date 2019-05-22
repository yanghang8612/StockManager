package com.casc.stockmanager.adapter;

import android.support.annotation.Nullable;

import com.casc.stockmanager.R;
import com.casc.stockmanager.bean.Stack;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class StackAdapter extends BaseQuickAdapter<Stack, BaseViewHolder> {

    public StackAdapter(@Nullable List<Stack> data) {
        super(R.layout.item_stack, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Stack item) {
        if (item.size() == 0) {
            helper.setVisible(R.id.ll_stack_root, false);
        } else {
            helper.setVisible(R.id.ll_stack_root, true);
        }
        helper.setText(R.id.tv_stack_type, item.isBulk() ? "散货" : "整垛")
                .setText(R.id.tv_stack_size, item.size() + "桶");
    }
}
