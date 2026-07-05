package com.kindred.core.data

import com.kindred.core.data.model.ChatMessage
import com.kindred.core.data.model.OwnProfile
import com.kindred.core.data.model.Profile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * In-memory stand-in for the Firestore-backed repositories arriving in Phase 1+.
 * Interface shape mirrors the planned backend: deck, like/pass, matches, chat.
 */
@Singleton
class MockDatingRepository @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val allProfiles = listOf(
        Profile("p1", "Aisha", 26, 3, "Casual & fun", listOf("Night owl", "Foodie", "Music"), "Here for good coffee and better banter.", 0xFFB90D5D),
        Profile("p2", "Rohan", 28, 5, "Open to exploring", listOf("Gym", "Trekking", "Movies"), "Weekday hustle, weekend treks.", 0xFF7B4E7F),
        Profile("p3", "Priya", 24, 2, "Short-term", listOf("Dancing", "Music", "Art"), "Will out-dance you. Try me.", 0xFF00696E),
        Profile("p4", "Kabir", 30, 8, "Casual & fun", listOf("Gaming", "Movies", "Foodie"), "Co-op partner wanted, in games and in biryani.", 0xFF8F4C00),
        Profile("p5", "Meera", 27, 4, "Adventure partner", listOf("Traveler", "Trekking", "Art"), "42 countries down. Company welcome.", 0xFF386A20),
        Profile("p6", "Arjun", 29, 6, "Open to exploring", listOf("Music", "Night owl", "Foodie"), "Vinyl collector, midnight maggi chef.", 0xFF004397),
        Profile("p7", "Zoya", 25, 3, "Friends first", listOf("Art", "Movies", "Music"), "Gallery hopping and film festivals.", 0xFF9A25AE),
        Profile("p8", "Dev", 31, 10, "Short-term", listOf("Gym", "Gaming", "Night owl"), "Deadlifts by day, ranked by night.", 0xFF974810),
        Profile("p9", "Ananya", 26, 5, "Casual & fun", listOf("Foodie", "Dancing", "Traveler"), "Rating every dosa place in the city.", 0xFF006C4C),
        Profile("p10", "Vikram", 33, 7, "Adventure partner", listOf("Trekking", "Traveler", "Gym"), "Sunrise summits > sunrise scrolling.", 0xFF5D5F00),
    )

    // Deterministic mock: these profiles "already liked you", so liking them is a match.
    private val likesYou = allProfiles.filterIndexed { i, _ -> i % 2 == 0 }.map { it.id }.toSet()

    private val _deck = MutableStateFlow(allProfiles)
    val deck: StateFlow<List<Profile>> = _deck.asStateFlow()

    private val _matches = MutableStateFlow<List<Profile>>(emptyList())
    val matches: StateFlow<List<Profile>> = _matches.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messagesByProfile: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    val ownProfile = MutableStateFlow(OwnProfile())

    private var nextMessageId = 0L

    fun pass(profileId: String) {
        _deck.update { it.filterNot { p -> p.id == profileId } }
    }

    /** Returns the profile if the like produced a mutual match. */
    fun like(profileId: String): Profile? {
        val profile = _deck.value.firstOrNull { it.id == profileId } ?: return null
        _deck.update { it.filterNot { p -> p.id == profileId } }
        if (profile.id !in likesYou) return null
        _matches.update { it + profile }
        return profile
    }

    fun profile(profileId: String): Profile? = allProfiles.firstOrNull { it.id == profileId }

    fun messagesFor(profileId: String): Flow<List<ChatMessage>> =
        _messages.map { it[profileId].orEmpty() }

    fun sendMessage(profileId: String, text: String) {
        if (text.isBlank()) return
        append(profileId, ChatMessage(nextMessageId++, fromMe = true, text = text.trim()))
        scope.launch {
            delay(1200)
            val count = _messages.value[profileId].orEmpty().count { !it.fromMe }
            append(profileId, ChatMessage(nextMessageId++, fromMe = false, text = cannedReplies[count % cannedReplies.size]))
        }
    }

    private fun append(profileId: String, message: ChatMessage) {
        _messages.update { it + (profileId to (it[profileId].orEmpty() + message)) }
    }

    private val cannedReplies = listOf(
        "Hey! Nice to match with you 😄",
        "Haha, tell me more",
        "That sounds fun! What else?",
        "What are you up to this weekend?",
        "Okay you have my attention 😄",
    )
}
