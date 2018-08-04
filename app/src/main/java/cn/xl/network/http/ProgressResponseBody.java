package cn.xl.network.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

class ProgressResponseBody implements Handler.Callback {

    private static final String TAG = "xxx";
    private Handler uiHandler;
    private BufferedSource source;

    private Http.Callback progressListener;
    private long totalLength;
    private long contentLength;
    private long progress;

    private String url;
    private File dir;
    private BufferedSink saveSink;
    private File file;

    ProgressResponseBody(long length, BufferedSource source) {
        uiHandler = new Handler(Looper.getMainLooper(), this);
        contentLength = length;
        this.source = source;
    }

    void saveContent() throws IOException {
        if (saveSink == null && dir != null) {
            if (!dir.exists() && dir.mkdirs());
            file = new File(dir, getNameByUrl(url));
            Log.i(TAG, "contentLength: " + contentLength);
            if (file.exists()) {
                if (file.length() == contentLength) {
                    saveSink = Okio.buffer(Okio.sink(file));
                } else {
                    progress = file.length();
                    uiHandler.obtainMessage().sendToTarget();
                    saveSink = Okio.buffer(Okio.appendingSink(file));
                }
            } else if (file.createNewFile()) {
                saveSink = Okio.buffer(Okio.sink(file));
            }
            totalLength = progress + contentLength;
        }
        Buffer bf = new Buffer();
        long readCount;
        while((readCount = source.read(bf, 8192)) > 0) {
            progress += readCount;
            uiHandler.obtainMessage().sendToTarget();
            saveSink.write(bf, readCount);
            saveSink.flush();
            bf.clear();
        }
        saveSink.close();
        source.close();
        bf.close();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (progressListener != null) {
            progressListener.onProgress((int) (100F * progress / totalLength));
        }
        return true;
    }

    ProgressResponseBody setProgressListener(Http.Callback progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    ProgressResponseBody setUrl(String url) {
        this.url = url;
        return this;
    }

    ProgressResponseBody setDir(File dir) {
        this.dir = dir;
        return this;
    }

    static String getNameByUrl(String url) {
        int nameIndex = url.lastIndexOf('/') + 1;
        return url.substring(nameIndex);
    }
}
