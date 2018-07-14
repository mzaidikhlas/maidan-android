package com.example.wasifnadeem.maidan_android.controllers

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wasifnadeem.maidan_android.R




class HomeFragment : Fragment() {

//    private lateinit var user:User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)
//            user = User("email", "name", "password", 923214658283, 123456789101234,
//                    "displayAvatar.jpg", UserRecord("something"))

        return view
    }
//    fun apiCall (){
//        val apiService = ApiClient.getClient()!!.create(ApiInterface::class.java)
//    }
}
