package com.casc.stockmanager.adapter;

import android.support.annotation.Nullable;

import com.casc.stockmanager.R;
import com.casc.stockmanager.bean.DeliveryBill;
import com.casc.stockmanager.view.NumberSwitcher;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class BillAdapter extends BaseQuickAdapter<DeliveryBill, BaseViewHolder> {

    public BillAdapter(@Nullable List<DeliveryBill> data) {
        super(R.layout.item_delivery_bill, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, DeliveryBill item) {
        helper.setText(R.id.tv_bill_id, item.getBillID())
                .setText(R.id.tv_dealer, item.getDealer())
                .setText(R.id.tv_driver, item.getDriver())
                .setText(R.id.tv_delivery_count, String.valueOf(item.getBuckets().size()))
                .setText(R.id.tv_total_count, String.valueOf(item.getBuckets().size()));
    }
}
