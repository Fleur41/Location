package com.sam.location.location

import android.Manifest
import android.annotation.SuppressLint
import android.location.Address
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.sam.location.debugLog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LocationScreen(
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current
    var location by remember { mutableStateOf<MyLocation?>(null) }
    var address by remember { mutableStateOf<Address?>(null) }
    val coarseLocationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
    val preciseLocationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "LocationScreen") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick =  {
                    if (coarseLocationPermissionState.status.isGranted){
                        val retrievedLocation = LocationUtility.getLocationFromLocationManager(context)
                        location = retrievedLocation
                        location?.let {
                            debugLog("Latitude: ${it.latitude}")
                            debugLog("Longitude: ${it.longitude}")

                            val retrievedAddress = LocationUtility.getAddressFromLocationCoordinates(context, it)
                            address?.let {
                                debugLog("Address: $address")
                                address = retrievedAddress
                            }
                        }
                    } else {
                        coarseLocationPermissionState.launchPermissionRequest()
                    }
                }
            ) {
                Text(text = "Get approximate (coarse) location")
            }

            Button(
                    onClick = { LocationUtility.getLiveLocation(context){location ->
                        debugLog("Got coarse live location: $location")
                    }
                }
            ) {
                Text(text = "Get coarse live location")
            }

            Button(
                onClick =
                    {
                        if (preciseLocationPermissionState.status.isGranted){
                            @SuppressLint("MissingPermission")
                            fusedLocationClient
                                .lastLocation
                                .addOnSuccessListener { retrievedLocation ->
                                    location?.let{
                                        location = MyLocation(retrievedLocation.latitude, retrievedLocation.longitude)
                                        debugLog("Got precise location: $location")
                                        debugLog("Latitude: ${location!!.latitude}")
                                        debugLog("Longitude: ${location!!.longitude}")

                                        val retrievedAddress = LocationUtility.getAddressFromLocationCoordinates(context,
                                            location!!
                                        )
                                        address?.let {
                                            debugLog("Address: $address")
                                            address = retrievedAddress
                                        }
                                    } ?: run {
                                        debugLog("Precise location (lastKnown) is null.")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    debugLog("Error getting precise location: ${e.message}")
                                }
                        } else{
                            preciseLocationPermissionState.launchPermissionRequest()
                        }
                    }
            ) {
                Text(text = "Get precise location")
            }

//            Button(
//                onClick =
//                     {
//                    if (preciseLocationPermissionState.status.isGranted){
//                        fusedLocationClient
//                            .lastLocation
//                            .addOnSuccessListener { location ->
//                                debugLog("Got precise location: $location")
//                            }
//                    } else{
//                        preciseLocationPermissionState.launchPermissionRequest()
//                    }
//                }
//            ) {
//                Text(text = "Get precise live location")
//            }
            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                visible = location != null
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(text = "Location Details", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Latitude: ${location?.latitude}")
                        Text(text = "Longitude: ${location?.longitude}")

                        //VerticalSpacer(12)
                        Text(text = "Address Details", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Address: ${address?.getAddressLine(0)}")
                    }
                }
            }

        }
    }
}
