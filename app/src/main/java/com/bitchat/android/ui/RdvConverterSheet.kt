package com.bitchat.android.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.R
import com.bitchat.android.core.ui.component.sheet.BitchatBottomSheet
import com.bitchat.android.geohash.ChannelID
import com.bitchat.android.geohash.Geohash
import com.bitchat.android.geohash.MeetingPoint
import com.bitchat.android.security.SecureClipboard
import com.bitchat.android.ui.theme.BitchatFontFamily

/**
 * chatbit: meeting point converter. Type or paste a geohash, a Plus Code or coordinates and get
 * the two others, then copy, send in the current channel or open in a map app. Offline only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RdvConverterSheet(viewModel: ChatViewModel, onDismiss: () -> Unit, onSent: () -> Unit) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val selected by viewModel.selectedLocationChannel.collectAsStateWithLifecycle()
    // the channel's zone: starting point, and the reference that places short plus codes
    val zone = (selected as? ChannelID.Location)?.channel?.geohash
    val reference = zone?.let { Geohash.decodeToCenter(it) }

    var input by remember { mutableStateOf(zone.orEmpty()) }
    val parsed = remember(input, reference) { MeetingPoint.parse(input, reference) }
    val prefix = stringResource(R.string.rdv_message_prefix)

    BitchatBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(stringResource(R.string.rdv_title), fontFamily = BitchatFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.rdv_intro),
                fontFamily = BitchatFontFamily, fontSize = 12.sp,
                color = colorScheme.onSurface.copy(alpha = 0.7f)
            )
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                singleLine = true,
                label = { Text(stringResource(R.string.rdv_input_label), fontFamily = BitchatFontFamily) },
                placeholder = { Text("u09tvw0f6 · 8FW4V75V+8QH · 48.8584, 2.2945", fontFamily = BitchatFontFamily, fontSize = 12.sp) },
                textStyle = LocalTextStyle.current.copy(fontFamily = BitchatFontFamily),
                isError = input.isNotBlank() && parsed == null,
                supportingText = {
                    if (input.isNotBlank() && parsed == null) {
                        Text(stringResource(R.string.rdv_input_error), fontFamily = BitchatFontFamily, fontSize = 11.sp)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            parsed?.point?.let { point ->
                if (parsed.kind == MeetingPoint.Companion.Kind.GEOHASH && input.trim().removePrefix("#").length < MeetingPoint.GEOHASH_PRECISION) {
                    Text(
                        stringResource(R.string.rdv_zone_center),
                        fontFamily = BitchatFontFamily, fontSize = 11.sp, color = colorScheme.tertiary
                    )
                }
                RdvValueRow(stringResource(R.string.rdv_geohash), point.geohash, context)
                RdvValueRow(stringResource(R.string.rdv_plus_code), point.plusCode, context)
                RdvValueRow(stringResource(R.string.rdv_coordinates), point.coordinates, context)

                Button(
                    onClick = {
                        viewModel.sendMessage(point.message(prefix))
                        onSent()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.rdv_send), fontFamily = BitchatFontFamily, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(point.geoUri)))
                        }.onFailure {
                            android.widget.Toast.makeText(context, context.getString(R.string.rdv_no_map_app), android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.rdv_open_map), fontFamily = BitchatFontFamily)
                }
                Text(
                    stringResource(R.string.rdv_privacy),
                    fontFamily = BitchatFontFamily, fontSize = 11.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RdvValueRow(label: String, value: String, context: android.content.Context) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, fontFamily = BitchatFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontFamily = BitchatFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = { SecureClipboard.copy(context, label, value, sensitive = false) }) {
            Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.rdv_copy, label))
        }
    }
}
