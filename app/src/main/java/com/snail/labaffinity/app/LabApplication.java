package com.snail.labaffinity.app;

import android.app.Application;
import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;
import com.snail.labaffinity.utils.LogUtils;

import cn.campusapp.router.Router;

/**
 * Author: hzlishang
 * Data: 16/10/11 下午12:44
 * Des:
 * version:
 */
public class LabApplication extends Application {
    private HttpProxyCacheServer proxy;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        Router.initBrowserRouter(this);
        Router.initActivityRouter(getApplicationContext());
        LogUtils.v("LabApplication create");

    }

    public static HttpProxyCacheServer getProxy(Context context) {
        LabApplication app = (LabApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }

    private static Application sApplication;

    public static Application getContext() {
        return sApplication;
    }
}
