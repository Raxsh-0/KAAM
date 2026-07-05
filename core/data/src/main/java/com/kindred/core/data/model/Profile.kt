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
