package com.maidan.android.client.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null

    val instance: Retrofit
        get(){
            if (retrofit == null){
                retrofit = Retrofit.Builder()
                        .baseUrl("http://172.12.0.60:3000/api/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
            }
            return retrofit!!
        }
}
