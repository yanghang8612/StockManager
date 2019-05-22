package com.casc.stockmanager.helper.net;

import com.casc.stockmanager.helper.net.param.Reply;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface NetInterface {

    @GET
    Call<Reply> get(@Url String url, @QueryMap Map<String, String> header, @QueryMap Map<String, Object> body);

    @POST
    Call<Reply> post(@Url String url, @QueryMap Map<String, String> header, @Body RequestBody body);

    @DELETE
    Call<Reply> delete(@Url String url, @QueryMap Map<String, String> header, @Body RequestBody body);

    @PUT
    Call<Reply> put(@Url String url, @QueryMap Map<String, String> header, @Body RequestBody body);
}
