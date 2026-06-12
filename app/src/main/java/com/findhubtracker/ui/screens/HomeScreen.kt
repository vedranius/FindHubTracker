package com.findhubtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.findhubtracker.FindHubApp
import com.findhubtracker.bluetooth.BleScanner
import com.findhubtracker.data.model.Tracker
import com.findhubtracker.service.BleScannerService
import com.findhubtracker.ui.components.TrackerCard
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTrackerClick: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as FindHubApp
    val trackers by app.repository.allTrackers.collectAsState(initial = emptyList())
    var isServiceRunning by remember { mutableStateOf(app.repository.isServiceRunning()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var manualName by remember { mutableStateOf("") }
    var manualAddress by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FindHub Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj tracker")
                }
                Spacer(modifier = Modifier.height(12.dp))
                ExtendedFloatingActionButton(
                    onClick = {
                        if (isServiceRunning) {
                            BleScannerService.stop(context)
                            isServiceRunning = false
                        } else {
                            BleScannerService.start(context)
                            isServiceRunning = true
                        }
                        app.repository.setServiceRunning(isServiceRunning)
                    },
                    icon = {
                        Icon(
                            imageVector = if (isServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(if (isServiceRunning) "Zaustavi" else "Skeniraj")
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isServiceRunning) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BluetoothSearching,
                        contentDescription = null,
                        tint = if (isServiceRunning) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isServiceRunning) "Skeniranje u tijeku…" else "Skeniranje zaustavljeno",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (trackers.isNotEmpty()) {
                            Text(
                                text = "Pronađeno: ${trackers.size} uređaja",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (trackers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BluetoothSearching,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nema detektiranih trackera",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pokrenite skeniranje ili ručno dodajte tracker",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                Text(
                    text = "Trackeri (${trackers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trackers) { tracker ->
                        TrackerCard(
                            tracker = tracker,
                            onClick = { onTrackerClick(tracker.address) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Dodaj tracker ručno") },
            text = {
                Column {
                    Text(
                        text = "Unesite podatke o trackeru. MAC adresa se nalazi na samom uređaju ili u Find Hub aplikaciji.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = manualName,
                        onValueChange = { manualName = it },
                        label = { Text("Naziv trackera") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualAddress,
                        onValueChange = { manualAddress = it.uppercase() },
                        label = { Text("MAC adresa (XX:XX:XX:XX:XX:XX)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (manualName.isNotEmpty() && manualAddress.isNotEmpty()) {
                            val tracker = Tracker(
                                address = manualAddress,
                                name = manualName,
                                brand = "Manual",
                                lastLatitude = 0.0,
                                lastLongitude = 0.0,
                                lastSeenTimestamp = System.currentTimeMillis(),
                                rssi = 0
                            )
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                app.repository.insertOrUpdateTracker(tracker)
                            }
                            showAddDialog = false
                            manualName = ""
                            manualAddress = ""
                        }
                    },
                    enabled = manualName.isNotEmpty() && manualAddress.isNotEmpty()
                ) {
                    Text("Dodaj")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    manualName = ""
                    manualAddress = ""
                }) {
                    Text("Odustani")
                }
            }
        )
    }
}
