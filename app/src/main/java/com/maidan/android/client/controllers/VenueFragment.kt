package com.maidan.android.client.controllers

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
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
import android.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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
    private lateinit var pickGround : TextView
    private lateinit var filter: Button

    private var TAG = "VenueFragment"

    //Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var mapView: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null){
            venues = arguments!!.getSerializable("venues") as ArrayList<Venue>
            Log.d(TAG, venues.toString())
        }else{
            Toast.makeText(context, "No venues found", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_venue, container, false)

        venueCard = view.findViewById(R.id.groundCards)
        mapView = view.findViewById(R.id.mapVenue)
        back = view.findViewById(R.id.back)
        filter = view.findViewById(R.id.filter)
        pickGround = view.findViewById(R.id.pickaground)
        pickGround.letterSpacing = 0.3F

//        activity!!.actionBar.setDisplayHomeAsUpEnabled(true)
        venueCard.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
        venueCard.adapter = VenueCardAdaptor(venues, fragmentManager!!)

        back.setOnClickListener{fragmentManager!!.popBackStack()}
        filter.setOnClickListener{Log.d(TAG, "filter")}

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = childFragmentManager.findFragmentById(R.id.mapVenue) as SupportMapFragment
        fragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.setMaxZoomPreference(12F)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("VenuePage", "IDhr aya hai")
                setMarkers()
            }else
                Toast.makeText(context, "Venue permissions issue", Toast.LENGTH_LONG).show()
        }
        else
            Toast.makeText(context, "Venue versions issue", Toast.LENGTH_LONG).show()
    }

    //Getting all values according to recyclerview selected items
    private fun setMarkers(){
        val boundsBuilder = LatLngBounds.builder()

        if (!venues.isEmpty()){
            for (venue: Venue in venues){
                Log.d(TAG, venue.toString())

                val latLng = LatLng(venue.getLocation().getLatitude(), venue.getLocation().getLongitude())
                markerOptions = MarkerOptions()
                        .position(latLng)
                        .title(venue.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.venue_marker))
                mMap.addMarker(markerOptions)

                boundsBuilder.include(latLng)
                val bounds = boundsBuilder.build()
                try{
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                }catch (e:Exception ){
                    e.printStackTrace()
                }
            }
        }
        else
            Log.d(TAG,"Venue empty")
    }

}
