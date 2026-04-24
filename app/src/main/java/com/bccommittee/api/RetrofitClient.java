package com.bccommittee.api;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // Default: Swagger Petstore demo (replace with your server)
    public static final String DEFAULT_BASE_URL = "https://petstore.swagger.io/v2/";
    public static final String PREFS_NAME       = "bc_api_prefs";
    public static final String PREF_BASE_URL    = "base_url";

    private static RetrofitClient instance;
    private Retrofit retrofit;
    private BCCommitteeApiService apiService;
    private String currentBaseUrl;

    private RetrofitClient(String baseUrl) {
        this.currentBaseUrl = baseUrl;
        buildRetrofit(baseUrl);
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUrl = prefs.getString(PREF_BASE_URL, DEFAULT_BASE_URL);

        if (instance == null || !savedUrl.equals(instance.currentBaseUrl)) {
            instance = new RetrofitClient(savedUrl);
        }
        return instance;
    }

    public static synchronized void setBaseUrl(Context context, String newUrl) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_BASE_URL, newUrl).apply();
        instance = new RetrofitClient(newUrl);
    }

    private void buildRetrofit(String baseUrl) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header("X-App-Name", "BCCommittee")
                            .header("X-App-Version", "1.0.0")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(BCCommitteeApiService.class);
    }

    public BCCommitteeApiService getApiService() {
        return apiService;
    }

    public String getCurrentBaseUrl() {
        return currentBaseUrl;
    }
}
