package cn.com.lushunming.server.plugin

import cn.com.lushunming.models.Downloads
import cn.com.lushunming.server.M3u8ProxyServer
import cn.com.lushunming.server.ProxyServer
import cn.com.lushunming.service.ConfigService
import cn.com.lushunming.service.DownloadService
import cn.com.lushunming.service.TaskService
import cn.com.lushunming.util.Constant
import cn.com.lushunming.util.Util
import cn.com.lushunming.viewmodel.TaskViewModel
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.DownloadStatus
import model.Task
import org.koin.java.KoinJavaComponent.inject
import org.slf4j.LoggerFactory
import java.io.File


fun Application.configureRouting() {
    val port = Constant.port
    val logger = LoggerFactory.getLogger(Application::class.java)
    val taskService = TaskService()

    val configService = ConfigService()










    routing {
        get("/") {
            call.respondText("ktor is running .....")
        }


        get("/proxy") {
            logger.info("代理中: ${call.parameters["url"]}")
            val path = configService.getConfig()?.downloadPath ?: Constant.downloadPath

            val url = Util.base64Decode(call.parameters["url"]!!)
            val header: Map<String, String> = Gson().fromJson<Map<String, String>>(
                Util.base64Decode(call.parameters["headers"]!!), MutableMap::class.java
            )
            if (url.contains("m3u8")) {
                val dir = path + File.separator + Util.md5(url)
                M3u8ProxyServer().proxyAsyncM3u8(url, header, dir, call)
            } else {
                ProxyServer().proxyAsync(
                    url, header, call
                )
            }

        }

        /**
         * 代理ts
         */

        get("/ts/{path}/{tsName}") {
            logger.info("路径: ${call.parameters["path"]}")
            logger.info("tsName: ${call.parameters["tsName"]}")
            val path = configService.getConfig()?.downloadPath ?: Constant.downloadPath

            val url = call.parameters["path"] ?: ""
            val tsName = call.parameters["tsName"] ?: ""

            call.response.header(
                HttpHeaders.ContentType, "video/mp2t"
            )
            call.respondFile(File(path + File.separator + url + File.separator + tsName))

        }


        /**
         * 提交下载
         */
        post("/download") {
            val downloadPath = configService.getConfig()?.downloadPath ?: Constant.downloadPath
            val download = call.receive<Downloads>()
            val urlParam = download.list[0].url
            val headerParam = download.list[0].headers
            val url = ProxyServer().buildProxyUrl(urlParam, headerParam, port)
            val id = Util.md5(urlParam)
            val old = taskService.getTaskById(id);
            if (old != null) {
                call.respondText("已经存在")
                return@post
            }

            val taskProcess: DownloadService by inject(DownloadService::class.java)
            taskProcess.addTask(urlParam, downloadPath, headerParam, id, download.list[0].filename, url)

            call.respondText(url)
        }


    }
}

