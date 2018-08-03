package cn.xl.network.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

class ProgressResponseBody extends ResponseBody implements Handler.Callback {

    private static final String TAG = "xxx";
    private Handler uiHandler;
    private MediaType mediaType;
    private BufferedSource source;

    private Http.Callback progressListener;
    private long contentLength;
    private long progress;

    ProgressResponseBody(MediaType type, long length, Source source) {
        uiHandler = new Handler(Looper.getMainLooper(), this);
        mediaType = type;
        contentLength = length;
        this.source = Okio.buffer(new ForwardingSource(source) {
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long readByteCount = super.read(sink, byteCount);
                if (readByteCount > 0) {
                    progress += readByteCount;
                    uiHandler.obtainMessage().sendToTarget();
                }
                return readByteCount;
            }
        });
    }

    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public BufferedSource source() {
        return source;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (progressListener != null) {
            progressListener.onProgress((int) (100 * progress / contentLength));
        }
        return true;
    }

    public void setProgressListener(Http.Callback progressListener) {
        this.progressListener = progressListener;
    }
}
