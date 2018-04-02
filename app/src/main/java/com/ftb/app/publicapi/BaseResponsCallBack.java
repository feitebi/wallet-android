package com.wallet.crypto.ftb.publicapi;

import com.wallet.crypto.ftb.publicapi.publicbean.BaseResponsBean;

import java.io.Serializable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zhanghesong on 2018/3/18.
 */

public abstract class BaseResponsCallBack<T> implements Callback<BaseResponsBean<T>>,Serializable {
    protected Call<BaseResponsBean<T>> call;
    @Override
    public void onResponse(Call<BaseResponsBean<T>> call, Response<BaseResponsBean<T>> response) {
        this.call=call;
        if(response.isSuccessful()){//服务器返回正常
            BaseResponsBean<T> contentBean=response.body();
            if(contentBean!=null){
                T obj=response.body().getEntity();
                onResponse(obj);
            }else{
                onServerError();
            }
        }else {//服务器返回异常

        }
        onFinish();
    }

    @Override
    public void onFailure(Call<BaseResponsBean<T>> call, Throwable t) {
        this.call=call;
        onFailure(t);
        onFinish();
    }
    public abstract  void onResponse(T result);

    public void onFailure(Throwable e) {
    }
    public void onFailure(String msg) {
    }
    public void onFinish() {

    }

    /**
     * 服务器返回异常时的方法
     */
    protected void onServerError( ) {

    }
}
