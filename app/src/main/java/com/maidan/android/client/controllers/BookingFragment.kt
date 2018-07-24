package com.maidan.android.client.controllers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.maidan.android.client.R
import com.maidan.android.client.adapter.CategoryRecyclerviewAdapter
import com.maidan.android.client.models.Category
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import android.location.Location
import android.os.Build
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import android.location.LocationManager
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.fragment_booking.*


class BookingFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    //layouts
    private lateinit var mapView: FrameLayout
    private lateinit var date: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var search: Button

    private lateinit var myDataSet: ArrayList<Category> ;

    private var latitude: Double = 0.toDouble();
    private var longitude: Double = 0.toDouble();

    private lateinit var myLastLocation: Location
    private var mMarker: Marker? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var locationManager: LocationManager? = null

    companion object {
        private const val MY_PERMISSION_CODE: Int = 1000
    }

    private val isLocationEnabled: Boolean
        get() {
            locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState:    Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_booking, container, false)

        mapView = view.findViewById(R.id.map)
        date = view.findViewById(R.id.date_btn)
        search = view.findViewById(R.id.search_btn)
        recyclerView = view.findViewById(R.id.category);

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

        recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayout.HORIZONTAL, false);
        recyclerView.adapter = CategoryRecyclerviewAdapter(myDataSet);

       if  (checkLocation()) {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
               if (checkLocationPermission()) {
                   buildLocationRequest();
                   buildLocationCallback()
                   fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
                   fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
               } else {
                   buildLocationRequest();
                   buildLocationCallback()
                   fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
                   fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
               }

           }
       }
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment ;
        fragment.getMapAsync(this);

    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                myLastLocation = p0!!.locations[p0.locations.size-1]

                if (mMarker != null)
                    mMarker!!.remove()

                latitude = myLastLocation.latitude
                longitude = myLastLocation.longitude

                val latLng = LatLng(latitude,longitude)
                Log.d("LatLng", latLng.toString())
                val markerOptions = MarkerOptions()
                        .position(latLng)
                        .title("Your position")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = mMap.addMarker(markerOptions)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(activity!!, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            }
            else{
                ActivityCompat.requestPermissions(activity!!, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            }

            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            MY_PERMISSION_CODE ->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if (checkLocationPermission()){
                            buildLocationRequest()
                            buildLocationCallback()
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
                            mMap.isMyLocationEnabled = true
                        }
                    }
                }
                else
                    Toast.makeText(this.context!!, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    private fun checkLocation(): Boolean {
        if (!isLocationEnabled)
            showAlert()
        return isLocationEnabled
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(context!!)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
            }
        }
        else
            mMap.isMyLocationEnabled = true
    }
}