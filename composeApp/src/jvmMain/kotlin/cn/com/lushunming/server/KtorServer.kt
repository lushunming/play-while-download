package cn.com.lushunming.server

import cn.com.lushunming.configTemplate
import cn.com.lushunming.server.plugin.configureHTTP
import cn.com.lushunming.server.plugin.configureMonitoring
import cn.com.lushunming.server.plugin.configureRouting
import cn.com.lushunming.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import cn.com.lushunming.service.DatabaseFactory
import cn.com.lushunming.util.Constant

fun startServer(args: Array<String>) {
    embeddedServer(
        Netty, port = Constant.port, host = "0.0.0.0", module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
    configTemplate()

}
