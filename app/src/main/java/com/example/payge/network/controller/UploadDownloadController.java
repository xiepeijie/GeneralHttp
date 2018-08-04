package com.example.payge.network.controller;

import android.os.Environment;
import android.util.Log;

import com.example.payge.network.App;
import com.example.payge.network.response.CommonResponse;

import java.io.File;

import cn.xl.network.http.Http;
import cn.xl.network.http.Request;

public class UploadDownloadController extends Controller {

    private final String url = "http://dldir1.qq.com/weixin/android/weixin667android1320.apk";

    public UploadDownloadController(Object tag) {
        super(tag);
    }

    public void upload(Http.Callback<CommonResponse> callback) {
        File DCIM = Environment.getExternalStoragePublicDirectory("DCIM");
        //巡更上报事件接口，一次请求只支持一张图
        final Request request = Request.fileRequest("http://10.101.70.235:8040/scp-communityplatformapicomponent/app/uploadFile");
        request.addHeader("Authorization", App.token);
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
        Http.getInstance().upload(tag, request, callback);
    }

    public void download(Http.Callback<Void> callback) {
        /*
        http://imtt.dd.qq.com/16891/4FAE27142B24E812D2FD69A9C604CA2D.apk
         */
        File dir = Environment.getExternalStoragePublicDirectory("AAA");
        Http.getInstance().download(tag, url, callback);
    }

    public void cancelDownload() {
        Http.getInstance().cancelByUrl(url);
    }
}
