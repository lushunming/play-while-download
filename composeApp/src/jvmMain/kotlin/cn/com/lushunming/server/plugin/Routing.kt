package cn.com.lushunming.server.plugin

import androidx.lifecycle.viewModelScope
import cn.com.lushunming.models.Downloads
import cn.com.lushunming.server.M3u8ProxyServer
import cn.com.lushunming.server.ProxyServer
import cn.com.lushunming.server.startDownload
import cn.com.lushunming.service.ConfigService
import cn.com.lushunming.service.TaskService
import cn.com.lushunming.util.Constant
import cn.com.lushunming.util.Constant.jobMap
import cn.com.lushunming.util.Util
import cn.com.lushunming.viewmodel.TaskViewModel
import com.google.gson.Gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.port
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.thymeleaf.ThymeleafContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.DownloadStatus
import model.Task
import org.slf4j.LoggerFactory
import java.io.File


fun Application.configureRouting() {
    val port = environment.config.port
    val logger = LoggerFactory.getLogger(Application::class.java)
    val taskService = TaskService()

    val configService = ConfigService()
    val service = TaskService()
    val viewModel = TaskViewModel()









    routing {
        get("/") {
            call.respondText("ktor is running .....")
        }
        get("/video/{id}") {
            val id = call.parameters["id"]
            val task = taskService.getTaskById(id!!)
            call.respond(
                ThymeleafContent(
                    "video", mapOf(
                        "url" to (task?.url ?: ""),
                        "type" to (task?.type ?: "application/x-mpegURL")
                    )
                )
            )
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
            val path = configService.getConfig()?.downloadPath ?: Constant.downloadPath
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
            var type = ContentType.Video.MP4.toString()
            //M3U8开始下载
            if (urlParam.contains("m3u8")) {
                type = "application/x-mpegURL"
                val dir = path + File.separator + Util.md5(urlParam)
                val job = viewModel.viewModelScope.launch(Dispatchers.IO) {
                    File(dir).mkdirs()
                    val headerFile = File(dir, "header.tmp")
                    headerFile.writeText(Util.json(headerParam))
                    startDownload(
                        dir, urlParam, headerParam
                    ) { taskId: String, progress: Int ->
                        viewModel.updateProgress(id, progress)
                    }
                }
                jobMap[id] = job
            }

            viewModel.addTask(
                Task(
                    id, download.list[0].filename, url, urlParam, type, 0, DownloadStatus.PENDING
                )
            )

            call.respondText(url)
        }


    }
}
