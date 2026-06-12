package com.findhubtracker.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
            onDismissRequest = { },
            title = { Text("Potrebne dozvole") },
            text = {
                Text("Aplikacija zahtijeva dozvole za Bluetooth skeniranje, pristup lokaciji i notifikacije kako bi mogla pratiti trackere.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                    }
                ) {
                    Text("Odobri dozvole")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Text("Otvori postavke")
                }
            }
        )
    }
}
