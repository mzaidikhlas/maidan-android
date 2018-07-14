package com.example.wasifnadeem.maidan_android.controllers

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.wasifnadeem.maidan_android.R
import com.example.wasifnadeem.maidan_android.models.User
import com.example.wasifnadeem.maidan_android.retrofit.ApiInterface
import com.example.wasifnadeem.maidan_android.retrofit.ApiResponse
import com.example.wasifnadeem.maidan_android.retrofit.RetrofitClient
import com.google.android.gms.common.api.Api
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList


class HomeFragment : Fragment() {

    private lateinit var users: ArrayList<User>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)
            apiCall()
        return view
    }
    private fun apiCall (){

        val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)

        val call: Call<ApiResponse> = apiService.getAllUsers()

        call.enqueue(object : Callback<ApiResponse>{
            override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                Log.d("Error", t!!.message)
            }

            override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                Log.d("Response check", "Aya hai")
                if (response!!.isSuccessful) {
                    users = ArrayList()
                    users = response.body()!!.getPayload()
                }
            }
        });

    }
}
