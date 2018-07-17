package com.example.wasifnadeem.maidan_android.controllers

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wasifnadeem.maidan_android.R
import com.example.wasifnadeem.maidan_android.models.User
import com.example.wasifnadeem.maidan_android.retrofit.ApiInterface
import com.example.wasifnadeem.maidan_android.retrofit.ApiResponse
import com.example.wasifnadeem.maidan_android.retrofit.PayloadFormat
import com.example.wasifnadeem.maidan_android.retrofit.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class HomeFragment : Fragment() {

    private var payload: ArrayList<PayloadFormat>? = null
    private var payloadData: Any? = null

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
                Log.d("Error", t!!.toString())
                throw error(t.message!!)
            }

            override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {

                if (response!!.isSuccessful) {
                    if (response.body()!!.getStatusCode() == 200) {
                        payload = response.body()!!.getPayload()
                        Log.d("ApiResponsePayload", payload!!.toString())

                        if (response.body()!!.getType() == "User" ) {
                            val gson = Gson()

                            payloadData = response.body()!!.getPayload()[0].getData()

                            val jsonObject = gson.toJsonTree(payloadData).asJsonObject
                            payloadData = gson.fromJson(jsonObject, User::class.java)

                            Log.d("ApiResponsePayloadData", (payloadData as User).getName())
                        }
                        else throw error("The requested data is not compatible with this fragment")

                    } else {
                        Log.d("ApiResponse", "Nahe aya")
                        throw error(response.body()!!.getMessage())
                    }
                }
            }
        });
    }
}
//var a: Any? = null
//val u: Any = User("null","Yoo","null",3131212,3213212,"null", UserRecord("null"))
//
//a = u as User
//Log.d("ApiTesting",a.getName())
////                            Log.d("ApiTestingPayload", name)
