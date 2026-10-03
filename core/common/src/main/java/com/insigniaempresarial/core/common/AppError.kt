package com.insigniaempresarial.core.common

sealed interface AppError {
    val message: String

    data class Network(override val message: String, val code: Int? = null) : AppError
    data class Database(override val message: String) : AppError
    data class Validation(override val message: String) : AppError
    data class Conflict(override val message: String) : AppError
    data class Unknown(override val message: String, val cause: Throwable? = null) : AppError
}
