package com.casc.stockmanager.fragment;

import android.support.v4.app.Fragment;

/**
 * Created by Asuka on 2/6/16
 */
public class FragmentFactory {

    private static BaseFragment[] fragments = {
            new BillsFragment(), new ReturnFragment()
    };

    public static Fragment getInstanceByIndex(int index) {
        return fragments[index];
    }
}
