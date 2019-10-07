package ru.ivmak.raspisanie_iktib

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class NetworkService {
    private val BASE_URL = "http://165.22.28.187/"
    private var mRetrofit: Retrofit

    init {
        mRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    companion object {
        private var mInstance: NetworkService? = null
        fun getInstance(): NetworkService {
            if (mInstance == null) {
                mInstance = NetworkService()
            }
            return mInstance as NetworkService
        }
    }
    fun getJSONApi() = mRetrofit.create(JSONPlaceHolderApi::class.java)
}