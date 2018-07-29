package com.maidan.android.client.controllers

import android.app.DatePickerDialog
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
import android.location.Location
import android.os.Build
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import android.location.LocationManager
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.FrameLayout
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.maidan.android.client.models.Venue
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.PayloadFormat
import com.maidan.android.client.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class BookingFragment : Fragment(), OnMapReadyCallback {

    //Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions

    //Log Tag
    private val TAG = "Venues"

    //layouts
    private lateinit var mapView: FrameLayout
    private lateinit var date: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBtn: Button
    private lateinit var datePicker: DatePickerDialog

    private lateinit var myDataSet: ArrayList<Category>
    private lateinit var venues: ArrayList<Venue>

    //Api Call Response
    private var payload: ArrayList<PayloadFormat>? = null

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
        searchBtn = view.findViewById(R.id.search_btn)
        recyclerView = view.findViewById(R.id.category);

        //calender code
        date.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            datePicker = DatePickerDialog(context, R.style.DatePickerTheme, DatePickerDialog.OnDateSetListener { p0, p1, p2, p3 ->
                Log.d("P0", p0.toString());
                Log.d("P1", p1.toString());
                Log.d("P2", p2.toString());
                Log.d("P3", p3.toString());
            }, year, month, day)
            datePicker.show()
        }

        myDataSet = ArrayList();

        myDataSet.add(Category(null, "FootBall"))
        myDataSet.add(Category(null, "Hockey"))
        myDataSet.add(Category(null, "Cricket"))
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

        //Search Button click listner
        searchBtn.setOnClickListener {
            Log.d(TAG, "search btn")
            onSearchClick()
        }

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment ;
        fragment.getMapAsync(this);
    }

    private fun onSearchClick(){
        //val fragment = childFragmentManager.findFragmentById(R.id.bookingFooter) as SupportMapFragment
        Log.d(TAG, "search btn click liestner")
        val venueFragment = VenueFragment()
        val args = Bundle()
        args.putSerializable("venues", venues)

        venueFragment.arguments = args

        childFragmentManager.beginTransaction().addToBackStack("booking fragment").add(R.id.bookingFooter, venueFragment).commit()
    }

    //Getting all values according to recyclerview selected items
    private fun setMarkers(categoryName: String){
        val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
        val call: Call<ApiResponse> = apiService.getVenues(categoryName)
        venues = ArrayList()

        call.enqueue(object: Callback<ApiResponse>{
            override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                Log.d(TAG, t.toString())
            }

            override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                if (response!!.isSuccessful){
                    if (response.body()!!.getStatusCode() == 200){
                        Log.d(TAG, "Response")

                        if (response.body()!!.getType() == "Venue"){
                            val gson = Gson()
                            payload = response.body()!!.getPayload()

                            var venue: Venue? = null
                            Log.d(TAG, "Payload$payload")

                            for (item: PayloadFormat in payload!!){
                                val jsonObject = gson.toJsonTree(item.getData()).asJsonObject
                                Log.d(TAG, "Json$jsonObject")
                                venue = gson.fromJson(jsonObject, Venue::class.java)

                                Log.d(TAG, venue.toString())

                                markerOptions = MarkerOptions()
                                        .position(LatLng(venue!!.getLocation().getLatitude(), venue.getLocation().getLongitude()))
                                        .title(venue.getName())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                                mMap.addMarker(markerOptions)

                                venues.add(venue)
                            }
                        }

                    }else {
                        Log.d(TAG, "Error Response")
                        Log.d(TAG, response.body()!!.getMessage())
                    }
                }
            }
        });

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
                Log.d(TAG, "IDhr aya hai")
                setMarkers("Cricket")
            }
        }
        else
            mMap.isMyLocationEnabled = true
    }
}