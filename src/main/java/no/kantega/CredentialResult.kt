package no.kantega

sealed class CredentialResult {
    data class InvalidClientsFault(val errorMessage: String) : CredentialResult()
    data class Valid(val credential: IssuedCredential) : CredentialResult()
}

data class IssuedCredential(val value: String)

