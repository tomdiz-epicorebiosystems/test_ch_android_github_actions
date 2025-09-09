package com.epicorebiosystems.rehydrate.modelData

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

//
// Manages Location for CSV file upload
//

// data class to store the user Latitude and longitude
data class LatAndLong(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

// A callback for receiving notifications from the FusedLocationProviderClient.
lateinit var locationCallback: LocationCallback
// The main entry point for interacting with the Fused Location Provider
lateinit var locationProvider: FusedLocationProviderClient

@SuppressLint("MissingPermission")
@Composable
fun getUserLocation(context: Context): LatAndLong {

    // The Fused Location Provider provides access to location APIs.
    locationProvider = LocationServices.getFusedLocationProviderClient(context)

    var currentUserLocation by remember { mutableStateOf(LatAndLong()) }

    DisposableEffect(key1 = locationProvider) {
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(result: LocationResult) {
                /**
                 * This option returns the locations computed, ordered from oldest to newest.
                 * */
                for (location in result.locations) {
                    // Update data class with location data
                    currentUserLocation = LatAndLong(location.latitude, location.longitude)
                    //Log.d("LOCATION REQUEST", "${location.latitude},${location.longitude}")
                }


                /**
                 * This option returns the most recent historical location currently available.
                 * Will return null if no historical location is available
                 * */
                locationProvider.lastLocation
                    .addOnSuccessListener { location ->
                        location?.let {
                            val lat = location.latitude
                            val long = location.longitude
                            // Update data class with location data
                            currentUserLocation = LatAndLong(latitude = lat, longitude = long)
                        }
                    }
                    .addOnFailureListener {
                        Log.e("Location_error", "${it.message}")
                    }

            }
        }

        locationUpdate()

        onDispose {
            locationProvider.removeLocationUpdates(locationCallback)
        }
    }

    return currentUserLocation
}

@SuppressLint("MissingPermission")
fun locationUpdate() {
    locationCallback.let {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 30 * 1000)
            .setWaitForAccurateLocation(false)
//            .setWaitForAccurateLocation(true)
            .build()
        locationProvider.requestLocationUpdates(locationRequest, it, Looper.getMainLooper())
    }
}