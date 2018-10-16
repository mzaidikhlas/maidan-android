package com.maidan.android.client.controllers

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.maidan.android.client.MainActivity

import com.maidan.android.client.R
import com.maidan.android.client.adapter.BookingScheduleAdapter
import com.maidan.android.client.models.Booking
import com.maidan.android.client.models.User
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.PayloadFormat
import com.maidan.android.client.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyBookings : Fragment() {
    private val TAG = "MyBookings"

    //Layouts
    private lateinit var bookingsRecyclerView: RecyclerView

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    //Local objects
    private var bookings: ArrayList<Booking>? = null
    private lateinit var loggedInUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        loggedInUser = (activity as MainActivity).getLoggedInUser()!!
        getUserBookings()
    }

    private fun getUserBookings() {
        showProgressDialog()
        currentUser.getIdToken(true).addOnCompleteListener { task ->
            if (task.isSuccessful){
                val idToken = task.result!!.token
                val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                val call: Call<ApiResponse> = apiService.getUserBookings(loggedInUser.getId()!!, idToken!!)
                call.enqueue(object: Callback<ApiResponse>{
                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.d(TAG, "OnCallFailure: $t")
                        hideProgressDialog()
                    }

                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful){
                            if (response.body()!!.getStatusCode() == 200){
                                val gson = Gson()
                                val payload = response.body()!!.getPayload()
                                if (payload.isNotEmpty()){
                                    var booking: Booking
                                    bookings = ArrayList()
                                    for (item: PayloadFormat in payload){
                                        val jsonObject = gson.toJsonTree(item.getData()).asJsonObject
                                        booking = gson.fromJson(jsonObject, Booking::class.java)
                                        booking.setRef(item.getDocId())

                                        bookings!!.add(booking)
                                    }
                                    bookingsRecyclerView.adapter = BookingScheduleAdapter(bookings!!)
                                    Log.d(TAG, "Payload $bookings")
                                }else{
                                    Log.d(TAG, "OnPayloadEmpty")
                                }
                            }else{
                                Log.d(TAG,"OnResponseCodeFailure: ${response.body()!!.getStatusCode()}")
                            }
                        }else{
                            Log.d(TAG, "OnResponseFailure: ${response.errorBody()}")
                        }
                        hideProgressDialog()
                    }
                })

            }else{
                hideProgressDialog()
                Log.d(TAG, "OnTaskFailure: ${task.exception}")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_bookings, container, false)
        bookingsRecyclerView = view.findViewById(R.id.myBookings)

        bookingsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        return view
    }

    private fun showProgressDialog(){
        (activity as MainActivity).showProgressDialog()
    }

    private fun hideProgressDialog(){
        (activity as MainActivity).hideProgressDialog()
    }
}
