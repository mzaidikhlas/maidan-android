package com.example.wasifnadeem.maidan_android

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {

    val BASE_URL: String = "localhost:3000/api"
    private var retrofit: Retrofit? = null

    fun getClient(): Retrofit? {
        if (retrofit == null){
            retrofit = Retrofit.Builder().baseUrl(this.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        }
        return retrofit
    }

}