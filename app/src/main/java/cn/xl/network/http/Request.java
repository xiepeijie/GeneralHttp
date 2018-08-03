package cn.xl.network.http;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class Request<T> {

    @NonNull
    public String path;
    public Map<String, String> headers = new HashMap<>();
    public Map<String, String> params;
    public Map<String, RequestBody> files;
    public T jsonParams;

    public static Request create(String path) {
        return new Request<Object>(path, false, false, null);
    }

    public static Request paramRequest(String path) {
        return new Request<Object>(path, true, false, null);
    }

    public static Request fileRequest(String path) {
        return new Request<Object>(path, false, true, null);
    }

    public static <E> Request<E> jsonRequest(String path, E params) {
        return new Request<>(path, false, false,  params);
    }

    private Request(@NonNull String path, boolean hasParams, boolean hasFile, T paramsBean) {
        this.path = path;
        if (hasParams) {
            params = new HashMap<>();
        } else {
            params = Collections.emptyMap();
        }
        if (hasFile) {
            files = new LinkedHashMap<>();
        } else {
            files = Collections.emptyMap();
        }
        jsonParams = paramsBean;
    }

    public Request addHeader(String name, Object value) {
        headers.put(name, String.valueOf(value));
        return this;
    }

    public Request addParam(String key, Object value) {
        if (params == Collections.EMPTY_MAP) {
            Log.i("xxx", "EMPTY_MAP");
            return this;
        }
        params.put(key, String.valueOf(value));
        return this;
    }

    public Request addFilePath(String key, String filePath) {
        File file = new File(filePath);
        addFile(key, file);
        return this;
    }

    public Request addFile(String key, File file) {
        if (file == null || !file.exists()) {
            Log.e("xxx", "addFile: file not exist");
            return this;
        }
        String name = String.format("%s\"; filename=\"%s", key, file.getName());
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        files.put(name, requestBody);
        return this;
    }

    public void addFilePaths(String key, List<String> filePathList) {
        File file;
        for (String filePath : filePathList) {
            file = new File(filePath);
            addFile(key, file);
        }
    }

    public void addFiles(String key, List<File> fileList) {
        for (File file : fileList) {
            addFile(key, file);
        }
    }

}
