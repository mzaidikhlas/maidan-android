package com.maidan.android.client.controllers.login


import android.content.Intent
import android.os.Bundle
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

class SignupDetailsFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var user: User
    private val TAG = "SignupDetails"

    //Temp variables
    private lateinit var name: String
    private lateinit var email: String
    private var password: String? = null

    //layout
    private lateinit var signupBackBtn: Button
    private lateinit var userImage: ImageButton
    private lateinit var nameTxt: TextView
    private lateinit var emailTxt: TextView
    private lateinit var phoneNumberTxt: EditText
    private lateinit var cnicTxt: EditText
    private lateinit var dobTxt: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var submitBtn: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (!arguments!!.isEmpty){
            name = arguments!!.getString("name")
            email = arguments!!.getString("email")

            if (arguments!!.getString("password") != null)
                password = arguments!!.getString("password")
        }
        val view =  inflater.inflate(R.layout.fragment_signup_details, container, false)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        signupBackBtn = view.findViewById(R.id.signupBack)
        userImage = view.findViewById(R.id.uploadPicture)
        nameTxt = view.findViewById(R.id.name)
        emailTxt = view.findViewById(R.id.email)
        phoneNumberTxt = view.findViewById(R.id.phonenumber)
        cnicTxt = view.findViewById(R.id.CNIC)
        dobTxt = view.findViewById(R.id.DOB)
        genderSpinner = view.findViewById(R.id.gender)
        submitBtn = view.findViewById(R.id.signup_submit_btn)

        var displayAvatar: String? = null

        //populating layout
        nameTxt.text = name
        emailTxt.text = email
        //Upload image using picasso
        if (currentUser!!.photoUrl != null) {
            Picasso.get().load(currentUser.photoUrl).into(userImage)
            displayAvatar = currentUser.photoUrl.toString()
        }
        submitBtn.setOnClickListener {
            Log.d(TAG, "AYA hai")

            if (phoneNumberTxt.text.isNotEmpty() && cnicTxt.text.isNotEmpty() && dobTxt.text.isNotEmpty()){

                user = User(email, name, password, phoneNumberTxt.text.toString(), cnicTxt.text.toString(), displayAvatar,
                        dobTxt.text.toString(), gender.selectedItem.toString(), true, false, null)

                Log.d(TAG, currentUser.providerId)
                currentUser.getIdToken(true).addOnCompleteListener { task ->
                    val idToken = task.result.token

                    val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)

                    val call: Call<ApiResponse> = apiService.createUser(idToken!!, user)
                    call.enqueue(object: Callback<ApiResponse>{
                        override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                            Log.d(TAG, t!!.message)
                        }

                        override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                            Log.d(TAG, "OnResponse")
                            updateUI(currentUser)
                        }
                    })
                }
            }else{
                Toast.makeText(context, "All Fields are required", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }
    private fun updateUI(user: FirebaseUser) {
        val mainActivity = Intent(context, MainActivity::class.java)
        mainActivity.putExtra("loginUser", user)
        this.startActivity(mainActivity)
    }
}