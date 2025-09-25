package cn.com.lushunming.server

import cn.com.lushunming.configureSerialization
import cn.com.lushunming.server.plugin.configureHTTP
import cn.com.lushunming.server.plugin.configureMonitoring
import cn.com.lushunming.server.plugin.configureRouting
import cn.com.lushunming.util.Constant
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer

fun startServer(args: Array<String>) {
    embeddedServer(
        CIO, port = Constant.port, host = "0.0.0.0", module = Application::module
    ).start(wait = true).application.log
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()


}
