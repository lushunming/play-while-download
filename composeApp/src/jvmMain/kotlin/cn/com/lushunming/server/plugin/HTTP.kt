package cn.com.lushunming.server.plugin

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureHTTP() {


    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        // allowHeader(HttpHeaders.Authorization)
        allowNonSimpleContentTypes = true
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
        //allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }


    /*routing {
        swaggerUI(path = "openapi")
    }*/
   /* routing {
        staticResources("/static", "static")
    }*/
}

