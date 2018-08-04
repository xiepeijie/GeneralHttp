package cn.xl.network.http;

import java.util.HashMap;
import java.util.Map;

public final class Config {

    private String baseUrl;
    private int connectTimeout = 3;
    private int readTimeout = 5;
    private int writeTimeout = 5;
    private Map<String, String> headers = new HashMap<>();

    public static Config create() {
        return new Config();
    }

    private Config(){}

    public Config addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Config baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public Config connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public Config readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public Config writeTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }
}
