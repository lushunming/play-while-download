package cn.com.lushunming.util

import javax.swing.filechooser.FileSystemView

object Constant {
    val downloadPath: String = FileSystemView.getFileSystemView().homeDirectory.absolutePath

    // val  jobMap = ConcurrentHashMap<String, Job>()
    const val port = 3800
    const val maxRetries = 3

    //批量下载数
      val batchSize: Int = Runtime.getRuntime().availableProcessors()

    const  val partSize=1024*1024

}
