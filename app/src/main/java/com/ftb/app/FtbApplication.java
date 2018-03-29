package com.ftb.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import devliving.online.securedpreferencestore.DefaultRecoveryHandler;
import devliving.online.securedpreferencestore.SecuredPreferenceStore;

/**
 * Created by mangoo on 2017/9/5.
 */

public class FtbApplication extends Application {

    private static Application instance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            SecuredPreferenceStore.init(instance.getApplicationContext(), new DefaultRecoveryHandler());

            // TEST clear every time.
            // SecuredPreferenceStore.getSharedInstance().edit().clear().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Application getInstance() {
        return instance;
    }

}