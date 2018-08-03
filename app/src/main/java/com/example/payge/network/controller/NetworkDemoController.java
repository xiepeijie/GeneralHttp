package com.example.payge.network.controller;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.example.payge.network.request.LoginRequest;
import com.example.payge.network.response.CommonResponse;
import com.example.payge.network.response.LoginResponse;
import com.example.payge.network.response.StoriesResponse;

import java.io.File;
import java.io.FileOutputStream;

import cn.xl.network.http.Http;
import cn.xl.network.http.Request;
import okhttp3.internal.Util;

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
            protected void onSuccess(LoginResponse response) {
                upload(response.data.token);
            }

            @Override
            protected void onError(int errorCode, String msg) {

            }
        });
    }

    public void upload(String token) {
        File DCIM = Environment.getExternalStoragePublicDirectory("DCIM");
        //巡更上报事件接口，一次请求只支持一张图
        Request request = Request.fileRequest("http://10.101.70.235:8040/scp-communityplatformapicomponent/app/uploadFile");
        request.addHeader("Authorization", token);
        request.addHeader("FrontType", "scp-mobile-patrol-ui");
        File[] files = new File(DCIM, "Camera").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Log.i("xxx", "upload: " + file.getPath());
                    request.addFile("file", file);
                    break;
                }
            }
        }
        Http.getInstance().upload(tag, request, new Http.Callback<CommonResponse>() {
            @Override
            protected void onProgress(int progress) {
                Log.i("xxx", "upload onProgress: " + progress);
            }

            @Override
            protected void onHandle(String rawData, CommonResponse response) {
                Log.i("xxx", "onHandle: " + rawData);
            }

            @Override
            protected void onSuccess(CommonResponse response) {

            }

            @Override
            protected void onError(int errorCode, String msg) {

            }
        });
    }

    public void download() {
        Http.getInstance().download(tag, "http://10.101.70.246:8888/group1/M00/51/70/CmVG9lthsReAD6VfACJgAMOJYnI302.jpg", new Http.Callback<Void>() {
            @Override
            protected void onProgress(int progress) {
                Log.i("xxx", "download onProgress: " + progress);
            }

            @Override
            protected void onDownloaded(byte[] file) {
                super.onDownloaded(file);
                File dir = Environment.getExternalStoragePublicDirectory("Download");
                File f = new File(dir, Integer.toHexString(hashCode()) + ".jpg");
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(f);
                    out.write(file);
                } catch (Exception e) {
                    Log.e("xxx", "onDownloaded: ", e);
                } finally {
                    Util.closeQuietly(out);
                }

            }

            @Override
            protected void onSuccess(Void d) {

            }

            @Override
            protected void onError(int errorCode, String msg) {

            }
        });
    }
}
