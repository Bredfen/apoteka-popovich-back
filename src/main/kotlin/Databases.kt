package com.bredfen

import com.mongodb.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.*

fun Application.configureDatabases() {
    val mongoDatabase = connectToMongoDB()
    val productService = ProductService(mongoDatabase)
    routing {
        authenticate("auth-jwt") {
            post("/createProduct") {
                val product = call.receive<Product>()
                val id = productService.create(product)
                call.respond(HttpStatusCode.Created, id)
            }
            // Read product
            get("/products/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
                productService.read(id)?.let { product ->
                    call.respond(product)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
            // Update product
            put("/products/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
                val product = call.receive<Product>()
                productService.update(id, product)?.let {
                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
            // Delete product
            delete("/products/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
                productService.delete(id)?.let {
                    call.respond(HttpStatusCode.OK)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
            // List all products by searching
            get("/products") {
                val search = call.request.queryParameters["search"]
                val products = productService.search(search)
                call.respond(products)
            }
        }
    }
}



/**
 * Establishes connection with a MongoDB database.
 *
 * The following configuration properties (in application.yaml/application.conf) can be specified:
 * * `db.mongo.user` username for your database
 * * `db.mongo.password` password for the user
 * * `db.mongo.host` host that will be used for the database connection
 * * `db.mongo.port` port that will be used for the database connection
 * * `db.mongo.maxPoolSize` maximum number of connections to a MongoDB server
 * * `db.mongo.database.name` name of the database
 *
 * IMPORTANT NOTE: in order to make MongoDB connection working, you have to start a MongoDB server first.
 * See the instructions here: https://www.mongodb.com/docs/manual/administration/install-community/
 * all the paramaters above
 *
 * @returns [MongoDatabase] instance
 * */



private const val MONGO_DATABASE_NAME = "myDatabase"

fun Application.connectToMongoDB(): MongoDatabase {
    val uri = "mongodb://217.198.5.4:27017"

    println("Connecting to MongoDB at URI: $uri")
    val mongoClient = MongoClients.create(uri)
    val database = mongoClient.getDatabase(MONGO_DATABASE_NAME)

    environment.monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return database
}
