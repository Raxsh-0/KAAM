package com.kindred.feature.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kindred.core.data.AuthRepository
import com.kindred.core.data.PremiumConfig
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

sealed interface PremiumUiState {
    data object Loading : PremiumUiState
    data object NotConfigured : PremiumUiState
    data object NeedsPurchase : PremiumUiState
    data class AwaitingCuration(val notes: String) : PremiumUiState
    data class HasCurated(val profiles: List<AdminProfileRow>, val interestedUids: Set<String>) : PremiumUiState
    data class Failed(val message: String) : PremiumUiState
}

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val premiumRepository: PremiumRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<PremiumUiState>(PremiumUiState.Loading)
    val state: StateFlow<PremiumUiState> = _state.asStateFlow()

    val priceLabel: String get() = PremiumConfig.PRICE_LABEL

    init {
        refresh()
    }

    fun refresh() {
        val uid = authRepository.currentUid ?: return
        _state.value = PremiumUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val request = premiumRepository.getMine(uid)
                toUiState(request)
            } catch (e: Exception) {
                PremiumUiState.Failed(e.message ?: "Couldn't load. Check your connection.")
            }
        }
    }

    private suspend fun toUiState(request: PremiumRequest?): PremiumUiState = when {
        request == null -> if (PremiumConfig.isConfigured) PremiumUiState.NeedsPurchase else PremiumUiState.NotConfigured
        request.curatedUids.isEmpty() -> PremiumUiState.AwaitingCuration(request.priorityNotes)
        else -> PremiumUiState.HasCurated(
            profiles = profileRepository.getProfilesByUids(request.curatedUids),
            interestedUids = request.interestedUids.toSet(),
        )
    }

    /** Called once Razorpay checkout succeeds — see PremiumScreen for where this hooks in. */
    fun onPaymentSuccess(paymentId: String, name: String, priorityNotes: String) {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            runCatching { premiumRepository.submit(uid, name, priorityNotes, paymentId) }
            refresh()
        }
    }

    fun markInterested(profileUid: String) {
        val uid = authRepository.currentUid ?: return
        val current = _state.value as? PremiumUiState.HasCurated ?: return
        _state.value = current.copy(interestedUids = current.interestedUids + profileUid)
        premiumRepository.markInterested(uid, profileUid)
    }
}
