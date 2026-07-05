package com.kindred.feature.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kindred.core.data.model.Curated

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    isOnboarding: Boolean = false,
    onOnboardingComplete: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val photoUploadState by viewModel.photoUploadState.collectAsStateWithLifecycle()

    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(viewModel::uploadPhoto) }

    LaunchedEffect(isOnboarding, saveState) {
        if (isOnboarding && saveState == SaveState.Saved) onOnboardingComplete()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            if (isOnboarding) "Set up your profile" else "Your profile",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = photoUploadState != PhotoUploadState.Uploading) {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.Center,
        ) {
            when {
                photoUploadState == PhotoUploadState.Uploading -> CircularProgressIndicator()
                profile.photoUrl != null -> AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profile.photoUrl)
                        .allowHardware(false)
                        .build(),
                    contentDescription = "Your photo",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                else -> Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add photo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        (photoUploadState as? PhotoUploadState.Failed)?.let { failed ->
            Text(
                failed.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

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
        Button(
            onClick = { if (isOnboarding) viewModel.completeOnboarding() else viewModel.save() },
            enabled = saveState != SaveState.Saving && (!isOnboarding || profile.name.isNotBlank()),
        ) {
            Text(
                when {
                    saveState == SaveState.Saving -> "Saving…"
                    saveState == SaveState.Saved && !isOnboarding -> "Saved ✓"
                    isOnboarding -> "Continue"
                    else -> "Save profile"
                }
            )
        }
        (saveState as? SaveState.Failed)?.let { failed ->
            Text(
                failed.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (!isOnboarding) {
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = {
                viewModel.signOut()
                onSignedOut()
            }) {
                Text("Sign out")
            }
        }
    }
}
