package com.vivekcorp.echoapplication.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.vivekcorp.echoapplication.R
import com.vivekcorp.echoapplication.fragment.AboutUsFragment
import com.vivekcorp.echoapplication.fragment.AllSongsFragment
import com.vivekcorp.echoapplication.fragment.FavoritesFragment
import com.vivekcorp.echoapplication.fragment.SettingsFragment

class DashboardActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar: Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView

    var previousMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frame)
        navigationView = findViewById(R.id.navigationView)
        setUpToolbar()

        openAllSongs()

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@DashboardActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener {

            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }

            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it

            when (it.itemId) {

                R.id.allSongs -> {
                    openAllSongs()
                    drawerLayout.closeDrawers()
                }

                R.id.favorites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FavoritesFragment()
                        )
                        .addToBackStack("FavoritesFragment")
                        .commit()

                    supportActionBar?.title = "Favorites songs"

                    drawerLayout.closeDrawers()
                }
                R.id.settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, SettingsFragment())

                        .commit()

                    supportActionBar?.title = "Settings"
                    drawerLayout.closeDrawers()
                }
                R.id.aboutUs -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, AboutUsFragment())

                        .commit()

                    supportActionBar?.title = "About us"
                    drawerLayout.closeDrawers()
                }
            }

            return@setNavigationItemSelectedListener true

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setUpToolbar() {

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Toolbar Title"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun openAllSongs() {

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, AllSongsFragment())
            .addToBackStack("AllSongsFragment")
            .commit()

        supportActionBar?.title = "All Songs"
        navigationView.setCheckedItem(R.id.allSongs)
    }

    override fun onBackPressed() {

        val frag = supportFragmentManager.findFragmentById(R.id.frame)

        when (frag) {
            !is AllSongsFragment -> openAllSongs()

            else -> super.onBackPressed()
        }
    }
}