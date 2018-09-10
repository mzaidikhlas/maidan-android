package com.maidan.android.client.controllers

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.maidan.android.client.R
import com.maidan.android.client.adapter.CategoryRecyclerviewAdapter
import com.maidan.android.client.models.Category
import com.google.android.gms.maps.*
import android.location.Location
import android.os.Build
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.*
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.widget.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
import kotlin.collections.ArrayList

class BookingFragment : Fragment(), OnMapReadyCallback{

    //Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var mapView: FrameLayout

    //Log Tag
    private val TAG = "Venues"

    //layouts
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBtn: Button
    private lateinit var datePicker: DatePickerDialog
    private lateinit var selectDate : TextView
    private lateinit var mapFragment: SupportMapFragment

    //   private lateinit var category:Spinner

    private lateinit var myDataSet: ArrayList<Category>
    private lateinit var venues: ArrayList<Venue>

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    //Api Call Response
    private lateinit var payload: ArrayList<PayloadFormat>

    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    //Google Maps
    private lateinit var myLastLocation: Location
    private var bounds: LatLngBounds? = null
    private lateinit var boundsBuilder: LatLngBounds.Builder
    private var city: String = "Lahore"
    private var country: String = "Pakistan"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState:    Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_booking, container, false)

        mapView = view.findViewById(R.id.mapBooking)
        selectDate = view.findViewById(R.id.selectDateCalendar)
        searchBtn = view.findViewById(R.id.search_btn)
        //    category = view.findViewById(R.id.bookingCategory)

        searchBtn.letterSpacing = 0.3F
        recyclerView = view.findViewById(R.id.my_recycler_view)
        val snapHelper = SnapHelperOneByOne()
        snapHelper.attachToRecyclerView(recyclerView)

        // calender code
        selectDate.setOnClickListener {
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
            datePicker.datePicker.minDate = c.timeInMillis
            datePicker.show()
        }
        myDataSet = ArrayList()

        myDataSet.add(Category(R.drawable.football, "FootBall"))
        myDataSet.add(Category(R.drawable.hockey, "Hockey"))
        myDataSet.add(Category(R.drawable.cricket, "Cricket"))
        myDataSet.add(Category(R.drawable.swimming, "Swimming"))
        myDataSet.add(Category(null, "Boxing"))
        myDataSet.add(Category(null, "Rugby"))
        myDataSet.add(Category(null, "Tennis"))
        myDataSet.add(Category(null, "Squash"))

        recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayout.HORIZONTAL, false)
        recyclerView.adapter = CategoryRecyclerviewAdapter(myDataSet)
        val middleOfView = (myDataSet.size)/2
        Log.d("POSITION", middleOfView.toString())
        recyclerView.scrollToPosition(middleOfView)

        if  (checkLocation()) {
            Log.d(TAG, "onCreateView: checklocation if")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "onCreateView: version if")
                if (checkLocationPermission()) {
                    Log.d(TAG, "onCreateView: checklocationPermission if")
                    buildLocationRequest()
                    buildLocationCallback()
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                } else {
                    Log.d(TAG, "onCreateView: checklocationPermission else")
                    buildLocationRequest()
                    buildLocationCallback()
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                }
            }else{
                Log.d(TAG, "onCreateView: version else")
            }
        }else{
            Log.d(TAG, "onCreateView: checklocation else")
        }

        //Search Button click listner
        searchBtn.setOnClickListener {
            Log.d(TAG, "search btn")
            onSearchClick()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.mapBooking) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun onSearchClick(){
        //val fragment = childFragmentManager.findFragmentById(R.id.bookingFooter) as SupportMapFragment
        Log.d(TAG, "search btn click liestner")
        if (venues.isEmpty()){
            Toast.makeText(context, "No Venues found", Toast.LENGTH_LONG).show()
        }else {
            val venueFragment = VenueFragment()
            val args = Bundle()
            args.putSerializable("venues", venues)

            venueFragment.arguments = args

            fragmentManager!!.beginTransaction().addToBackStack("booking fragment").replace(R.id.fragment_layout, venueFragment).commit()
        }
    }

    //Getting all values according to recyclerview selected items
    private fun setMarkers(categoryName: String){
        Log.d(TAG, "First")
        venues = ArrayList()
        currentUser.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result.token
                        Log.d("User", idToken)

                        val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                        val call: Call<ApiResponse> = apiService.getVenues(country, city, idToken!!)
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

                                            if (payload.isNotEmpty()){
                                                var venue: Venue? = null
                                                Log.d(TAG, "Payload$payload")
                                                for (item: PayloadFormat in payload){
                                                    val jsonObject = gson.toJsonTree(item.getData()).asJsonObject
                                                    Log.d(TAG, "Json$jsonObject")
                                                    venue = gson.fromJson(jsonObject, Venue::class.java)
                                                    venue.setRef(item.getDocId())
                                                    Log.d(TAG, venue.toString())

                                                    val latLng = LatLng(venue!!.getLocation().getLatitude(), venue.getLocation().getLongitude())
                                                    markerOptions = MarkerOptions()
                                                            .position(latLng)
                                                            .title(venue.getName())
                                                            .snippet("${venue.getLocation().getCountry()}," +
                                                                    "${venue.getLocation().getCity()}," +
                                                                    "${venue.getRate().getPerHrRate()}")
                                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.venue_marker))
                                                    mMap.addMarker(markerOptions)
                                                    Log.d(TAG, "Venues markers $latLng")
                                                    boundsBuilder.include(latLng)
                                                    venues.add(venue)
                                                }
                                                bounds = boundsBuilder.build()
                                                try{
                                                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                                                }catch (e:Exception ){
                                                    e.printStackTrace()
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

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    private fun buildLocationCallback() {
        Log.d(TAG, "buildLocationCallback")
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                myLastLocation = p0!!.locations[p0.locations.size-1]

//                if (mMarker != null)
//                    mMarker!!.remove()

                latitude = myLastLocation.latitude
                longitude = myLastLocation.longitude

                val gcd = Geocoder(context, Locale.getDefault())
                val address =  gcd.getFromLocation(latitude, longitude, 1)
                Log.d(TAG, "Address $address")

                city = address[0].locality
                country = address[0].countryName

                Log.d(TAG, "Locality ${address[0].locality}")
                Log.d(TAG, "Latitude ${address[0].latitude}")
                Log.d(TAG, "Longitude ${address[0].longitude}")

                val latLng = LatLng(latitude,longitude)
                Log.d("LatLng", latLng.toString())

                val markerOptions = MarkerOptions()
                        .position(latLng)
                        .title("Your position")
                        .snippet("$country," +
                                "$city," +
                                "0")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.venue_marker))
                mMap.addMarker(markerOptions)
                Log.d(TAG, "Current location marker $latLng")
                boundsBuilder.include(latLng)
            }
        }
    }

    private fun buildLocationRequest() {
        Log.d(TAG, "buildLocationRequest")
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                requestPermissions(arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            }
            else{
                requestPermissions(arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            }

            return false
        }
        return true
    }

    //Permission result check
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            MY_PERMISSION_CODE ->{
                Log.d(TAG, "OnPermsissionResult")
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onRequestPermissionResult grant permission if")
                    if(ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG, "onRequestPermissionResult if")
                        if (checkLocationPermission()){
                            Log.d(TAG, "onRequestPermissionResult checkLocationPermission if")
                            buildLocationRequest()
                            buildLocationCallback()
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
                            mMap.isMyLocationEnabled = true
                            mapFragment.getMapAsync(this)

                        }else{
                            Log.d(TAG, "onRequestPermissionResult checkLocationPermission else")
                        }
                    }else{
                        Log.d(TAG, "onRequestPermissionResult else")
                    }
                }
                else
                    Toast.makeText(this.context!!, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //User location enable check
    private fun checkLocation(): Boolean {
        if (!isLocationEnabled){
            showAlert()
            Log.d(TAG, "checklocation if")
        }else{
            Log.d(TAG, "checklocation else")
        }
        return isLocationEnabled
    }

    //Mobile location permission ask dialog box
    private fun showAlert() {
        val dialog = AlertDialog.Builder(context!!)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt ->
                    Log.d(TAG, "In show alert else")
                }
        dialog.show()
    }

    //Making maps ready
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        boundsBuilder = LatLngBounds.builder()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "IDhr aya hai")
                setMarkers("Cricket")

                mMap.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter{
                    override fun getInfoContents(p0: Marker?): View {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun getInfoWindow(p0: Marker?): View {
                        val v: View = View.inflate(context,R.layout.map_info_window, null)

                        val image: ImageView = v.findViewById(R.id.infoWindowImageview)
                        val venueName: TextView = v.findViewById(R.id.infoWindowName)
                        val country: TextView = v.findViewById(R.id.infoWindowCountry)
                        val city: TextView = v.findViewById(R.id.infoWindowCity)
                        val price: TextView = v.findViewById(R.id.infoWindowPrice)

                        val data: List<String> = p0!!.snippet.split(",")

                        venueName.text = p0.title
                        country.text = data[0]
                        city.text = data[1]
                        price.text = data[2]

                        return v
                    }

                })
                mMap.setOnInfoWindowClickListener {p0: Marker? ->
                    Log.d(TAG, "geo:$latitude,$longitude?q=${p0!!.position.latitude},${p0.position.longitude}")
                    val gmmIntentUri = Uri.parse("geo:0,0?q=${p0.position.latitude},${p0.position.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    if (mapIntent.resolveActivity(context!!.packageManager) != null){
                        Log.d(TAG, "map intent")
                        startActivity(mapIntent)
                    }
                }
            }
        }

    }
}