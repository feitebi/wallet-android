package com.wallet.crypto.ftb.publicapi;

import com.wallet.crypto.ftb.publicapi.publicbean.AirDropListBean;
import com.wallet.crypto.ftb.publicapi.publicbean.BaseResponsBean;
import com.wallet.crypto.ftb.publicapi.publicbean.CandyBean;
import com.wallet.crypto.ftb.publicapi.publicbean.TokenListBean;
import com.wallet.crypto.ftb.publicapi.publicbean.UserDetailBean;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by zhanghesong on 2018/3/18.
 */

public interface PublicService {

    @FormUrlEncoded
    @POST("user/summary")
    Call<BaseResponsBean<UserDetailBean>>userDate(@Field("toAddr") String toAddr);

    @FormUrlEncoded
    @POST("open_list")
    Call<BaseResponsBean<TokenListBean>> tokenList(@Field("toAddr") String toAddr,
            @Field("start") String start, @Field("limit") String limit);

    @FormUrlEncoded
    @POST("airdrop_list")
    Call<BaseResponsBean<AirDropListBean>> airDroplist(@Field("toAddr") String toAddr);

    @FormUrlEncoded
    @POST("get")
    Call<BaseResponsBean<CandyBean>> candy(@Field("toAddr") String toAddr,
            @Field("symbol") String symbol, @Field("tokenId") String tokenId
            , @Field("imei") String imei, @Field("timestamp") String timestamp,
            @Field("sign") String sign);
}
