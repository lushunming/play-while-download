package cn.com.lushunming.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import javax.swing.filechooser.FileSystemView


@Serializable
data class AppConfig(

    val id: Int?,
    val proxy: String = "http://127.0.0.1:1080",
    val open: Int = 0,
    val downloadPath: String = FileSystemView.getFileSystemView().homeDirectory.absolutePath
)


object Config : Table("config") {
    val id = integer("id").autoIncrement()
    val proxy = varchar("proxy", 1000)
    val open = integer("open")

    val downloadPath = varchar("downloadPath", 1000)

    override val primaryKey = PrimaryKey(id)

}