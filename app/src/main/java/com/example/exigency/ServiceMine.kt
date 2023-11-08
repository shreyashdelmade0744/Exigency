package com.example.exigency

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import com.github.tbouron.shakedetector.library.ShakeDetector
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener

class ServiceMine : Service() {
    private var isRunning = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val manager: SmsManager = SmsManager.getDefault()
    private var myLocation: String = ""

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                sendSmsWithLocation()
                myLocation = if (location != null) {
                    location.latitude
                    location.longitude

                    "http://maps.google.com/maps?q=loc:${location.latitude},${location.longitude}"
                } else {
                    "http://maps.google.com/maps?q=loc:26.800425,81.023964"

//                    "Unable to Find Location :("

                }
            }

        ShakeDetector.create(this) {
            val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
            val ENUM = sharedPreferences.getString("ENUM", "NONE")
            if (!ENUM.equals("NONE", ignoreCase = true)) {
                manager.sendTextMessage(ENUM, null, "Im in Trouble!\nSending My Location :\n$myLocation", null, null)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals("STOP", ignoreCase = true)) {
            if (isRunning) {
                stopForeground(true)
                stopSelf()
            }
        } else {
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = NotificationChannel("MYID", "CHANNELFOREGROUND", NotificationManager.IMPORTANCE_DEFAULT)
                val m = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                m.createNotificationChannel(channel)

                val notification = Notification.Builder(this, "MYID")
                    .setContentTitle("Women Safety")
                    .setContentText("Shake Device to Send SOS")
                    .setSmallIcon(R.drawable.shield)
                    .setContentIntent(pendingIntent)
                    .build()
                startForeground(115, notification)
                isRunning = true
                return START_NOT_STICKY
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendSmsWithLocation() {
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val ENUM = sharedPreferences.getString("ENUM", "NONE")
        if (!ENUM.equals("NONE", ignoreCase = true)) {
            manager.sendTextMessage(ENUM, null, "Im in Trouble!\nSending My Location :\n$myLocation", null, null)


        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
