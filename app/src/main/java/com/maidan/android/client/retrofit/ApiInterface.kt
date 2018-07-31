package com.maidan.android.client.retrofit

import com.maidan.android.client.models.Booking
import retrofit2.Call
import retrofit2.CallAdapter
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
    @POST("booking")
        fun createBooking(@Body booking: Booking, @Header("Authorization") idToken: String): Call<ApiResponse>


    //Venue Routes
    @GET("venue/selectedVenues/{category}")
        fun getVenues(@Path("category") category: String, @Header("Authorization") idToken: String): Call<ApiResponse>

    //Transaction Routes


}