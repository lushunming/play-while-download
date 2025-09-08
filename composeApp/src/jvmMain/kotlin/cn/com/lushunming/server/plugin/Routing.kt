package cn.com.lushunming.server.plugin

import cn.com.lushunming.models.Downloads
import cn.com.lushunming.server.M3u8ProxyServer
import cn.com.lushunming.server.ProxyServer
import cn.com.lushunming.service.ConfigService
import cn.com.lushunming.service.DownloadService
import cn.com.lushunming.service.TaskService
import cn.com.lushunming.util.Constant
import cn.com.lushunming.util.Util
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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

        get("/video") {
            val url = Util.base64Decode(call.request.queryParameters["url"]!!)
            val type = call.parameters["type"]
            var player = ""
            player = if (type == "M3u8") {
                "hls"
            } else {
                "auto"
            }
            val html = """
    
		  <!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DPlayer HLS(m3u8)播放Demo</title>
    <!-- 引入 DPlayer 样式 -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/dplayer/dist/DPlayer.min.css">
</head>
<body>
    <!-- 播放器容器 -->
    <div id="dplayer"></div>
    
    <!-- 引入 hls.js 库（必须在 DPlayer 之前引入） -->
    <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
    <!-- 引入 DPlayer -->
    <script src="https://cdn.jsdelivr.net/npm/dplayer/dist/DPlayer.min.js"></script>
    
    <script>
						  
	var source="$url"
        // 初始化 DPlayer
        const dp = new DPlayer({
            container: document.getElementById('dplayer'), // 容器元素
            video: {
                url: source, // 替换为你的 m3u8 地址
                type: '${player}', // 明确指定视频类型为 HLS
            },
			lang:'zh-cn'
            // 可选：开启弹幕功能（若需直播弹幕，需自建WebSocket后端）
           
            // 可选：开启直播模式（如果播放的是直播流）
            // live: true,
            // 可选：自定义主题色
            // theme: '#FADFA3'
        });
        
        // 你可以通过 dp 对象控制播放器，例如 dp.play()、dp.pause()
        console.log('播放器实例:', dp);
		dp.fullScreen.request('web');
		dp.play()
    </script>
</body>
</html>
""".trimIndent()
            call.respondText(
                contentType = ContentType.parse("text/html"), text = html
            )
        }


        get("/proxy") {
            logger.info("代理中: ${call.parameters["url"]}")
            val path = configService.getConfig()?.downloadPath ?: Constant.downloadPath

            val url = Util.base64Decode(call.parameters["url"]!!)
            val header: Map<String, String> = Gson().fromJson<Map<String, String>>(
                Util.base64Decode(call.parameters["headers"]!!), MutableMap::class.java
            )
            val dir = path + File.separator + Util.md5(url)

            if (url.contains("m3u8")) {

                M3u8ProxyServer().proxyAsyncM3u8(url, header, dir, call)
            } else {
                ProxyServer().proxyAsync(
                    url, header, dir,call
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

