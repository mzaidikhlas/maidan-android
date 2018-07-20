package com.example.wasifnadeem.maidan_android

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.wasifnadeem.maidan_android.controllers.BookingFragment
import com.example.wasifnadeem.maidan_android.controllers.FavoritesFragment
import com.example.wasifnadeem.maidan_android.controllers.HomeFragment
import com.example.wasifnadeem.maidan_android.controllers.SettingsFragment
import com.example.wasifnadeem.maidan_android.retrofit.ApiInterface
import com.example.wasifnadeem.maidan_android.retrofit.ApiResponse
import com.example.wasifnadeem.maidan_android.retrofit.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, HomeFragment()).commit();
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_book-> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, BookingFragment()).commit();
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorites -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, FavoritesFragment()).commit();
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings-> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, SettingsFragment()).commit();
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser = mAuth.currentUser!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        // Configure sign-in to request the user's ID, email address, and basic
//        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build()

        mAuth = FirebaseAuth.getInstance()
        signInWithEmailPassword("waif.nadeem90@gmail.com", "passwordNeedToBeSecure")

        supportFragmentManager.beginTransaction().add(R.id.fragment_layout, HomeFragment()).commit();
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun signInWithEmailPassword(email: String, password: String) {
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        Log.d("UserException", "NotCompleted")
                        if (task.isSuccessful){
                            Log.d("UserException", "Completed")
                            val user: FirebaseUser = mAuth.currentUser!!
                            user.getIdToken(true)
                                    .addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful) {
                                            val idToken = task2.result.token
                                            Log.d("User", idToken)

                                            val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)

                                            val call: Call<ApiResponse> = apiService.testing(idToken!!)

                                            call.enqueue(object: Callback<ApiResponse>{
                                                override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                                                    Log.d("UserApiError", t.toString())
                                                }

                                                override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                                                    if (response!!.isSuccessful){
                                                        Log.d("UserApiSuccess", response.body().toString())
                                                    }
                                                }

                                            })
                                        } else {
                                            // Handle error -> task.getException();
                                            Log.d("UserTokenError1", "Error")
                                        }
                                    }
                        }

                        else{
                            Log.d("UserTokenError", "Error")
                        }
                    }

        }
        catch (e: Exception){
            Log.d("UserException", "Yeh hai")
        }
    }

    private fun signInWithGoogle(){
        try {

        }
        catch (e: Exception){
            Log.d("GoogleException", e.toString())
        }
    }
}
