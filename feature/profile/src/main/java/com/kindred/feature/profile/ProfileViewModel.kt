package com.kindred.feature.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kindred.core.data.AdminConfig
import com.kindred.core.data.AuthRepository
import com.kindred.core.data.MockDatingRepository
import com.kindred.core.data.PhotoRepository
import com.kindred.core.data.ProfileRepository
import com.kindred.core.data.model.OwnProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data object Saved : SaveState
    data class Failed(val message: String) : SaveState
}

sealed interface PhotoUploadState {
    data object Idle : PhotoUploadState
    data object Uploading : PhotoUploadState
    data class Failed(val message: String) : PhotoUploadState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val localState: MockDatingRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    val profile: StateFlow<OwnProfile> = localState.ownProfile
    val isAdmin: Boolean = AdminConfig.isAdmin(authRepository.currentUserEmail)

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val _photoUploadState = MutableStateFlow<PhotoUploadState>(PhotoUploadState.Idle)
    val photoUploadState: StateFlow<PhotoUploadState> = _photoUploadState.asStateFlow()

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

    fun uploadPhoto(uri: Uri) {
        if (_photoUploadState.value == PhotoUploadState.Uploading) return
        _photoUploadState.value = PhotoUploadState.Uploading
        viewModelScope.launch {
            _photoUploadState.value = try {
                val url = photoRepository.uploadPhoto(uri)
                edit { it.copy(photoUrl = url) }
                PhotoUploadState.Idle
            } catch (e: TimeoutCancellationException) {
                PhotoUploadState.Failed("Upload timed out. Check your connection and try again.")
            } catch (e: Exception) {
                PhotoUploadState.Failed(e.message ?: "Couldn't upload photo. Try again.")
            }
        }
    }

    fun save() {
        val uid = authRepository.currentUid ?: run {
            _saveState.value = SaveState.Failed("Not signed in")
            return
        }
        _saveState.value = SaveState.Saving
        viewModelScope.launch {
            _saveState.value = try {
                withTimeout(10_000) { profileRepository.save(uid, localState.ownProfile.value) }
                SaveState.Saved
            } catch (e: TimeoutCancellationException) {
                SaveState.Failed("Timed out. Check your connection and try again.")
            } catch (e: Exception) {
                SaveState.Failed(e.message ?: "Couldn't save. Check your connection.")
            }
        }
    }

    fun completeOnboarding() {
        edit { it.copy(onboardingComplete = true) }
        save()
    }

    fun signOut() {
        authRepository.signOut()
        localState.ownProfile.value = OwnProfile()
    }
}
