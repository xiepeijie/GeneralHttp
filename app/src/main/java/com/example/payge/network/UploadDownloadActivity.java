package com.example.payge.network;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.payge.network.controller.UploadDownloadController;
import com.example.payge.network.response.CommonResponse;

import cn.xl.network.http.Http;

public class UploadDownloadActivity extends AppCompatActivity {

    UploadDownloadController controller;
    ProgressBar uploadProgress;
    ProgressBar downloadProgress;

    boolean isDownloading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_download);
        controller = new UploadDownloadController(this);
        uploadProgress = findViewById(R.id.progress1);
        downloadProgress = findViewById(R.id.progress2);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upload:
                controller.upload(new Http.Callback<CommonResponse>() {
                    @Override
                    protected void onProgress(int progress) {
                        Log.i("xxx", "upload onProgress: " + progress);
                        uploadProgress.setProgress(progress);
                    }

                    @Override
                    protected void onHandle(String rawData, CommonResponse response) {
                        Log.i("xxx", "onHandle: " + rawData);
                    }

                    @Override
                    public void onSuccess(CommonResponse response) {
                        uploadProgress.setProgress(0);
                    }

                    @Override
                    public void onError(int errorCode, String msg) {

                    }
                });
                break;
            case R.id.download:
                if (!isDownloading) {
                    controller.download(new Http.Callback<Void>() {
                        @Override
                        protected void onProgress(int progress) {
                            Log.i("xxx", "download onProgress: " + progress);
                            downloadProgress.setProgress(progress);
                        }

                        @Override
                        public void onSuccess(Void d) {
                            downloadProgress.setProgress(0);
                        }

                        @Override
                        public void onError(int errorCode, String msg) {

                        }
                    });
                } else {
                    controller.cancelDownload();
                }
                isDownloading = !isDownloading;
        }
    }
}
