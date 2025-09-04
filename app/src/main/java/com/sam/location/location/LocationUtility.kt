package com.sam.location.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.sam.location.debugLog
import java.util.Locale


data class MyLocation(
    val latitude: Double,
    val longitude: Double,

)
object LocationUtility {
    //@RequiresPermission(allOf = [ Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getLocationFromLocationManager(context: Context): MyLocation? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ){
            return null
        }
        val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (lastKnownLocation != null){

            val location = MyLocation(
                latitude = lastKnownLocation.latitude,
                longitude = lastKnownLocation.longitude
            )
            return location
        }
        return null
    }

    //@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getLiveLocation(
        context: Context,
        onLocationUpdateReceived: (MyLocation) -> Unit
        ){
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = LocationListener { location ->
            val myLocation = MyLocation(
                latitude = location.latitude,
                longitude = location.longitude
            )
            onLocationUpdateReceived(myLocation)
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            return
        }

        val isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val provider = if (isGpsProviderEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        locationManager.requestLocationUpdates(
            provider,
            5000,
            0F,
            locationListener
        )

    }

    fun getAddressFromLocationCoordinates(context: Context, location: MyLocation): Address? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        debugLog("Address: $address")
        return address?.first()
    }
}