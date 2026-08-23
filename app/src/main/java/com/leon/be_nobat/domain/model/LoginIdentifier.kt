package com.leon.be_nobat.domain.model

sealed interface LoginIdentifier {
    val value: String

    data class Email(override val value: String) : LoginIdentifier
    data class Mobile(override val value: String) : LoginIdentifier

    companion object {
        private val emailPattern = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
        private val iranianMobilePattern = Regex("^(?:\\+98|0098|98|0)?9\\d{9}$")

        fun parse(rawValue: String): Result<LoginIdentifier> {
            val normalized = rawValue.trim()
            return when {
                emailPattern.matches(normalized) -> Result.success(Email(normalized.lowercase()))
                iranianMobilePattern.matches(normalized) -> Result.success(
                    Mobile(normalizeIranianMobile(normalized))
                )
                else -> Result.failure(AuthException.InvalidIdentifier)
            }
        }

        private fun normalizeIranianMobile(value: String): String {
            val nationalNumber = when {
                value.startsWith("0098") -> value.removePrefix("0098")
                value.startsWith("+98") -> value.removePrefix("+98")
                value.startsWith("98") -> value.removePrefix("98")
                value.startsWith("0") -> value.removePrefix("0")
                else -> value
            }
            return "+98$nationalNumber"
        }
    }
}

sealed class AuthException : Exception() {
    data object InvalidIdentifier : AuthException()
    data object EmptyPassword : AuthException()
    data object InvalidCredentials : AuthException()
    data object TooManyRequests : AuthException()
    data object ServiceUnavailable : AuthException()
    data object NetworkUnavailable : AuthException()
    data object RequestTimedOut : AuthException()
    data object Unexpected : AuthException()
}
