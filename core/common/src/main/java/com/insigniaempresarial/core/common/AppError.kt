package com.insigniaempresarial.core.common

/**
 * Typed application errors. User-facing copy lives in Android string resources
 * and is resolved via ErrorMessageMapper — domain/data never embed UI strings.
 */
sealed interface AppError {
    data class Network(
        val code: Int? = null,
        val detail: String? = null,
    ) : AppError

    data class Database(
        val detail: String? = null,
    ) : AppError

    data class Validation(
        val reason: ValidationReason,
    ) : AppError

    data class Conflict(
        val detail: String? = null,
    ) : AppError

    data class Unknown(
        val detail: String? = null,
        val cause: Throwable? = null,
    ) : AppError
}
