package com.kaam.app.update

import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

/** Checks for a published update once per app launch and prompts if one exists. */
@Composable
fun UpdatePrompt() {
    val context = LocalContext.current
    var update by remember { mutableStateOf<UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        update = UpdateChecker.check(UpdateChecker.installedVersionCode(context))
    }

    update?.let { info ->
        AlertDialog(
            onDismissRequest = { update = null },
            title = { Text("Update available") },
            text = {
                Text(
                    buildString {
                        append("KAAM ${info.versionName} is out.")
                        if (info.notes.isNotBlank()) append("\n\n${info.notes}")
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, info.apkUrl.toUri()))
                    update = null
                }) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { update = null }) { Text("Later") }
            },
        )
    }
}
