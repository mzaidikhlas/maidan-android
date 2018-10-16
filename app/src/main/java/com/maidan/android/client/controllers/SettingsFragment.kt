package com.maidan.android.client.controllers


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.util.EventLog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.maidan.android.client.LoginActivity
import com.maidan.android.client.R
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TableLayout
import com.google.firebase.auth.UserProfileChangeRequest
import com.maidan.android.client.MainActivity
import com.maidan.android.client.models.User
import com.squareup.picasso.Picasso


class SettingsFragment : Fragment() {

    //Layout
    private lateinit var profileImageView: ImageView
    private lateinit var profileNameTextView: TextView
    private lateinit var profileEmailTextView: TextView
    private lateinit var facebookLayout: ConstraintLayout
    private lateinit var twitterLayout: ConstraintLayout
    private lateinit var instagramLayout: ConstraintLayout
    private lateinit var contactUsLayout: ConstraintLayout
    private lateinit var privacyPolicyLayout: ConstraintLayout
    private lateinit var signoutLayout: ConstraintLayout

    private lateinit var loggedInUser: User

    //Firebase
    private lateinit var mAuth: FirebaseAuth

    //TAG
    private var TAG = "Signout"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        loggedInUser = (activity as MainActivity).getLoggedInUser()!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_settings, container, false)

        // Layout init
        profileImageView = view.findViewById(R.id.profileImage)
        profileNameTextView = view.findViewById(R.id.profileName)
        profileEmailTextView = view.findViewById(R.id.profileEmail)
        facebookLayout = view.findViewById(R.id.facebookLayout)
        twitterLayout = view.findViewById(R.id.twitter)
        instagramLayout = view.findViewById(R.id.instagram)
        contactUsLayout = view.findViewById(R.id.contactUs)
        privacyPolicyLayout = view.findViewById(R.id.privacyPolicy)
        signoutLayout = view.findViewById(R.id.signout)

        //Populating layout
        if (loggedInUser.getDisplayAvatar() != null){
            Picasso.get().load(loggedInUser.getDisplayAvatar())
                    .fit()
                    .centerInside()
                    .into(profileImageView)
        }
        profileNameTextView.text = loggedInUser.getName()
        profileEmailTextView.text = loggedInUser.getEmail()

        facebookLayout.setOnClickListener {
            Log.d(TAG, "Facebook")

            val intent = openFacebook(context!!)
            startActivity(intent)
        }

        twitterLayout.setOnClickListener {
            Log.d(TAG, "Twitter")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/maidan_pk"))
            startActivity(intent)
        }

        instagramLayout.setOnClickListener {
            Log.d(TAG, "Instagram")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/maidan_pk/"))
            startActivity(intent)
        }

        contactUsLayout.setOnClickListener {
            Log.d(TAG, "Contact Us")
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:03214658283")
            startActivity(intent)
        }

        privacyPolicyLayout.setOnClickListener {
            Log.d(TAG, "Privacy Policy")
        }

        signoutLayout.setOnClickListener{
            Log.d(TAG, "Sign out click listner")
            if (mAuth.currentUser != null) {
                mAuth.signOut()
                val loginActivity = Intent(activity, LoginActivity::class.java)
                activity!!.startActivity(loginActivity)
            }
        }

        return view
    }



    private fun openFacebook(context: Context): Intent{
        return try{
            context.packageManager.getPackageInfo("com.facebook.katana",0)
            Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/1886326514993833"))
        }
        catch (e: Exception){
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/maidansports/"))
        }
    }

//    private fun AddEvent(event: Event) {
//
////        val timeMS = event.GetTime()
////        val timeM = Utils.GetTimeAsMinutes(timeMS)
////        val lengthM = event.GetEventLength()
////
////        val fromPixels = DpsToPixels(timeM)
////        val heightPixels = DpsToPixels(lengthM)
////
////        val table = _inflater.inflate(R.layout.calendar_event, null) as TableLayout
////        val paramsTable = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
////        paramsTable.topMargin = fromPixels
////        paramsTable.height = heightPixels
////        table.layoutParams = paramsTable
////
////        val indicator = table.findViewById<View>(R.id.indicator)
////        SetIndicatorContent(event, indicator)
////
////        _container.addView(table, paramsTable)
////        _dayViews.add(table)
//    }
}
