package cn.com.lushunming.models

import java.io.File

sealed class DownloadProgressStatus {
    object None : DownloadProgressStatus()
    data class Progress(val value: Int) : DownloadProgressStatus()
    data class Error(val throwable: Throwable) : DownloadProgressStatus()
    data class Done(val file: File) : DownloadProgressStatus()
}
