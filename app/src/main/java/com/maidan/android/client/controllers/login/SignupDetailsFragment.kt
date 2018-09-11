package com.maidan.android.client.controllers.login


import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.maidan.android.client.MainActivity
import com.maidan.android.client.R
import com.maidan.android.client.models.User
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.RetrofitClient
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_signup_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SignupDetailsFragment : Fragment() {
    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private lateinit var user: User
    private val TAG = "SignupDetails"

    // Upload Image
    private var PICK_IMAGE = 100
    private lateinit var imageUri : Uri

    //Temp variables
    private lateinit var name: String
    private lateinit var email: String
    private var password: String? = null

    //layout
    private lateinit var userImage: ImageButton
    private lateinit var nameTxt: TextView
    private lateinit var emailTxt: TextView
    private lateinit var phoneNumberTxt: EditText
    private lateinit var cnicTxt: EditText
    private lateinit var dobTxt: TextView
    private lateinit var genderSpinner: Spinner
    private lateinit var submitBtn: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null){
            if (arguments!!.getString("name") != null)
                name = arguments!!.getString("name")

            email = arguments!!.getString("email")

            if (arguments!!.getString("password") != null)
                password = arguments!!.getString("password")
        }

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view =  inflater.inflate(R.layout.fragment_signup_details, container, false)

        userImage = view.findViewById(R.id.uploadPicture)
        nameTxt = view.findViewById(R.id.name)
        emailTxt = view.findViewById(R.id.email)
        phoneNumberTxt = view.findViewById(R.id.phonenumber)
        cnicTxt = view.findViewById(R.id.CNIC)
        dobTxt = view.findViewById(R.id.DOB)
        genderSpinner = view.findViewById(R.id.gender)
        submitBtn = view.findViewById(R.id.signup_submit_btn)
        progressBar = view.findViewById(R.id.signupProgressBar)

        //populating layout
        nameTxt.text = name
        emailTxt.text = email

        var displayAvatar: String? = null

        dobTxt.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            var dateString = "$day/$month/$year"

            val datePicker = DatePickerDialog(context,android.R.style.Theme_Holo_Dialog,
                    DatePickerDialog.OnDateSetListener { _, yr, monthOfYear, dayOfMonth ->
                        Log.d(TAG, "Year: $yr, Month $monthOfYear, Day: $dayOfMonth")
                        dateString = "$dayOfMonth/$monthOfYear/$yr"
                        dobTxt.text = dateString
                    },year,month,day)

            datePicker.datePicker.maxDate = c.timeInMillis
            datePicker.show()
            Log.d(TAG,dateString)
        }

        //Upload image using picasso
        if (currentUser.photoUrl != null) {
            Picasso.get().load(currentUser.photoUrl)
                    .fit()
                    .centerInside()
                    .into(userImage)
            displayAvatar = currentUser.photoUrl.toString()
        }
        else
        {
            // Upload Picture
            userImage.setOnClickListener {
                openGallery()
            }

        }
        submitBtn.setOnClickListener {
            Log.d(TAG, "AYA hai")

            progressBar.visibility = View.VISIBLE
            submitBtn.isEnabled = false

            if (phoneNumberTxt.text.isNotEmpty() && cnicTxt.text.isNotEmpty() && dobTxt.text.isNotEmpty()){
                user = User(null, email, name, password, phoneNumberTxt.text.toString(), cnicTxt.text.toString(), displayAvatar,
                        dobTxt.text.toString(), gender.selectedItem.toString(), true, false, null)

                Log.d(TAG, currentUser.providerId)
                currentUser.getIdToken(true).addOnCompleteListener { task ->
                    val idToken = task.result.token

                    val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)

                    val call: Call<ApiResponse> = apiService.createUser(idToken!!, user)
                    call.enqueue(object: Callback<ApiResponse>{
                        override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                            Log.d(TAG, t.toString())
                            progressBar.visibility = View.INVISIBLE
                            submitBtn.isEnabled = true
                            throw t!!
                        }

                        override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                            Log.d(TAG, "OnResponse")
                            updateUI(currentUser)
                        }
                    })
                }
            }else{
                progressBar.visibility = View.INVISIBLE
                submitBtn.isEnabled = true
                Toast.makeText(context, "All Fields are required", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }

    private fun openGallery() {

        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery,PICK_IMAGE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE)
        {
            imageUri = data!!.data
            userImage.setImageURI(imageUri)
        }
    }

    private fun updateUI(user: FirebaseUser) {
        progressBar.visibility = View.INVISIBLE
        val mainActivity = Intent(context, MainActivity::class.java)
        mainActivity.putExtra("loginUser", user)
        this.startActivity(mainActivity)
    }
}
