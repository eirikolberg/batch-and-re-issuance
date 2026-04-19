package no.kantega.dontChangeThese

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT

data class AccessToken(val value: String) {

  fun getDecodedJWT(): DecodedJWT {
    return JWT.decode(value)
  }

  override fun toString(): String {
    return value
  }

  companion object {
    fun create(
    ): AccessToken {
      val jwtString = JWT.create()
        .withIssuer("Kantega Issuer")
        .withAudience("Kantega Issuer")
        .withClaim("scope", "id123")
        .sign(SignAlgoritmFactory().getRSA256Algorithm())
      return AccessToken(jwtString)
    }
  }
}

fun String.toAccessToken(): AccessToken {
  return AccessToken(this)
}