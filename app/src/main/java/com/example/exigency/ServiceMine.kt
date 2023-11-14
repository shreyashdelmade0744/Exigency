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
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.github.tbouron.shakedetector.library.ShakeDetector
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener

class ServiceMine : Service() {

    val channelId = "exigency"
    val channelName = "exigency"
    private var isRunning = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val manager: SmsManager = SmsManager.getDefault()
    private var myLocation: String = ""

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
//                Toast.makeText(applicationContext, "SMS sent successfully", Toast.LENGTH_SHORT).show()

                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(channelId, channelName, importance)
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)

                // Create a notification
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.shield)
                    .setContentTitle("Message Sent")
                    .setContentText("Message sent successfully")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

// Post the notification
                val notificationId = 1  // Unique ID for the notification
                notificationManager.notify(notificationId, notificationBuilder.build())

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel("MYID", "CHANNELFOREGROUND", NotificationManager.IMPORTANCE_DEFAULT)
                val m = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                m.createNotificationChannel(channel)

                val notification = Notification.Builder(this, "MYID")
                    .setContentTitle("Exigency")
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
