package com.example.lbankscanner.data.api

import com.example.lbankscanner.data.model.Candle
import com.example.lbankscanner.data.model.CandleDeserializer
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface LBankApiService {
    @GET("v2/kline.do")
    suspend fun getKLines(
        @Query("symbol") symbol: String,
        @Query("size") size: Int = 60,
        @Query("type") type: String
    ): Map<String, Any>
}

object RetrofitClient {
    private const val BASE_URL = "https://api.lbank.info/"

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val gson by lazy {
        val candleListType = object : TypeToken<List<Candle>>() {}.type
        GsonBuilder()
            .registerTypeAdapter(candleListType, CandleDeserializer())
            .create()
    }

    val apiService: LBankApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(LBankApiService::class.java)
    }
}
