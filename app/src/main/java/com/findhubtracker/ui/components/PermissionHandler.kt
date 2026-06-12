package com.findhubtracker.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.findhubtracker.util.PermissionUtils

@Composable
fun PermissionHandler(
    onAllPermissionsGranted: @Composable () -> Unit
) {
    val context = LocalContext.current

    var hasPermissions by remember {
        mutableStateOf(PermissionUtils.hasAllRequiredPermissions(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = PermissionUtils.hasAllRequiredPermissions(context)
    }

    if (hasPermissions) {
        onAllPermissionsGranted()
    } else {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Potrebne dozvole") },
            text = {
                Text("Aplikacija zahtijeva dozvole za Bluetooth skeniranje, pristup lokaciji i notifikacije kako bi mogla pratiti trackere.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                    }
                ) {
                    Text("Odobri dozvole")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val activity = context as? Activity
                        activity?.let {
                            PermissionUtils.requestPermissions(it, 1001)
                        }
                    }
                ) {
                    Text("Otvori postavke")
                }
            }
        )
    }
}
