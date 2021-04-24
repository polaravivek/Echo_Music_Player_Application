package com.vivekcorp.echoapplication

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.vivekcorp.echoapplication.activity.DashboardActivity

class MainActivity : AppCompatActivity() {

    var permissionsString = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
        android.Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (hasPermissions(this@MainActivity, *permissionsString)){
            println("true")
            //We have to ask for permissions
            ActivityCompat.requestPermissions(this@MainActivity, permissionsString, 131)
        }else{
            println("false")
            Handler().postDelayed({
                val startAct = Intent(this@MainActivity, DashboardActivity::class.java)
                startActivity(startAct)
                this.finish()
            },1000)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        println("dd")

        when(requestCode){
            131-> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                ) {
                    Handler().postDelayed({
                        val startAct = Intent(this@MainActivity, DashboardActivity::class.java)
                        startActivity(startAct)
                        this.finish()
                    }, 1000)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please grant all permissions to continue.",
                        Toast.LENGTH_SHORT
                    ).show()
                    this.finish()
                }
                return
            }
            else->{
                Toast.makeText(this@MainActivity, "Something went wrong.", Toast.LENGTH_SHORT).show()
                this.finish()
                return
            }
        }
    }

    fun hasPermissions (context: Context, vararg permissions: String): Boolean{
        var hasAllPermissions = false
        for (permission in permissions){
            val res = context.checkCallingOrSelfPermission(permission)
            println(permission)
            if (res != PackageManager.PERMISSION_GRANTED){
                hasAllPermissions = true
            }
        }
        return hasAllPermissions
    }
}