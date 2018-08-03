package com.maidan.android.client

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.maidan.android.client.models.User
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.RetrofitClient
import retrofit2.Call
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException






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

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    //Retrofit


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_email)

        //Firebase init
        mAuth = FirebaseAuth.getInstance()

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
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = mAuth.currentUser
                            user!!.sendEmailVerification().addOnCompleteListener { task1 ->
                                if (task1.isSuccessful){
                                    Toast.makeText(this,
                                            "Verification email sent to " + user.email,
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    Log.e(TAG, "sendEmailVerification", task.exception);
                                    Toast.makeText(this,
                                            "Failed to send verification email.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else{
                            try {
                                throw task.exception!!
                            } catch (weakPassword: FirebaseAuthWeakPasswordException) {
                                Log.d(TAG, "onComplete: weak_password")

                                // TODO: take your actions!
                            } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                                Log.d(TAG, "onComplete: malformed_email")

                                // TODO: Take your action
                            } catch (existEmail: FirebaseAuthUserCollisionException) {
                                Log.d(TAG, "onComplete: exist_email")

                                // TODO: Take your action
                            } catch (e: Exception) {
                                Log.d(TAG, "onComplete: " + e.message)
                            }
                        }
                    }


//                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
//                        if (task.isSuccessful){
//                            Log.d(TAG, "createUserWithEmail:success")
//                            val user = mAuth.currentUser
//                            user!!.sendEmailVerification().addOnCompleteListener { task1 ->
//                                if (task1.isSuccessful){
//                                    Toast.makeText(this,
//                                            "Verification email sent to " + user.email,
//                                            Toast.LENGTH_SHORT).show();
//                                }else{
//                                    Log.e(TAG, "sendEmailVerification", task.exception);
//                                    Toast.makeText(this,
//                                            "Failed to send verification email.",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        }else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                            //updateUI(null);
//                        }
//
//                    }
//                    val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
//                    val call: Call<ApiResponse> = apiService.createUser(user)
                }else{
                    Toast.makeText(this,"Password and confirm password is not same", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this,"All fields are required", Toast.LENGTH_LONG).show()
            }

        }

    }
}
