package com.casc.stockmanager.bean;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Stack {

    private int id;

    private Set<String> mBuckets = new HashSet<>();

    public Stack() {
        this.id = -1;
    }

    public Stack(List<String> buckets) {
        this(-1, buckets);
    }

    public Stack(int id, List<String> buckets) {
        this.id = id;
        mBuckets.addAll(buckets);
    }

    public int getId() {
        return id;
    }

    public boolean isBulk() {
        return id == -1;
    }

    public int size() {
        return mBuckets.size();
    }

    public List<String> getBuckets() {
        return new LinkedList<>(mBuckets);
    }

    public void addBuckets(List<String> buckets) {
        mBuckets.addAll(buckets);
    }

    public void addBucket(String epcStr) {
        mBuckets.add(epcStr);
    }

    public boolean contains(String epcStr) {
        return mBuckets.contains(epcStr);
    }

    public void removeBucket(String epcStr) {
        mBuckets.remove(epcStr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stack stack = (Stack) o;
        return id == stack.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
