package com.khaga.ksociety.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val DEFAULT_BASE_URL = "https://petstore.swagger.io/v2/"
    private const val PREFS_NAME       = "bc_api_prefs"
    private const val KEY_BASE_URL     = "base_url"

    @Volatile private var service: KSocietyApiService? = null
    @Volatile private var currentBaseUrl: String = DEFAULT_BASE_URL

    fun getInstance(context: Context): KSocietyApiService {
        val savedUrl = getSavedUrl(context)
        if (service == null || savedUrl != currentBaseUrl) {
            synchronized(this) {
                if (service == null || savedUrl != currentBaseUrl) {
                    service = buildService(savedUrl)
                    currentBaseUrl = savedUrl
                }
            }
        }
        return service!!
    }

    fun setBaseUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_BASE_URL, url).apply()
        synchronized(this) {
            service = buildService(url)
            currentBaseUrl = url
        }
    }

    fun getCurrentBaseUrl(context: Context): String = getSavedUrl(context)

    private fun getSavedUrl(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

    private fun buildService(baseUrl: String): KSocietyApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("X-App-Name", "KSociety")
                    .header("X-App-Version", "1.0.0")
                    .build()
                chain.proceed(request)
            }
            .build()

        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KSocietyApiService::class.java)
    }
}
