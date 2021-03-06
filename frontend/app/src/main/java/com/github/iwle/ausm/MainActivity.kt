package com.github.iwle.ausm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.github.iwle.ausm.adapter.TabPagerAdapter
import com.github.iwle.ausm.model.User
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.rtchagas.pingplacepicker.PingPlacePicker
import kotlin.math.abs

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var toolbar: Toolbar
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var floatingActionButton: ExtendedFloatingActionButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var logOutAlertDialog: AlertDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var isFabEnabled = false
    private val pingActivityRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.tab_view_pager)
        toolbar = findViewById(R.id.toolbar)
        appBarLayout = findViewById(R.id.app_bar_layout)
        floatingActionButton = findViewById(R.id.extended_floating_action_button)
        navigationView = findViewById(R.id.navigation_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        firebaseAuth = FirebaseAuth.getInstance()

        initialiseTabPagerAdapter()
        initialiseFloatingActionButton()
        initialiseNavigationDrawer()
        initialiseAlertDialog()
    }

    private fun initialiseTabPagerAdapter() {
        val tabFragmentList = listOf(MapFragment(), ReviewFragment())
        val tabNameList = listOf(getString(R.string.tab_gps), getString(R.string.tab_reviews))
        val tabPagerAdapter =
            TabPagerAdapter(this, tabFragmentList)
        viewPager.adapter = tabPagerAdapter
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                // Show app bar when tab is selected
                appBarLayout.setExpanded(true, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Do nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Show app bar when tab is reselected
                appBarLayout.setExpanded(true, true)
            }
        })
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                toolbar.title = tabNameList[position]
                // Show extended FAB only on Reviews tab
                when(position) {
                    0 -> updateFabStatus(false)
                    1 -> updateFabStatus(true)
                }
            }
        })
        // Disable swiping of view pager
        viewPager.isUserInputEnabled = false
        // Set initial toolbar title
        toolbar.title = tabNameList[0]
    }

    private fun initialiseFloatingActionButton() {
        // Open Place Picker on click
        floatingActionButton.setOnClickListener {
            showPlacePicker()
        }
        // Hide extended FAB when toolbar is collapsed
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if(abs(verticalOffset) - appBarLayout!!.totalScrollRange == 0) {
                // Collapsed
                hideFab()
            } else {
                // Expanded
                showFab()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initialiseNavigationDrawer() {
        // Attach navigation drawer
        setSupportActionBar(toolbar)
        val actionBarDrawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
            }
        }
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        // Update navigation drawer header
        val view: View = navigationView.getHeaderView(0)
        val nameTextView: TextView = view.findViewById(R.id.name_text_view)
        val emailTextView: TextView = view.findViewById(R.id.email_text_view)
        val firebaseUser: FirebaseUser = firebaseAuth.currentUser!!
        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(firebaseUser.uid).get().addOnSuccessListener {
            val user: User? = it.toObject(User::class.java)
            if(user != null) {
                nameTextView.text = user.firstName + " " + user.lastName
            }
        }
        emailTextView.text = firebaseUser.email
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout_item -> logOutAlertDialog.show()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun initialiseAlertDialog() {
        logOutAlertDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.alert_logout_title)
            .setMessage(R.string.alert_logout_message)
            .setPositiveButton(R.string.alert_logout_positive) { dialog, which ->
                firebaseAuth.signOut()
                startActivity(Intent(this, AuthenticateActivity::class.java))
                // Destroy MainActivity
                finish()
            }
            .setNegativeButton(R.string.alert_logout_negative) { dialog, which ->
                dialog.cancel()
            }
            .create()
    }

    private fun updateFabStatus(status: Boolean) {
        isFabEnabled = status
        if(isFabEnabled) {
            showFab()
        } else {
            hideFab()
        }
    }

    private fun showFab() {
        if(isFabEnabled) {
            floatingActionButton.show()
        }
    }

    private fun hideFab() {
        floatingActionButton.hide()
    }

    private fun showPlacePicker() {
        val builder = PingPlacePicker.IntentBuilder()
        builder.setAndroidApiKey(getString(R.string.place_picker_places_key)).setMapsApiKey(getString(R.string.place_picker_maps_key))

        try {
            val placeIntent = builder.build(this)
            startActivityForResult(placeIntent, pingActivityRequestCode)
        } catch(ex: Exception) {
            // TODO: toast("Google Play Services is not Available")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == pingActivityRequestCode && resultCode == Activity.RESULT_OK) {
            val place: Place? = PingPlacePicker.getPlace(data!!)
            val intent = Intent(this, AddReviewActivity::class.java)
            intent.putExtra("place", place)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        // Intercept back press and close drawer layout if open
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}