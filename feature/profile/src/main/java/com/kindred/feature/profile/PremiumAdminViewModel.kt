package com.kindred.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kindred.core.data.PremiumRepository
import com.kindred.core.data.ProfileRepository
import com.kindred.core.data.model.AdminProfileRow
import com.kindred.core.data.model.PremiumRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PremiumAdminUiState {
    data object Loading : PremiumAdminUiState
    data class Loaded(val requests: List<PremiumRequest>, val allProfiles: List<AdminProfileRow>) : PremiumAdminUiState
    data class Failed(val message: String) : PremiumAdminUiState
}

@HiltViewModel
class PremiumAdminViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<PremiumAdminUiState>(PremiumAdminUiState.Loading)
    val state: StateFlow<PremiumAdminUiState> = _state.asStateFlow()

    private val _selectedUid = MutableStateFlow<String?>(null)
    val selectedUid: StateFlow<String?> = _selectedUid.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = PremiumAdminUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                // Both calls are admin-only, enforced by matching Firestore rules.
                val requests = premiumRepository.listAll()
                val allProfiles = profileRepository.listAll()
                PremiumAdminUiState.Loaded(requests, allProfiles)
            } catch (e: Exception) {
                PremiumAdminUiState.Failed(e.message ?: "Couldn't load. Check your connection.")
            }
        }
    }

    fun selectRequest(uid: String) {
        _selectedUid.value = uid
    }

    fun backToList() {
        _selectedUid.value = null
    }

    fun toggleCurated(requestUid: String, profileUid: String, currentlyAssigned: Boolean) {
        val loaded = _state.value as? PremiumAdminUiState.Loaded ?: return
        val updatedRequests = loaded.requests.map { req ->
            if (req.uid != requestUid) req
            else req.copy(
                curatedUids = if (currentlyAssigned) req.curatedUids - profileUid else req.curatedUids + profileUid
            )
        }
        _state.value = loaded.copy(requests = updatedRequests)
        if (currentlyAssigned) premiumRepository.unassignCurated(requestUid, profileUid)
        else premiumRepository.assignCurated(requestUid, profileUid)
    }

    fun markCompleted(requestUid: String) {
        val loaded = _state.value as? PremiumAdminUiState.Loaded ?: return
        _state.value = loaded.copy(
            requests = loaded.requests.map { if (it.uid == requestUid) it.copy(status = "completed") else it }
        )
        premiumRepository.markCompleted(requestUid)
    }
}
