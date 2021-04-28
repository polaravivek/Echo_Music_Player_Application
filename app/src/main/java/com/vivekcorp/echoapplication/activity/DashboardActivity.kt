package com.vivekcorp.echoapplication.activity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.vivekcorp.echoapplication.R
import com.vivekcorp.echoapplication.fragment.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var toolbar: Toolbar
    private lateinit var frameLayout: FrameLayout
    private lateinit var navigationView: NavigationView

    private lateinit var notificationManager: NotificationManager

    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "com.vivekcorp.notificationactivity"
    private val description = " Test notification"

    private var previousMenuItem: MenuItem? = null

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

                    navigationView.setCheckedItem(R.id.allSongs)

                    drawerLayout.closeDrawers()
                }
                R.id.settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, SettingsFragment())
                        .addToBackStack("SettingFragment")
                        .commit()

                    navigationView.setCheckedItem(R.id.allSongs)
                    drawerLayout.closeDrawers()
                }
                R.id.aboutUs -> {

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, AboutUsFragment())
                        .addToBackStack("AboutUsFragment")
                        .commit()

                    navigationView.setCheckedItem(R.id.allSongs)
                    drawerLayout.closeDrawers()
                }
            }

            return@setNavigationItemSelectedListener true
        }

        val intent =  Intent(this,DashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        val contentView = RemoteViews(packageName,R.layout.notification_layout)
        contentView.setTextViewText(R.id.notification_title,"A track is playing in the background.")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId,description,NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this,channelId)
                .setContent(contentView)
                .setSmallIcon(R.drawable.echo_logo)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.echo_logo))
                .setContentIntent(pendingIntent)
        }else{
            builder = Notification.Builder(this)
                .setContent(contentView)
                .setContentTitle("A track is playing in the background.")
                .setSmallIcon(R.drawable.echo_logo)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.echo_logo))
                .setContentIntent(pendingIntent)
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
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun openAllSongs() {

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, AllSongsFragment())
            .addToBackStack("AllSongsFragment")
            .commit()

        navigationView.setCheckedItem(R.id.allSongs)
    }

    override fun onBackPressed() {
        val fragments = fragmentManager.backStackEntryCount
        if (fragments == 1) {
        }
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        try {
            notificationManager.cancel(1234)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            if (NowPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                notificationManager.notify(1234, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            notificationManager.cancel(1978)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}