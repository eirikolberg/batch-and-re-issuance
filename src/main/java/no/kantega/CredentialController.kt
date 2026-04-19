package no.kantega

import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.oid4vc.requests.CredentialRequest
import no.kantega.dontChangeThese.CredentialResult
import no.kantega.dontChangeThese.CredentialService
import no.kantega.dontChangeThese.toAccessToken
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
class CredentialController(private val credentialService: CredentialService) {

  @PostMapping(value = ["/api/eidas/openid/credential"])
  @Throws(IOException::class)
  fun credential(request: RequestEntity<*>?): Any? {
    // TODO: Oppgave 2 - Batch issuance

    // TODO: 2.1
    //  Hvorfor trenger vi batch issuance? Hvilke(t) problem skal det løse?
    //  https://eudi.dev/2.8.0/discussion-topics/a-privacy-risks-and-mitigations/#:~:text=3%20Possible%20mitigation%20measures%20for%20Relying%20Party%20linkability%20within%20the%20current%20ARF

    // TODO: 2.2
    //  I OID4VCI står det at bevis kan variere i tre "dimensjoner".
    //  Når vi utsteder en batch med bevis, skal vi bare variere 1 dimensjon.
    //  Hvilken?
    //  https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-batch-credential-issuance

    // TODO: 2.3
    //  Implementer batch issuance i sin enkleste form.
    //  Kjør testen CredentialControllerTest

    val body = (request?.body as MutableMap<Any, Any>)

    val authorization = request.headers.getFirst("Authorization")
    if (authorization == null) {
      return ResponseEntity.badRequest().body("missing authorization header")
    }

    val accessTokenFromWallet = authorization.substring(7).toAccessToken()


    // The "format" field is removed in OID4VCI v1.0. Until walt library is updated, we still must add it to the body
    val bodyFormat = body["format"] as? String
    val backup_format = "dc+sd-jwt"

    body["format"] = bodyFormat ?: backup_format

    if(body["proof"] == null && body["proofs"] != null) {
      // TODO: Hint! Her må det gjøres noe!
      val proofType = (body["proofs"] as Map<String, Any>).keys.single()
      val proof = (body["proofs"] as Map<String, List<Any>>)[proofType]!!.first()
      body["proof"] = mapOf(
        "proof_type" to proofType,
        proofType to proof
      )
    }

    val credentialRequest = CredentialRequest.fromJSON(body.toJsonObject())

    val credentialResult = credentialService.getCredential(
      credentialRequest = credentialRequest,
      accessToken = accessTokenFromWallet
    )

    when (credentialResult) {
      is CredentialResult.InvalidClientsFault -> {
        return ResponseEntity.badRequest().body(credentialResult.errorMessage)
      }

      is CredentialResult.Valid -> {
        // TODO: Hint! Her må det gjøres noe!
        val response = JSONObject()
        val credentialJson = JSONObject(mapOf("credential" to credentialResult.credential.value))
        response.put("credentials", JSONArray(listOf(credentialJson)))

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response.toString())
      }
    }
  }
}
