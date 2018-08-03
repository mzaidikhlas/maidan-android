package com.maidan.android.client.retrofit

import com.maidan.android.client.models.Booking
import com.maidan.android.client.models.User
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
        fun createUser(@Body user: User): Call<ApiResponse>

    @DELETE("user/{id}")
        fun deleteUser(@Path("id") id: Int): Call<ApiResponse>

    @POST("user/testing")
        fun testing(@Header("Authorization") idToken: String): Call<ApiResponse>

    //Booking Routes
    @POST("booking")
        fun createBooking(@Header("Authorization") idToken: String,
                          @Body booking: Booking): Call<ApiResponse>


    //Venue Routes
    @GET("venue/selectedVenues")
        fun getVenues(
            @Query("category") category: String,
            @Query("country") countr: String,
            @Query("city") city: String,
            @Header("Authorization") idToken: String): Call<ApiResponse>

    //Transaction Routes


}