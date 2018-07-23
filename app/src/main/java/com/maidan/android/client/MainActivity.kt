package com.maidan.android.client

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.maidan.android.client.controllers.BookingFragment
import com.maidan.android.client.controllers.FavoritesFragment
import com.maidan.android.client.controllers.HomeFragment
import com.maidan.android.client.controllers.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //private lateinit var mAuth: FirebaseAuth

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

//    override fun onStart() {
//        super.onStart()
//        val currentUser: FirebaseUser = mAuth.currentUser!!
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        mAuth = FirebaseAuth.getInstance()

//        signInWithEmailPassword("waif.nadeem90@gmail.com", "passwordNeedToBeSecure")

        supportFragmentManager.beginTransaction().add(R.id.fragment_layout, HomeFragment()).commit();
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
