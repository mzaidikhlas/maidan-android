package com.maidan.android.client.controllers.login


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.maidan.android.client.MainActivity
import com.maidan.android.client.R
import com.maidan.android.client.models.User
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.RetrofitClient
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
    private lateinit var password: String

    //layout
    private lateinit var phoneNumberTxt: EditText
    private lateinit var cnicTxt: EditText
    private lateinit var dobTxt: EditText
    private lateinit var genderTxt: EditText

    private lateinit var submitBtn: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (!arguments!!.isEmpty){
            name = arguments!!.getString("name")
            email = arguments!!.getString("email")
            password = arguments!!.getString("password")
        }
        val view =  inflater.inflate(R.layout.fragment_signup_details, container, false)

        phoneNumberTxt = view.findViewById(R.id.phoneNumber)
        cnicTxt = view.findViewById(R.id.cnic)
        dobTxt = view.findViewById(R.id.dob)
        genderTxt = view.findViewById(R.id.gender)
        submitBtn = view.findViewById(R.id.submit_btn)

        mAuth = FirebaseAuth.getInstance()

        submitBtn.setOnClickListener {
            val currentUser = mAuth.currentUser

            user = User(email, name, password, phoneNumberTxt.text.toString(), cnicTxt.text.toString(), "null", null)

            currentUser!!.getIdToken(true).addOnCompleteListener { task ->
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
        }
        return view
    }
    private fun updateUI(user: FirebaseUser) {
        val mainActivity = Intent(context, MainActivity::class.java)
        mainActivity.putExtra("loginUser", user)
        this.startActivity(mainActivity)
    }
}
