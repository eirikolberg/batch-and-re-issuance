package no.kantega

import com.auth0.jwt.JWT
import no.kantega.dontChangeThese.AccessToken
import no.kantega.dontChangeThese.SignAlgoritmFactory
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
        // TODO: Hint! Her må det gjøres noe!
    ): ResponseEntity<String> {
        // TODO: Oppgave 1 - Re-issuance

        // TODO: 1.1
        //  Det finnes flere grunner til å skulle gjennutstede bevis.
        //  Nevn 3. Du finner mer info her:
        //  https://eudi.dev/2.8.0/discussion-topics/b-re-issuance-and-batch-issuance-of-pids-and-attestations/#:~:text=3%20Reasons%20for%20re%2Dissuance

        // TODO: 1.2
        //  For å kunne støtte re-issuance, må vi returnere et refresh token
        //  Legg til et refresh token i responsen
        //  Dette er spesifisert i OID4VCI: https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-refreshing-issued-credentia

        // TODO: 1.3
        //  Når wallet kommer til dette endepunktet med sitt refresh-token, skal vi gi ut et nytt access token.
        //  Implementer dette, og kjør testen TokenControllerTest

        // TODO: BONUS-OPPGAVE
        //  Det finnes flere måter å trigge en re-issuance.
        //  Det er spesielt vanskelig når grunnen er at data i beviset har endret seg.
        //  Hva sier ARF er riktig måte å trigge re-issuance når data i beviset har endret seg?
        //  https://eudi.dev/2.8.0/discussion-topics/b-re-issuance-and-batch-issuance-of-pids-and-attestations/#:~:text=4.3%20Triggers%20for%20the%20issuance%20process


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
            // TODO: Hint! Her må det gjøres noe!
            .put("expires_in", 3600)
            .put("scope", "openid")

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody.toString())

    }
}
