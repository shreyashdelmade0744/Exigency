package com.example.exigency

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var alarm : ToggleButton

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("MySharedPref", MODE_PRIVATE)
    }

    override fun onResume() {
        super.onResume()
        val ENUM = sharedPreferences.getString("ENUM", "NONE")
        if (ENUM.equals("NONE", ignoreCase = true)) {
            startActivity(Intent(this, RegisterNumberActivity::class.java))
        } else {
            val textView = findViewById<TextView>(R.id.textNum)
            textView.text = "SOS Will Be Sent To\n$ENUM"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarm = findViewById(R.id.Alarm)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("MYID", "CHANNELFOREGROUND", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }



        var mp = MediaPlayer.create(applicationContext, R.raw.siren)
        mp.isLooping = true

        alarm.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                 mp = MediaPlayer.create(applicationContext, R.raw.siren)
                 mp.isLooping = true
                 mp.start()
             }
            else{
                mp.stop()
             }
        }

    }

    private val multiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
        result.entries.forEach { entry ->
            if (!entry.value) {
                showPermissionSnackbar(entry.key)
            }
        }
    }

    private fun showPermissionSnackbar(permission: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), "Permission Must Be Granted!", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("Grant Permission") {
            multiplePermissions.launch(arrayOf(permission))
            snackbar.dismiss()
        }
        snackbar.show()
    }


    fun stopService(view: View) {
        val notificationIntent = Intent(this, ServiceMine::class.java)
        notificationIntent.action = "stop"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(notificationIntent)
            Snackbar.make(findViewById(android.R.id.content), "Service Stopped!", Snackbar.LENGTH_LONG).show()
        }
    }

    fun startServiceV(view: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            val notificationIntent = Intent(this, ServiceMine::class.java)
            notificationIntent.action = "Start"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(notificationIntent)
                Snackbar.make(findViewById(android.R.id.content), "Service Started!", Snackbar.LENGTH_LONG).show()
            }
        } else {
            multiplePermissions.launch(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    fun change(view: View){
        val intent = Intent(this,RegisterNumberActivity::class.java)
        startActivity(intent)
    }



}
