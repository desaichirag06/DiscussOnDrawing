package com.chirag.discussondrawing

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.chirag.discussondrawing.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var firebaseAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private lateinit var currentUserId: String
    private var mRootRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        appBarConfiguration = AppBarConfiguration
            .Builder(R.id.homeFragment)
            .build()

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null as DrawerLayout?)
    }

    override fun onBackPressed() {
        if (appBarConfiguration.topLevelDestinations.contains(navController.currentDestination?.id))
            finish()
        else
            onSupportNavigateUp()

    }

    override fun onStart() {
        super.onStart()
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth?.currentUser

        mRootRef = FirebaseDatabase.getInstance().reference

        Log.e("mRootRef", "==> $mRootRef")

        if (currentUser == null) {
            firebaseAuth!!.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        currentUser = firebaseAuth?.currentUser

                        currentUserId = currentUser?.uid.toString()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
        } else {
            currentUserId = currentUser?.uid.toString()
        }

    }
}