package com.findhubtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.findhubtracker.FindHubApp
import com.findhubtracker.data.model.EmailConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as FindHubApp

    var scanInterval by remember { mutableLongStateOf(app.repository.getScanInterval()) }
    val emailConfig by remember { mutableStateOf(app.repository.getEmailConfig()) }

    var recipientEmail by remember { mutableStateOf(emailConfig.recipientEmail) }
    var smtpServer by remember { mutableStateOf(emailConfig.smtpServer) }
    var smtpPort by remember { mutableStateOf(emailConfig.smtpPort) }
    var smtpUsername by remember { mutableStateOf(emailConfig.username) }
    var smtpPassword by remember { mutableStateOf(emailConfig.password) }
    var emailEnabled by remember { mutableStateOf(emailConfig.isEnabled) }

    var serverUrl by remember { mutableStateOf(app.backendRepository.serverUrl) }
    var autoRefresh by remember { mutableStateOf(app.backendRepository.autoRefresh) }
    var backendRefreshInterval by remember { mutableIntStateOf(app.backendRepository.refreshInterval) }

    var showSaved by remember { mutableStateOf(false) }

    val intervalOptions = listOf(
        60_000L to "1 minuta",
        300_000L to "5 minuta",
        600_000L to "10 minuta",
        900_000L to "15 minuta",
        1_800_000L to "30 minuta",
        3_600_000L to "1 sat"
    )

    val backendIntervalOptions = listOf(
        60 to "1 minuta",
        300 to "5 minuta",
        600 to "10 minuta",
        900 to "15 minuta",
        1800 to "30 minuta",
        3600 to "1 sat"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Postavke") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Backend postavke",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("URL servera") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("http://192.168.1.100:8000") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Automatsko osvježavanje",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = autoRefresh,
                    onCheckedChange = { autoRefresh = it }
                )
            }

            if (autoRefresh) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Interval osvježavanja",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))

                backendIntervalOptions.forEach { (intervalSec, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = backendRefreshInterval == intervalSec,
                            onClick = { backendRefreshInterval = intervalSec }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Interval skeniranja",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            intervalOptions.forEach { (intervalMs, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = scanInterval == intervalMs,
                        onClick = {
                            scanInterval = intervalMs
                            app.repository.setScanInterval(intervalMs)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Email postavke",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Omogući email obavijesti",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = emailEnabled,
                    onCheckedChange = { emailEnabled = it }
                )
            }

            if (emailEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = recipientEmail,
                    onValueChange = { recipientEmail = it },
                    label = { Text("Primatelj emaila") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = smtpServer,
                    onValueChange = { smtpServer = it },
                    label = { Text("SMTP server") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = smtpPort,
                    onValueChange = { smtpPort = it },
                    label = { Text("SMTP port") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = smtpUsername,
                    onValueChange = { smtpUsername = it },
                    label = { Text("Korisničko ime") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = smtpPassword,
                    onValueChange = { smtpPassword = it },
                    label = { Text("Lozinka") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Za Gmail koristite app password (ne regularnu lozinku)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    app.backendRepository.updateServerUrl(serverUrl)
                    app.backendRepository.autoRefresh = autoRefresh
                    app.backendRepository.refreshInterval = backendRefreshInterval

                    app.repository.saveEmailConfig(
                        EmailConfig(
                            recipientEmail = recipientEmail,
                            smtpServer = smtpServer,
                            smtpPort = smtpPort,
                            username = smtpUsername,
                            password = smtpPassword,
                            isEnabled = emailEnabled
                        )
                    )
                    showSaved = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Spremi postavke")
            }

            if (showSaved) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Postavke spremljene!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
