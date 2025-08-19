package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table

@Serializable
data class Task(
    val id: String,
    val name: String,
    val url: String,
    val oriUrl: String, //原来的URL
    val type: String, //m3u8 or mp4
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
)

/**
 * 下载状态枚举
 */
enum class DownloadStatus {
    PENDING,    // 等待中
    DOWNLOADING, // 下载中
    PAUSED,     // 已暂停
    COMPLETED,  // 已完成
    ERROR       // 错误
}


object Tasks : Table("tasks") {
    val id = varchar("id", 255)
    val name = varchar("name", 1000)
    val url = varchar("url", 1000)
    val oriUrl = varchar("oriUrl", 1000)
    val type = varchar("type", 255)
    val progress = integer("progress")
    val status = integer("status")

}

