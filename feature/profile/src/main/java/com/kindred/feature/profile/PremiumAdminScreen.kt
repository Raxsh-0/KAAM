package com.kindred.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kindred.core.data.model.AdminProfileRow
import com.kindred.core.data.model.PremiumRequest

@Composable
fun PremiumAdminScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumAdminViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedUid by viewModel.selectedUid.collectAsStateWithLifecycle()

    when (val s = state) {
        PremiumAdminUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is PremiumAdminUiState.Failed -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(s.message, color = MaterialTheme.colorScheme.error)
        }
        is PremiumAdminUiState.Loaded -> {
            val selected = s.requests.firstOrNull { it.uid == selectedUid }
            if (selected != null) {
                PremiumRequestDetail(
                    request = selected,
                    allProfiles = s.allProfiles,
                    onBack = viewModel::backToList,
                    onToggle = { profileUid, assigned -> viewModel.toggleCurated(selected.uid, profileUid, assigned) },
                    onMarkCompleted = { viewModel.markCompleted(selected.uid) },
                    modifier = modifier,
                )
            } else {
                PremiumRequestList(
                    requests = s.requests,
                    onBack = onBack,
                    onSelect = viewModel::selectRequest,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun PremiumRequestList(
    requests: List<PremiumRequest>,
    onBack: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(4.dp))
            Text("Premium requests", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(8.dp))

        if (requests.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No premium customers yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(requests, key = { it.uid }) { req ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(req.uid) }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(req.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            req.priorityNotes.ifBlank { "No notes" },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        "${req.curatedUids.size} curated · ${req.status}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (req.status == "completed") MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumRequestDetail(
    request: PremiumRequest,
    allProfiles: List<AdminProfileRow>,
    onBack: () -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onMarkCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(request.name, style = MaterialTheme.typography.headlineSmall)
                Text(request.priorityNotes.ifBlank { "No notes" }, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(8.dp))

        if (request.status != "completed") {
            TextButton(onClick = onMarkCompleted) { Text("Mark as completed — found their partner") }
        } else {
            Text("Completed ✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))
        Text("Pick profiles to show this customer:", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(allProfiles, key = { it.uid }) { profile ->
                val assigned = profile.uid in request.curatedUids
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(profile.uid, assigned) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = assigned, onCheckedChange = { onToggle(profile.uid, assigned) })
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(profile.name.firstOrNull()?.toString() ?: "?", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(profile.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            profile.intent,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (assigned) {
                        Icon(Icons.Filled.Check, contentDescription = "Assigned", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
