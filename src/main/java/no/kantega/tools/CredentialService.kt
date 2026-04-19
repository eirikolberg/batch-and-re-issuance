package no.kantega.tools

import id.walt.oid4vc.requests.CredentialRequest
import no.kantega.CredentialResult
import no.kantega.IssuedCredential
import org.springframework.stereotype.Service

@Service
class CredentialService {
    fun getCredential(credentialRequest: CredentialRequest, accessToken: AccessToken): CredentialResult {
        val dummyCredential = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJkdW1teSJ9.signature"
        return CredentialResult.Valid(IssuedCredential(dummyCredential))
    }


}