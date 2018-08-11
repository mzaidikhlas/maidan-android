package com.maidan.android.client

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.maidan.android.client.controllers.BookingFragment
import com.maidan.android.client.controllers.FavoritesFragment
import com.maidan.android.client.controllers.HomeFragment
import com.maidan.android.client.controllers.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.maidan.android.client.controllers.login.SignupDetailsFragment
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private val TAG = "Main Activity"

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
        val user = mAuth.currentUser
        if (user == null) {
            redirectLogin()
        }else{
            user.getIdToken(true).addOnCompleteListener { task ->
                Log.d(TAG, "Calls 1")
                if (task.isSuccessful){
                    val idToken = task.result.token
                    val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)

                    val call: Call<ApiResponse> = apiService.getUserInfoByEmail(idToken!!)

                    call.enqueue(object: Callback<ApiResponse> {
                        override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                            Log.d("UserApiError", t.toString())
                        }

                        override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                            if (response!!.isSuccessful){
                                if (response.body()!!.getStatusCode() == 200) {
                                    if (response.body()!!.getPayload().isEmpty()) {
                                        navigation.visibility = View.INVISIBLE
                                        val signupDetailsFragment = SignupDetailsFragment()

                                        val bundle = Bundle()
                                        bundle.putString("name", user.displayName)
                                        bundle.putString("email", user.email)
                                        bundle.putString("password", null)

                                        signupDetailsFragment.arguments = bundle
                                        supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, signupDetailsFragment).commit()
                                    } else {
                                        navigation.visibility = View.VISIBLE
                                        supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, HomeFragment()).commit()
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    private fun redirectLogin() {
        val loginActivity = Intent(this, LoginActivity::class.java)
        startActivity(loginActivity)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
