package com.maidan.android.client.controllers

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
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
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class BookingFragment : Fragment(), OnMapReadyCallback{

    //Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var mapView: FrameLayout
    private lateinit var markerLayout: View

    //Log Tag
    private val TAG = "BookingFragment"

    //layouts
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBtn: Button
    private lateinit var datePicker: DatePickerDialog
    private lateinit var selectDate : TextView
    private lateinit var mapFragment: SupportMapFragment

    //   private lateinit var category:Spinner

    private lateinit var categories: ArrayList<Category>
    private lateinit var venues: ArrayList<Venue>
    private var dateString: String? = null

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
    private var boundsBuilder: LatLngBounds.Builder? = null
    private var city: String = "Lahore"
    private var country: String = "Pakistan"
    private var categoryName: String? = null

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
        markerLayout = (activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.custom_marker_layout, null)

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

            dateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.time)

            datePicker = DatePickerDialog(context, R.style.DatePickerTheme, DatePickerDialog.OnDateSetListener { p0, p1, p2, p3 ->
                Log.d("P0", p0.toString())
                Log.d("P1", p1.toString())
                Log.d("P2", p2.toString())
                Log.d("P3", p3.toString())

                selectDate.text = dateString

            }, year, month, day)
            datePicker.datePicker.minDate = c.timeInMillis
            datePicker.show()
        }
        categories = ArrayList()

        categories.add(Category(R.drawable.football, "Football"))
        categories.add(Category(R.drawable.cricket, "Cricket"))
        categories.add(Category(R.drawable.hockey, "Tennis"))
        categories.add(Category(R.drawable.swimming, "Swimming"))
        categories.add(Category(R.drawable.swimming, "Horse Riding"))

        recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayout.HORIZONTAL, false)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = CategoryRecyclerviewAdapter(categories, this)
