package com.ics342.labs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.ics342.labs.ui.theme.LabsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var hasPermission by remember { mutableStateOf(false) } // state for tracking if the permission has been granted
            var showPermissionRationale by remember { mutableStateOf(false) } // state for tracking if the rationale should be shown
            val context = LocalContext.current

            val launcher = rememberLauncherForActivityResult(RequestPermission()) { isGranted ->
                if (isGranted) {
                    hasPermission = true
                    showPermissionRationale = false
                } else {
                    showPermissionRationale = !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }

            // ...

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // if permission has been granted, show the LocationView
                // else if permission has not been granted, show a button to to request permission
                if (hasPermission) {
                    LocationView()
                } else {
                    Button(onClick = {
                        checkOrRequestPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            launcher,
                            { hasPermission = true }
                        )
                    }) {
                        Text("Request Location Permission")
                    }
                }

                // if user has denied permission and we should show the rationale, show the dialog
                if (showPermissionRationale) {
                    PermissionRationaleDialog(onConfirm = {
                        checkOrRequestPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            launcher,
                            { hasPermission = true }
                        )
                    }, onCancel = { showPermissionRationale = false })
                }
            }
        }
    }
}

@Composable
private fun LocationView() {
    Text("Has Location Permission")
}

@Composable
private fun PermissionRationaleDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = @Composable {
            Button(onClick = { onConfirm() }) {
                Text("OK")
            }
        },
        dismissButton = @Composable {
            Button(onClick = { onCancel() }) {
                Text("No")
            }
        },
        title = @Composable {
            Text("Permissions Required")
        },
        text = @Composable {
            Text("This app requires this permission to use this feature.")
        }
    )
}

private fun checkOrRequestPermission(
    context: Context,
    permission: String,
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    permissionGranted: () -> Unit
) {
    // Ask Android if the app has the permission with ContextCompat.checkSelfPermission
    when (ContextCompat.checkSelfPermission(context, permission)) {
        PackageManager.PERMISSION_GRANTED -> permissionGranted()
        else -> launcher.launch(permission)
    }
}
