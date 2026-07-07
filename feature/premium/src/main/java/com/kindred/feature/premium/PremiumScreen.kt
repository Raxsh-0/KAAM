package com.kindred.feature.premium

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kindred.core.data.PremiumConfig
import com.kindred.core.data.RazorpayResultBus
import com.kindred.core.data.model.AdminProfileRow
import com.razorpay.Checkout
import org.json.JSONObject

@Composable
fun PremiumScreen(
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    var pendingPurchase by remember { mutableStateOf<Pair<String, String>?>(null) }
    var paymentError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        RazorpayResultBus.events.collect { result ->
            when (result) {
                is RazorpayResultBus.Result.Success -> {
                    pendingPurchase?.let { (name, notes) ->
                        viewModel.onPaymentSuccess(result.paymentId, name, notes)
                    }
                    pendingPurchase = null
                }
                is RazorpayResultBus.Result.Failure -> {
                    pendingPurchase = null
                    paymentError = result.message
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Premium", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            PremiumUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is PremiumUiState.Failed -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }
            PremiumUiState.NotConfigured -> PitchContent(
                priceLabel = "Coming soon",
                enabled = false,
                paymentError = null,
                onGetPremium = { _, _ -> },
            )
            PremiumUiState.NeedsPurchase -> PitchContent(
                priceLabel = viewModel.priceLabel,
                enabled = true,
                paymentError = paymentError,
                onGetPremium = { name, notes ->
                    val hostActivity = activity ?: return@PitchContent
                    paymentError = null
                    pendingPurchase = name to notes
                    val options = JSONObject().apply {
                        put("name", "Ally")
                        put("description", "Premium matchmaking")
                        put("currency", "INR")
                        put("amount", PremiumConfig.PRICE_PAISE)
                    }
                    val checkout = Checkout().apply { setKeyID(PremiumConfig.RAZORPAY_KEY_ID) }
                    checkout.open(hostActivity, options)
                },
            )
            is PremiumUiState.AwaitingCuration -> AwaitingContent(notes = s.notes)
            is PremiumUiState.HasCurated -> CuratedContent(
                profiles = s.profiles,
                interestedUids = s.interestedUids,
                onInterested = viewModel::markInterested,
            )
        }
    }
}

@Composable
private fun PitchContent(
    priceLabel: String,
    enabled: Boolean,
    paymentError: String?,
    onGetPremium: (name: String, notes: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(
            "Our team will personally review and prioritize genuine, compatible people for you.",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "No swiping — your matches are hand-picked and shown here, until you find your person.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("What are you looking for?") },
            placeholder = { Text("Tell us your priorities so our team can find the right people") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onGetPremium(name, notes) },
            enabled = enabled && name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (enabled) "Get Premium — $priceLabel" else priceLabel)
        }
        paymentError?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AwaitingContent(notes: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "You're premium! Our team is reviewing genuine, compatible people for you.",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Check back here — your curated matches will appear as soon as we've found them.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (notes.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            Text("What you told us you're looking for:", style = MaterialTheme.typography.labelLarge)
            Text(notes, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CuratedContent(
    profiles: List<AdminProfileRow>,
    interestedUids: Set<String>,
    onInterested: (String) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(profiles, key = { it.uid }) { profile ->
            Card(shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (profile.photoUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profile.photoUrl)
                                .allowHardware(false)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(profile.name.firstOrNull()?.toString() ?: "?", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(profile.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            profile.intent,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(profile.bio, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    }
                    Spacer(Modifier.width(8.dp))
                    val isInterested = profile.uid in interestedUids
                    Button(onClick = { onInterested(profile.uid) }, enabled = !isInterested) {
                        Text(if (isInterested) "Interested ✓" else "Interested")
                    }
                }
            }
        }
    }
}
