package com.maidan.android.client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseUser

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        val user = intent.extras.getSerializable("user") as FirebaseUser

        val emailTxt = findViewById<TextView>(R.id.welcomeEmail)
        val verifyTxt = findViewById<TextView>(R.id.welcomeVerify)
        emailTxt.text = user.email

        if (user.isEmailVerified) {
            verifyTxt.visibility = View.INVISIBLE
        }
    }
}
