package com.kindred.feature.discovery

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kindred.core.data.model.Profile
import kotlin.math.abs
import kotlinx.coroutines.launch

@Composable
fun DiscoveryScreen(
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiscoveryViewModel = hiltViewModel(),
) {
    val deck by viewModel.deck.collectAsStateWithLifecycle()
    val match by viewModel.matchEvent.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Discover", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (deck.isEmpty()) {
                Text(
                    "You've seen everyone nearby.\nNew people appear as they join.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            } else {
                // Render the next card underneath the top one
                deck.take(2).asReversed().forEach { profile ->
                    key(profile.id) {
                        SwipeableProfileCard(
                            profile = profile,
                            isTop = profile.id == deck.first().id,
                            onSwiped = { liked ->
                                if (liked) viewModel.like(profile.id) else viewModel.pass(profile.id)
                            },
                        )
                    }
                }
            }
        }

        if (deck.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
            ) {
                FilledIconButton(
                    onClick = { viewModel.pass(deck.first().id) },
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Pass")
                }
                FilledIconButton(
                    onClick = { viewModel.like(deck.first().id) },
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Like")
                }
            }
        }
    }

    match?.let { matched ->
        AlertDialog(
            onDismissRequest = viewModel::dismissMatch,
            title = { Text("It's a match! 🎉") },
            text = { Text("You and ${matched.name} liked each other.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissMatch()
                    onOpenChat(matched.id)
                }) { Text("Say hi") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissMatch) { Text("Keep browsing") }
            },
        )
    }
}

@Composable
private fun SwipeableProfileCard(
    profile: Profile,
    isTop: Boolean,
    onSwiped: (liked: Boolean) -> Unit,
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = offsetX.value / 60f
            }
            .pointerInput(isTop) {
                if (!isTop) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val x = offsetX.value
                        if (abs(x) > 250f) {
                            scope.launch {
                                offsetX.animateTo(if (x > 0) 2000f else -2000f, tween(180))
                                onSwiped(x > 0)
                            }
                        } else {
                            scope.launch { offsetX.animateTo(0f) }
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                    },
                )
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTop) 6.dp else 1.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
            val base = Color(profile.colorArgb)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(base, base.copy(alpha = 0.55f)))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    profile.name.first().toString(),
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Column(Modifier.padding(20.dp)) {
                Text(
                    "${profile.name}, ${profile.age}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "~${profile.distanceKm} km away · ${profile.intent}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(profile.bio, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    profile.tags.take(3).forEach { tag ->
                        AssistChip(onClick = {}, label = { Text(tag) })
                    }
                }
            }
        }
    }
}
