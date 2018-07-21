package com.maidan.android.client

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import java.util.*
import com.facebook.FacebookException
import com.facebook.FacebookCallback
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.*


class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: LoginButton

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var user: FirebaseUser? = null

    //Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var TAG = "SignIn"

    //facebook
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser

        if (user == null){
            //Facebook login
            FacebookSdk.sdkInitialize(applicationContext);
            AppEventsLogger.activateApp(this);

            callbackManager = CallbackManager.Factory.create()
            val EMAIL = "email"

            loginButton = findViewById<LoginButton>(R.id.signInUsingFacebook)
            loginButton.setReadPermissions(Arrays.asList(EMAIL))
            // If you are using in a fragment, call loginButton.setFragment(this);

            loginButton.setOnClickListener {
                Log.d(TAG, "yo")
                signInWithFacebook()
            }
        }

        //Google Sign In
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build()
//        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)
//        signInWithGoogle()

    }

    //Google sign in
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == 101){
//            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                val account = task.getResult(ApiException::class.java)
//                firebaseAuthWithGoogle(account)
//            } catch (e: ApiException) {
//                // Google Sign In failed, update UI appropriately
//                Log.w(TAG, "Google sign in failed", e)
//                // ...
//            }
//
//
//        }
    }

    //Google
    private fun signInWithGoogle(){
        try {
            Log.d(TAG, "Try")
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, 101)
        }
        catch (e: Exception){
            Log.d(TAG, e.toString())
        }
    }
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account!!.id)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val user = mAuth.currentUser
                        //updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
//                        Snackbar.make(this, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
//                        updateUI(null)
                    }

                    // ...
                }

    }

    //Sign in through email and password
    private fun signInWithEmailPassword(email: String, password: String) {
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        Log.d("UserException", "NotCompleted")
                        if (task.isSuccessful){
                            Log.d("UserException", "Completed")
                            val user: FirebaseUser = mAuth.currentUser!!
                            user.getIdToken(true)
                                    .addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful) {
                                            val idToken = task2.result.token
                                            Log.d("User", idToken)

                                            val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)

                                            val call: Call<ApiResponse> = apiService.testing(idToken!!)

                                            call.enqueue(object: Callback<ApiResponse> {
                                                override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                                                    Log.d("UserApiError", t.toString())
                                                }

                                                override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                                                    if (response!!.isSuccessful){
                                                        Log.d("UserApiSuccess", response.body().toString())
                                                    }
                                                }

                                            })
                                        } else {
                                            // Handle error -> task.getException();
                                            Log.d("UserTokenError1", "Error")
                                        }
                                    }
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

    //Facebook sign in
    private fun signInWithFacebook(){
        // Callback registration
        loginButton.registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        // App code
                        Log.d(TAG, loginResult.toString())
                        handleFacebookToken(loginResult.accessToken)
                    }

                    override fun onCancel() {
                        // App code
                        Log.d(TAG, "On Cancel")
                    }

                    override fun onError(exception: FacebookException) {
                        // App code
                        Log.d(TAG, exception.toString())
                    }
                });

//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }
    private fun handleFacebookToken(accessToken: AccessToken?) {
        val credential: AuthCredential = FacebookAuthProvider.getCredential(accessToken!!.token)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Log.d(TAG, "signInWithCredential:success")
                        user = mAuth.currentUser

                        Log.d(TAG, user!!.email)
                    }else{
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                    }
                }
    }

}
