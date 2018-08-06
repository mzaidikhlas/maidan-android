package com.maidan.android.client.controllers


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.maidan.android.client.LoginActivity
import com.maidan.android.client.R
import com.maidan.android.client.models.Booking
import com.maidan.android.client.models.User
import com.maidan.android.client.models.Venue
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.PayloadFormat
import com.maidan.android.client.retrofit.RetrofitClient
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DetailedVenueFragment : Fragment() {

    //Layout
    private lateinit var carouselView: CarouselView
    private lateinit var datePicker: DatePickerDialog
    private lateinit var timePicker: Button
    private lateinit var overSpinner: Spinner
    private lateinit var venueNameTxt: TextView
    private lateinit var addressTxt: TextView
    private lateinit var perHourTxt: TextView
    private lateinit var minPlayTimeTxt: TextView
    private lateinit var dateBtn: Button
    private lateinit var timeBtn: Button
    private lateinit var clockImage: ImageView
    private lateinit var bookBtn: Button

    //Model objects
    private lateinit var venue: Venue
    private lateinit var booking: Booking
    private var dateString: String? = null
    private var timeString: String? = null

    private val TAG = "VenueDetailedFragment"

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    //Api Call Response
    private var payload: ArrayList<PayloadFormat>? = null
    private lateinit var user: User

    private var sampleImages = intArrayOf(R.drawable.sample, R.drawable.sample, R.drawable.sample, R.drawable.sample, R.drawable.sample)

    private var imageListener: ImageListener = ImageListener { position, imageView -> imageView.setImageResource(sampleImages[position]) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_detailed_venue, container, false)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        //If not logged in
        if (currentUser == null){
            val loginIntent  = Intent(activity, LoginActivity::class.java)
            activity!!.startActivity(loginIntent)
        }else {
            currentUser.getIdToken(true)
                    .addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            val idToken = task2.result.token
                            Log.d("User", idToken)

                            val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)

                            val call: Call<ApiResponse> = apiService.getUserInfoByEmail(idToken!!)

                            call.enqueue(object : Callback<ApiResponse> {
                                override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                                    Log.d("UserApiError", t.toString())
                                }

                                override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                                    if (response!!.isSuccessful) {
                                        Log.d("UserApiSuccess", response.body().toString())
                                        if (response.body()!!.getStatusCode() == 200){
                                            if (response.body()!!.getType() == "User") {
                                                payload = response.body()!!.getPayload()

                                                val gson = Gson()
                                                val jsonObject = gson.toJsonTree(payload!![0].getData()).asJsonObject
                                                Log.d(TAG, "Json$jsonObject")
                                                user = gson.fromJson(jsonObject, User::class.java)
                                            }else{
                                                Log.d(TAG, "Expected Data is user and we get ${response.body()!!.getType()}")
                                            }
                                        }else{
                                            Log.d(TAG, response.body()!!.getMessage())
                                        }
                                    }
                                }
                            })
                        } else {
                            // Handle error -> task.getException();
                            Log.d("UserTokenError1", "Error")
                        }
                    }
        }
        // Getting Data from bundle
        if (!arguments!!.isEmpty){
            venue = arguments!!.getSerializable("venue") as Venue
            Log.d(TAG, venue.toString())
        }
        else
            Log.d(TAG, "Empty")

        //layout Init
        carouselView = view.findViewById(R.id.carouselView) as CarouselView
        dateBtn = view.findViewById(R.id.selectDate)
        timePicker = view.findViewById(R.id.selectTime)
        overSpinner = view.findViewById(R.id.over_spinner)
        venueNameTxt = view.findViewById(R.id.venueName)
        addressTxt = view.findViewById(R.id.address)
        perHourTxt = view.findViewById(R.id.perhour)
        minPlayTimeTxt = view.findViewById(R.id.min_playtime)
        dateBtn = view.findViewById(R.id.selectDate)
        timeBtn = view.findViewById(R.id.selectTime)
        clockImage = view.findViewById(R.id.clock)
        bookBtn = view.findViewById(R.id.book_btn)

        //Populating layout
        val hr = venue.getMinBookingHour().toString()

        venueNameTxt.text = venue.getName()
        addressTxt.text = venue.getLocation().getArea()
        perHourTxt.text = venue.getRate().getPerHrRate().toString()
        minPlayTimeTxt.text = hr

        // Image Slider
        carouselView.pageCount = sampleImages.size
        carouselView.setImageListener(imageListener)

        //book
        bookBtn.setOnClickListener {

            val bookingDuration = overSpinner.selectedItem.toString()
            Log.d(TAG, "Duration $bookingDuration")
            //Populate Booking object and pass it to receipt fragment
            booking = Booking(venue, null, user, bookingDuration, timeString!!, dateString!!)

            Log.d(TAG, "Booking ${booking.toString()}")

            Log.d(TAG, "BookBtn")
            val receiptFragment = ReceiptFragment()
            val args = Bundle()
            args.putSerializable("booking", booking)

            receiptFragment.arguments = args

            fragmentManager!!.beginTransaction().addToBackStack("detailed venue fragment").replace(R.id.fragment_layout, receiptFragment).commit()
        }

        // Date Selector
        dateBtn.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            dateString = "$day/$month/$year"

            datePicker = DatePickerDialog(context,R.style.DatePickerTheme,
                    DatePickerDialog.OnDateSetListener { view, yr, monthOfYear, dayOfMonth ->
                        Log.d(TAG, "Year: $yr, Month $monthOfYear, Day: $dayOfMonth")
                        dateString = "$dayOfMonth/$monthOfYear/$yr"
                    },year,month,day)
            datePicker.datePicker.minDate = c.timeInMillis
            datePicker.show()
            Log.d(TAG,dateString)
        };

        // Time Selector
        timePicker.setOnClickListener {
            val c = Calendar.getInstance()
            val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            timeString = "$hourOfDay:$minute"
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,R.style.DatePickerTheme,
                    TimePickerDialog.OnTimeSetListener { view, hr, min ->
                        Log.d(TAG, "Hour: $hr, Min: $min")
                        timeString = "$hr:$min"
                    }, hourOfDay, minute, false)

            timePickerDialog.show()
            Log.d(TAG, timeString)
        };

        // Dropdown list for overs
        val adapter = ArrayAdapter.createFromResource(context,
                R.array.match_overs, android.R.layout.simple_spinner_item)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        overSpinner.adapter = adapter;

        return view
    }
}
