package com.findhubtracker.ui.components

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.findhubtracker.util.PermissionUtils

@Composable
fun PermissionHandler(
    onAllPermissionsGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var hasPermissions by remember {
        mutableStateOf(PermissionUtils.hasAllRequiredPermissions(context))
    }

    val permissionLauncher = rememberPermissionLauncher(
        onResult = { granted ->
            hasPermissions = PermissionUtils.hasAllRequiredPermissions(context)
        }
    )

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
                        if (activity != null) {
                            PermissionUtils.requestPermissions(activity, 1001)
                        }
                    }
                ) {
                    Text("Otvori postavke")
                }
            }
        )
    }
}

@Composable
fun rememberPermissionLauncher(
    onResult: (Boolean) -> Unit
): androidx.activity.result.ActivityResultLauncher<Array<String>> {
    val context = LocalContext.current
    val activity = context as Activity

    return remember {
        activity.registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            onResult(allGranted)
        }
    }
}
