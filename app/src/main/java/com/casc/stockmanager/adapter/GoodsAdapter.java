package com.casc.stockmanager.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import com.casc.stockmanager.R;
import com.casc.stockmanager.bean.Goods;
import com.casc.stockmanager.view.NumberSwitcher;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class GoodsAdapter extends BaseQuickAdapter<Goods, BaseViewHolder> {

    public GoodsAdapter(@Nullable List<Goods> data) {
        super(R.layout.item_goods, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Goods item) {
        ((NumberSwitcher) helper.getView(R.id.ns_delivery_count))
                .setNumber(item.getCurCount());
        helper.setText(R.id.tv_goods_name, item.getName())
                .setText(R.id.tv_total_count, String.valueOf(item.getTotalCount()));
        helper.getView(R.id.tv_total_count)
                .setVisibility(item.getTotalCount() == 0 ? View.INVISIBLE : View.VISIBLE);
    }
}
