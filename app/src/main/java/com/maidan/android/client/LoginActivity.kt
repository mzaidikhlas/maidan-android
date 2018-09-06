package com.maidan.android.client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import com.maidan.android.client.controllers.login.LoginFragment

class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        this.window.setSoftInputMode(SOFT_INPUT_ADJUST_PAN)
        supportFragmentManager.beginTransaction().replace(R.id.login_layout, LoginFragment()).commit()
    }
}
