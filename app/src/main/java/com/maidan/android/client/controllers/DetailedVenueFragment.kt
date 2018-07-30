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
import com.maidan.android.client.R
import com.maidan.android.client.models.Venue
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
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

    private lateinit var venue: Venue

    private val TAG = "VenueDetailedFragment"

    private var sampleImages = intArrayOf(R.drawable.sample, R.drawable.sample, R.drawable.sample, R.drawable.sample, R.drawable.sample)

    private var imageListener: ImageListener = ImageListener { position, imageView -> imageView.setImageResource(sampleImages[position]) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_detailed_venue, container, false)

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
            Log.d(TAG, "BookBtn")
            val receiptFragment = ReceiptFragment()
            val args = Bundle()
            args.putSerializable("venue", venue)

            receiptFragment.arguments = args

            fragmentManager!!.beginTransaction().addToBackStack("detailed venue fragment").replace(R.id.fragment_layout, receiptFragment).commit()
        }

        // Date Selector
        dateBtn.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            datePicker = DatePickerDialog(context,R.style.DatePickerTheme, DatePickerDialog.OnDateSetListener { p0, p1, p2, p3 -> },year,month,day)
            datePicker.show()
        };

        // Time Selector
        timePicker.setOnClickListener {
            val c = Calendar.getInstance()
            val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,R.style.DatePickerTheme,
                    TimePickerDialog.OnTimeSetListener { p0, p1, p2 -> }, hourOfDay, minute, false)
            timePickerDialog.show()
        };

        // Dropdown list for overs
        val adapter = ArrayAdapter.createFromResource(context,
                R.array.match_overs, android.R.layout.simple_spinner_item)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        overSpinner.adapter = adapter;

        return view
    }
}
