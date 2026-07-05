package com.kindred.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kindred.core.data.MockDatingRepository
import com.kindred.core.data.model.ChatMessage
import com.kindred.core.data.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MockDatingRepository,
) : ViewModel() {

    private val profileId: String = checkNotNull(savedStateHandle["profileId"])

    val peer: Profile? = repository.profile(profileId)

    val messages: StateFlow<List<ChatMessage>> =
        repository.messagesFor(profileId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun send(text: String) {
        repository.sendMessage(profileId, text)
    }
}
