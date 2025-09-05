package cn.com.lushunming.server

import cn.com.lushunming.util.HttpClientUtil
import cn.com.lushunming.util.Util
import com.google.gson.Gson
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.utils.io.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

class ProxyServer {

    val logger = LoggerFactory.getLogger(ProxyServer::class.java)
    val partSize = 1024 * 1024 // 1MB
    val THREAD_NUM = Runtime.getRuntime().availableProcessors()


    fun buildProxyUrl(url: String, headers: Map<String, String>, port: Int): String {
        return "http://127.0.0.1:$port/proxy?url=${
            Util.base64Encode(url.toByteArray(Charset.defaultCharset()))
        }&headers=${
            Util.base64Encode(
                Gson().toJson(headers).toByteArray(
                    Charset.defaultCharset()
                )
            )
        }"
    }


    suspend fun proxyAsync(
        url: String, headers: Map<String, String>, dir: String, call: ApplicationCall
    ) {

        try {
            logger.info("--proxyMultiThread: THREAD_NUM: $THREAD_NUM")
            logger.info("--proxyMultiThread: url: $url")
            logger.info("--proxyMultiThread: headers: ${Gson().toJson(headers)}")


            var rangeHeader = call.request.headers[HttpHeaders.Range]
            //没有range头
            if (rangeHeader.isNullOrEmpty()) {
                // 处理初始请求
                rangeHeader = "bytes=0-"
            }
            val header = headers.toMutableMap()
            header.put(HttpHeaders.Range, rangeHeader)

            // 解析范围请求
            val (startPoint, endPoint) = parseRangePoint(
                rangeHeader
            )
            logger.info("startPoint: $startPoint; endPoint: $endPoint")
            val contentLength = getContentLength(url, header)
            logger.info("contentLength: $contentLength")
            val finalEndPoint = if (endPoint == -1L) contentLength - 1 else endPoint

            call.response.headers.apply {
                append(HttpHeaders.Connection, "keep-alive")
                append(HttpHeaders.ContentLength, (finalEndPoint - startPoint + 1).toString())
                append(HttpHeaders.ContentRange, "bytes $startPoint-$finalEndPoint/$contentLength")
            }
            call.response.status(HttpStatusCode.PartialContent)

            // 使用流式响应
            call.respondBytesWriter() {
                var currentStart = startPoint


                // 启动生产者协程下载数据

                while (currentStart <= finalEndPoint) {
                    //第几块
                    val index = currentStart / partSize
                    //偏移量
                    val offset = currentStart % partSize
                    val fileStart = index * partSize
                    val fileEnd = minOf(index * partSize + partSize - 1, finalEndPoint)

                    val fileName = "${fileStart}-${fileEnd}.video"
                    val file = File(dir, fileName)
                    if (file.exists()) {
                        val fileInputStream = FileInputStream(file)
                        fileInputStream.skip(offset)
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                            writeFully(buffer, 0, bytesRead) // 写入到输出流中，直到达到所需的长度或文件结束
                        }
                    } else {
                        writeFully(getVideoStream(currentStart, finalEndPoint, url, headers))
                    }
                    currentStart = fileEnd + 1
                }


            }
        } catch (e: Exception) {
            logger.info("error: ${e.message}")
            call.respondText("error: ${e.message}", ContentType.Text.Plain)
        } finally {

        }
    }


    // 辅助函数（需要实现）
    private fun parseRangePoint(rangeHeader: String): Pair<Long, Long> {
        // 实现范围解析逻辑
        val regex = """bytes=(\d+)-(\d*)""".toRegex()
        val match = regex.find(rangeHeader) ?: return 0L to -1L
        val start = match.groupValues[1].toLong()
        val end = match.groupValues[2].takeIf { it.isNotEmpty() }?.toLong() ?: -1L
        return start to end
    }

    private suspend fun getContentLength(url: String, headers: Map<String, String>): Long {
        val header = headers.toMutableMap()
        header.put(HttpHeaders.Range, "bytes=0-1")
        // 实现获取内容长度逻辑
        val res = HttpClientUtil.get(url, header)

        return res.headers[HttpHeaders.ContentRange]?.split("/")?.get(1)?.toLong() ?: 0L
    }

    private suspend fun getVideoStream(
        start: Long, end: Long, url: String, headers: Map<String, String>
    ): ByteArray {
        val header = headers.toMutableMap()
        // 实现分段下载逻辑
        logger.info("getVideoStream: $start-$end; ")
        header[HttpHeaders.Range] = "bytes=$start-$end"
        val res = HttpClientUtil.get(url, header)
        val body = res.bodyAsBytes()
        return body
    }


    private suspend fun downloadRange(
        url: String, headerNew: Map<String, String>
    ): HttpResponse = HttpClientUtil.get(url, headerNew)

}