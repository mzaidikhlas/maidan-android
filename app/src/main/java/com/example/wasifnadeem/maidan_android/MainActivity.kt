package com.example.wasifnadeem.maidan_android

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.example.wasifnadeem.maidan_android.controllers.BookingFragment
import com.example.wasifnadeem.maidan_android.controllers.FavoritesFragment
import com.example.wasifnadeem.maidan_android.controllers.HomeFragment
import com.example.wasifnadeem.maidan_android.controllers.SettingsFragment
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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

        supportFragmentManager.beginTransaction().add(R.id.fragment_layout, HomeFragment()).commit();

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
