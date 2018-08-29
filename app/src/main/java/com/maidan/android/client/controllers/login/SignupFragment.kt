package com.maidan.android.client.controllers.login

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.maidan.android.client.MainActivity
import com.maidan.android.client.R

import java.util.*

class SignupFragment : Fragment() {

    private var TAG = "Signup"

    //layout
    private lateinit var signupBtn: Button
    private lateinit var loginBtnFB: Button
    private lateinit var loginBtnGoogle: Button
    private lateinit var progressBar: ProgressBar

    //Firebase
    private lateinit var mAuth: FirebaseAuth

    //Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    //facebook
    private var callbackManager: CallbackManager? = null

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        if (user != null)
            updateUI(user)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_signup, container, false)
        loginBtnGoogle = view.findViewById(R.id.google_btn)
        loginBtnFB = view.findViewById(R.id.facebook_btn)
        signupBtn = view.findViewById(R.id.signup_btn)
        progressBar = view.findViewById(R.id.signupProgressBar2)

        mAuth = FirebaseAuth.getInstance()

        //Google
        loginBtnGoogle.setOnClickListener {
            Log.d(TAG, "Google listener")
            progressBar.visibility = View.VISIBLE
            signupBtn.isEnabled = false
            loginBtnFB.isEnabled = false
            loginBtnGoogle.isEnabled = false
            signInWithGoogle()
        }
        //Facebook
        loginBtnFB.setOnClickListener {
            Log.d(TAG, "Facebook listener")
            progressBar.visibility = View.VISIBLE
            signupBtn.isEnabled = false
            loginBtnFB.isEnabled = false
            loginBtnGoogle.isEnabled = false
            signInWithFacebook()
        }
        signupBtn.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.login_layout, SignupFormFragment()).commit()
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Facebook
        // Pass the activity result back to the Facebook SDK
        if (callbackManager != null)
            callbackManager!!.onActivityResult(requestCode, resultCode, data)

        //Google
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101){
            try {
                Toast.makeText(context,"Activity result google task", Toast.LENGTH_LONG).show()
                // Google Sign In was successful, authenticate with Firebase
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                progressBar.visibility = View.INVISIBLE
                signupBtn.isEnabled = true
                loginBtnFB.isEnabled = true
                loginBtnGoogle.isEnabled = true
                Toast.makeText(context,"Activity result catch $e", Toast.LENGTH_LONG).show()
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                throw e
            }
        }
    }

    //Google
    private fun signInWithGoogle(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(context!!,gso)

        try {
            Log.d(TAG, "Try")
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, 101)
        }
        catch (e: Exception){
            progressBar.visibility = View.INVISIBLE
            signupBtn.isEnabled = true
            loginBtnFB.isEnabled = true
            loginBtnGoogle.isEnabled = true

            Log.d(TAG, "Exception ${e.message}")
            throw e
        }
    }
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account!!.id)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context,"Google sign in ", Toast.LENGTH_SHORT).show()
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredentialGoogle:success")
                        val user = mAuth.currentUser
                        Log.d(TAG, user!!.email)
                        Log.d(TAG, "Display name ${user.displayName}")
                        Log.d(TAG, "Email ${user.isEmailVerified}")
                        Log.d(TAG, "Picture ${user.photoUrl.toString()}")
                        Toast.makeText(context,"Google sign in user $user", Toast.LENGTH_LONG).show()
                        mGoogleSignInClient.revokeAccess().addOnCompleteListener { task2 ->
                            if (task2.isSuccessful)
                                updateUI(user)
                        }

                    } else {
                        progressBar.visibility = View.INVISIBLE
                        signupBtn.isEnabled = true
                        loginBtnFB.isEnabled = true
                        loginBtnGoogle.isEnabled = true
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredentialGoogle:failure", task.exception)
                        Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                        throw task.exception!!
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
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"))

        LoginManager.getInstance().registerCallback(callbackManager,
                object: FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        Log.d(TAG, "facebook:onSuccess:$result")
                        handleFacebookAccessToken(result!!.accessToken)
                    }

                    override fun onCancel() {
                        Log.d(TAG, "facebook:onCancel")
                        progressBar.visibility = View.INVISIBLE
                        signupBtn.isEnabled = true
                        loginBtnFB.isEnabled = true
                        loginBtnGoogle.isEnabled = true
                        Toast.makeText(context, "Facebook:onCancel", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: FacebookException?) {
                        Log.d(TAG, "facebook:onError", error)
                        progressBar.visibility = View.INVISIBLE
                        signupBtn.isEnabled = true
                        loginBtnFB.isEnabled = true
                        loginBtnGoogle.isEnabled = true
                        Toast.makeText(context, "Facebook:onError", Toast.LENGTH_SHORT).show()
                    }
                })
    }
    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        Log.d(TAG, "handleFacebookAccessToken:$accessToken")

        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!) { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val user = mAuth.currentUser
                        LoginManager.getInstance().logOut()
                        updateUI(user!!)
                    } else {
                        progressBar.visibility = View.INVISIBLE
                        signupBtn.isEnabled = true
                        loginBtnFB.isEnabled = true
                        loginBtnGoogle.isEnabled = true
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(context!!, "User exist with google provider.",Toast.LENGTH_SHORT).show()
                        Toast.makeText(context!!, "Signing in with google provider.",Toast.LENGTH_SHORT).show()
                        signInWithGoogle()
                    }

                }
    }
}
