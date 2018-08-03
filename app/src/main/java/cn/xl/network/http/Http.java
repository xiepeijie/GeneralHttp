package cn.xl.network.http;


import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;


public final class Http implements GenericLifecycleObserver {

    private static final String TAG = "xxx";
    private static final String COMMON_TAG = Http.class.getName() + ".common.tag";

    public interface HttpApi {
        @GET
        Observable<retrofit2.Response<String>> rxJavaGet(
                @Url String url,
                @HeaderMap Map<String, String> headers,
                @QueryMap Map<String, String> params
        );
        @POST
        Observable<retrofit2.Response<String>> rxJavaPost(
                @Url String url,
                @HeaderMap Map<String, String> headers,
                @QueryMap Map<String, String> params
        );
        @POST
        Observable<retrofit2.Response<String>> rxJavaPost(
                @Url String url,
                @HeaderMap Map<String, String> headers,
                @Body Object jsonParams
        );
        @Multipart
        @POST
        Observable<retrofit2.Response<String>> rxJavaUpload(
                @Url String url,
                @HeaderMap Map<String, String> headers,
                @PartMap Map<String, RequestBody> partMap
        );
        @Streaming
        @GET
        Observable<ResponseBody> rxJavaDownload(
                @Url String url
        );
    }

    public static abstract class Callback<T> {
        protected void onStart(){}
        protected void onHandle(String rawData, T t){}
        protected abstract void onSuccess(T t);
        protected abstract void onError(int errorCode, String msg);
        protected void onProgress(int progress){}
        protected void onDownloaded(byte[] file){}
    }

    private static Http api = new Http();

    private OkHttpClient client;
    private OkHttpClient uploadClient;
    private OkHttpClient downloadClient;
    private Retrofit retrofit;
    private Config config;

    private Interceptor uploadProgressInterceptor;
    private Interceptor downloadProgressInterceptor;


    private final HashMap<Integer, List<Pair<Integer, Disposable>>> disposableCache = new HashMap<>();

    private final Gson gson = new Gson();

    private Toast toast;
    private String toastText;

    public static Http getInstance() {
        return api;
    }

    private Http() {}

