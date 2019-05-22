package com.casc.stockmanager.bean;

public class Goods {

    // 产品名称
    private IntStrPair product;

    // 总数量
    private int totalCount;

    // 出库数量
    private int curCount;

    public Goods(IntStrPair product, int totalCount) {
        this.product = product;
        this.totalCount = totalCount;
        this.curCount += totalCount == 0 ? 1 : 0;
    }

    public int getCode() {
        return product.getInt();
    }

    public String getName() {
        return product.getStr();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void addTotalCount(int count) {
        totalCount += count;
    }

    public int getLeftCount() {
        return Math.max(0, totalCount - curCount);
    }

    public int getCurCount() {
        return curCount;
    }

    public void addCurCount() {
        this.curCount++;
    }

    public void minusCurCount() {
        this.curCount--;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Goods) {
            Goods other = (Goods) obj;
            return this.product.getInt() == other.product.getInt();
        }
        return false;
    }
}
