package com.argent.core.common.result

/**
 * A deliberate, exhaustive result type for operations that can fail as part of
 * normal flow (network down, validation failed, conflict). Exceptions are for
 * bugs; [Outcome] is for expected failure the caller must handle.
 *
 * Named `Outcome` rather than `Result` to avoid clashing with `kotlin.Result`,
 * whose semantics (carries a `Throwable`, encourages `getOrThrow`) we don't want.
 */
sealed interface Outcome<out T, out E> {
    data class Success<out T>(val value: T) : Outcome<T, Nothing>
    data class Failure<out E>(val error: E) : Outcome<Nothing, E>
}

inline fun <T, E, R> Outcome<T, E>.fold(
    onSuccess: (T) -> R,
    onFailure: (E) -> R,
): R = when (this) {
    is Outcome.Success -> onSuccess(value)
    is Outcome.Failure -> onFailure(error)
}

inline fun <T, E, R> Outcome<T, E>.map(transform: (T) -> R): Outcome<R, E> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

fun <T> T.asSuccess(): Outcome<T, Nothing> = Outcome.Success(this)
fun <E> E.asFailure(): Outcome<Nothing, E> = Outcome.Failure(this)
