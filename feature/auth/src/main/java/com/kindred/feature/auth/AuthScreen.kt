package com.kindred.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthScreen(
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isAdult by remember { mutableStateOf(false) }
    var showEmailForm by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loading = state == AuthUiState.Loading

    LaunchedEffect(state) {
        if (state == AuthUiState.SignedIn) onSignedIn()
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Ally", style = MaterialTheme.typography.displaySmall)
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

        if (!showEmailForm) {
            Button(
                onClick = { viewModel.signInWithGoogle(context) },
                enabled = isAdult && !loading,
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(0.8f),
            ) {
                if (loading) LoadingDot() else Text("Continue with Google")
            }
            TextButton(
                onClick = { showEmailForm = true },
                enabled = isAdult,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text("Continue with email instead")
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth(0.85f).padding(top = 16.dp)) {
                if (isSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = {
                        if (isSignUp) viewModel.signUpWithEmail(name, email, password)
                        else viewModel.signInWithEmail(email, password)
                    },
                    enabled = isAdult && !loading && email.isNotBlank() && password.isNotBlank() &&
                        (!isSignUp || name.isNotBlank()),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                ) {
                    if (loading) LoadingDot() else Text(if (isSignUp) "Create account" else "Sign in")
                }
                TextButton(
                    onClick = { isSignUp = !isSignUp },
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(if (isSignUp) "Already have an account? Sign in" else "New here? Create an account")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                OutlinedButton(
                    onClick = { viewModel.signInWithGoogle(context) },
                    enabled = isAdult && !loading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Continue with Google instead")
                }
                TextButton(
                    onClick = { showEmailForm = false },
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text("Back")
                }
            }
        }

        (state as? AuthUiState.Error)?.let { error ->
            Text(
                text = error.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun LoadingDot() {
    CircularProgressIndicator(
        modifier = Modifier.size(18.dp),
        strokeWidth = 2.dp,
        color = MaterialTheme.colorScheme.onPrimary,
    )
}
