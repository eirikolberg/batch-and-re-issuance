package no.kantega

import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.oid4vc.requests.CredentialRequest
import no.kantega.tools.CredentialService
import no.kantega.tools.toAccessToken
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
class CredentialController(private val credentialService: CredentialService) {

  @CrossOrigin(origins = ["https://wallet.verifiablecredentials.dev/"])
  @PostMapping(value = ["/api/eidas/openid/credential"])
  @Throws(IOException::class)
  // TODO: Return proper error responses: https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-error-response
  fun credential(request: RequestEntity<*>?): Any? {
    val body = (request?.body as MutableMap<Any, Any>)

    val authorization = request.headers.getFirst("Authorization")
    if (authorization == null) {
      return ResponseEntity.badRequest().body("missing authorization header")
    }

    val accessTokenFromWallet = authorization.substring(7).toAccessToken()


    // The "format" field is removed in OID4VCI v1.0. Until walt library is updated we add it to the body by parsing it from credential_configuration_id
    val bodyFormat = body["format"] as? String
    val credentialConfigurationId = body["credential_configuration_id"] as? String

    val formatFromId = credentialConfigurationId ?.let {
      CredentialConfigId.create(credentialConfigurationId).getFormat().value
    }

    body["format"] = bodyFormat ?: formatFromId ?: throw IllegalArgumentException("Could not parse format, current version of Walt oid4vc library needs it")

    if(body["proof"] == null && body["proofs"] != null) {
      val proofType = (body["proofs"] as Map<String, Any>).keys.single()
      val proof = (body["proofs"] as Map<String, List<Any>>)[proofType]!!.first()
      body["proof"] = mapOf(
        "proof_type" to proofType,
        proofType to proof
      )
    }

    // TODO validate body.proof (JWT) signature

    val credentialRequest = CredentialRequest.fromJSON(body.toJsonObject())

    val credentialResult = credentialService.getCredential(
      credentialRequest = credentialRequest,
      accessToken = accessTokenFromWallet
    )

    when (credentialResult) {
      is CredentialResult.InvalidClientsFault -> {
        logger.info("Failed to issue credential: ${credentialResult.errorMessage}")
        return ResponseEntity.badRequest().body(credentialResult.errorMessage)
      }

      is CredentialResult.Valid -> {
        logger.debug("Credential issued successfully")

        val response = JSONObject()
        response.put("credential", credentialResult.credential.value) //TODO: This can be removed in OID4VCI v1.0
        val credentialJson = JSONObject(mapOf("credential" to credentialResult.credential.value))
        response.put("credentials", JSONArray(listOf(credentialJson)))

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response.toString())
      }
    }
  }
}
