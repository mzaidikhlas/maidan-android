package com.maidan.android.client.controllers


import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.maidan.android.client.MainActivity
import com.maidan.android.client.R
import com.maidan.android.client.models.Booking
import com.maidan.android.client.models.User
import com.maidan.android.client.models.Venue
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.PayloadFormat
import com.maidan.android.client.retrofit.RetrofitClient
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class DetailedVenueFragment : Fragment() {

    //Layout
    private lateinit var carouselView: CarouselView
    private lateinit var datePicker: DatePickerDialog
    private lateinit var timePicker: TextView
    private lateinit var overSpinner: Spinner
    private lateinit var venueNameTxt: TextView
    private lateinit var addressTxt: TextView
    private lateinit var price: TextView
    private lateinit var minPlayTimeTxt: TextView
    private lateinit var dateBtn: TextView
    private lateinit var timeBtn: TextView
    private lateinit var clockImage: ImageView
    private lateinit var bookBtn: Button

    private var availableSlotsFrom: ArrayList<Int>? = null
    private var availableSlotsTo: ArrayList<Int>? = null

    private var fromTimeSeconds: Int? = null
    private var toTimeSeconds: Int? = null

    //Model objects
    private lateinit var venue: Venue
    private var booking: Booking? = null
    private var dateString: String? = null
    private var timeString: String? = null
    private var bookings: ArrayList<Booking>? = null

    private val TAG = "VenueDetailedFragment"

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    //Api Call Response
    private var payload: ArrayList<PayloadFormat>? = null
    private lateinit var loggedInUser: User

    private var imageListener: ImageListener = ImageListener { position, imageView ->
        if (venue.getPictures() != null)
            Picasso.get().load(venue.getPictures()!![position]).into(imageView)

        else
            imageView.setImageResource(R.drawable.google_logo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loggedInUser = (activity as MainActivity).getLoggedInUser()!!

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        // Getting Data from bundle
        if (arguments != null){
            venue = arguments!!.getSerializable("venue") as Venue
            Log.d(TAG, venue.toString())
            getAllBookingsOfThisVenue()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_detailed_venue, container, false)

        //layout Init
        carouselView = view.findViewById(R.id.carouselView) as CarouselView
        dateBtn = view.findViewById(R.id.selectDate)
        timePicker = view.findViewById(R.id.selectTime)
        overSpinner = view.findViewById(R.id.over_spinner)
        venueNameTxt = view.findViewById(R.id.venueName)
        addressTxt = view.findViewById(R.id.address)
        price = view.findViewById(R.id.price)
        minPlayTimeTxt = view.findViewById(R.id.min_playtime)
        dateBtn = view.findViewById(R.id.selectDate)
        timeBtn = view.findViewById(R.id.selectTime)
        clockImage = view.findViewById(R.id.clock)
        bookBtn = view.findViewById(R.id.book_btn)

        bookBtn.letterSpacing = 0.3F

        //Populating layout
        val hr = venue.getMinBookingHour().toString()

        venueNameTxt.text = venue.getName()
        addressTxt.text = venue.getLocation().getArea()
        price.text = venue.getRate().getPerHrRate().toString()
    //    minPlayTimeTxt.text = hr

        // Image Slider
        if (venue.getPictures() != null)
            carouselView.pageCount = venue.getPictures()!!.size
        else
            carouselView.pageCount = 0
        carouselView.setImageListener(imageListener)

        //book
        bookBtn.setOnClickListener {

            if (timeString.isNullOrEmpty() || dateString.isNullOrEmpty()){
                Log.d(TAG, "aa")
                Toast.makeText(context, "Please set time and date for booking", Toast.LENGTH_LONG).show()
            }else{
                val bookingDuration = overSpinner.selectedItem.toString()
                val parsing = bookingDuration.split(" ")
                toTimeSeconds = parsing[0].toInt()*3600
                if (availabiltyCheck(fromTimeSeconds!!, toTimeSeconds!!)) {
                    timePicker.error = null
                    timePicker.clearFocus()

                    Log.d(TAG, "Duration $bookingDuration")
                    //Populate Booking object and pass it to receipt fragment
                    booking = Booking(null, venue, null, loggedInUser, bookingDuration, timeString!!, dateString!!,null, "pending")

                    Log.d(TAG, "Booking $booking")

                    Log.d(TAG, "BookBtn")
                    val receiptFragment = ReceiptFragment()
                    val args = Bundle()
                    args.putSerializable("booking", booking)

                    receiptFragment.arguments = args

                    fragmentManager!!.beginTransaction().addToBackStack("detailed venue fragment").replace(R.id.fragment_layout, receiptFragment).commit()
                }else{
                    Toast.makeText(context, "Your having clash with other booking change start time or change booking hours to fix it",Toast.LENGTH_LONG).show()
                    timePicker.error = "This slot is not available"
                    timePicker.requestFocus()
                }
            }
        }

        // Date Selector
        dateBtn.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            dateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.time)
            //dateString = "$day/$month/$year"

            datePicker = DatePickerDialog(context,R.style.DatePickerTheme,
                    DatePickerDialog.OnDateSetListener { view, yr, monthOfYear, dayOfMonth ->
                        Log.d(TAG, "Year: $yr, Month $monthOfYear, Day: $dayOfMonth")
                        c.set(yr, monthOfYear, dayOfMonth)
                        dateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.time)

                        checkingAvailabilitySlots(dateString!!)
                        //dateString = "$dayOfMonth/$monthOfYear/$yr"
                        dateBtn.text = dateString
                    },year,month,day)
            datePicker.datePicker.minDate = c.timeInMillis
            datePicker.show()
            Log.d(TAG,dateString)
        }

        // Time Selector
        timePicker.setOnClickListener {
            val c = Calendar.getInstance()
            val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,R.style.DatePickerTheme,
                    TimePickerDialog.OnTimeSetListener { view, hr, min ->
                        val cc = Calendar.getInstance()
                        cc.set(Calendar.HOUR_OF_DAY, hr)
                        cc.set(Calendar.MINUTE, min)

                        if (c.timeInMillis > cc.timeInMillis){
                            timePicker.error = "Enter a valid time"
                            timePicker.requestFocus()
                        }else{
                            timePicker.error = null
                            timePicker.clearFocus()

                            fromTimeSeconds = ((hr*3600)+(min*60))

                            if (!availabiltyFromCheck(fromTimeSeconds!!)){
                                timePicker.error = "This slot is not available"
                                timePicker.requestFocus()
                            }else{
                                timePicker.error = null
                                timePicker.clearFocus()
                            }
                            timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(cc.time)
                            timePicker.text = timeString
                        }
                    }, hourOfDay, minute, false)

            timePickerDialog.show()
            Log.d(TAG, timeString)
        }

        // Dropdown list for overs
        val adapter = ArrayAdapter.createFromResource(context,
                R.array.match_overs, android.R.layout.simple_spinner_item)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        overSpinner.adapter = adapter

        return view
    }

    private fun getAllBookingsOfThisVenue(){
        currentUser.getIdToken(true).addOnCompleteListener { task ->
            if (task.isSuccessful){
                val idToken = task.result.token
                Log.d(TAG, "id ${venue.getRef()}")
                Log.d(TAG, "token $idToken")
                val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                val call: Call<ApiResponse> = apiService.getVenueBookings(venue.getRef(),idToken!!)
                call.enqueue(object: Callback<ApiResponse>{
                    override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                        Log.d(TAG, t.toString())
                    }

                    override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                        if (response!!.isSuccessful){
                            if (response.body()!!.getStatusCode() == 200){
                                Log.d(TAG, "Response")

                                if (response.body()!!.getType() == "Booking") {
                                    val gson = Gson()
                                    payload = response.body()!!.getPayload()
                                    if (payload != null) {

                                        Log.d(TAG, "Payload $payload")
                                        var booking: Booking

                                        for (item: PayloadFormat in payload!!) {
                                            val jsonObject = gson.toJsonTree(item.getData()).asJsonObject
                                            Log.d(TAG, "Json$jsonObject")
                                            booking = gson.fromJson(jsonObject, Booking::class.java)

                                            if (bookings == null)
                                                bookings = ArrayList()

                                            bookings!!.add(booking)
                                        }
                                    }
                                }
                            }else {
                                Log.d(TAG, "Error Response")
                                Log.d(TAG, response.body()!!.getMessage())
                            }
                        }
                    }
                })
            } else {
                // Handle error -> task.getException();
                Log.d(TAG, "Task Exception ${task.exception}")
                throw task.exception!!
            }
        }
    }

    private fun checkingAvailabilitySlots(date: String){
        availableSlotsFrom = ArrayList()
        availableSlotsTo = ArrayList()

        var timeFrom:ArrayList<String>
        var timeTo:ArrayList<String>
        var temp = 0
        if (bookings != null){
            for (booking: Booking in bookings!!){
                if (booking.getBookingDate() == date){
                    //Parsing dates
                    timeFrom = booking.getStartTime().split(":") as ArrayList<String>
                    timeTo = booking.getDurationOfBooking().split(":") as ArrayList<String>

                    //Converting dates in seconds to do some calculations
                    val timeFromInSeconds = ((timeFrom[0].toInt() * 3600) + (timeFrom[1].toInt() * 60))
                    val timeToInSeconds = ((timeTo[0].toInt() * 3600) + (timeTo[1].toInt() * 60))

                    //Populating available slots list
                    availableSlotsFrom!!.add(temp)
                    availableSlotsTo!!.add(timeFromInSeconds)
                    temp = timeToInSeconds
                }
            }
            if (availableSlotsFrom != null && availableSlotsTo != null){
                availableSlotsFrom!!.add(temp)
                availableSlotsTo!!.add(86400)

                availableSlotsFrom!!.sort()
                availableSlotsTo!!.sort()
            }else {
                availableSlotsFrom!!.add(0)
                availableSlotsTo!!.add(86400)
            }
        }else{
            availableSlotsFrom!!.add(0)
            availableSlotsTo!!.add(86400)
        }
        Log.d(TAG, "Availability from $availableSlotsFrom")
        Log.d(TAG, "Availability to $availableSlotsTo")
    }

    private fun availabiltyFromCheck(from: Int): Boolean{
        var i = 0
        var flag = false

        while (i < availableSlotsFrom!!.size){
            if (from >= availableSlotsFrom!![i]) {
                if (from < availableSlotsTo!![i])
                    flag = true
            }
            i++
        }
        return flag
    }
    private fun availabiltyToCheck(to: Int): Boolean{
        var i = 0
        var flag = false

        while (i < availableSlotsTo!!.size){
            if (to < availableSlotsTo!![i]) {
                if (to >= availableSlotsFrom!![i])
                    flag = true
            }
            i++
        }
        return flag
    }

    private fun availabiltyCheck(from:Int, to:Int): Boolean{
        val count = if (availableSlotsFrom!!.size > availableSlotsTo!!.size) availableSlotsFrom!!.size else availableSlotsTo!!.size
        var i = 0
        var flag = false

        while (i < count){
            if (from >= availableSlotsFrom!![i]) {
                if (to <= availableSlotsTo!![i])
                    flag = true
            }
            i++
        }
        return flag
    }
}
