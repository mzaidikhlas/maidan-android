package com.example.wasifnadeem.maidan_android.controllers

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.wasifnadeem.maidan_android.R
import com.example.wasifnadeem.maidan_android.adapter.CategoryRecyclerviewAdapter
import com.example.wasifnadeem.maidan_android.models.Category
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng

class BookingFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var recyclerView: RecyclerView

    private lateinit var myDataSet: ArrayList<Category> ;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_booking, container, false)

        myDataSet = ArrayList();

        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))
        myDataSet.add(Category(null, "Something"))


        recyclerView = view.findViewById(R.id.my_recycler_view);
        recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayout.HORIZONTAL, false);
        recyclerView.adapter = CategoryRecyclerviewAdapter(myDataSet);

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment ;
        fragment.getMapAsync(this);
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
         //Add a marker in Sydney, Australia, and move the camera.
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

//    fun showDatePicker(v: View) {
//        val newFragment = MyDatePickerFragment()
//        newFragment.show(childFragmentManager, "date picker")
//    }
}