package com.compensatuviaje.tracker.feature.locationservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.compensatuviaje.tracker.model.GpsPoint
import java.time.Instant
import android.util.Log
import android.os.Looper

class TrackingForegroundService : Service() {
    private val capturedPoints = mutableListOf<GpsPoint>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L
            )
                .setMinUpdateIntervalMillis(3000L)
                .build()

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(
                result: LocationResult
            ) {

                val location: Location =
                    result.lastLocation ?: return

                if (location.accuracy > 50f) {
                    return
                }

                val gpsPoint = GpsPoint(
                    tripId = "TEMP_TRIP",
                    timestampIso = Instant.now().toString(),
                    lat = location.latitude,
                    lng = location.longitude,
                    speedKmh = location.speed * 3.6,
                    heading = location.bearing.toDouble(),
                    accuracyMeters = location.accuracy.toDouble(),
                    synced = false
                )

                Log.d(
                    "TrackingService",
                    gpsPoint.toString()
                )
                capturedPoints.add(gpsPoint)
            }
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        try {

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        } catch (securityException: SecurityException) {

            Log.e(
                "TrackingService",
                "Permiso de ubicación no concedido",
                securityException
            )
        }

        return START_STICKY
    }

    override fun onDestroy() {

        fusedLocationClient.removeLocationUpdates(
            locationCallback
        )

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}