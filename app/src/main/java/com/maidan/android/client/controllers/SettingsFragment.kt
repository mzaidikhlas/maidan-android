package com.maidan.android.client.controllers


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.maidan.android.client.LoginActivity
import com.maidan.android.client.R

class SettingsFragment : Fragment() {

    //Layout
    private lateinit var signout: TextView

    //Firebase
    private lateinit var mAuth: FirebaseAuth

    //TAG
    private var TAG = "Signout"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_settings, container, false)
        mAuth = FirebaseAuth.getInstance()
        signout = view.findViewById(R.id.settingTxt)
        signout.setOnClickListener{
            Log.d(TAG, "Sign out click listner")
            if (mAuth.currentUser != null) {
                mAuth.signOut()
                val loginActivty = Intent(activity, LoginActivity::class.java)
                activity!!.startActivity(loginActivty)
            }
        }
        return view
    }
}
