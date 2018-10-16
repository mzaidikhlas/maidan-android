package com.maidan.android.client.controllers


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Gravity
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
    private lateinit var getDirectionsBtn: Button
    private lateinit var amenitiesView: LinearLayout

    //Model objects
    private lateinit var venue: Venue
    private var booking: Booking? = null
    private var bookings: ArrayList<Booking>? = null
    private var today: Date? = null

    private val TAG = "DetailedVenueFragment"

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

        val c = Calendar.getInstance()
        today = c.time
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
        amenitiesView = view.findViewById(R.id.listOfAmenities)
        getDirectionsBtn = view.findViewById(R.id.getDirections)

        bookBtn.letterSpacing = 0.3F

        //Populating layout
        //val hr = venue.getMinBookingHour().toString()

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

            if (dateBtn.text.isEmpty() || timePicker.text.isEmpty()){
                Log.d(TAG, "aa")
                Toast.makeText(context, "Please set time and date for booking", Toast.LENGTH_LONG).show()
            }else{
                Log.d(TAG, "Date ${dateBtn.text} - Time ${timePicker.text}")
                val playHrs = overSpinner.selectedItem.toString().split(" ")

                //Time date calculations
                val temp = Calendar.getInstance()
                //From Time
                temp.time = DateFormat.getDateInstance(DateFormat.MEDIUM).parse(dateBtn.text.toString())
                val time = timePicker.text.toString().split(":")
                temp.set(Calendar.HOUR, time[0].toInt())
                temp.set(Calendar.MINUTE, time[1].toInt())
                val from = temp.time
                val fromDate = DateFormat.getDateInstance(DateFormat.FULL).format(temp.time)
                val fromTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(temp.time)

                //To time
                temp.set(Calendar.HOUR, (temp.get(Calendar.HOUR) + playHrs[0].toInt()))
                val to = temp.time
                val toTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(temp.time)
                val toDate = DateFormat.getDateInstance(DateFormat.FULL).format(temp.time)
                //Finish

                if (slotCheck(from, to)) {
                    timePicker.error = null
                    timePicker.clearFocus()

                    //Populate Booking object and pass it to receipt fragment
                    booking = Booking(null, venue, null, loggedInUser, toTime, fromTime, fromDate,toDate, "booked", to, from)

                    Log.d(TAG, "Booking $booking")

                    Log.d(TAG, "BookBtn")
                    val receiptFragment = ReceiptFragment()
                    val args = Bundle()
                    args.putSerializable("booking", booking)
                    args.putInt("playHrs", playHrs[0].toInt())
                    receiptFragment.arguments = args

                    fragmentManager!!.beginTransaction().addToBackStack("detailed venue fragment").replace(R.id.fragment_layout, receiptFragment).commit()
                }else{
                    Toast.makeText(context, "This slot is unavailable",Toast.LENGTH_LONG).show()
                    timePicker.error = "This slot is not available"
                    timePicker.requestFocus()
                }
            }
        }

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        // Date Selector
        dateBtn.setOnClickListener {

            datePicker = DatePickerDialog(context,R.style.DatePickerTheme,
                    DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->
                        Log.d(TAG, "Year: $yr, Month $monthOfYear, Day: $dayOfMonth")
                        c.set(yr, monthOfYear, dayOfMonth)
                        val dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.time)

                        //checkingAvailabilitySlots(dateString!!)
                        //dateString = "$dayOfMonth/$monthOfYear/$yr"
                        dateBtn.text = dateString
                    },year,month,day)
            datePicker.datePicker.minDate = c.timeInMillis
            datePicker.show()
        }

        // Time Selector
        timePicker.setOnClickListener {
            val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,R.style.DatePickerTheme,
                    TimePickerDialog.OnTimeSetListener { _, hr, min ->

                        c.set(Calendar.HOUR, hr)
                        c.set(Calendar.MINUTE, min)

                        if (today!! <= c.time){
                            timePicker.error = null
                            timePicker.clearFocus()

                            val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
                            timePicker.text = timeString
                        }else{
                            timePicker.error = "Enter valid time"
                            timePicker.requestFocus()
                        }
                    }, hourOfDay, minute, false)

            timePickerDialog.show()
        }

        // Dropdown list for overs
        val adapter = ArrayAdapter.createFromResource(context,
                R.array.match_overs, android.R.layout.simple_spinner_item)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        overSpinner.adapter = adapter

        getDirectionsBtn.setOnClickListener {

            val dialog = AlertDialog.Builder(context!!)
            dialog.setTitle("Open google maps")
                    .setMessage("This request is leading to open google maps")
                    .setPositiveButton("Open") { _, _ ->
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${venue.getLocation().getLatitude()}" +
                                ",${venue.getLocation().getLongitude()}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        if (mapIntent.resolveActivity(context!!.packageManager) != null){
                            Log.d(TAG, "map intent")
                            startActivity(mapIntent)
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        Log.d(TAG, "In show alert else")
                    }
            dialog.show()
        }

        //Creating Amenities layout
        if (venue.getAmenities() != null){
            venue.getAmenities()!!.map {item ->
                val textView = TextView(context)
                val drawable = when(item.imageView) {
                    "clock" -> R.drawable.clock_1
                    "toilet" -> R.drawable.marker
                    else -> R.drawable.clock_1
                }
                textView.text = item.name
                textView.setPadding(0, 4, 0, 0)
                textView.setCompoundDrawablesWithIntrinsicBounds(drawable, 0,0,0)
                textView.gravity = Gravity.CENTER
                textView.setTextColor(Color.GRAY)
                textView.textSize = 18F
                amenitiesView.addView(textView)
            }
        }
        return view
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(TAG, "OnViewStateRestored")
        if (savedInstanceState != null){
            booking = savedInstanceState.getSerializable("booking") as Booking?
            val c = Calendar.getInstance()
            c.time = DateFormat.getDateInstance(DateFormat.FULL).parse(booking!!.getBookingDate())
            dateBtn.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.time)
            timePicker.text = booking!!.getStartTime()
        }else if (booking != null){
            val c = Calendar.getInstance()
            c.time = DateFormat.getDateInstance(DateFormat.FULL).parse(booking!!.getBookingDate())
            dateBtn.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.time)
            timePicker.text = booking!!.getStartTime()
        }else{
            Toast.makeText(context, "No state have saved", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "OnSaveStateRestored")
        outState.putSerializable("booking", booking)
    }

    private fun getAllBookingsOfThisVenue(){
        currentUser.getIdToken(true).addOnCompleteListener { task ->
            if (task.isSuccessful){
                val idToken = task.result!!.token
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

    private fun slotCheck(from: Date?, to: Date?): Boolean {
        var flag = true
        var i = 0
        if (bookings != null) {
            while (i < bookings!!.size){
                val startB = bookings!![i].getFrom()
                val endB = bookings!![i].getTo()
                if ((from!! <= endB) && (to!! >= startB)){
                    flag = false
                    break
                }
                i++
            }
        }
        return flag
    }
}