    public static void init(Context context, final Config config) {
        if (config == null || config.getBaseUrl() == null) {
            Log.e(TAG, "Config is null, http can't work");
            return;
        }
        if (api.config == config) {
            Log.e(TAG, "Config not change, http init invalidate");
            return;
        }
        api.config = config;
//        Cache cache = new Cache(new File(context.getCacheDir(), "http"), 10 * 1024 * 1024);
        api.client = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
//                .cache(cache)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        okhttp3.Request.Builder builder = chain.request().newBuilder();
                        if (!config.getHeaders().isEmpty()) {
                            Set<Map.Entry<String, String>> entrySet = config.getHeaders().entrySet();
                            for (Map.Entry<String, String> entry : entrySet) {
                                builder.addHeader(entry.getKey(), entry.getValue());
                            }
                        }
                        return chain.proceed(builder.build());
                    }
                }).build();

        Retrofit.Builder builder = new Retrofit.Builder();
        builder.client(api.client)
        .baseUrl(config.getBaseUrl());
        if (api.retrofit == null) {
            builder.addConverterFactory(new ConvertFactory(api.gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()));
        }

        api.retrofit = builder.build();

        if (api.toast == null && context != null)
            api.toast = Toast.makeText(context.getApplicationContext(), "", Toast.LENGTH_SHORT);
    }

    /**
     * @param keys 需要取消的请求的url
     */
    private void cancel(String... keys) {
        if (keys == null || keys.length == 0) return;
        Collection<List<Pair<Integer, Disposable>>> pairList;
        pairList = disposableCache.values();
        boolean isBreak;
        for (String key : keys) {
            isBreak = false;
            for (List<Pair<Integer, Disposable>> list : pairList) {
                for (Pair<Integer, Disposable> pair : list) {
                    if (key.hashCode() == pair.first) {
                        pair.second.dispose();
                        list.remove(pair);
                        isBreak = true;
                        break;
                    }
                }
                if (isBreak) break;
            }
        }
    }

    /**
     * @param tag 请求时传入的tag
     */
    public void cancel(Object tag) {
        if (tag == null) return;
        List<Pair<Integer, Disposable>> disposableList;
        disposableList = disposableCache.get(tag.hashCode());
        if (disposableList != null) {
            for (Pair<Integer, Disposable> pair : disposableList) {
                pair.second.dispose();
            }
            disposableCache.remove(tag.hashCode());
        }
    }

    public <T> void post(final Request request, final Callback<T> callback) {
        post(null, request, callback);
    }

    /**
     * @param tag 一般传Activity或Fragment，如果传其他类型的对象作为tag，则需手动主动取消请求
     */
    public <T> void post(Object tag, final Request request,
                         final Callback<T> callback) {
        rxJavaRequest(tag, true, request, callback);
    }

    public <T> void get(final String url, final Callback<T> callback) {
        get(null, Request.create(url), callback);
    }

    public <T> void get(final Request request, final Callback<T> callback) {
        get(null, request, callback);
    }

    /**
     * @param tag 一般传Activity或Fragment，如果传其他类型的对象作为tag，则需手动主动取消请求
     */
    public <T> void get(Object tag, final Request request,
                        final Callback<T> callback) {
        rxJavaRequest(tag, false, request, callback);
    }

    public <T, E> void rxJavaRequest(final Object tag, final boolean post, @NonNull final Request<E> request,
                                     @NonNull final Callback<T> callback) {
        callback.onStart();
        retrofit = retrofit.newBuilder().client(client).build();
        HttpApi http = retrofit.create(HttpApi.class);
        Observable<retrofit2.Response<String>> observable;
        E json = request.jsonParams;
        if (post) {
            if (json == null) {
                observable = http.rxJavaPost(request.path, request.headers, request.params);
            } else {
                observable = http.rxJavaPost(request.path, request.headers, json);
            }
        } else {
            observable = http.rxJavaGet(request.path, request.headers, request.params);
        }
        process(tag, request, callback, observable);
    }

    public <T, E> void upload(Request<E> request, final Callback<T> callback) {
        upload(null, request, callback);
    }

    public <T, E> void upload(final Object tag, Request<E> request, final Callback<T> callback) {
        callback.onStart();
        if (uploadProgressInterceptor == null) {
            uploadProgressInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    okhttp3.Request okHttpRequest = chain.request();
                    okhttp3.RequestBody body = okHttpRequest.body();
                    if (body != null) {
                        Log.i(TAG, "upload intercept: " + body.getClass().getSimpleName());
                        ProgressRequestBody prb = new ProgressRequestBody(body);
                        prb.setProgressListener(callback);
                        okHttpRequest = okHttpRequest.newBuilder().method(okHttpRequest.method(), prb).build();
                    }
                    return chain.proceed(okHttpRequest);
                }
            };
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.addInterceptor(uploadProgressInterceptor);
            uploadClient = builder.build();
        }
        retrofit = retrofit.newBuilder().client(uploadClient).build();
        HttpApi http = retrofit.create(HttpApi.class);
        Observable<retrofit2.Response<String>> observable;
        Log.i(TAG, "file count: " + request.files.size());
        observable = http.rxJavaUpload(request.path, request.headers, request.files);
        process(tag, request, callback, observable);
    }

    public <T> void download(String url, final Callback<T> callback) {
        download(null, url, callback);
    }

    public <T> void download(final Object tag, String url, final Callback<T> callback) {
        callback.onStart();
        if (downloadProgressInterceptor == null) {
            downloadProgressInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response response = chain.proceed(chain.request());
                    ResponseBody body = response.body();
                    if (body != null) {
                        Log.i(TAG, "download intercept: " + body.getClass().getSimpleName());
                        ProgressResponseBody prb = new ProgressResponseBody(body.contentType(), body.contentLength(), body.source());
                        prb.setProgressListener(callback);
                        response = response.newBuilder().body(prb).build();
                    }
                    return response;
                }
            };
            downloadClient = new OkHttpClient.Builder().addInterceptor(downloadProgressInterceptor).build();
        }
        retrofit = retrofit.newBuilder().client(downloadClient).build();
        HttpApi http = retrofit.create(HttpApi.class);
        Observable<ResponseBody> observable;
        observable = http.rxJavaDownload(url);
        Observable<String> mapObservable = observable.map(new Function<ResponseBody, String>() {
            @Override
            public String apply(ResponseBody body) throws Exception {
                callback.onDownloaded(body.bytes());
                return "";
            }
        });
        final int hash = tag == null ? COMMON_TAG.hashCode() : tag.hashCode();
        final String pathKey = url;
        mapObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        Log.i(TAG, "onSubscribe: " + pathKey);
                        registerLifecycle(tag);
                        cacheDisposableIfNeed(disposable, hash, pathKey);
                    }

                    @Override
                    public void onNext(String non) {}

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "onError", throwable);
                        dispose(hash, pathKey);
                        callback.onError(-1, throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, "onComplete");
                        dispose(hash, pathKey);
                        callback.onSuccess(null);
                    }
                });
    }

    private <T, E> void process(final Object tag, @NonNull final Request<E> request, @NonNull final Callback<T> callback,
                                Observable<retrofit2.Response<String>> observable) {
        Observable<Pair<String, T>> mapObservable = observable.map(new Function<retrofit2.Response<String>, Pair<String, T>>() {
            @Override
            public Pair<String, T> apply(retrofit2.Response<String> response) throws Exception {
                int code;
                String msg;
                Pair<String, T> pair;
                code = response.code();
                if (code == 200) {
                    String data = response.body();
                    if (data != null) {
                        Class<T> cls = getParameterizedTypeClass(callback);
                        T t = gson.fromJson(data, cls);
                        if (t != null) {
                            pair = new Pair<>(data, t);
                            callback.onHandle(data, t);
                        } else {
                            msg = "json parse fail";
                            pair = new Pair<>(msg, null);
                            Log.e(TAG, "apply: " + msg);
                        }
                        return pair;
                    } else {
                        msg = "response body is null";
                    }
                } else {
                    ResponseBody errorBody = response.errorBody();
                    if (errorBody == null) {
                        msg = "errorBody is null";
                    } else {
                        msg = errorBody.string();
                    }
                }
                Log.e(TAG, "apply: " + msg);
                toastText = msg;
                pair = new Pair<>(msg, null);
                return pair;
            }
        });
        final int hash = tag == null ? COMMON_TAG.hashCode() : tag.hashCode();
        final String pathKey = request.path;
        mapObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Pair<String, T>>() {

                    @Override
                    public void onSubscribe(Disposable disposable) {
                        Log.i(TAG, "onSubscribe: " + pathKey);
                        registerLifecycle(tag);
                        cacheDisposableIfNeed(disposable, hash, pathKey);
                    }

                    @Override
                    public void onNext(Pair<String, T> pair) {
                        T t = pair.second;
                        if (t != null) {
                            Log.i(TAG, "onNext: success");
                            callback.onSuccess(t);
                        } else {
                            Log.i(TAG, "onNext: fail");
                            callback.onError(-1, pair.first);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "onError", throwable);
                        dispose(hash, pathKey);
                        callback.onError(-2, throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        dispose(hash, pathKey);
                    }
                });
    }

    private void registerLifecycle(Object tag) {
        LifecycleOwner owner;
        if (tag instanceof LifecycleOwner) {
            owner = (LifecycleOwner) tag;
            owner.getLifecycle().addObserver(this);
        }
    }

    private void cacheDisposableIfNeed(Disposable disposable, int hash, String key) {
        Pair<Integer, Disposable> pair = Pair.create(key.hashCode(), disposable);
        List<Pair<Integer, Disposable>> list = disposableCache.get(hash);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(pair);
        disposableCache.put(hash, list);
    }

    private void dispose(int hash, @NonNull String key) {
        List<Pair<Integer, Disposable>> list = disposableCache.get(hash);
        if (list != null) {
            for (Pair<Integer, Disposable> pair : list){
                if (key.hashCode() == pair.first) {
                    pair.second.dispose();
                    list.remove(pair);
                    break;
                }
            }
        }
        showToast();
    }

    private void showToast() {
        if (toast == null || toastText == null) return;
        try {
            toast.setText(toastText);
            toast.show();
            toastText = null;
        } catch (Throwable e) {
            Log.e(TAG, "showToast", e);
        }
    }

    public static <T> Class<T> getParameterizedTypeClass(Object obj) {
        ParameterizedType pt = (ParameterizedType) obj.getClass().getGenericSuperclass();
        Type[] atr = pt.getActualTypeArguments();
        if (atr != null && atr.length > 0) {
            return (Class<T>) atr[0];
        }
        return null;
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        if (source.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            source.getLifecycle().removeObserver(this);
            cancel(source);
        }
    }
}
