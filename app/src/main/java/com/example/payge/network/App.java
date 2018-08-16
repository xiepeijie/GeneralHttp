package com.example.payge.network;

import android.app.Application;
import android.os.Environment;

import java.io.File;

import cn.xl.network.http.Config;
import cn.xl.network.http.Http;

public final class App extends Application {

    public static String token;

    @Override
    public void onCreate() {
        super.onCreate();
        Config config = Config.create()
        .baseUrl("https://news-at.zhihu.com/")
        .addHeader("Content-Type", "application/json")
        .addHeader("FrontType", "scp-mobile-patrol-ui")
        .addHeader("terminalType", "android")
        .addHeader("terminalVersion", "2.3.3")
        .addHeader("traceId", String.valueOf(System.currentTimeMillis()))
        .cacheDir(new File(Environment.getExternalStoragePublicDirectory("AAA"), "HTTP"))
        .maxCacheSize(10 * 1024 * 1024)
        .ssLProtocolAndCert("TLSv1");
        Http.init(this, config);
    }
}
