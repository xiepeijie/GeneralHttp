package cn.xl.network.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

class ProgressRequestBody extends RequestBody implements Handler.Callback {

    private static final String TAG = "xxx";
    private Handler uiHandler;
    private RequestBody body;

    private Http.Callback progressListener;
    private long contentLength;
    private long progress;

    ProgressRequestBody(RequestBody body) {
        uiHandler = new Handler(Looper.getMainLooper(), this);
        this.body = body;
    }

    @Override
    public MediaType contentType() {
        return body.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return body.contentLength();
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        contentLength = body.contentLength();
        BufferedSink sink = Okio.buffer(new ForwardingSink(bufferedSink) {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (byteCount > 0) {
                    progress += byteCount;
                    uiHandler.obtainMessage().sendToTarget();
                }
            }
        });
        body.writeTo(sink);
        sink.flush();
        sink.close();
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
