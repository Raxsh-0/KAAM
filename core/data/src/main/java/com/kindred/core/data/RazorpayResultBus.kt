package com.kindred.core.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Razorpay's classic SDK delivers checkout results via a callback on the host Activity,
 * not the Composable that opened it — this bus bridges that callback back to whichever
 * screen is listening, since MainActivity has no direct reference to the active ViewModel.
 */
object RazorpayResultBus {
    sealed interface Result {
        data class Success(val paymentId: String) : Result
        data class Failure(val message: String) : Result
    }

    private val _events = MutableSharedFlow<Result>(extraBufferCapacity = 1)
    val events: SharedFlow<Result> = _events.asSharedFlow()

    fun emit(result: Result) {
        _events.tryEmit(result)
    }
}
