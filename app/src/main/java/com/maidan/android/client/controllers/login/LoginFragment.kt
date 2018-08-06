package com.maidan.android.client.controllers.login


import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.facebook.*
import com.maidan.android.client.retrofit.ApiInterface
import com.maidan.android.client.retrofit.ApiResponse
import com.maidan.android.client.retrofit.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.maidan.android.client.MainActivity


import com.maidan.android.client.R
import java.util.*

private var TAG = "SignIn"

class LoginFragment : Fragment() {

    //layout
    private lateinit var loginBtnFB: Button
    private lateinit var loginBtnGoogle: Button
    private lateinit var loginBtnEmail: Button
    private lateinit var signupTxt: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var userEmailTxt: TextView
    private lateinit var passwordTxt: TextView

    //Firebase
    private lateinit var mAuth: FirebaseAuth

    //Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    //facebook
    private var callbackManager: CallbackManager? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_login, container, false)

        //layout
        loginBtnFB = view.findViewById(R.id.facebook);
        loginBtnGoogle = view.findViewById(R.id.google);
        loginBtnEmail = view.findViewById(R.id.login_btn)
        signupTxt = view.findViewById(R.id.signup)
        userEmailTxt = view.findViewById(R.id.signupUserEmail)
        passwordTxt = view.findViewById(R.id.signupPassword)
        progressBar = view.findViewById(R.id.progress_loader)

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
                signInWithGoogle()
            }
            //Facebook
            loginBtnFB.setOnClickListener {
                Log.d(TAG, "Facebook listener")
                signInWithFacebook()
            };
            signupTxt.setOnClickListener {
                fragmentManager!!.beginTransaction().replace(R.id.login_layout,SignupFragment()).commit()
            }
        }
        else
            updateUI(user)

        return view
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
            progressBar.visibility = View.VISIBLE

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

                                                        progressBar.visibility = View.INVISIBLE
                                                        updateUI(user)
                                                    }
                                                }
                                            })
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithEmailAndPasswordToken:failure", task.exception)
                                            Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                        }
                        else{
                            try {
                                progressBar.visibility = View.INVISIBLE
                                throw task.exception!!;
                            }
                            // if user enters wrong email.
                            catch (invalidEmail: FirebaseAuthInvalidUserException ) {
                                Log.d(TAG, "onComplete: invalid_email");
                            }
                            // if user enters wrong password.
                            catch (wrongPassword: FirebaseAuthInvalidCredentialsException ) {
                                Log.d(TAG, "onComplete: wrong_password");
                            }
                            catch (e: Exception ) {
                                Log.d(TAG, "onComplete: " + e.message);
                            }
                        }
                    }
            progressBar.visibility = View.INVISIBLE
        }
        catch (e: Exception){

            Log.d("UserException", "Yeh hai")
        }
    }

    //Google
    private fun signInWithGoogle(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context!!,gso)

        try {
            progressBar.visibility = View.VISIBLE
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
                .addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredentialGoogle:success")
                        val user = mAuth.currentUser
                        Log.d(TAG, user!!.email)
                        Log.d(TAG, "Display name ${user.displayName}")
                        Log.d(TAG, "Email ${user.isEmailVerified.toString()}")
                        Log.d(TAG, "Picture ${user.photoUrl.toString()}")

                        updateUI(user)
                    } else {
                        progressBar.visibility = View.INVISIBLE
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredentialGoogle:failure", task.exception)
                        Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_SHORT).show()
//                        updateUI(null)
                    }
                }
    }

    private fun updateUI(user: FirebaseUser) {
        progressBar.visibility = View.INVISIBLE
        val mainActivity = Intent(context, MainActivity::class.java)
        mainActivity.putExtra("loginUser", user)
        this.startActivity(mainActivity)
    }

    //Facebook sign in
    private fun signInWithFacebook(){
        // Callback registration
        progressBar.visibility = View.VISIBLE
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));

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
        progressBar.visibility = View.INVISIBLE
//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }
    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        Log.d(TAG, "handleFacebookAccessToken:$accessToken")

        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!) { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        val user = mAuth.currentUser;
                        Log.d(TAG, user!!.email)
                        Log.d(TAG, "Display name ${user.displayName}")
                        Log.d(TAG, "Display name ${user.phoneNumber}")
                        Log.d(TAG, "Email ${user.isEmailVerified.toString()}")
                        Log.d(TAG, "Picture ${user.photoUrl.toString()}")

                        updateUI(user);
                    } else {
                        progressBar.visibility = View.INVISIBLE
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(context!!, "User exist with google provider.",Toast.LENGTH_SHORT).show()
                        Toast.makeText(context!!, "Signing in with google provider.",Toast.LENGTH_SHORT).show()
                        signInWithGoogle()
                        //updateUI(null);
                    }

                }
        ;
    }
}
