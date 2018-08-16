package cn.xl.network.http;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class Config {

    private String baseUrl;
    private int connectTimeout = 3;
    private int readTimeout = 5;
    private int writeTimeout = 5;
    private Map<String, String> headers = new HashMap<>();
    private File cacheDir;
    private int maxCacheSize;

    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager trustManager;

    private List<String> trustHostNames = new ArrayList<>();

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

    public File getCacheDir() {
        return cacheDir;
    }

    public Config cacheDir(File dir) {
        cacheDir = dir;
        return this;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public Config maxCacheSize(int cacheSize) {
        maxCacheSize = cacheSize;
        return this;
    }

    public Config ssLProtocolAndCert(String protocol, String... certs) {
        TrustManager[] trustManagers;
        try {
            if (certs.length > 0) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null);
                InputStream certInput;
                for (String cert : certs) {
                    certInput = new ByteArrayInputStream(cert.getBytes("UTF-8"));
                    keyStore.setCertificateEntry(Integer.toHexString(cert.hashCode()), certFactory.generateCertificate(certInput));
                    certInput.close();
                }
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                trustManagers = trustManagerFactory.getTrustManagers();
            } else {
                trustManagers = new TrustManager[]{getAcceptAllTrustManager()};
            }
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(null, trustManagers, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            Log.e("config", "", e);
        }
        return this;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    private X509TrustManager getAcceptAllTrustManager() {
        if (trustManager == null) {
            trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
        }
        return trustManager;
    }

    public Config trustHostNames(String... hostNames) {
        if (hostNames.length > 0) {
            trustHostNames.addAll(Arrays.asList(hostNames));
        }
        return this;
    }

    public List<String> getTrustHostNames() {
        return trustHostNames;
    }
}
