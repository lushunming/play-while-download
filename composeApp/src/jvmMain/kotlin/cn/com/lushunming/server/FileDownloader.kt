package cn.com.lushunming.server

import androidx.lifecycle.viewModelScope
import cn.com.lushunming.models.DownloadProgressStatus
import cn.com.lushunming.service.DownloadService
import cn.com.lushunming.util.Constant
import cn.com.lushunming.util.Constant.partSize
import cn.com.lushunming.util.DownloadUtil
import cn.com.lushunming.util.HttpClientUtil
import cn.com.lushunming.util.Util
import cn.com.lushunming.viewmodel.TaskViewModel
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.koin.java.KoinJavaComponent.inject
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream


class FileDownloader(private val outputDir: String) {
    val logger = LoggerFactory.getLogger(FileDownloader::class.java)


    suspend fun downloadAllFiles(
        url: String,
        info: TaskViewModel.DownloadInfo,
        headers: Map<String, String>,
        callback: (id: String, progress: Int, status: DownloadProgressStatus) -> Unit
    ) {
        // 创建输出目录
        val dir = File(outputDir)
        if (!dir.exists()) dir.mkdirs()


        val taskViewModel: TaskViewModel by inject(TaskViewModel::class.java)
        if (info.acceptRanges) {
            downloadMulti(url, headers, dir, info.fileName, taskViewModel).collect { it ->
                progressStatus(it, taskViewModel, callback, url)
            }
        } else {
            downloadSingle(url, headers, dir, info.fileName, taskViewModel).collect { it ->
                progressStatus(it, taskViewModel, callback, url)
            }
        }


    }


    fun downloadMulti(
        url: String, headers: Map<String, String>, dir: File, fileName: String, taskViewModel: TaskViewModel
    ): Flow<DownloadProgressStatus> {
        return flow {

            emit(DownloadProgressStatus.Progress(0))

            val contentLength = getContentLength(url, headers)
            logger.info("contentLength: $contentLength")

            var currentStart = 0L
            val finalEndPoint = contentLength - 1

            // 启动生产者协程下载数据

            val producerJob = mutableListOf<Job>()
            var downloadedSize = 0L
            while (currentStart <= finalEndPoint) {
                producerJob.clear()
                // 创建通道用于接收数据块

                for (i in 0 until Constant.batchSize) {

                    if (currentStart > finalEndPoint) break
                    val chunkStart = currentStart
                    val chunkEnd = minOf(currentStart + partSize - 1, finalEndPoint)
                    producerJob += CoroutineScope(Dispatchers.IO).launch {
                       val tmpHeaders = headers.toMutableMap()
                        tmpHeaders.put(HttpHeaders.Range, "bytes=$chunkStart-$chunkEnd")
                        // 异步下载数据块
                        DownloadUtil.downloadWithRetry(url, tmpHeaders, dir, "${chunkStart}-${chunkEnd}.video")
                        logger.info("下载完成: ${chunkStart}-${chunkEnd}")
                    }
                    currentStart = chunkEnd + 1
                    downloadedSize += chunkEnd - chunkStart + 1
                }
                producerJob.joinAll()
                emit(DownloadProgressStatus.Progress((downloadedSize * 100 / contentLength).toInt()))
            }



            emit(DownloadProgressStatus.Done(dir))
        }.catch {
            logger.info("下载失败: ${it.message}")
            emit(DownloadProgressStatus.Error(it))
        }
    }

    private suspend fun getContentLength(url: String, headers: Map<String, String>): Long {
        val header = headers.toMutableMap()
        header.put(HttpHeaders.Range, "bytes=0-1")
        // 实现获取内容长度逻辑
        val res = HttpClientUtil.get(url, header)

        return res.headers[HttpHeaders.ContentRange]?.split("/")?.get(1)?.toLong() ?: 0L
    }

    fun downloadSingle(
        url: String, headers: Map<String, String>, dir: File, fileName: String, taskViewModel: TaskViewModel
    ): Flow<DownloadProgressStatus> {
        return flow {
            emit(DownloadProgressStatus.Progress(0))
            DownloadUtil.downloadWithRetry(url, headers, dir, fileName)
            emit(DownloadProgressStatus.Done(dir))
        }.catch {
            logger.info("下载失败: ${it.message}")
            emit(DownloadProgressStatus.Error(it))
        }
    }


    fun mergeFiles(outputDir: File, mergedFile: File) {

        FileOutputStream(mergedFile).use { output ->
            outputDir.listFiles { _, name ->
                 name.endsWith(".video")
            }?.sortedBy { it.nameWithoutExtension.split("-")[0].toLong() }?.forEach { tsFile ->
                tsFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun progressStatus(
        it: DownloadProgressStatus,
        taskViewModel: TaskViewModel,
        callback: (String, Int, DownloadProgressStatus) -> Unit,
        url: String
    ) {
        when (it) {
            is DownloadProgressStatus.Progress -> {
                logger.info("已下载 ${it.value} %")
                taskViewModel.viewModelScope.launch(Dispatchers.IO) {
                    callback(Util.md5(url), it.value, it)
                }
            }

            is DownloadProgressStatus.Done -> {
                logger.info("下载完成")
                taskViewModel.viewModelScope.launch(Dispatchers.IO) {
                    callback(Util.md5(url), 100, it)
                }
            }

            is DownloadProgressStatus.Error -> {
                taskViewModel.viewModelScope.launch(Dispatchers.IO) {
                    callback(Util.md5(url), -1, it)
                }
                logger.info("下载失败: ${it.throwable.message}")
            }

            is DownloadProgressStatus.None -> {
                logger.info("下载还未开始")
            }

        }
    }


}

