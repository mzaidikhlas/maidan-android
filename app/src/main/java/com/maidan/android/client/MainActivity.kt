package com.maidan.android.client

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.maidan.android.client.controllers.BookingFragment
import com.maidan.android.client.controllers.FavoritesFragment
import com.maidan.android.client.controllers.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.maidan.android.client.controllers.login.SignupDetailsFragment
import com.maidan.android.client.models.User
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var dialog: AlertDialog? = null
    private lateinit var animation: AnimationDrawable

    private var loggedInUser: User? = null
    private val TAG = "MainActivity"

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_book-> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, BookingFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorites -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, FavoritesFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings-> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, SettingsFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "OnStart: Main")
        if (loggedInUser != null){
            Log.d(TAG, "hai idhr")
            Log.d(TAG, "User $loggedInUser")
            //redirect()
        }else {
            Log.d(TAG, "Nope")

            val user = mAuth.currentUser
            if (user == null) {
                redirectLogin()
            }else {
                showProgressDialog()
                user.getIdToken(true).addOnCompleteListener { task ->
                    Log.d(TAG, "Calls 1")
                    if (task.isSuccessful) {
                        val idToken = task.result.token
                        val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                        val call: Call<ApiResponse> = apiService.getUserInfoByEmail(idToken!!)
                        call.enqueue(object : Callback<ApiResponse> {
                            override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                                Log.d("UserApiError", t.toString())
                            }

                            override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                                if (response!!.isSuccessful) {
                                    if (response.body()!!.getStatusCode() == 200) {
                                        val payload = response.body()!!.getPayload()
                                        if (payload.isEmpty()) {

                                            navigation.setBackgroundResource(R.drawable.light_green_to_dark_green_1)
                                            val signupDetailsFragment = SignupDetailsFragment()

                                            val bundle = Bundle()
                                            bundle.putString("name", user.displayName)
                                            bundle.putString("email", user.email)
                                            bundle.putString("password", null)

                                            signupDetailsFragment.arguments = bundle
                                            hideProgressDialog()
                                            supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, signupDetailsFragment).commit()
                                        } else {
                                            hideProgressDialog()
                                            val gson = Gson()
                                            val jsonObject = gson.toJsonTree(payload[0].getData()).asJsonObject
                                            loggedInUser = gson.fromJson(jsonObject, User::class.java)
                                            (loggedInUser as User).setId(payload[0].getDocId())
                                            Log.d(TAG, "User $loggedInUser")
                                            supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, BookingFragment()).commit()
                                        }
                                    }
                                }else{
                                    Log.d(TAG, "Fetchning user response error ${response.errorBody()}")
                                }
                            }
                        })
                    }else{
                        Log.d(TAG, "user token generation error ${task.exception}")
                    }
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
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        mAuth = FirebaseAuth.getInstance()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.d(TAG, "OnSaveInstanceState: Main")
        outState!!.putSerializable("loggedInUser", loggedInUser)
    }

    private fun showProgressDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        val loader = dialogView.findViewById<ImageView>(R.id.loadingProgressbar)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()
        dialog!!.window.setLayout(400,400)
        dialog!!.window.setBackgroundDrawableResource(R.drawable.loader_styles)
        animation = loader.drawable as AnimationDrawable
        animation.start()
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressDialog(){
        animation.stop()
        dialog!!.dismiss()
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun getLoggedInUser(): User? {
        return this.loggedInUser
    }
}
