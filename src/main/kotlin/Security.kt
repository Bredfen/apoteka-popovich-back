package com.bredfen

import com.bredfen.LoginDto
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.http.content.staticResources
import java.sql.Date

/**
 * Configures authentication routes and security for the application.
 */

fun Application.configureSecurity() {
    // Initialize AuthService with MongoDB and JWT settings
    val mongoDb = connectToMongoDB()
    val secret = "secret"
    val issuer = "http://localhost:80"
    val audience = "myAudience"
    val myRealm = "myRealm"
    val authService = AuthService(mongoDb)
    

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build())
                    validate { credential ->
                        if (credential.payload.getClaim("username").asString() != "") {
                            JWTPrincipal(credential.payload)
                        } else {
                            null
                        }
                    }
                    challenge { defaultScheme, realm ->
                        call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
                    }
        }
        
    }

    routing {
        post("/login") {
            val user = call.receive<LoginDto>()
            // Check username and password
            val userId = authService.checkUser(user)
            if (!userId) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", user.login)
                .sign(Algorithm.HMAC256(secret))
            call.respond(hashMapOf("token" to token))
        }


        post("/auth/register") {
            val dto = call.receive<LoginDto>()
            //log 
            print(dto)
            try {
                val userId = authService.register(dto)
                call.respond(HttpStatusCode.Created, mapOf("userId" to userId))
            } catch (e: Throwable) {
                print(e)
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid registration data")
            }
        }
    }
}