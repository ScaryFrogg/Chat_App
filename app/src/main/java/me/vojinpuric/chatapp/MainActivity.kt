package me.vojinpuric.chatapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUserMetadata
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import me.vojinpuric.chatapp.fragments.NewContactFragment
import me.vojinpuric.chatapp.fragments.NewMessageFragment
import me.vojinpuric.chatapp.fragments.ProfileFragment
import me.vojinpuric.chatapp.fragments.RecentMessagesFragment
import me.vojinpuric.chatapp.helpers.PROFILE_IMAGE_PLACEHOLDER
import me.vojinpuric.chatapp.helpers.RC_SIGN_IN
import me.vojinpuric.chatapp.model.User


class MainActivity : AppCompatActivity() {

    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController


    companion object {
        //Firebase variables
        val mFirebaseAuth by lazy { FirebaseAuth.getInstance() }
        val mFirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
        var refresh = false
        var currentUser: User? = null
        var isHomeFragment: Boolean = true

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.recentMessagesFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

        mAuthStateListener = FirebaseAuth.AuthStateListener {

            if (it.currentUser != null) {
                //signed in
                Log.e("signed in ", "called")
                Toast.makeText(this, "signed in", Toast.LENGTH_SHORT).show()
            } else {
                //signed out
                Log.e(" not signed in ", "called")
                Toast.makeText(this, "not signed in", Toast.LENGTH_SHORT).show()

                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                            listOf(
                                GoogleBuilder().build(),
                                EmailBuilder().build()
                            )
                        )
                        .build(),
                    RC_SIGN_IN
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            refresh = true
            if (resultCode == Activity.RESULT_CANCELED) finish()
            else {
                mFirebaseAuth.currentUser?.let { currentUser ->
                    val metadata: FirebaseUserMetadata = currentUser.metadata!!
                    if (metadata.creationTimestamp == metadata.lastSignInTimestamp) {
                        val email = if (currentUser.email != null) currentUser.email!! else ""
                        val ref =
                            FirebaseDatabase.getInstance().getReference("/users/${currentUser.uid}")
                        ref.setValue(
                            User(
                                currentUser.uid,
                                PROFILE_IMAGE_PLACEHOLDER,
                                email,
                                HashMap()
                            )
                        ).addOnSuccessListener { Log.e("saved user", "successfully") }
                    } else {
                        // TODO This is an existing user, show them a welcome back screen.
                    }
                }

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_new_message -> {
                navController.navigate(R.id.mi_new_message)
                isHomeFragment = false
                true
            }
            R.id.mi_profile -> {
                navController.navigate(R.id.mi_profile)
                isHomeFragment = false

                true
            }
            R.id.mi_new_contact -> {
                navController.navigate(R.id.mi_new_contact)
                isHomeFragment = false
                true
            }
            R.id.mi_sign_out -> {
                currentUser = null
                navController.navigate(R.id.recentMessagesFragment)
                AuthUI.getInstance().signOut(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        Log.e("MainActivity", "onResume called")
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
        mFirebaseAuth.currentUser?.let {
            mFirebaseDatabase.getReference("/users/${it.uid}")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        //not successfull
                        currentUser = null
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        currentUser = p0.getValue(User::class.java)
                    }
                })
        }
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.hamburger_navigation, menu)
        return super.onCreateOptionsMenu(menu)
    }


}
