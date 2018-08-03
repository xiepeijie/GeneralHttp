package com.example.payge.network;

import android.app.Application;

import cn.xl.network.http.Config;
import cn.xl.network.http.Http;

public final class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Config config = Config.create()
        .baseUrl("https://news-at.zhihu.com/")
        .addHeader("Content-Type", "application/json")
        .addHeader("FrontType", "scp-mobile-patrol-ui")
        .addHeader("terminalType", "android")
        .addHeader("terminalVersion", "2.3.3")
        .addHeader("traceId", String.format("%s0201%s00000000000000000000000000000000",
                System.currentTimeMillis(), String.valueOf((int) ((Math.random()*9+1)*Math.pow(10, 6)))));
        Http.init(this, config);
    }
}
