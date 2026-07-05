package com.kindred.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kindred.core.data.MockDatingRepository
import com.kindred.core.data.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MatchRow(val profile: Profile, val lastMessage: String?)

@HiltViewModel
class MatchesViewModel @Inject constructor(
    repository: MockDatingRepository,
) : ViewModel() {

    val matches: StateFlow<List<MatchRow>> =
        combine(repository.matches, repository.messagesByProfile) { matches, messages ->
            matches.map { profile ->
                MatchRow(profile, messages[profile.id]?.lastOrNull()?.text)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
