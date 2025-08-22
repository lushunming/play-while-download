package cn.com.lushunming.util

import javax.swing.filechooser.FileSystemView

object Constant {
    val downloadPath: String = FileSystemView.getFileSystemView().homeDirectory.absolutePath

    // val  jobMap = ConcurrentHashMap<String, Job>()
    const val port = 3800
}
