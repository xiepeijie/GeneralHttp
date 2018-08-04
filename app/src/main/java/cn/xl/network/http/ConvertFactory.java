package cn.xl.network.http;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

final class ConvertFactory extends Converter.Factory {

    private static MediaType jsonType = MediaType.parse("application/json");

    private Gson gson;

    ConvertFactory(Gson g) {
        gson = g;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new ToStringConverter();
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations, Retrofit retrofit) {
        return new RequestConverter();
    }

    private final class RequestConverter implements Converter<Object, RequestBody> {
        @Override
        public RequestBody convert(Object o) throws IOException {
            Log.i("xxx", String.format("request convert: %s", o.getClass().getSimpleName()));
            if (o instanceof String) {
                return RequestBody.create(jsonType, (String) o);
            } else if (o instanceof RequestBody) {
                return (RequestBody) o;
            } else {
                return RequestBody.create(jsonType, gson.toJson(o));
            }
        }
    }

    private static final class ToStringConverter implements Converter<ResponseBody, String> {
        @Override
        public String convert(@NonNull ResponseBody value) {
            MediaType type = value.contentType();
            Log.i("xxx", "response convert: " + type);
            try {
                String data = value.string();
                value.close();
                return data;
            } catch (IOException e) {
                Log.e("xxx", "convert", e);
                return "";
            }
        }
    }

}
