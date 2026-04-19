package no.kantega

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.assertj.core.api.Assertions.assertThat

@WebMvcTest(TokenController::class)
class TokenControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser
    fun `requesting a token with a valid code returns access token response`() {
        val result = mockMvc.post("/api/eidas/openid/token") {
            param("code", "validCode123")
            with(csrf())
        }
            .andExpect {
                status { isOk() }
                content { contentType("application/json") }
                jsonPath("$.access_token") { exists() }
                jsonPath("$.id_token") { exists() }
                jsonPath("$.token_type") { value("Bearer") }
                jsonPath("$.expires_in") { value(3600) }
                jsonPath("$.scope") { value("openid") }
            }
            .andReturn()

        val body = result.response.contentAsString
        assertThat(body).contains("access_token")
    }
}

