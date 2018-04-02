package com.wallet.crypto.ftb.publicapi;

import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wallet.crypto.ftb.App;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zhanghesong on 2018/3/18.
 */

public class ApiPublic {
    //读超时长，单位：毫秒
    public static final int READ_TIME_OUT = 20;
    //连接时长，单位：毫秒
    public static final int CONNECT_TIME_OUT = 10;
    //写的时长
    public static final int WRITE_TIME_OUT = 20;

    public Retrofit retrofit;

    public OkHttpClient okHttpClient;

    public PublicService publicService;

    private static SparseArray<ApiPublic> sRetrofitManager = new SparseArray<>(2);
    /**
     * 设缓存有效期为两天
     */
    private static final long CACHE_STALE_SEC = 60 * 60 * 24 * 2;
    /**
     * 查询缓存的Cache-Control设置，为if-only-cache时只查询缓存而不会请求服务器，max-stale可以配合设置缓存失效时间
     * max-stale 指示客户机可以接收超出超时期间的响应消息。如果指定max-stale消息的值，那么客户机可接收超出超时期指定值之内的响应消息。
     */
    private static final String CACHE_CONTROL_CACHE = "only-if-cached, max-stale=" + CACHE_STALE_SEC;
    /**
     * 查询网络的Cache-Control设置，头部Cache-Control设为max-age=0
     * (假如请求了服务器并在a时刻返回响应结果，则在max-age规定的秒数内，浏览器将不会发送对应的请求到服务器，数据由缓存直接返回)时则不会使用缓存而请求服务器
     */
    private static final String CACHE_CONTROL_AGE = "max-age=0";


    private ApiPublic(int host) {
        //缓存
        File cacheFile = new File(App.getApp().getCacheDir(), "cache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb
        //增加头部信息
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request build = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .build();
                return chain.proceed(build);
            }
        };

        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)
                .cache(cache)
                .build();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls().create();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                //添加Gson转换器
                .addConverterFactory(GsonConverterFactory.create(gson))
                //添加Retrofit 到 Rxjava 转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(ApiUtils.URLCON)
                .build();
        publicService = retrofit.create(PublicService.class);
    }

    public static PublicService getDefault(int hostType) {
        ApiPublic retrofitManager = sRetrofitManager.get(hostType);
        if (retrofitManager == null) {
            retrofitManager = new ApiPublic(hostType);
            sRetrofitManager.put(hostType, retrofitManager);
        }
        return retrofitManager.publicService;
    }
}
