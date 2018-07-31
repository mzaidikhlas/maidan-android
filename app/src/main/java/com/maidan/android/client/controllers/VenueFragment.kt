package com.maidan.android.client.controllers

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.maidan.android.client.LoginActivity
import com.maidan.android.client.R
import com.maidan.android.client.models.Venue
import java.util.ArrayList
import com.maidan.android.client.adapter.VenueCardAdaptor

class VenueFragment : Fragment(), OnMapReadyCallback {

    //Layout
    private lateinit var venueCard: RecyclerView
    private lateinit var venues: ArrayList<Venue>
    private lateinit var back: Button
    private lateinit var filter: Button

    private var TAG = "VenuePage"

    //Firebase
    private lateinit var mAuth: FirebaseAuth

    //Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var mapView: FrameLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        if (currentUser == null ){
            val loginIntent =  Intent(activity,  LoginActivity::class.java)
            activity!!.startActivity(loginIntent)
        }

        val view = inflater.inflate(R.layout.fragment_venue, container, false)

        venues = arguments!!.getSerializable("venues") as ArrayList<Venue>
        Log.d(TAG, venues.toString())

        venueCard = view.findViewById(R.id.groundCards)
        mapView = view.findViewById(R.id.mapVenue)
        back = view.findViewById(R.id.back)
        filter = view.findViewById(R.id.filter)

//        activity!!.actionBar.setDisplayHomeAsUpEnabled(true)
        venueCard.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
        venueCard.adapter = VenueCardAdaptor(venues, fragmentManager!!)

        back.setOnClickListener{fragmentManager!!.popBackStack()}
        filter.setOnClickListener{Log.d(TAG, "filter")}

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = childFragmentManager.findFragmentById(R.id.mapVenue) as SupportMapFragment;
        fragment.getMapAsync(this);

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("VenuePage", "IDhr aya hai")
                setMarkers("Cricket")
            }
        }
        else
            setMarkers("Cricket")
    }

    //Getting all values according to recyclerview selected items
    private fun setMarkers(categoryName: String){
        if (!venues.isEmpty()){
            for (venue: Venue in venues){
                Log.d(TAG, venue.toString())

                markerOptions = MarkerOptions()
                        .position(LatLng(venue.getLocation().getLatitude(), venue.getLocation().getLongitude()))
                        .title(venue.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.venue_marker))

                mMap.addMarker(markerOptions)
            }
        }
        else
            Log.d(TAG,"Venue empty")
    }

}
