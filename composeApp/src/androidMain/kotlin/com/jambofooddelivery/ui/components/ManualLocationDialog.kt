package com.jambofooddelivery.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun ManualLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Enter Location Manually")
        },
        text = {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Delivery Address") },
                placeholder = { Text("e.g. 123 Main St, City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (address.isNotBlank()) {
                        onConfirm(address)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
