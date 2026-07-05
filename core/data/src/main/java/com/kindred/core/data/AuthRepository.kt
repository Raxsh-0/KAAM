package com.kindred.core.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class AuthRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    /** Uid of the signed-in user, without exposing Firebase types to feature modules. */
    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun signInWithGoogleIdToken(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return checkNotNull(result.user) { "Firebase returned no user" }
    }

    fun signOut() {
        auth.signOut()
    }
}
