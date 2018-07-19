package com.example.wasifnadeem.maidan_android

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.wasifnadeem.maidan_android.controllers.BookingFragment
import com.example.wasifnadeem.maidan_android.controllers.FavoritesFragment
import com.example.wasifnadeem.maidan_android.controllers.HomeFragment
import com.example.wasifnadeem.maidan_android.controllers.SettingsFragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var firebase: FirebaseApp? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
//        firebase = FirebaseApp.initializeApp(this)

        login("waif.nadeem90@gmail.com", "passwordNeedToBeSecure")

        supportFragmentManager.beginTransaction().add(R.id.fragment_layout, HomeFragment()).commit();

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun login(email: String, password: String) {
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        Log.d("UserException", "NotCompleted")
                        if (task.isSuccessful){
                            Log.d("UserException", "Completed")

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
}
