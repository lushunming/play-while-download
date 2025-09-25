package cn.com.lushunming.util

import dev.dirs.UserDirectories

object Constant {
    val downloadPath: String = UserDirectories.get().downloadDir

    // val  jobMap = ConcurrentHashMap<String, Job>()
    const val port = 3800
    const val maxRetries = 3

    //批量下载数
    val batchSize: Int = Runtime.getRuntime().availableProcessors()

    const val partSize = 1024 * 1024

}
