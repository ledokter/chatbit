package com.bitchat.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.R
import com.bitchat.android.ui.theme.BitchatFontFamily

/**
 * chatbit: the timeline filters, kept out of the way. The search is a single folded line of
 * text at the top of the timeline; a focus on one person or timed mutes show as one line each,
 * with the way out next to them.
 */
@Composable
fun TimelineFilterBar(
    focus: TimelineFilter.Focus?,
    query: String,
    regex: Boolean,
    muted: Map<String, TimelineFilter.Mute>,
    shownCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    var expanded by rememberSaveable { mutableStateOf(false) }
    val filtering = focus != null || query.isNotBlank()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.surfaceVariant.copy(alpha = if (expanded || filtering || muted.isNotEmpty()) 0.85f else 0.35f)
    ) {
        Column(Modifier.fillMaxWidth()) {
            // the folded line: "Search the timeline ▾" (or the active search, so it is never forgotten)
            Surface(
                onClick = { expanded = !expanded },
                color = androidx.compose.ui.graphics.Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.ic_lucide_search),
                        contentDescription = null,
                        tint = if (query.isNotBlank()) colorScheme.primary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (query.isNotBlank() && !expanded) stringResource(R.string.timeline_search_active, query, shownCount)
                        else stringResource(R.string.timeline_search_fold),
                        modifier = Modifier.weight(1f),
                        fontFamily = BitchatFontFamily,
                        fontSize = 11.sp,
                        color = if (query.isNotBlank()) colorScheme.primary else colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = TimelineFilter::setQuery,
                            singleLine = true,
                            placeholder = {
                                Text(
                                    stringResource(if (regex) R.string.timeline_search_hint_regex else R.string.timeline_search_hint),
                                    fontFamily = BitchatFontFamily, fontSize = 13.sp
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(fontFamily = BitchatFontFamily, fontSize = 13.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            trailingIcon = if (query.isNotEmpty()) {
                                {
                                    IconButton(onClick = { TimelineFilter.setQuery("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.timeline_search_clear))
                                    }
                                }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = regex,
                            onClick = { TimelineFilter.setRegex(!regex) },
                            label = { Text(".*", fontFamily = BitchatFontFamily, fontWeight = FontWeight.Bold) }
                        )
                    }
                    val error = TimelineFilter.regexError(query, regex)
                    Text(
                        when {
                            error != null -> stringResource(R.string.timeline_search_bad_regex, error)
                            query.isNotBlank() -> stringResource(R.string.timeline_search_count, shownCount, totalCount)
                            else -> stringResource(R.string.timeline_search_help)
                        },
                        fontFamily = BitchatFontFamily,
                        fontSize = 11.sp,
                        color = if (error != null) colorScheme.error else colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            focus?.let { f ->
                FilterLine(
                    text = stringResource(R.string.timeline_focus_active, f.nickname, shownCount),
                    action = stringResource(R.string.timeline_focus_clear),
                    onAction = TimelineFilter::clearFocus
                )
            }

            if (muted.isNotEmpty()) {
                val now = System.currentTimeMillis()
                val names = muted.values.joinToString(", ") { m ->
                    "@${m.nickname} (${((m.until - now + 59_999) / 60_000).coerceAtLeast(1)} min)"
                }
                FilterLine(
                    text = stringResource(R.string.timeline_muted_active, names),
                    action = stringResource(R.string.timeline_muted_clear),
                    onAction = TimelineFilter::unmuteAll
                )
            }
        }
    }
}

@Composable
private fun FilterLine(text: String, action: String, onAction: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            modifier = Modifier.weight(1f),
            fontFamily = BitchatFontFamily,
            fontSize = 12.sp,
            color = colorScheme.primary,
            maxLines = 2
        )
        TextButton(onClick = onAction) {
            Text(action, fontFamily = BitchatFontFamily, fontSize = 12.sp)
        }
    }
}
