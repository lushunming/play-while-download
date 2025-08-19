package cn.com.lushunming.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory
import java.io.File

class M3u8ProxyServer {

    val logger = LoggerFactory.getLogger(M3u8ProxyServer::class.java)


    suspend fun proxyAsyncM3u8(
        url: String, headers: Map<String, String>, dir: String, call: ApplicationCall
    ) {
        //所在目录
        try {

            call.response.header(
                HttpHeaders.ContentType, "application/vnd.apple.mpegurl"
            )
            call.respondFile(File(dir), "local.m3u8")
        } catch (e: Exception) {
            logger.info("error: ${e.message}")
            call.respondText("error: ${e.message}", ContentType.Text.Plain)
        } finally {

        }
    }


}