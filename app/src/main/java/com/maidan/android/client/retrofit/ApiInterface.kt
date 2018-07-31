package com.maidan.android.client.retrofit

import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    //User Routes
    @GET("user/getByEmail")
        fun getUserInfoByEmail(@Header("Authorization") idToken: String): Call<ApiResponse>

    @GET("user")
        fun getAllUsers(): Call<ApiResponse>

    @GET("user/{id}")
        fun getUserById(@Path("id") id: Int): Call<ApiResponse>

    @POST("user")
        fun createUser(@Body id: Int): Call<ApiResponse>

    @DELETE("user/{id}")
        fun deleteUser(@Path("id") id: Int): Call<ApiResponse>

    @POST("user/testing")
        fun testing(@Header("Authorization") idToken: String): Call<ApiResponse>

    //Booking Routes

    //Venue Routes
    @GET("venue/selectedVenues/{category}")
        fun getVenues(@Path("category") category: String, @Header("Authorization") idToken: String): Call<ApiResponse>

    //Transaction Routes


}