package com.findhubtracker.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.findhubtracker.FindHubApp
import com.findhubtracker.ui.components.GeofenceRadiusSlider
import com.findhubtracker.util.Constants
import com.findhubtracker.util.LocationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerDetailScreen(
    trackerAddress: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as FindHubApp
    val scope = rememberCoroutineScope()
    var tracker by remember { mutableStateOf<com.findhubtracker.data.model.Tracker?>(null) }

    var geofenceEnabled by remember { mutableStateOf(false) }
    var geofenceRadius by remember { mutableFloatStateOf(Constants.DEFAULT_GEOFENCE_RADIUS_METERS) }
    var geofenceName by remember { mutableStateOf("") }
    var checkInterval by remember { mutableLongStateOf(300_000L) }
    var showIntervalMenu by remember { mutableStateOf(false) }

    LaunchedEffect(trackerAddress) {
        val t = app.repository.getTracker(trackerAddress)
        tracker = t
        t?.let {
            geofenceEnabled = it.geofenceEnabled
            geofenceRadius = it.geofenceRadiusMeters
            geofenceName = it.geofenceName
            checkInterval = it.checkIntervalMs
        }
    }

    val intervalOptions = listOf(
        60_000L to "1 minuta",
        300_000L to "5 minuta",
        600_000L to "10 minuta",
        900_000L to "15 minuta",
        1_800_000L to "30 minuta",
        3_600_000L to "1 sat"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tracker?.name ?: "Detalji") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Natrag")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        tracker?.let { t ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BluetoothSearching,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = t.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = t.brand,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        DetailRow(
                            icon = Icons.Default.LocationOn,
                            label = "Lokacija",
                            value = LocationUtils.formatCoordinates(t.lastLatitude, t.lastLongitude)
                        )

                        DetailRow(
                            icon = Icons.Default.AccessTime,
                            label = "Zadnji put viđen",
                            value = LocationUtils.formatTimestamp(t.lastSeenTimestamp)
                        )

                        DetailRow(
                            icon = Icons.Default.SignalCellularAlt,
                            label = "Signal (RSSI)",
                            value = "${t.rssi} dBm"
                        )

                        DetailRow(
                            icon = if (t.isInsideGeofence) Icons.Default.CheckCircle else Icons.Default.Warning,
                            label = "Status geofence",
                            value = if (!t.geofenceEnabled) "Geofence onemogućen"
                            else if (t.isInsideGeofence) "Unutar zone" else "Izvan zone!",
                            valueColor = if (!t.geofenceEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                            else if (t.isInsideGeofence) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )

                        DetailRow(
                            icon = Icons.Default.Memory,
                            label = "MAC adresa",
                            value = t.address
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Geofence postavke",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Omogući geofence", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = geofenceEnabled,
                                onCheckedChange = { geofenceEnabled = it }
                            )
                        }

                        if (geofenceEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = geofenceName,
                                onValueChange = { geofenceName = it },
                                label = { Text("Naziv zone") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            GeofenceRadiusSlider(
                                radiusMeters = geofenceRadius,
                                onRadiusChange = { geofenceRadius = it }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Interval provjere", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))

                            Box {
                                OutlinedCard(
                                    onClick = { showIntervalMenu = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = intervalOptions.find { it.first == checkInterval }?.second ?: "5 minuta"
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }

                                DropdownMenu(
                                    expanded = showIntervalMenu,
                                    onDismissRequest = { showIntervalMenu = false }
                                ) {
                                    intervalOptions.forEach { (interval, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                checkInterval = interval
                                                showIntervalMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Lokacija centra zone: ${LocationUtils.formatCoordinates(t.lastLatitude, t.lastLongitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            app.repository.updateTrackerGeofence(
                                address = t.address,
                                enabled = geofenceEnabled,
                                latitude = t.lastLatitude,
                                longitude = t.lastLongitude,
                                radius = geofenceRadius,
                                name = geofenceName,
                                interval = checkInterval
                            )
                            tracker = app.repository.getTracker(t.address)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Spremi geofence postavke")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:${t.lastLatitude},${t.lastLongitude}?q=${t.lastLatitude},${t.lastLongitude}(${t.name})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Otvori u karti")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, LocationUtils.getGoogleMapsUrl(t.lastLatitude, t.lastLongitude))
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Podijeli lokaciju"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Podijeli lokaciju")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            app.repository.deleteTracker(t)
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Obriši tracker")
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor
            )
        }
    }
}
