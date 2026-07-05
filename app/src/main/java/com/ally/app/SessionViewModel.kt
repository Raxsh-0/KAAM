package com.ally.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kindred.core.data.AuthRepository
import com.kindred.core.data.MockDatingRepository
import com.kindred.core.data.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SessionViewModel @Inject constructor(
    authRepository: AuthRepository,
    profileRepository: ProfileRepository,
    private val localState: MockDatingRepository,
) : ViewModel() {

    val startSignedIn: Boolean = authRepository.currentUser != null

    /** Read right after a sign-in/sign-up completes, once hydrateProfile has set local state. */
    fun needsOnboarding(): Boolean = !localState.ownProfile.value.onboardingComplete

    init {
        // Returning user: hydrate the local profile from Firestore in the background.
        authRepository.currentUser?.let { user ->
            viewModelScope.launch {
                runCatching { profileRepository.load(user.uid) }.getOrNull()?.let {
                    localState.ownProfile.value = it
                }
            }
        }
    }
}
