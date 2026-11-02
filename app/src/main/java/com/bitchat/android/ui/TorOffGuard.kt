package com.bitchat.android.ui

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bitchat.android.R
import com.bitchat.android.net.TorMode
import com.bitchat.android.net.TorPreferenceManager
import com.bitchat.android.security.SecurityPreferences
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Turning Tor off exposes the IP address to every relay, so it is kept out of the normal flow:
 * the switch only goes off in advanced mode, and only after an explicit confirmation.
 * Turning Tor on is always immediate.
 */
private val torOffAsked = MutableStateFlow(false)

fun requestTorMode(context: Context, on: Boolean) {
    if (on) TorPreferenceManager.set(context, TorMode.ON)
    else if (SecurityPreferences.advancedMode.value) torOffAsked.value = true
}

/** The Tor switch can be used: turning on always, turning off in advanced mode only. */
@Composable
fun torSwitchEnabled(available: Boolean, mode: TorMode): Boolean {
    val advanced by SecurityPreferences.advancedMode.collectAsState()
    return available && (mode == TorMode.OFF || advanced)
}

/** Shown once at the top of the app (MainActivity), whichever screen asked. */
@Composable
fun TorOffConfirmDialog() {
    val asked by torOffAsked.collectAsState()
    if (!asked) return
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { torOffAsked.value = false },
        title = { Text(stringResource(R.string.tor_off_title)) },
        text = { Text(stringResource(R.string.tor_off_ask)) },
        confirmButton = {
            TextButton(onClick = {
                torOffAsked.value = false
                TorPreferenceManager.set(context, TorMode.OFF)
            }) { Text(stringResource(R.string.tor_off_yes)) }
        },
        dismissButton = {
            TextButton(onClick = { torOffAsked.value = false }) { Text(stringResource(R.string.tor_off_keep)) }
        }
    )
}
