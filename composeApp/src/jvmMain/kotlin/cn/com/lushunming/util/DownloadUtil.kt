package cn.com.lushunming.util

import cn.com.lushunming.util.Constant.maxRetries
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.io.File

object DownloadUtil {
    val logger = LoggerFactory.getLogger(DownloadUtil::class.java)
    suspend fun downloadWithRetry(
        url: String, headers: Map<String, String>,  dir: File, fileName: String
    ) {


        var retryCount = 0
        var success = false

        while (retryCount < maxRetries && !success) {
            try {
                val file = File(dir, fileName)
                if (file.exists()) {
                    logger.info("文件 $fileName 已存在")
                    success = true
                    continue
                }
                val response = HttpClientUtil.get(url, headers)
                file.outputStream().use { outputStream ->
                    outputStream.write(response.bodyAsBytes())
                }
                success = true
            } catch (e: Exception) {
                retryCount++
                if (retryCount == maxRetries) {

                    logger.info("文件 $fileName 下载失败: ${e.message}")
                    throw e
                }

                delay(1000L * retryCount)
            }
        }
    }

}