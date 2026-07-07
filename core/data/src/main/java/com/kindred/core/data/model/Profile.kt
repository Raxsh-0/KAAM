package com.kindred.core.data.model

data class Profile(
    val id: String,
    val name: String,
    val age: Int,
    val distanceKm: Int,
    val intent: String,
    val tags: List<String>,
    val bio: String,
    val colorArgb: Long,
)

data class OwnProfile(
    val name: String = "You",
    val bio: String = "",
    val intent: String = "Open to exploring",
    val tags: Set<String> = emptySet(),
    val photoUrl: String? = null,
    val onboardingComplete: Boolean = false,
)

data class AdminProfileRow(
    val uid: String,
    val name: String,
    val bio: String,
    val intent: String,
    val photoUrl: String?,
)

/**
 * A concierge/paid-matchmaking request. Curation is manual — [curatedUids] is populated
 * by an admin/curator picking real profiles, not an algorithm. [status] tracks payment
 * separately from curation progress so the two can be reasoned about independently.
 */
data class PremiumRequest(
    val uid: String = "",
    val name: String = "",
    val priorityNotes: String = "",
    val status: String = "awaiting_payment", // awaiting_payment | active | completed
    val paymentId: String? = null,
    val curatedUids: List<String> = emptyList(),
    val interestedUids: List<String> = emptyList(),
    val createdAt: Long = 0L,
)

data class ChatMessage(
    val id: Long,
    val fromMe: Boolean,
    val text: String,
)

/** Curated lists — free-form intent/tag text is a Play-policy risk, so these are fixed. */
object Curated {
    val intents = listOf(
        "Casual & fun",
        "Open to exploring",
        "Short-term",
        "Friends first",
        "Adventure partner",
    )
    val tags = listOf(
        "Night owl", "Foodie", "Traveler", "Gym", "Music",
        "Movies", "Gaming", "Art", "Dancing", "Trekking",
    )
}
