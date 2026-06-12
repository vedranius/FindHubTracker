package com.findhubtracker.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.findhubtracker.FindHubApp
import com.findhubtracker.data.model.GeofenceZone
import com.findhubtracker.ui.components.GeofenceRadiusSlider
import com.findhubtracker.util.Constants
import com.findhubtracker.util.LocationUtils
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as FindHubApp
    val trackers by app.repository.allTrackers.collectAsState(initial = emptyList())
    val geofenceZones by app.repository.allGeofenceZones.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var geofenceRadius by remember { mutableFloatStateOf(Constants.DEFAULT_GEOFENCE_RADIUS_METERS) }
    var showGeofenceDialog by remember { mutableStateOf(false) }
    var geofenceName by remember { mutableStateOf("") }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa - držite prst za geofence") },
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
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(12.0)
                        controller.setCenter(GeoPoint(45.8150, 15.9819))
                        mapViewRef = this

                        val mapEventsReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean = false
                            override fun longPressHelper(p: GeoPoint): Boolean {
                                selectedLocation = p
                                showGeofenceDialog = true
                                return true
                            }
                        }
                        overlays.add(0, MapEventsOverlay(mapEventsReceiver))
                    }
                },
                update = { mapView ->
                    mapView.overlays.removeAll { it is Marker || it is Polygon }

                    trackers.forEach { tracker ->
                        if (tracker.lastLatitude != 0.0 && tracker.lastLongitude != 0.0) {
                            val geoPoint = GeoPoint(tracker.lastLatitude, tracker.lastLongitude)
                            val marker = Marker(mapView).apply {
                                position = geoPoint
                                title = tracker.name
                                snippet = "${tracker.brand}\nZadnji put viđen: ${LocationUtils.formatTimestamp(tracker.lastSeenTimestamp)}"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            mapView.overlays.add(marker)
                        }
                    }

                    geofenceZones.forEach { zone ->
                        val center = GeoPoint(zone.latitude, zone.longitude)
                        val circle = Polygon().apply {
                            points = createCirclePoints(center, zone.radiusMeters.toDouble(), 64)
                            fillPaint.color = 0x331A73E8.toInt()
                            outlinePaint.color = 0xFF1A73E8.toInt()
                            outlinePaint.strokeWidth = 2f
                        }
                        mapView.overlays.add(circle)
                    }

                    mapView.invalidate()
                }
            )
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

private fun createCirclePoints(center: GeoPoint, radius: Double, numberOfPoints: Int): List<GeoPoint> {
    val points = mutableListOf<GeoPoint>()
    for (i in 0 until numberOfPoints) {
        val angle = 2 * Math.PI * i / numberOfPoints
        val lat = center.latitude + (radius / 111320) * Math.cos(angle)
        val lon = center.longitude + (radius / (111320 * Math.cos(Math.toRadians(center.latitude)))) * Math.sin(angle)
        points.add(GeoPoint(lat, lon))
    }
    points.add(points[0])
    return points
}
