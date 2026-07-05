package com.kindred.feature.profile

import androidx.lifecycle.ViewModel
import com.kindred.core.data.MockDatingRepository
import com.kindred.core.data.model.OwnProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: MockDatingRepository,
) : ViewModel() {

    val profile: StateFlow<OwnProfile> = repository.ownProfile

    fun setName(name: String) = repository.ownProfile.update { it.copy(name = name) }

    fun setBio(bio: String) = repository.ownProfile.update { it.copy(bio = bio) }

    fun setIntent(intent: String) = repository.ownProfile.update { it.copy(intent = intent) }

    fun toggleTag(tag: String) = repository.ownProfile.update {
        it.copy(tags = if (tag in it.tags) it.tags - tag else it.tags + tag)
    }
}
