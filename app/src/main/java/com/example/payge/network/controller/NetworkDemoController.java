package com.example.payge.network.controller;

import android.util.Base64;

import com.example.payge.network.App;
import com.example.payge.network.request.LoginRequest;
import com.example.payge.network.response.LoginResponse;
import com.example.payge.network.response.StoriesResponse;

import cn.xl.network.http.Http;
import cn.xl.network.http.Request;

public class NetworkDemoController extends Controller {

    public NetworkDemoController(Object tag) {
        super(tag);
    }

    public void getStoryList(Http.Callback<StoriesResponse> callback) {
        Request request = Request.create("api/4/news/latest");
        Http.getInstance().get(tag, request, callback);
    }

    public void login() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.userName = "boss0101";
        loginRequest.password = Base64.encodeToString("123456".getBytes(), Base64.NO_WRAP);
        Request<LoginRequest> request = Request.jsonRequest("http://10.101.70.235:8040/scp-communityplatformapicomponent/app/login", loginRequest);
        Http.getInstance().post(tag, request, new Http.Callback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                App.token = response.data.token;
            }

            @Override
            public void onError(int errorCode, String msg) {

            }
        });
    }

}
