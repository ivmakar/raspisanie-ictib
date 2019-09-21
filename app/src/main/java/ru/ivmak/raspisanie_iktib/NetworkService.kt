package ru.ivmak.raspisanie_iktib

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class NetworkService {
    private var mInstance: NetworkService? = null
    private val BASE_URL = "http://165.22.28.187/schedule-api/"
    private var mRetrofit: Retrofit

    init {
        mRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getInstance(): NetworkService {
        if (mInstance == null) {
            mInstance = NetworkService()
        }
        return mInstance as NetworkService
    }

    fun getJSONApi() = mRetrofit.create(JSONPlaceHolderApi::class.java)
}