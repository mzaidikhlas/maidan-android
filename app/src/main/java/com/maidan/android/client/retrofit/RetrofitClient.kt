package com.maidan.android.client.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null

    val instance: Retrofit
        get(){
            if (retrofit == null){
                retrofit = Retrofit.Builder()
                        .baseUrl("192.168.43.16:3000/api/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
            }
            return retrofit!!
        }
}