package com.bredfen

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bson.Document

/**
 * Data Transfer Object for user credentials.
 * @property login User's login name
 * @property password User's password
 */
@Serializable
data class LoginDto(
    val login: String,
    val password: String
){
    
    fun toDocument(dto: LoginDto): Document = Document().apply {
        append("login", dto.login)
        append("password", dto.password)
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): LoginDto = LoginDto(
            login = document.getString("login"),
            password = document.getString("password")
        )
    }
}

@Serializable
data class LoginResponseDto(
    val token: String
)
