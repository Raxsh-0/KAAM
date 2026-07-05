package com.kindred.feature.discovery

import androidx.lifecycle.ViewModel
import com.kindred.core.data.MockDatingRepository
import com.kindred.core.data.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val repository: MockDatingRepository,
) : ViewModel() {

    val deck: StateFlow<List<Profile>> = repository.deck

    private val _matchEvent = MutableStateFlow<Profile?>(null)
    val matchEvent: StateFlow<Profile?> = _matchEvent.asStateFlow()

    fun like(profileId: String) {
        _matchEvent.value = repository.like(profileId)
    }

    fun pass(profileId: String) {
        repository.pass(profileId)
    }

    fun dismissMatch() {
        _matchEvent.value = null
    }
}
