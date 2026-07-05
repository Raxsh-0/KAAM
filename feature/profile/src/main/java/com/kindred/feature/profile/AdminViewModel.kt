package com.kindred.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kindred.core.data.ProfileRepository
import com.kindred.core.data.model.AdminProfileRow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminUiState {
    data object Loading : AdminUiState
    data class Loaded(val profiles: List<AdminProfileRow>) : AdminUiState
    data class Failed(val message: String) : AdminUiState
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = AdminUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                AdminUiState.Loaded(profileRepository.listAll())
            } catch (e: Exception) {
                AdminUiState.Failed(e.message ?: "Couldn't load profiles. Check your connection.")
            }
        }
    }

    /** Removes it from the list immediately; the delete call runs in the background. */
    fun deleteProfile(uid: String) {
        val current = (_state.value as? AdminUiState.Loaded)?.profiles ?: return
        _state.value = AdminUiState.Loaded(current.filterNot { it.uid == uid })
        viewModelScope.launch {
            runCatching { profileRepository.deleteProfile(uid) }
        }
    }
}