//        val middleOfView = (categories.size)/2
//        Log.d("POSITION", middleOfView.toString())
//        recyclerView.scrollToPosition(middleOfView)

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
        val selectedVenues = ArrayList<Venue>()
        if (venues.isEmpty()){
            Toast.makeText(context, "No Venues found", Toast.LENGTH_LONG).show()
        }else {
            var i = 0
            while (i < venues.size){
                if (venues[i].getActivityType() == categoryName)
                    selectedVenues.add(venues[i])

                i++
            }
            if (selectedVenues.isNotEmpty()){
                val venueFragment = VenueFragment()
                val args = Bundle()
                args.putSerializable("venues", selectedVenues)
                venueFragment.arguments = args
                fragmentManager!!.beginTransaction().addToBackStack("booking fragment").replace(R.id.fragment_layout, venueFragment).commit()
            }else{
                Toast.makeText(context, "No Venues found of this category", Toast.LENGTH_LONG).show()
            }
        }
    }

    //Getting all values according to recyclerview selected items
    private fun setMarkers(){
        Log.d(TAG, "First")
        venues = ArrayList()
        currentUser.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result!!.token
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

                                                var venue: Venue?
                                                Log.d(TAG, "Payload$payload")
                                                for (item: PayloadFormat in payload){
                                                    val jsonObject = gson.toJsonTree(item.getData()).asJsonObject
                                                    Log.d(TAG, "Json$jsonObject")
                                                    venue = gson.fromJson(jsonObject, Venue::class.java)
                                                    venue.setRef(item.getDocId())
                                                    Log.d(TAG, venue.toString())
                                                    if (venue!!.getActivityType() == categoryName){

                                                        val bm = getMarkerBitmapFromView(markerLayout, venue.getRate().getPerHrRate().toString())
                                                        val latLng = LatLng(venue.getLocation().getLatitude(), venue.getLocation().getLongitude())
                                                        markerOptions = MarkerOptions()
                                                                .position(latLng)
                                                                .title(venue.getName())
                                                                .snippet(venue.getRef())
                                                                .icon(BitmapDescriptorFactory.fromBitmap(bm))
                                                        mMap.addMarker(markerOptions)
                                                        Log.d(TAG, "Venues markers $latLng")
                                                        boundsBuilder!!.include(latLng)
                                                    }
                                                    venues.add(venue)
                                                }
                                            }
                                            bounds = boundsBuilder!!.build()
                                            try{
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 12))
                                            }catch (e:Exception ){
                                                e.printStackTrace()
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
                        Log.d(TAG, "Task Exception ${task.exception}")
                        throw task.exception!!
                    }
                }
    }

    override fun onStop() {
        //fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }


    private fun getMarkerBitmapFromView(view: View, priceText: String): Bitmap{
        view.findViewById<TextView>(R.id.priceTextView).text = priceText
//        view.findViewById<ImageView>(R.id.ImageView01).setImageResource(R.drawable.marker_)
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight,
        Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        view.background?.draw(canvas)
        view.draw(canvas)

        return returnedBitmap
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
                        .snippet(null)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.venue_marker))
                mMap.addMarker(markerOptions)
                Log.d(TAG, "Current location marker $latLng")
                boundsBuilder!!.include(latLng)
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
                            updateMaps("Cricket")
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
                .setPositiveButton("Location Settings") { _, _ ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Log.d(TAG, "In show alert else")
                }
        dialog.show()
    }

    //Making maps ready
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        boundsBuilder = LatLngBounds.builder()
        mMap.setMaxZoomPreference(12F)

        if (categoryName == null)
            categoryName = "Cricket"

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

                    Log.d(TAG, "IDhr aya hai")
                    setMarkers()
                    mMap.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter{
                        override fun getInfoContents(p0: Marker?): View {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun getInfoWindow(p0: Marker?): View {
                            val v: View = View.inflate(context,R.layout.venue_card, null)

                            val type = v.findViewById(R.id.type) as TextView
                            val imageViewIcon = v.findViewById(R.id.ground) as ImageView
                            val name = v.findViewById(R.id.groundname) as TextView
                            val address = v.findViewById(R.id.address) as TextView
                            val price = v.findViewById(R.id.price) as TextView
                            var selectedVenue: List<Venue>? = null

                            if (p0!!.snippet != null){
//                                selectedVenue = ArrayList()
                               selectedVenue = venues.filter {
                                    venue -> venue.getRef() == p0.snippet
                               }
                                type.text = selectedVenue[0].getActivityType()
                                name.text = selectedVenue[0].getName()
                                address.text = selectedVenue[0].getLocation().getArea()
                                price.text = selectedVenue[0].getRate().getPerHrRate().toString()
                                if (selectedVenue[0].getPictures() != null)
                                    Picasso.get().load(selectedVenue[0].getPictures()!![0]).into(imageViewIcon)
                            }
                            return v
                        }

                    })
                    mMap.setOnInfoWindowClickListener {p0: Marker? ->

                        if (p0!!.snippet != null){
                            val selectedVenue = venues.filter {
                                venue -> venue.getRef() == p0.snippet
                            }
                            val detailedVenueFragment = DetailedVenueFragment()
                            val args = Bundle()
                            args.putSerializable("venue", selectedVenue[0])
                            detailedVenueFragment.arguments = args
                            fragmentManager!!.beginTransaction().addToBackStack("venue fragment").replace(R.id.fragment_layout, detailedVenueFragment).commit()
                        }

//                        Log.d(TAG, "geo:$latitude,$longitude?q=${p0!!.position.latitude},${p0.position.longitude}")
//                        val gmmIntentUri = Uri.parse("geo:0,0?q=${p0.position.latitude},${p0.position.longitude}")
//                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//                        if (mapIntent.resolveActivity(context!!.packageManager) != null){
//                            Log.d(TAG, "map intent")
//                            startActivity(mapIntent)
//                        }
                    }
                }
            }else{
                Log.d(TAG, "onCreateView: version else")
            }
        }else{
            Log.d(TAG, "onCreateView: checklocation else")
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "IDhr aya hai")
//                setMarkers()
//
//                mMap.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter{
//                    override fun getInfoContents(p0: marker?): View {
//                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                    }
//
//                    override fun getInfoWindow(p0: marker?): View {
//                        val v: View = View.inflate(context,R.layout.map_info_window, null)
//
//                        //val image: ImageView = v.findViewById(R.id.infoWindowImageview)
//                        val venueName: TextView = v.findViewById(R.id.infoWindowName)
//                        val country: TextView = v.findViewById(R.id.infoWindowCountry)
//                        val city: TextView = v.findViewById(R.id.infoWindowCity)
//                        val price: TextView = v.findViewById(R.id.infoWindowPrice)
//
//                        val data: List<String> = p0!!.snippet.split(",")
//
//                        venueName.text = p0.title
//                        country.text = data[0]
//                        city.text = data[1]
//                        price.text = data[2]
//
//                        return v
//                    }
//
//                })
//                mMap.setOnInfoWindowClickListener {p0: marker? ->
//                    Log.d(TAG, "geo:$latitude,$longitude?q=${p0!!.position.latitude},${p0.position.longitude}")
//                    val gmmIntentUri = Uri.parse("geo:0,0?q=${p0.position.latitude},${p0.position.longitude}")
//                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//                    if (mapIntent.resolveActivity(context!!.packageManager) != null){
//                        Log.d(TAG, "map intent")
//                        startActivity(mapIntent)
//                    }
//                }
//            }
//        }

    }

    fun updateMaps(categoryName: String){
        this.categoryName = categoryName
        mMap.clear()
        mapFragment.getMapAsync(this)
    }
}