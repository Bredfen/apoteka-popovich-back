package com.bredfen

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.types.ObjectId
import com.bredfen.LoginDto
import com.bredfen.LoginResponseDto
import io.ktor.server.plugins.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
/**
 * Service for user registration and authentication.
 * Uses MongoDB 'users' collection and issues JWT tokens.
 */
class AuthService(
    private val database: MongoDatabase,
) {
    private val collection: MongoCollection<Document>


    init {
        // Ensure 'users' collection exists
        val existing = database.listCollectionNames().toList()
        if (!existing.contains("users")) {
            database.createCollection("users")
        }
        collection = database.getCollection("users")
    }

    /**
     * Registers a new user by inserting their credentials into the database.
     * @param dto User credentials
     * @return ID of the newly created user
     */

    suspend fun register(dto: LoginDto): String = withContext(Dispatchers.IO) {
        val existingUser = collection.find(Filters.eq("login", dto.login)).firstOrNull()
        if (existingUser != null) {
            throw UserAlreadyExistsException("User with login ${dto.login} already exists")
        }
        val document = dto.toDocument(dto)
        collection.insertOne(document)
        return@withContext document.getObjectId("_id").toString()
    }

    /**
     * checks user credentials if valid return true else false
    **/

    suspend fun checkUser(dto: LoginDto): Boolean = withContext(Dispatchers.IO) {
        val existingUser = collection.find(Filters.eq("login", dto.login)).firstOrNull()
        if (existingUser != null) {
            val password = existingUser.getString("password")
            return@withContext password == dto.password
        }
        return@withContext false
    }


}

// Custom exception for authentication failures
data class AuthenticationException(override val message: String): RuntimeException(message)
data class UserAlreadyExistsException(override val message: String): RuntimeException(message)