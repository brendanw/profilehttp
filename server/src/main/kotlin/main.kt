package com.basebeta

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.withCharset
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

val html_utf8 = ContentType.Text.Html.withCharset(Charsets.UTF_8)

val port = Integer.valueOf(System.getenv("PORT") ?: "5003")

var server = embeddedServer(Netty, port) {
    install(DefaultHeaders)
    install(ConditionalHeaders)
    install(Compression)
    install(CallLogging)

    install(ContentNegotiation) {
        gson {
            // Configure Gson here
        }
    }

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(environment.classLoader, "templates")
    }

    install(StatusPages) {
        exception<Exception> { exception ->
            call.respond(FreeMarkerContent("error.ftl", exception, "", html_utf8))
        }
    }

    install(Routing) {
        // All endpoints that require an Authorization header should go in this block
        get("/locations") {
            call.respond(message = "hello world")
        }

        get("/mediumlocations") {
            var locations = mutableListOf<Location>()
            for (i in 0..100) {
                locations.add(Location())
            }
            call.respond(message = locations)
        }


        get("error") {
            throw IllegalStateException("An invalid place to be …")
        }
    }
}

fun main(args: Array<String>) {
    server.start()
    println(Thread.currentThread().name)
    /*GlobalScope.launch {
        async(Dispatchers.IO) {
            db.getCollection("locations").find().asFlow().collect {
                //print(it)
            }
        }
    }*/
}

