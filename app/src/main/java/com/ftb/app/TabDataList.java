package com.ftb.app;

import com.ftb.app.fragment.HomeFragment;
import com.ftb.app.fragment.SendFragment;
import com.ftb.app.fragment.SettingsFragment;


public class TabDataList {
    public static String[] getTabsTxt() {
        String[] tabs = {"钱包", "发送", "设置"};
        return tabs;
    }

    public static int[] getTabsImg() {
        int[] ids = {R.drawable.ftblgt, R.drawable.send_lgt,
                R.drawable.settings_lgt};
        return ids;
    }

    public static int[] getTabsImgLight() {
        int[] ids = {R.drawable.ftb, R.drawable.send,
                R.drawable.settings};
        return ids;
    }

    public static Class[] getFragments() {
        Class[] clz = {HomeFragment.class, SendFragment.class, SettingsFragment.class};
        return clz;
    }
}
