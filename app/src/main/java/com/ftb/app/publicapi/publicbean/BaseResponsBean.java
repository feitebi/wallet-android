package com.wallet.crypto.ftb.publicapi.publicbean;

/**
 * Created by zhanghesong on 2018/3/18.
 */

public class BaseResponsBean<T> {
    public T entity;

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
}
