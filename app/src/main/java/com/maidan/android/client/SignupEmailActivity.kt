package com.maidan.android.client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.maidan.android.client.models.User

class SignupEmailActivity : AppCompatActivity() {

    //Layout
    private lateinit var signupUsernameTxt: EditText
    private lateinit var signupUseremailTxt: EditText
    private lateinit var signupPasswordTxt: EditText
    private lateinit var signupConfirmPasswordTxt: EditText
    private lateinit var signupBtn: Button

    //Model object
    private lateinit var user: User

    //TAG
    private val TAG = "SignupEmail"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_email)

        //Init Layout
        signupUsernameTxt = findViewById(R.id.signupUserName)
        signupUseremailTxt = findViewById(R.id.signupUserEmail)
        signupPasswordTxt = findViewById(R.id.signupPassword)
        signupConfirmPasswordTxt = findViewById(R.id.signupConfirmPassword)
        signupBtn = findViewById(R.id.signup_btn)

        signupBtn.setOnClickListener {
            Log.d(TAG, "Signup click lisenter")

            val username = signupUsernameTxt.text.toString()
            val email = signupUseremailTxt.text.toString()
            val password = signupPasswordTxt.text.toString()
            val confirmPassword = signupConfirmPasswordTxt.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()){
                if (password == confirmPassword){

                }else{
                    Toast.makeText(this,"Password and confirm password is not same", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this,"All fields are required", Toast.LENGTH_LONG).show()
            }

        }

    }
}
