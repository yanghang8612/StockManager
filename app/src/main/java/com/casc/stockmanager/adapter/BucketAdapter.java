package com.casc.stockmanager.adapter;

import com.casc.stockmanager.R;
import com.casc.stockmanager.bean.Bucket;
import com.casc.stockmanager.utils.CommonUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class BucketAdapter extends BaseQuickAdapter<Bucket, BaseViewHolder> {

    public BucketAdapter(List<Bucket> data) {
        super(R.layout.item_bucket, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Bucket item) {
        helper.setText(R.id.tv_bucket_product_name, CommonUtils.getProduct(item.getEPC()).getStr())
                .setText(R.id.tv_bucket_body_code, CommonUtils.getBodyCode(item.getEPC()));
    }
}
