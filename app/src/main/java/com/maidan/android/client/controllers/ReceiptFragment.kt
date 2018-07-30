package com.maidan.android.client.controllers


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.maidan.android.client.R
import com.maidan.android.client.models.Venue

class ReceiptFragment : Fragment() {


    private lateinit var venue: Venue

    private val TAG = "VenueReceiptFragment"
    //Layout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_receipt, container, false)

        if (!arguments!!.isEmpty){
            venue = arguments!!.getSerializable("venue") as Venue
            Log.d(TAG, venue.toString())
        }
        else
            Log.d(TAG, "Empty")

        return view
    }

}
