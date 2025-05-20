package com.bredfen

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 80, host = "217.198.5.4", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
        configureSecurity()
        configureSerialization()
        configureHTTP()
        configureDatabases()
        configureRouting()
}
