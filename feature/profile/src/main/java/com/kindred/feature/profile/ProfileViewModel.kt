package com.kindred.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kindred.core.data.AuthRepository
import com.kindred.core.data.MockDatingRepository
import com.kindred.core.data.ProfileRepository
import com.kindred.core.data.model.OwnProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data object Saved : SaveState
    data class Failed(val message: String) : SaveState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val localState: MockDatingRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val profile: StateFlow<OwnProfile> = localState.ownProfile

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun setName(name: String) = edit { it.copy(name = name) }
    fun setBio(bio: String) = edit { it.copy(bio = bio) }
    fun setIntent(intent: String) = edit { it.copy(intent = intent) }
    fun toggleTag(tag: String) = edit {
        it.copy(tags = if (tag in it.tags) it.tags - tag else it.tags + tag)
    }

    private fun edit(transform: (OwnProfile) -> OwnProfile) {
        localState.ownProfile.update(transform)
        if (_saveState.value != SaveState.Idle) _saveState.value = SaveState.Idle
    }

    fun save() {
        val uid = authRepository.currentUid ?: run {
            _saveState.value = SaveState.Failed("Not signed in")
            return
        }
        _saveState.value = SaveState.Saving
        viewModelScope.launch {
            _saveState.value = try {
                profileRepository.save(uid, localState.ownProfile.value)
                SaveState.Saved
            } catch (e: Exception) {
                SaveState.Failed(e.message ?: "Couldn't save. Check your connection.")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        localState.ownProfile.value = OwnProfile()
    }
}
