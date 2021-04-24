package com.vivekcorp.echoapplication

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vivekcorp.echoapplication.activity.DashboardActivity

class MainActivity : AppCompatActivity() {

    var permissionsString = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
        android.Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermissions()
    }

    private fun setupPermissions(){
        if (hasPermissions(this, *permissionsString)){
            ActivityCompat.requestPermissions(this@MainActivity,
                permissionsString,
                101)
        }else{
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

        when(requestCode){
            101->{
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED
                    || grantResults[2] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("permission", "Permission has been denied by user")

                }else{
                    Handler().postDelayed({
                        val startAct = Intent(this@MainActivity, DashboardActivity::class.java)
                        startActivity(startAct)
                        this.finish()
                    },1000)

                    Log.i("permission", "Permission has been granted by user")
                }
            }
        }
    }

    fun hasPermissions (context: Context, vararg permissions: String): Boolean{
        var hasAllPermissions = false
        for (permission in permissions){
            val res = context.checkCallingOrSelfPermission(permission)
            if (res == PackageManager.PERMISSION_GRANTED){
                hasAllPermissions = true
            }
        }
        return hasAllPermissions
    }
}