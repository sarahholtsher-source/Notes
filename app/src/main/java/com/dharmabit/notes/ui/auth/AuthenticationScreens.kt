package com.dharmabit.notes.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.dharmabit.notes.security.SecurityManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySetupScreen(
    securityManager: SecurityManager,
    onSecurityEnabled: () -> Unit,
    onBack: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var useBiometric by remember { mutableStateOf(false) }
    var enablePrivateNotes by remember { mutableStateOf(false) }
    var showPinError by remember { mutableStateOf(false) }
    var pinVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Set up PIN",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                pin = it
                                showPinError = false
                            }
                        },
                        label = { Text("Enter 4-6 digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { pinVisible = !pinVisible }) {
                                Icon(
                                    if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (pinVisible) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showPinError
                    )

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                confirmPin = it
                                showPinError = false
                            }
                        },
                        label = { Text("Confirm PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        isError = showPinError
                    )

                    if (showPinError) {
                        Text(
                            text = "PINs don't match or must be 4-6 digits",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Authentication Options",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (securityManager.canUseBiometric()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = useBiometric,
                                onCheckedChange = { useBiometric = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Enable Fingerprint/Face Unlock")
                                Text(
                                    "Use biometric authentication for quick access",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = enablePrivateNotes,
                            onCheckedChange = { enablePrivateNotes = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Enable Private Notes")
                            Text(
                                "Create a hidden section for sensitive notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    when {
                        pin.length < 4 -> showPinError = true
                        pin != confirmPin -> showPinError = true
                        else -> {
                            securityManager.enableSecurity(pin, useBiometric)
                            if (enablePrivateNotes) {
                                securityManager.enablePrivateNotes()
                            }
                            onSecurityEnabled()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable Security")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockScreen(
    securityManager: SecurityManager,
    onUnlocked: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var pinVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (securityManager.isBiometricEnabled() && context is FragmentActivity) {
            securityManager.authenticateWithBiometric(
                activity = context,
                onSuccess = onUnlocked,
                onError = { /* User can fall back to PIN */ }
            )
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Lock",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Enter your PIN",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                pin = it
                                showError = false
                            }
                        },
                        label = { Text("PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { pinVisible = !pinVisible }) {
                                Icon(
                                    if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (pinVisible) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showError
                    )

                    if (showError) {
                        Text(
                            text = "Incorrect PIN",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (securityManager.isBiometricEnabled() && context is FragmentActivity) {
                            OutlinedButton(
                                onClick = {
                                    securityManager.authenticateWithBiometric(
                                        activity = context,
                                        onSuccess = onUnlocked,
                                        onError = { /* Show error if needed */ }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = "Use Biometric",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Biometric")
                            }
                        }

                        Button(
                            onClick = {
                                if (securityManager.validatePin(pin)) {
                                    onUnlocked()
                                } else {
                                    showError = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = pin.length >= 4
                        ) {
                            Text("Unlock")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivateNotesToggle(
    isPrivateMode: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isPrivateMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = if (isPrivateMode) "Private Mode" else "Normal Mode",
            tint = if (isPrivateMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isPrivateMode) "Private Notes" else "All Notes",
            style = MaterialTheme.typography.titleMedium,
            color = if (isPrivateMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isPrivateMode,
            onCheckedChange = { onToggle() }
        )
    }
}