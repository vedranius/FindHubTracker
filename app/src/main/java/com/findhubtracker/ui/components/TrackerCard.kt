package com.findhubtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.findhubtracker.data.model.Tracker
import com.findhubtracker.util.LocationUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackerCard(
    tracker: Tracker,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothSearching,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tracker.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = tracker.brand,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (tracker.isInsideGeofence) Icons.Default.CheckCircle else Icons.Default.LocationOff,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (tracker.isInsideGeofence) Color(0xFF34A853) else Color(0xFFD93025)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (tracker.isInsideGeofence) "Unutar zone" else "Izvan zone!",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (tracker.isInsideGeofence) Color(0xFF34A853) else Color(0xFFD93025)
                    )
                }

                Text(
                    text = "Zadnji put viđen: ${LocationUtils.formatTimestamp(tracker.lastSeenTimestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val rssiColor = when {
                    tracker.rssi > -50 -> Color(0xFF34A853)
                    tracker.rssi > -70 -> Color(0xFFFBBC04)
                    else -> Color(0xFFD93025)
                }
                Surface(
                    color = rssiColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${tracker.rssi}dBm",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = rssiColor
                    )
                }
            }
        }
    }
}
