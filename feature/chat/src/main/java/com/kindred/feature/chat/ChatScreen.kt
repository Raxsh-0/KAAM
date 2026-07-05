package com.kindred.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            viewModel.peer?.let { peer ->
                Box(
                    modifier = Modifier.size(36.dp).background(Color(peer.colorArgb), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(peer.name.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Text(peer.name, style = MaterialTheme.typography.titleLarge)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages, key = { it.id }) { message ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (message.fromMe) Alignment.CenterEnd else Alignment.CenterStart,
                ) {
                    Surface(
                        color = if (message.fromMe) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.fromMe) 16.dp else 4.dp,
                            bottomEnd = if (message.fromMe) 4.dp else 16.dp,
                        ),
                    ) {
                        Text(
                            message.text,
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message…") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
            )
            IconButton(
                onClick = {
                    viewModel.send(draft)
                    draft = ""
                },
                enabled = draft.isNotBlank(),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
