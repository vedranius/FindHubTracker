package com.findhubtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.findhubtracker.FindHubApp
import com.findhubtracker.data.model.GeofenceZone
import com.findhubtracker.data.model.Tracker
import com.findhubtracker.ui.components.GeofenceRadiusSlider
import com.findhubtracker.util.Constants
import com.findhubtracker.util.LocationUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as FindHubApp
    val trackers by app.repository.allTrackers.collectAsState(initial = emptyList())
    val geofenceZones by app.repository.allGeofenceZones.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var geofenceRadius by remember { mutableFloatStateOf(Constants.DEFAULT_GEOFENCE_RADIUS_METERS) }
    var showGeofenceDialog by remember { mutableStateOf(false) }
    var geofenceName by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(45.8150, 15.9819), 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    showGeofenceDialog = true
                },
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true
                )
            ) {
                trackers.forEach { tracker ->
                    val position = LatLng(tracker.lastLatitude, tracker.lastLongitude)
                    Marker(
                        state = rememberMarkerState(position = position),
                        title = tracker.name,
                        snippet = "${tracker.brand}\nZadnji put viđen: ${LocationUtils.formatTimestamp(tracker.lastSeenTimestamp)}",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (tracker.isInsideGeofence) BitmapDescriptorFactory.HUE_RED
                            else BitmapDescriptorFactory.HUE_ORANGE
                        )
                    )
                }

                geofenceZones.forEach { zone ->
                    val center = LatLng(zone.latitude, zone.longitude)
                    Circle(
                        center = center,
                        radius = zone.radiusMeters.toDouble(),
                        fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        strokeColor = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2f
                    )
                    Marker(
                        state = rememberMarkerState(position = center),
                        title = zone.name,
                        snippet = "Radijus: ${zone.radiusMeters.toInt()}m",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }
        }
    }

    if (showGeofenceDialog) {
        AlertDialog(
            onDismissRequest = {
                showGeofenceDialog = false
                selectedLocation = null
            },
            title = { Text("Nova geofence zona") },
            text = {
                Column {
                    OutlinedTextField(
                        value = geofenceName,
                        onValueChange = { geofenceName = it },
                        label = { Text("Naziv zone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GeofenceRadiusSlider(
                        radiusMeters = geofenceRadius,
                        onRadiusChange = { geofenceRadius = it }
                    )
                    if (selectedLocation != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lokacija: ${LocationUtils.formatCoordinates(selectedLocation!!.latitude, selectedLocation!!.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedLocation?.let { location ->
                            scope.launch {
                                val zone = GeofenceZone(
                                    id = UUID.randomUUID().toString(),
                                    name = geofenceName.ifEmpty { "Zona ${geofenceZones.size + 1}" },
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    radiusMeters = geofenceRadius,
                                    isActive = true
                                )
                                app.repository.insertGeofenceZone(zone)
                            }
                        }
                        showGeofenceDialog = false
                        selectedLocation = null
                        geofenceName = ""
                    },
                    enabled = selectedLocation != null && geofenceName.isNotEmpty()
                ) {
                    Text("Spremi")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showGeofenceDialog = false
                        selectedLocation = null
                        geofenceName = ""
                    }
                ) {
                    Text("Odustani")
                }
            }
        )
    }
}
