package no.kantega

import com.auth0.jwt.JWT
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(TokenController::class)
class TokenControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser
    fun `exchanging a code gives a refresh token which can be used to get a new access token`() {
        // Step 1: exchange code for access token + refresh token
        val codeResponse = mockMvc.post("/api/eidas/openid/token") {
            param("code", "validCode123")
            with(csrf())
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.access_token") { exists() }
                jsonPath("$.refresh_token") { exists() }
            }
            .andReturn()

        val refreshToken = JSONObject(codeResponse.response.contentAsString).getString("refresh_token")

        // Step 2: use refresh token to obtain a new access token
        val refreshResponse = mockMvc.post("/api/eidas/openid/token") {
            param("grant_type", "refresh_token")
            param("refresh_token", refreshToken)
            with(csrf())
        }
            .andExpect {
                status { isOk() }
                content { contentType("application/json") }
                jsonPath("$.access_token") { exists() }
                jsonPath("$.refresh_token") { exists() }
                jsonPath("$.token_type") { value("Bearer") }
                jsonPath("$.expires_in") { value(3600) }
                jsonPath("$.scope") { value("openid") }
            }
            .andReturn()

        val newAccessToken = JSONObject(refreshResponse.response.contentAsString).getString("access_token")
        assertThat(JWT.decode(newAccessToken).issuer).isNotBlank()
    }
}
