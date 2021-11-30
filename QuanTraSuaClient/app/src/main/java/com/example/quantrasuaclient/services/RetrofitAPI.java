package com.example.quantrasuaclient.Services;

import com.example.quantrasuaclient.Model.MsgModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RetrofitAPI {
    @GET
    Call<MsgModel> getMessage(@Url String url);
}
