package net.k1ra.succubotapp.features.base.model

sealed class LoadingStatus<T>

data class Success<T>(val result: T) : LoadingStatus<T>()
data class Loading<T>(val text: String) : LoadingStatus<T>()
data class Failure<T>(val code: Int, val error: String? = null) : LoadingStatus<T>()