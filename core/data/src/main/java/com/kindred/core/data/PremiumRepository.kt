package com.kindred.core.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.kindred.core.data.model.PremiumRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/** Concierge/paid-matchmaking requests, stored at premiumRequests/{uid}. */
@Singleton
class PremiumRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private fun doc(uid: String) = db.collection("premiumRequests").document(uid)

    suspend fun getMine(uid: String): PremiumRequest? {
        val snap = doc(uid).get().await()
        if (!snap.exists()) return null
        return snap.toPremiumRequest(uid)
    }

    /** Called once, right after a customer's payment succeeds. */
    suspend fun submit(uid: String, name: String, priorityNotes: String, paymentId: String) {
        doc(uid).set(
            mapOf(
                "name" to name,
                "priorityNotes" to priorityNotes,
                "status" to "active",
                "paymentId" to paymentId,
                "curatedUids" to emptyList<String>(),
                "interestedUids" to emptyList<String>(),
                "createdAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    fun markInterested(uid: String, profileUid: String) {
        doc(uid).update("interestedUids", FieldValue.arrayUnion(profileUid))
    }

    /** Admin-only: enforced by a matching Firestore rule, not just this call site. */
    suspend fun listAll(): List<PremiumRequest> {
        val snap = db.collection("premiumRequests").get().await()
        return snap.documents.map { it.toPremiumRequest(it.id) }
    }

    /** Admin-only: enforced by a matching Firestore rule, not just this call site. */
    fun assignCurated(uid: String, profileUid: String) {
        doc(uid).update("curatedUids", FieldValue.arrayUnion(profileUid))
    }

    fun unassignCurated(uid: String, profileUid: String) {
        doc(uid).update("curatedUids", FieldValue.arrayRemove(profileUid))
    }

    fun markCompleted(uid: String) {
        doc(uid).update("status", "completed")
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPremiumRequest(uid: String) = PremiumRequest(
        uid = uid,
        name = getString("name") ?: "?",
        priorityNotes = getString("priorityNotes") ?: "",
        status = getString("status") ?: "awaiting_payment",
        paymentId = getString("paymentId"),
        curatedUids = (get("curatedUids") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        interestedUids = (get("interestedUids") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        createdAt = getLong("createdAt") ?: 0L,
    )
}
