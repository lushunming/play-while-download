package cn.com.lushunming.server

import cn.com.lushunming.models.DownloadProgressStatus
import cn.com.lushunming.util.Util
import cn.com.lushunming.viewmodel.TaskViewModel
import org.slf4j.LoggerFactory
import java.io.File

class DownloadManager {
    val logger = LoggerFactory.getLogger(DownloadManager::class.java)
    suspend fun startDownload(
        info: TaskViewModel.DownloadInfo,
        outputDir: String,
        url: String,
        headers: Map<String, String>,
        callback: (id: String, progress: Int, status: DownloadProgressStatus) -> Unit
    ) {

        if (info.fileType == "M3u8") {
            downloadM3u8(outputDir, headers, url, callback)
        } else {
            downloadFile(info, outputDir, headers, url, callback)
        }
    }


    private suspend fun downloadM3u8(
        outputDir: String,
        headers: Map<String, String>,
        url: String,
        callback: (String, Int, DownloadProgressStatus) -> Unit
    ) {
        val downloader = M3U8Downloader(outputDir)
        File(outputDir, "header.tmp").writeText(Util.json(headers))

        try {
            // 解析M3U8
            val m3u8Info = downloader.parseM3U8(url, headers)
            logger.info("解析完成，找到${m3u8Info.tsUrls.size}个TS片段")

            // 下载所有文件
            downloader.downloadAllFiles(m3u8Info, headers, callback)
            logger.info("文件下载完成")


            // 4. 解密TS文件
            var key: ByteArray? = null
            if (m3u8Info.keyUrl != null) {
                key = File(outputDir, "key.key").readBytes()
            }
            downloader.decryptAllTsFiles(
                File(outputDir), File(outputDir), key, m3u8Info.iv?.replace("0x", "")
            )


            // 5. 合并TS文件
            val mergedFile = File(outputDir, "output.ts")
            downloader.mergeTsFiles(File(outputDir), mergedFile)


            logger.info("所有操作完成！本地M3U8文件位于：${File(outputDir).absolutePath}/local.m3u8")
        } catch (e: Exception) {
            logger.info("发生错误: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun downloadFile(
        info: TaskViewModel.DownloadInfo,
        outputDir: String,
        headers: Map<String, String>,
        url: String,
        callback: (String, Int, DownloadProgressStatus) -> Unit
    ) {
        val downloader = FileDownloader(outputDir)
        File(outputDir, "header.tmp").writeText(Util.json(headers))

        try {


            // 下载所有文件
            downloader.downloadAllFiles(url, info, headers, callback)
            logger.info("文件下载完成")


            //  合并文件
            val mergedFile = File(outputDir, info.fileName)
            downloader.mergeFiles(File(outputDir), mergedFile)


            logger.info("所有操作完成！本地M3U8文件位于：${File(outputDir).absolutePath}")
        } catch (e: Exception) {
            logger.info("发生错误: ${e.message}")
            e.printStackTrace()
        }
    }
}
