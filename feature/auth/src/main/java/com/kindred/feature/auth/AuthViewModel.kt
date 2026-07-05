package com.kindred.feature.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.kindred.core.data.AuthRepository
import com.kindred.core.data.MockDatingRepository
import com.kindred.core.data.ProfileRepository
import com.kindred.core.data.model.OwnProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Error(val message: String) : AuthUiState
    data object SignedIn : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val localState: MockDatingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    val alreadySignedIn: Boolean get() = authRepository.currentUser != null

    /** Launches the Google account picker and signs the chosen account into Firebase. */
    fun signInWithGoogle(activityContext: Context) {
        runAuth {
            val webClientId = activityContext.getString(
                activityContext.resources.getIdentifier(
                    "default_web_client_id", "string", activityContext.packageName
                )
            )
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setServerClientId(webClientId)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()
            val result = withTimeout(25_000) {
                CredentialManager.create(activityContext).getCredential(activityContext, request)
            }
            val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val user = withTimeout(20_000) {
                authRepository.signInWithGoogleIdToken(googleCredential.idToken)
            }
            hydrateProfile(user, fallbackName = user.displayName ?: "You")
        }
    }

    fun signUpWithEmail(name: String, email: String, password: String) {
        runAuth {
            val user = withTimeout(20_000) { authRepository.signUpWithEmail(email.trim(), password) }
            hydrateProfile(user, fallbackName = name.ifBlank { "You" }, forceCreate = true)
        }
    }

    fun signInWithEmail(email: String, password: String) {
        runAuth {
            val user = withTimeout(20_000) { authRepository.signInWithEmail(email.trim(), password) }
            hydrateProfile(user, fallbackName = "You")
        }
    }

    /**
     * Nothing is written to Firestore here — a brand-new profile stays local-only
     * (onboardingComplete = false) until the onboarding screen saves it for real.
     * Firestore is also best-effort: a broken/misconfigured backend must never block
     * sign-in, so the load is time-boxed and failure just falls back to a local default.
     */
    private suspend fun hydrateProfile(user: FirebaseUser, fallbackName: String, forceCreate: Boolean = false) {
        val profile = if (forceCreate) {
            OwnProfile(name = fallbackName)
        } else {
            runCatching { withTimeout(8_000) { profileRepository.load(user.uid) } }.getOrNull()
                ?: OwnProfile(name = fallbackName)
        }
        localState.ownProfile.value = profile
    }

    private fun runAuth(block: suspend () -> Unit) {
        if (_state.value == AuthUiState.Loading) return
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                block()
                _state.value = AuthUiState.SignedIn
            } catch (e: TimeoutCancellationException) {
                _state.value = AuthUiState.Error("Timed out. Check your connection and try again.")
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(friendlyMessage(e))
            }
        }
    }

    private fun friendlyMessage(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "Password is too weak — use at least 6 characters."
        is FirebaseAuthInvalidCredentialsException -> "That email or password looks wrong."
        is FirebaseAuthUserCollisionException -> "An account with that email already exists — try signing in instead."
        is FirebaseAuthInvalidUserException -> "No account found with that email."
        else -> e.message ?: "Something went wrong. Try again."
    }

    fun clearError() {
        if (_state.value is AuthUiState.Error) _state.value = AuthUiState.Idle
    }
}
