package cn.com.lushunming.util

import kotlinx.coroutines.Job
import javax.swing.filechooser.FileSystemView

object Constant {
    val downloadPath: String = FileSystemView.getFileSystemView().homeDirectory.absolutePath
    val jobMap = mutableMapOf<String, Job>()
}
