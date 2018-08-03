package com.maidan.android.client.controllers

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson

import com.maidan.android.client.R
import com.maidan.android.client.models.Booking
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.RetrofitClient
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReceiptFragment : Fragment() {
    private lateinit var booking: Booking

    private val TAG = "VenueReceiptFragment"

    //Layout
    private lateinit var maidanIcon: ImageView
    private lateinit var invoiceIdTxt: TextView
    private lateinit var customerNameTxt: TextView
    private lateinit var receiptDateTxt: TextView
    private lateinit var receiptTimeTxt: TextView
    private lateinit var venueNameTxt: TextView
    private lateinit var receiptBookingDateTxt: TextView
    private lateinit var receiptBookingHoursTxt: TextView
    private lateinit var totalPerHourPriceTxt: TextView
    private lateinit var totalConveniencePriceTxt: TextView
    private lateinit var totalPriceTxt: TextView
    private lateinit var payBtn: Button

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_receipt, container, false)

        if (!arguments!!.isEmpty){
            booking = arguments!!.getSerializable("booking") as Booking
            Log.d(TAG, booking.toString())
        }
        else
            Log.d(TAG, "Empty")

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        //Layout init
        maidanIcon = view.findViewById(R.id.receiptMaidanIcon)
        invoiceIdTxt = view.findViewById(R.id.invoiceId)
        customerNameTxt = view.findViewById(R.id.receiptCustomerName)
        receiptDateTxt = view.findViewById(R.id.receiptDate)
        receiptTimeTxt = view.findViewById(R.id.receiptTime)
        venueNameTxt = view.findViewById(R.id.receiptVenueName)
        receiptBookingDateTxt = view.findViewById(R.id.receiptBookingDate)
        receiptBookingHoursTxt = view.findViewById(R.id.receiptBookingHours)
        totalPerHourPriceTxt = view.findViewById(R.id.totalPerHourPrice)
        totalConveniencePriceTxt = view.findViewById(R.id.totalConvenienceFee)
        totalPriceTxt = view.findViewById(R.id.total)
        payBtn = view.findViewById(R.id.pay_btn)

        //Layout populating
        Picasso.get().load(currentUser.photoUrl).into(maidanIcon)
        invoiceIdTxt.text = "#001"
        customerNameTxt.text = booking.getUser().getName()
        venueNameTxt.text = booking.getVenue().getName()
        receiptBookingDateTxt.text = booking.getBookingDate()
        receiptBookingHoursTxt.text = booking.getDurationOfBooking()

        //Calculating rate
        val perhr: Int = booking.getVenue().getRate().getPerHrRate()
        val serviceFeePercent = booking.getVenue().getRate().getVendorServiceFee()
        var playhrs: Int? = null

        when(booking.getDurationOfBooking()){
            "5 overs" -> {
                playhrs = 1
            }
            "10 overs" -> {
                playhrs = 2
            }
            "20 overs" -> {
                playhrs = 4
            }
            "30 overs" -> {
                playhrs = 6
            }
        }

        val actualPrice = playhrs!! * perhr
        var serviceFee = serviceFeePercent/ 100.toFloat()

        serviceFee *= actualPrice

        val totalSum = actualPrice + serviceFee
        Log.d(TAG, serviceFee.toString())
        totalPerHourPriceTxt.text = actualPrice.toString()
        totalConveniencePriceTxt.text = serviceFee.toString()
        totalPriceTxt.text = totalSum.toString()

        payBtn.setOnClickListener{
            if (mAuth.currentUser != null){
                mAuth.currentUser!!.getIdToken(true).addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        val idToken = task.result.token

                        Log.d(TAG, "Token Receipt $idToken")

                        val gson = Gson()
                        val bookingJson = gson.toJson(booking)
                        Log.d(TAG, "Booking $booking")
                        Log.d(TAG, "Booking json $bookingJson")

                        val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                        val call: Call<ApiResponse> = apiService.createBooking(idToken!!, booking)

                        call.enqueue(object: Callback<ApiResponse>{
                            override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                                Log.d(TAG, "Receipt Error $t")
                            }

                            override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                                if (response!!.isSuccessful){
                                    if (response.body()!!.getStatusCode() == 200){
                                        Log.d(TAG, "Booking Created")
                                        Toast.makeText(context, "New booking created", Toast.LENGTH_LONG).show()
                                    }else{
                                        Log.d(TAG, "Bad Request")
                                    }
                                }
                            }

                        })
                    }
                }
            }
        }

        return view
    }

}
