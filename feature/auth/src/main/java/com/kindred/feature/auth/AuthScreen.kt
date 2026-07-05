package com.kindred.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(onSignedIn: () -> Unit, modifier: Modifier = Modifier) {
    var isAdult by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "KAAM", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "Dating for open-minded people",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 40.dp),
        ) {
            Checkbox(checked = isAdult, onCheckedChange = { isAdult = it })
            Text("I confirm I'm 18 or older", style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = onSignedIn,
            enabled = isAdult,
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text("Continue — sign-in arrives in Phase 1")
        }
    }
}
