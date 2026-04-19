package no.kantega

import com.auth0.jwt.JWT
import no.kantega.tools.AccessToken
import no.kantega.tools.SignAlgoritmFactory
import org.json.JSONObject
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class TokenController(
) {

    val accessTokenMap = mutableMapOf(
        "validCode123" to AccessToken.create(),
        "validPreAuthorizedCode123" to AccessToken.create()
    )

    @PostMapping(value = ["/api/eidas/openid/token"])
    fun token(
        @RequestParam(
            name = "code",
            required = false
        )
        code: String?,
        @RequestParam(
            name = "pre-authorized_code",
            required = false
        ) preAuthorizedCode: String?
    ): ResponseEntity<String> {

        if (code == null && preAuthorizedCode == null) {
            return ResponseEntity.badRequest().body("code or pre-authorized_code is null")
        }

        val accessToken = when {
            code.isNullOrBlank() && preAuthorizedCode.isNullOrBlank() -> {
                return ResponseEntity.badRequest().body("Either 'code' or 'pre-authorized_code' must be set.")
            }

            !code.isNullOrBlank() && !preAuthorizedCode.isNullOrBlank() -> {
                return ResponseEntity.badRequest().body("Only one of 'code' or 'pre-authorized_code' can be set.")
            }

            !code.isNullOrBlank() -> {
                // Pretend we look for a real access token
                accessTokenMap[code] ?: return ResponseEntity.badRequest().body("No session found for code: $code")
            }

            !preAuthorizedCode.isNullOrBlank() -> {
                // Pretend we look for a real access token
                accessTokenMap[preAuthorizedCode] ?: return ResponseEntity.badRequest().body("No session found for pre-authorized_code: $preAuthorizedCode")
            }

            else -> throw RuntimeException("No access token found")
        }

        val idToken = JWT.create()
            .withIssuer("bevisutsteder")
            .withSubject("dummySub")
            .sign(SignAlgoritmFactory().getRSA256Algorithm())

        val responseBody = JSONObject()
            .put("access_token", accessToken.value)
            .put("id_token", idToken)
            .put("token_type", "Bearer")

            .put("expires_in", 3600)
            .put("scope", "openid")

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody.toString())

    }
}
