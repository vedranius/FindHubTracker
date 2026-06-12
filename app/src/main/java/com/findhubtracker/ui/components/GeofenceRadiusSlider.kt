package com.findhubtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.findhubtracker.util.Constants

@Composable
fun GeofenceRadiusSlider(
    radiusMeters: Float,
    onRadiusChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Radijus geofence",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${radiusMeters.toInt()}m",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = radiusMeters,
            onValueChange = onRadiusChange,
            valueRange = Constants.MIN_GEOFENCE_RADIUS_METERS..Constants.MAX_GEOFENCE_RADIUS_METERS,
            steps = 48,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${Constants.MIN_GEOFENCE_RADIUS_METERS.toInt()}m",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "${Constants.MAX_GEOFENCE_RADIUS_METERS.toInt()}m",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
