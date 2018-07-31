package com.maidan.android.client

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.facebook.*
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.facebook.login.LoginResult
import com.facebook.FacebookException
import com.facebook.FacebookCallback
import com.google.firebase.auth.*
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import java.util.*

class LoginActivity : AppCompatActivity() {

    private var TAG = "SignIn"

    //layout
    private lateinit var loginBtnFB: Button
    private lateinit var loginBtnGoogle: Button
    private lateinit var loginBtnEmail: Button
    private lateinit var signupTxt: TextView

    private lateinit var userEmailTxt: TextView
    private lateinit var passwordTxt: TextView

    //Firebase
    private lateinit var mAuth: FirebaseAuth

    //Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    //facebook
    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //layout
        loginBtnFB = findViewById(R.id.facebook);
        loginBtnGoogle = findViewById(R.id.google);
        loginBtnEmail = findViewById(R.id.login_btn)
        signupTxt = findViewById(R.id.signup)
        userEmailTxt = findViewById(R.id.userEmail)
        passwordTxt = findViewById(R.id.password)

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser

        if (user == null){
            //Login through email password
            loginBtnEmail.setOnClickListener {
                if (userEmailTxt.text.isNotEmpty()){
                    if (passwordTxt.text.isNotEmpty()){
                        val email = userEmailTxt.text.toString()
                        val password = passwordTxt.text.toString()
                        Log.d(TAG, "Email: $email, Password: $password")
                        signInWithEmailPassword(email, password)
                    }
                    else
                        Log.d(TAG, "Password field is required")
                }
                else
                    Log.d(TAG, "Both fields are required")
            }
            //Google
            loginBtnGoogle.setOnClickListener {
                Log.d(TAG, "Google listener")
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(application,gso)
                signInWithGoogle()
            }
            //Facebook
            loginBtnFB.setOnClickListener {
                Log.d(TAG, "Facebook listener")
                callbackManager = CallbackManager.Factory.create();
                signInWithFacebook()
            };
            signupTxt.setOnClickListener {
                val signupIntent = Intent(this,SignupActivity::class.java)
                this.startActivity(signupIntent)
            }
        }
        else
            updateUI(user)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Facebook
        // Pass the activity result back to the Facebook SDK
        if (callbackManager != null)
            callbackManager!!.onActivityResult(requestCode, resultCode, data);

        //Google
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
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
                            Log.d(TAG,user.toString())
                            user.getIdToken(true)
                                    .addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful) {
                                            val idToken = task2.result.token
                                            Log.d("User", idToken)

                                            val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)

                                            val call: Call<ApiResponse> = apiService.getUserInfoByEmail(idToken!!)

                                            call.enqueue(object: Callback<ApiResponse> {
                                                override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                                                    Log.d("UserApiError", t.toString())
                                                }

                                                override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                                                    if (response!!.isSuccessful){
                                                        Log.d("UserApiSuccess", response.body().toString())
                                                        updateUI(user)
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
                        Log.d(TAG, "signInWithCredentialGoogle:success")
                        val user = mAuth.currentUser
                        Log.d(TAG, user!!.email)
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredentialGoogle:failure", task.exception)
//                        Snackbar.make(this, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
//                        updateUI(null)
                    }

                    // ...
                }
    }

    private fun updateUI(user: FirebaseUser) {
        val mainActivity = Intent(this,MainActivity::class.java)
        mainActivity.putExtra("loginUser", user)
        this.startActivity(mainActivity)
    }

    //Facebook sign in
    private fun signInWithFacebook(){
        // Callback registration
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_photos", "email", "public_profile", "user_posts"));
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));

        LoginManager.getInstance().registerCallback(callbackManager,
                object: FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        Log.d(TAG, "facebook:onSuccess:$result")
                        handleFacebookAccessToken(result!!.accessToken)
                    }

                    override fun onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                    }

                    override fun onError(error: FacebookException?) {
                        Log.d(TAG, "facebook:onError", error);
                    }
                });
//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }
    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        Log.d(TAG, "handleFacebookAccessToken:$accessToken")

        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) {task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        val user = mAuth.currentUser;
                        Log.d(TAG, user!!.email)

                        //updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception);
                        Toast.makeText(applicationContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        //updateUI(null);
                    }

                }
        ;
    }
}
