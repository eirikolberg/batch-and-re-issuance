package no.kantega

import no.kantega.tools.AccessToken
import no.kantega.tools.CredentialService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.springframework.context.annotation.Import

@WebMvcTest(CredentialController::class)
@Import(CredentialService::class)
class CredentialControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser
    fun `requesting multiple credentials should work`() {
        val accessToken = AccessToken.create().value

        val requestBody = """
            {
                "format": "dc+sd-jwt",
                "proofs": {
                    "jwt": [
                        "eyJhbGciOiJSUzI1NiJ9.eyJub25jZSI6ImR1bW15In0.signature",
                        "eyJhbGciOiJSUzI1NiJ9.eyJub25jZSI6ImR1bW15In0.signature",
                        "eyJhbGciOiJSUzI1NiJ9.eyJub25jZSI6ImR1bW15In0.signature"
                    ]
                }
            }
        """.trimIndent()

        val result = mockMvc.post("/api/eidas/openid/credential") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
            header("Authorization", "Bearer $accessToken")
            with(csrf())
        }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.credentials") { exists() }
            }
            .andReturn()

        val body = result.response.contentAsString
        // Parse body as json and assert that credentials is a list with three elements:
        val bodyJson = JSONObject(body)
        assertThat(bodyJson.getJSONArray("credentials").length()).isEqualTo(3)

    }
}
