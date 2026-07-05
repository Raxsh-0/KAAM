package com.kindred.feature.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kindred.core.data.AuthRepository
import com.kindred.core.data.MockDatingRepository
import com.kindred.core.data.ProfileRepository
import com.kindred.core.data.model.OwnProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        if (_state.value == AuthUiState.Loading) return
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
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
                val result = CredentialManager.create(activityContext)
                    .getCredential(activityContext, request)
                val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val user = authRepository.signInWithGoogleIdToken(googleCredential.idToken)

                // Pull the saved profile, or create one from the Google account on first sign-in.
                val profile = runCatching { profileRepository.load(user.uid) }.getOrNull()
                    ?: OwnProfile(name = user.displayName ?: "You").also {
                        runCatching { profileRepository.save(user.uid, it) }
                    }
                localState.ownProfile.value = profile

                _state.value = AuthUiState.SignedIn
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(e.message ?: "Sign-in failed. Try again.")
            }
        }
    }

    fun clearError() {
        if (_state.value is AuthUiState.Error) _state.value = AuthUiState.Idle
    }
}
