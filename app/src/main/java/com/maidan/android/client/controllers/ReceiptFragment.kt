package com.maidan.android.client.controllers

import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson

import com.maidan.android.client.R
import com.maidan.android.client.models.Booking
import com.maidan.android.client.models.Transaction
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.RetrofitClient
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*

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
    private lateinit var receiptBookingTimeTxt: TextView
    private lateinit var receiptBookingHoursTxt: TextView
    private lateinit var totalPerHourPriceTxt: TextView
    private lateinit var totalConveniencePriceTxt: TextView
    private lateinit var totalPriceTxt: TextView
    private lateinit var payBtn: Button
    private lateinit var progressBar: ProgressBar

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_receipt, container, false)

        if (!arguments!!.isEmpty){
            booking = arguments!!.getSerializable("booking") as Booking
        }
        else
            Log.d(TAG, "Empty")

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        //Layout init
        maidanIcon = view.findViewById(R.id.receiptUserImage)
        invoiceIdTxt = view.findViewById(R.id.invoiceId)
        customerNameTxt = view.findViewById(R.id.receiptCustomerName)
        receiptDateTxt = view.findViewById(R.id.receiptDate)
        receiptTimeTxt = view.findViewById(R.id.receiptTime)
        venueNameTxt = view.findViewById(R.id.receiptVenueName)
        receiptBookingDateTxt = view.findViewById(R.id.receiptBookingDate)
        receiptBookingTimeTxt = view.findViewById(R.id.receiptBookingTime)
        receiptBookingHoursTxt = view.findViewById(R.id.receiptBookingHours)
        totalPerHourPriceTxt = view.findViewById(R.id.totalPerHourPrice)
        totalConveniencePriceTxt = view.findViewById(R.id.totalConvenienceFee)
        totalPriceTxt = view.findViewById(R.id.total)
        payBtn = view.findViewById(R.id.pay_btn)
        progressBar = view.findViewById(R.id.receiptProgressBar)

        payBtn.letterSpacing = 0.3F

        //Populating Layout
        val rc = Calendar.getInstance()
        Picasso.get()
                .load(R.drawable.maidan_playstore_icon)
                .into(maidanIcon)
        receiptDateTxt.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(rc.time)
        receiptTimeTxt.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(rc.time)
        invoiceIdTxt.text = "#001"
        customerNameTxt.text = booking.getUser().getName()
        venueNameTxt.text = booking.getVenue().getName()
        receiptBookingHoursTxt.text = booking.getDurationOfBooking()

        val date = DateFormat.getDateInstance(DateFormat.FULL).parse(booking.getBookingDate())
        receiptBookingDateTxt.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)

        val hours = booking.getDurationOfBooking().split(" ")

        rc.set(Calendar.HOUR_OF_DAY, hours[0].toInt())
        rc.set(Calendar.MINUTE, 0)
        rc.set(Calendar.SECOND, 0)

        val newTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(rc.time)
        receiptBookingTimeTxt.text = "${booking.getStartTime()} - $newTime"

        //Calculating rate
        val perhr: Int = booking.getVenue().getRate().getPerHrRate()
        val serviceFeePercent = booking.getVenue().getRate().getVendorServiceFee()

        val temp = booking.getDurationOfBooking().split(" ")
        val playhrs: Int = temp[0].toInt()

        val actualPrice = playhrs * perhr
        var serviceFee = serviceFeePercent/ 100.toFloat()

        serviceFee *= actualPrice

        val totalSum = actualPrice + serviceFee
        Log.d(TAG, serviceFee.toString())
        totalPerHourPriceTxt.text = actualPrice.toString()
        totalConveniencePriceTxt.text = serviceFee.toString()
        totalPriceTxt.text = totalSum.toString()

        payBtn.setOnClickListener{
            if (mAuth.currentUser != null){
                progressBar.visibility = View.VISIBLE
                payBtn.isEnabled = false

                booking.setTransaction(Transaction(serviceFee, playhrs.toFloat(), actualPrice.toFloat(), totalSum, 0.toFloat()
                        , "maualCashReciving", "maidan-customer"))

                val st = booking.getStartTime().split(":")

                val stSecs = ((st[0].toInt()*3600) + (st[1].toInt()*60))

                var toSecs = (stSecs + (playhrs*3600))
                Log.d(TAG, "To seconds $toSecs")

                val c = Calendar.getInstance()
                c.time = DateFormat.getDateInstance(DateFormat.FULL).parse(booking.getBookingDate())

                if (toSecs > 86400){
                    toSecs -= 86400
                    Log.d(TAG, "in here")

                    toSecs /= 3600
                    c.set(Calendar.HOUR_OF_DAY, toSecs)
                    c.set(Calendar.MINUTE, 0)
                    c.set(Calendar.SECOND, 0)

                    c.add(Calendar.DATE, 1)

                    val newDate = DateFormat.getDateInstance(DateFormat.FULL).format(c.time)
                    Log.d(TAG, "New date $newDate")
                    booking.setToBookingDate(newDate)
                }else{
                    toSecs /= 3600
                    c.set(Calendar.HOUR_OF_DAY, toSecs)
                    c.set(Calendar.MINUTE, 0)
                    c.set(Calendar.SECOND, 0)
                    booking.setToBookingDate(booking.getBookingDate())
                }
                val toTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
                Log.d(TAG, "New to time $toTime")
                booking.setDurationOfBooking(toTime)

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
                                progressBar.visibility = View.INVISIBLE
                                payBtn.isEnabled = true
                                Log.d(TAG, "Receipt Error $t")
                                throw t!!
                            }

                            override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                                if (response!!.isSuccessful){
                                    if (response.body()!!.getStatusCode() == 201){
                                        Log.d(TAG, "Booking Created")
                                        Toast.makeText(context, "New booking created", Toast.LENGTH_LONG).show()
                                        progressBar.visibility = View.INVISIBLE
                                        fragmentManager!!.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                                    }else{
                                        progressBar.visibility = View.INVISIBLE
                                        payBtn.isEnabled = true
                                        Log.d(TAG, "Bad Request")
                                    }
                                }else{
                                    progressBar.visibility = View.INVISIBLE
                                    payBtn.isEnabled = true
                                    Log.d(TAG, "Exception ${response.errorBody()}")
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
