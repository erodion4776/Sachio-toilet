
package com.sashio.ops.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sashio.ops.data.BookingLiveLocation
import com.sashio.ops.data.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Instant

class StaffLocationService : Service() {

    companion object {
        const val EXTRA_BOOKING_ID = "booking_id"
        const val ACTION_STOP_TRACKING =
            "com.sashio.ops.action.STOP_TRACKING"

        private const val CHANNEL_ID = "staff_live_location"
        private const val NOTIFICATION_ID = 7101
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var bookingId: String? = null
    private var trackingStarted = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            uploadLocation(location)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (intent?.action == ACTION_STOP_TRACKING) {
            stopTracking()
            return START_NOT_STICKY
        }

        val requestedBookingId =
            intent?.getStringExtra(EXTRA_BOOKING_ID)

        if (requestedBookingId.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        bookingId = requestedBookingId

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (trackingStarted) return

        val finePermission = checkSelfPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission = checkSelfPermission(
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!finePermission && !coarsePermission) {
            stopTracking()
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        )
            .setMinUpdateIntervalMillis(5_000L)
            .setMinUpdateDistanceMeters(5f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            trackingStarted = true
        } catch (_: SecurityException) {
            stopTracking()
        }
    }

    private fun uploadLocation(location: Location) {
        val currentBookingId = bookingId ?: return

        serviceScope.launch {
            try {
                val user = SupabaseProvider.client.auth
                    .currentUserOrNull() ?: return@launch

                val liveLocation = BookingLiveLocation(
                    booking_id = currentBookingId,
                    staff_id = user.id,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy_m = if (location.hasAccuracy()) {
                        location.accuracy.toDouble()
                    } else {
                        null
                    },
                    heading = if (location.hasBearing()) {
                        location.bearing.toDouble()
                    } else {
                        null
                    },
                    speed_mps = if (location.hasSpeed()) {
                        location.speed.toDouble()
                    } else {
                        null
                    },
                    updated_at = Instant.now().toString()
                )

                SupabaseProvider.client.postgrest
                    .from("booking_live_locations")
                    .upsert(liveLocation)

            } catch (exception: Exception) {
                android.util.Log.e(
                    "StaffLocationService",
                    "Location upload failed",
                    exception
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Staff live location",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description =
                    "Shows when your location is being shared for an active booking"
            }

            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Live location is active")
            .setContentText(
                "Your location is being shared for an active booking."
            )
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun stopTracking() {
        try {
            fusedLocationClient.removeLocationUpdates(
                locationCallback
            )
        } catch (_: Exception) {
        }

        trackingStarted = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        try {
            fusedLocationClient.removeLocationUpdates(
                locationCallback
            )
        } catch (_: Exception) {
        }

        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
