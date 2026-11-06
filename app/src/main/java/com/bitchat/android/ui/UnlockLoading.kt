package com.bitchat.android.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.R
import com.bitchat.android.net.ArtiTorManager
import com.bitchat.android.net.NetActivity
import com.bitchat.android.net.TorMode
import com.bitchat.android.ui.theme.BitchatFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The chatbit cat hopping on its shadow (squash on landing). With [active] false it sits still,
 * so the lock screen logo can start hopping while the PIN is being checked without changing size.
 */
@Composable
fun HoppingMascot(height: Dp, active: Boolean = true) {
    val hop = if (active) {
        rememberInfiniteTransition(label = "hop").animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                keyframes {
                    durationMillis = 900
                    0f at 0
                    1f at 380 using FastOutSlowInEasing
                    0f at 700 using FastOutSlowInEasing
                    0f at 900
                }
            ),
            label = "hopY"
        ).value
    } else 0f
    // squash right after landing (frames 700-800 of the cycle are approximated by hop == 0)
    val squash = if (active) {
        rememberInfiniteTransition(label = "squash").animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                keyframes {
                    durationMillis = 900
                    0.9f at 0
                    1.04f at 120
                    1f at 380
                    1f at 680
                    0.88f at 760
                    0.9f at 900
                }
            ),
            label = "squashY"
        ).value
    } else 1f
    val lift = height * 0.28f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.graphicsLayer {
                translationY = -hop * lift.toPx()
                scaleY = squash
                scaleX = 2f - squash
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
        ) { ChatbitLogo(height = height, dressed = true) }
        // shadow: smaller and paler while the cat is up
        Box(
            Modifier
                .graphicsLayer { scaleX = 1f - hop * 0.45f; alpha = 1f - hop * 0.6f }
                .width(height * 0.8f)
                .height(height * 0.08f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
        )
    }
}

/** Three dots lighting up one after the other, like someone typing. */
@Composable
private fun TypingDots() {
    val t = rememberInfiniteTransition(label = "dots").animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "dotPhase"
    ).value
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(3) { i ->
            val d = ((t - i + 3f) % 3f) // 0 = this dot's turn
            val on = if (d < 1f) 1f - d else 0f
            Box(
                Modifier
                    .size(10.dp)
                    .graphicsLayer { scaleX = 1f + on * 0.4f; scaleY = 1f + on * 0.4f }
                    .alpha(0.3f + on * 0.7f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/**
 * Shown right after the PIN unlocked chatbit: the cat hops while Tor finishes starting and the
 * first messages come in, so the user knows the app is loading. Leaves on its own once things
 * are ready (or after [MAX_MS]); a tap skips it.
 */
@Composable
fun UnlockLoadingCover(onDone: () -> Unit) {
    val tor = remember { ArtiTorManager.getInstance() }
    val torStatus by tor.statusFlow.collectAsState()
    val receiving by NetActivity.receiving.collectAsState()
    val torReady = torStatus.mode == TorMode.OFF || (torStatus.running && torStatus.bootstrapPercent >= 100)
    val torReadyNow by rememberUpdatedState(torReady)
    var sawMessages by remember { mutableStateOf(false) }
    LaunchedEffect(receiving) { if (receiving) sawMessages = true }

    LaunchedEffect(Unit) {
        withTimeoutOrNull(MAX_MS) {
            delay(MIN_MS)
            // wait for Tor, then for the burst of stored messages to end (or a quiet relay)
            while (true) {
                if (torReadyNow && sawMessages && !receiving) break
                delay(200)
            }
        }
        onDone()
    }
    LaunchedEffect(torReady) {
        if (torReady) { delay(QUIET_MS); if (!sawMessages) onDone() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDone() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        HoppingMascot(height = 96.dp)
        Text(
            stringResource(R.string.unlock_loading),
            fontFamily = BitchatFontFamily, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground
        )
        TypingDots()
    }
}

private const val MIN_MS = 1_200L
private const val QUIET_MS = 4_000L
private const val MAX_MS = 15_000L
