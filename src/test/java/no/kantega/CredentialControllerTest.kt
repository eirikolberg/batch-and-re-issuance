package no.kantega

import no.kantega.tools.AccessToken
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.eq

@WebMvcTest(CredentialController::class)
class CredentialControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser
    fun `requesting a credential with a valid access token returns credential response`() {
        val accessToken = AccessToken.create().value

        val requestBody = """
            {
                "format": "dc+sd-jwt",
                "proof": {
                    "proof_type": "jwt",
                    "jwt": "eyJhbGciOiJSUzI1NiJ9.eyJub25jZSI6ImR1bW15In0.signature"
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
                jsonPath("$.credentials.length()") { eq(1) }
            }
            .andReturn()

        val body = result.response.contentAsString
        assertThat(body).contains("credentials")
    }
}
