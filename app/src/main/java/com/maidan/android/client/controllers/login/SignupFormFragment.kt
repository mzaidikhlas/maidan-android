package com.maidan.android.client.controllers.login

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.*
import com.maidan.android.client.R
import com.maidan.android.client.models.User

class SignupFormFragment : Fragment() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_signup_form, container, false)
        //Firebase init
        mAuth = FirebaseAuth.getInstance()

        //Init Layout
        signupUsernameTxt = view.findViewById(R.id.signupUserName)
        signupUseremailTxt = view.findViewById(R.id.signupUserEmail)
        signupPasswordTxt = view.findViewById(R.id.signupPassword)
        signupConfirmPasswordTxt = view.findViewById(R.id.signupConfirmPassword)
        signupBtn = view.findViewById(R.id.signup_btn)

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
                                    Toast.makeText(context,
                                            "Verification email sent to " + user.email,
                                            Toast.LENGTH_SHORT).show()
                                    val signupDetailsFragment = SignupDetailsFragment()

                                    val bundle = Bundle()
                                    bundle.putString("name", signupUsernameTxt.text.toString())
                                    bundle.putString("email", signupUseremailTxt.text.toString())
                                    bundle.putString("password", signupPasswordTxt.text.toString())

                                    signupDetailsFragment.arguments = bundle
                                    fragmentManager!!.beginTransaction().replace(R.id.login_layout, signupDetailsFragment).commit()
                                }else{
                                    Log.e(TAG, "sendEmailVerification", task.exception);
                                    Toast.makeText(context,
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
                }else{
                    Toast.makeText(context,"Password and confirm password is not same", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(context,"All fields are required", Toast.LENGTH_LONG).show()
            }

        }
        return view
    }
}
