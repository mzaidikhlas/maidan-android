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
import com.maidan.android.client.R
import com.maidan.android.client.models.Venue
import java.util.ArrayList
import com.maidan.android.client.adapter.VenueCardAdaptor

class VenueFragment : Fragment() {

    private lateinit var venueCard: RecyclerView
    private lateinit var venues: ArrayList<Venue>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.venue_details, container, false)

        venues = arguments!!.getSerializable("venues") as ArrayList<Venue>
        Log.d("Venue", venues.toString())

        venueCard = view.findViewById(R.id.groundCards)

        venueCard.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
        venueCard.adapter = VenueCardAdaptor(venues)

        return view;
    }


}
