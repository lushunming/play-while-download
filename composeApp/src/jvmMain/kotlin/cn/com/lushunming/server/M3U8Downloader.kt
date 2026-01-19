package cn.com.lushunming.server

import androidx.lifecycle.viewModelScope
import cn.com.lushunming.models.DownloadProgressStatus
import cn.com.lushunming.util.Constant.batchSize
import cn.com.lushunming.util.Constant.maxRetries
import cn.com.lushunming.util.HttpClientUtil
import cn.com.lushunming.util.Util
import cn.com.lushunming.viewmodel.TaskViewModel
import io.ktor.client.statement.*
import io.ktor.utils.io.copyTo
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.koin.java.KoinJavaComponent.inject
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class M3U8Downloader(private val outputDir: String) {

    val logger = LoggerFactory.getLogger(M3U8Downloader::class.java)

    // 解析M3U8文件
    suspend fun parseM3U8(m3u8Url: String, headers: Map<String, String>): M3U8Info {
        val response = HttpClientUtil.get(m3u8Url, headers)
        val content = response.bodyAsText()

        val lines = content.lines()
        val tsUrls = mutableListOf<String>()
        val infoLines = mutableListOf<String>()
        var keyUrl: String? = null
        var iv: String? = null

        lines.forEach { line ->
            when {
                line.startsWith("#EXT-X-KEY") -> {
                    // 解析加密信息
                    val methodMatch = Regex("METHOD=([^,]+)").find(line)
                    val uriMatch = Regex("URI=\"([^\"]+)\"").find(line)
                    val ivMatch = Regex("IV=([^,]+)").find(line)

                    if (methodMatch?.groupValues?.get(1) == "AES-128") {
                        keyUrl = uriMatch?.groupValues?.get(1)
                        iv = ivMatch?.groupValues?.get(1)
                    }
                }

                line.startsWith("#EXTINF") -> {
                    infoLines.add(line)
                }

                !line.startsWith("#") && line.isNotBlank() -> {
                    // 处理TS文件URL
                    tsUrls.add(
                        if (line.startsWith("http")) line else resolveRelativeUrl(
                            m3u8Url, line
                        )
                    )
                }
            }
        }

        return M3U8Info(
            infoLines, tsUrls, keyUrl?.let { resolveRelativeUrl(m3u8Url, it) }, iv, m3u8Url
        )
    }

    private fun resolveRelativeUrl(baseUrl: String, relativePath: String): String {
        return if (relativePath.startsWith("http")) {
            relativePath
        } else {
            val base = baseUrl.substringBeforeLast("/")
            "$base/$relativePath"
        }
    }

    // 在M3U8Downloader类中添加以下方法

    private suspend fun downloadFile(url: String, headers: Map<String, String>, outputFile: File) {
        val response = HttpClientUtil.get(url, headers)
        outputFile.outputStream().use { outputStream ->
            outputStream.write(response.bodyAsBytes())
        }
    }

    suspend fun downloadAllFiles(
        m3u8Info: M3U8Info,
        headers: Map<String, String>,
        callback: (id: String, progress: Int, status: DownloadProgressStatus) -> Unit
    ) {
        // 创建输出目录
        val dir = File(outputDir)
        if (!dir.exists()) dir.mkdirs()

        // 下载密钥文件
        m3u8Info.keyUrl?.let { keyUrl ->
            val keyFile = File(dir, "key.key")
            downloadFile(keyUrl, headers, keyFile)
        }
        logger.info("下载密钥文件完成")

        // 生成本地M3U8文件
        generateLocalM3U8(m3u8Info, dir)

        /* // 下载TS文件
         m3u8Info.tsUrls.forEachIndexed { index, tsUrl ->
             val tsFile = File(dir, "segment${index + 1}.ts")
             downloadFile(tsUrl, tsFile)
         }*/


        val taskViewModel: TaskViewModel by inject(TaskViewModel::class.java)//TaskViewModel()
        download(m3u8Info.tsUrls, headers, dir, taskViewModel).collect { it ->
            when (it) {
                is DownloadProgressStatus.Progress -> {
                    logger.info("已下载 ${it.value} %")
                    taskViewModel.viewModelScope.launch(Dispatchers.IO) {
                        callback(Util.md5(m3u8Info.url), it.value, it)
                    }
                }

                is DownloadProgressStatus.Done -> {
                    logger.info("下载完成")
                    taskViewModel.viewModelScope.launch(Dispatchers.IO) {
                        callback(Util.md5(m3u8Info.url), 100, it)
                    }
                }

                is DownloadProgressStatus.Error -> {
                    taskViewModel.viewModelScope.launch(Dispatchers.IO) {
                        callback(Util.md5(m3u8Info.url), -1, it)
                    }
                    logger.info("下载失败: ${it.throwable.message}")
                }

                is DownloadProgressStatus.None -> {
                    logger.info("下载还未开始")
                }

            }
        }


    }


    suspend fun download(
        tsUrls: List<String>, headers: Map<String, String>, dir: File, taskViewModel: TaskViewModel
    ): Flow<DownloadProgressStatus> {
        return flow {

            emit(DownloadProgressStatus.Progress(0))
            val batches = tsUrls.chunked(batchSize)
            val total = tsUrls.size

            batches.forEachIndexed { batchIndex, batch ->

                val deferredList = batch.mapIndexed { innerIndex, url ->
                    taskViewModel.viewModelScope.async(Dispatchers.IO) {
                        val globalIndex = batchIndex * batchSize + innerIndex + 1
                        downloadWithRetry(url, headers, globalIndex, dir)
                    }
                }
                deferredList.awaitAll()
                // onProgress(min((batchIndex) * batchSize + batch.size, total), total)
                emit(
                    DownloadProgressStatus.Progress(
                        (batchIndex * batchSize + batch.size) * 100 / total.floorDiv(
                            1
                        )
                    )
                )
            }
            emit(DownloadProgressStatus.Done(dir))
        }.catch {
            logger.info("下载失败: ${it.message}")
            emit(DownloadProgressStatus.Error(it))
        }
    }

    private suspend fun downloadWithRetry(
        url: String, headers: Map<String, String>, index: Int, dir: File
    ) {
        var retryCount = 0
        var success = false

        while (retryCount < maxRetries && !success) {
            try {
                val file = File(dir, "segment$index.ts")
                if (file.exists()) {
                    logger.info("TS文件 segment$index.ts 已存在")
                    success = true
                    continue
                }
                val response = HttpClientUtil.get(url, headers)

                file.outputStream().use { outputStream ->
                   // outputStream.write(response.bodyAsBytes())
                    response.bodyAsChannel().copyTo(outputStream)
                }
                success = true
            } catch (e: Exception) {
                retryCount++
                if (retryCount == maxRetries) {

                    logger.info("TS文件 $index 下载失败: ${e.message}")
                    throw e
                }

                delay(1000L * retryCount)
            }
        }
    }

    fun decryptAllTsFiles(
        inputDir: File, outputDir: File, key: ByteArray?, iv: String? = null
    ) {
        if (!outputDir.exists()) outputDir.mkdirs()

        val ivBytes = iv?.let { hexStringToByteArray(it) } ?: ByteArray(16)

        inputDir.listFiles { _, name -> name.startsWith("segment") && name.endsWith(".ts") }?.forEach { tsFile ->
            val decryptedFile = File(outputDir, "decrypted_${tsFile.name}")
            if (key == null) {
                Files.copy(tsFile.toPath(), decryptedFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } else {
                TSDecryptor.decryptTSFile(tsFile, decryptedFile, key, ivBytes)
            }

        }
    }

    fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun generateLocalM3U8(m3u8Info: M3U8Info, dir: File) {
        val m3u8Content = buildString {
            appendLine("#EXTM3U")
            appendLine("#EXT-X-VERSION:3")
            appendLine("#EXT-X-TARGETDURATION:10")
            appendLine("#EXT-X-MEDIA-SEQUENCE:0")

            m3u8Info.keyUrl?.let {
                val keyLine = if (m3u8Info.iv != null) {
                    "#EXT-X-KEY:METHOD=AES-128,URI=\"/ts/${Util.md5(m3u8Info.url)}/key.key\",IV=${m3u8Info.iv}"
                } else {
                    "#EXT-X-KEY:METHOD=AES-128,URI=\"/ts/${Util.md5(m3u8Info.url)}/key.key\""
                }
                appendLine(keyLine)
            }

            m3u8Info.tsUrls.forEachIndexed { index, _ ->
                appendLine(m3u8Info.infoLines[index])
                appendLine("/ts/${Util.md5(m3u8Info.url)}/segment${index + 1}.ts")
            }

            appendLine("#EXT-X-ENDLIST")
        }

        File(dir, "local.m3u8").writeText(m3u8Content)
    }

    fun mergeTsFiles(inputDir: File, outputFile: File) {
        FileOutputStream(outputFile).use { output ->
            inputDir.listFiles { _, name ->
                name.startsWith("decrypted_") && name.endsWith(".ts")
            }?.sortedBy { it.nameWithoutExtension.replace("decrypted_segment", "").toInt() }?.forEach { tsFile ->
                tsFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
        }
    }

}


data class M3U8Info(
    val infoLines: List<String>, val tsUrls: List<String>, val keyUrl: String?, val iv: String?, val url: String
)


object TSDecryptor {
    private const val AES_ALGORITHM = "AES"
    private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"

    fun decryptTSFile(
        inputFile: File, outputFile: File, key: ByteArray, iv: ByteArray = ByteArray(16) // 默认全零IV
    ) {
        val secretKey = SecretKeySpec(key, AES_ALGORITHM)
        val ivParameterSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

        val inputBytes = inputFile.readBytes()
        val decryptedBytes = cipher.doFinal(inputBytes)

        outputFile.writeBytes(decryptedBytes)
    }
}


