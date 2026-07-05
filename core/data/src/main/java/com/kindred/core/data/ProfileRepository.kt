package com.kindred.core.data

import com.google.firebase.firestore.FirebaseFirestore
import com.kindred.core.data.model.AdminProfileRow
import com.kindred.core.data.model.OwnProfile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/** Persists the user's own profile to Firestore (users/{uid}). */
@Singleton
class ProfileRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()

    suspend fun save(uid: String, profile: OwnProfile) {
        db.collection("users").document(uid).set(
            mapOf(
                "name" to profile.name,
                "bio" to profile.bio,
                "intent" to profile.intent,
                "tags" to profile.tags.toList(),
                "photoUrl" to profile.photoUrl,
                "onboardingComplete" to profile.onboardingComplete,
            )
        ).await()
    }

    suspend fun load(uid: String): OwnProfile? {
        val snap = db.collection("users").document(uid).get().await()
        if (!snap.exists()) return null
        return OwnProfile(
            name = snap.getString("name") ?: "You",
            bio = snap.getString("bio") ?: "",
            intent = snap.getString("intent") ?: "Open to exploring",
            tags = (snap.get("tags") as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet(),
            photoUrl = snap.getString("photoUrl"),
            onboardingComplete = snap.getBoolean("onboardingComplete") ?: false,
        )
    }

    /** Admin-only: enforced by a matching Firestore rule, not just this call site. */
    suspend fun listAll(): List<AdminProfileRow> {
        val snap = db.collection("users").get().await()
        return snap.documents.map { doc ->
            AdminProfileRow(
                uid = doc.id,
                name = doc.getString("name") ?: "?",
                bio = doc.getString("bio") ?: "",
                intent = doc.getString("intent") ?: "",
                photoUrl = doc.getString("photoUrl"),
            )
        }
    }

    /** Admin-only: enforced by a matching Firestore rule, not just this call site. */
    suspend fun deleteProfile(uid: String) {
        db.collection("users").document(uid).delete().await()
    }
}
