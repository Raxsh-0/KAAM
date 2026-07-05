package com.kindred.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kindred.core.data.model.Curated

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Your profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = profile.name,
            onValueChange = viewModel::setName,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = profile.bio,
            onValueChange = viewModel::setBio,
            label = { Text("Bio") },
            placeholder = { Text("A line or two about you") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )

        Spacer(Modifier.height(20.dp))
        Text("I'm looking for", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Curated.intents.forEach { intent ->
                FilterChip(
                    selected = profile.intent == intent,
                    onClick = { viewModel.setIntent(intent) },
                    label = { Text(intent) },
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Interests", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Curated.tags.forEach { tag ->
                FilterChip(
                    selected = tag in profile.tags,
                    onClick = { viewModel.toggleTag(tag) },
                    label = { Text(tag) },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Saved on this device for now — syncs to your account in Phase 1.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
