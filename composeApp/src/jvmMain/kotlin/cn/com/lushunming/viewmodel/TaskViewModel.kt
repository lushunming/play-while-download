package cn.com.lushunming.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.com.lushunming.server.DownloadManager
import cn.com.lushunming.service.ConfigService
import cn.com.lushunming.service.DownloadService
import cn.com.lushunming.service.TaskService
import cn.com.lushunming.util.HttpClientUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.server.http.content.CompressedFileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.DownloadStatus
import model.Task
import java.io.File
import java.util.concurrent.ConcurrentHashMap


class TaskViewModel() : ViewModel() {
    val service = TaskService()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    val jobMap = ConcurrentHashMap<String, Job>()
    val configService = ConfigService()

    init {
        getTaskList()
    }

    private fun getTaskList() {
        viewModelScope.launch {
            _tasks.value = service.getTaskList()
        }
    }

    fun getTaskById(id: String) {
        viewModelScope.launch {
            service.getTaskById(id)
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            service.addTask(task)
            getTaskList()
        }
    }

    fun deleteTask(id: String, path: String) {
        viewModelScope.launch {
            //取消协程

            val job = jobMap[id]
            job?.cancel()
            jobMap.remove(id)
            //删除文件

            val dir = path + File.separator + id
            File(dir).deleteRecursively()

            service.deleteTask(id)
            getTaskList()
        }
    }

    fun updateProgress(id: String, progress: Int) {
        viewModelScope.launch {
            service.updateProgress(id, progress)
            getTaskList()
        }
    }

    fun updateFileNameAndType(id: String, fileName: String,fileType: String ) {
        viewModelScope.launch {
            service.updateFileNameAndType(id, fileName,fileType)
            getTaskList()
        }
    }

    fun updateStatus(id: String, status: DownloadStatus, message: String? = "") {
        viewModelScope.launch {
            service.updateStatus(id, status, message)
            getTaskList()
        }
    }

    fun startDownload(task: Task, dir: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = task.id

            updateStatus(id, DownloadStatus.DOWNLOADING)

            if (jobMap[id] != null) {
                //已经在下载中
                return@launch
            }
            val headerFile = File(dir, "header.tmp")
            val headerParam = Gson().fromJson<MutableMap<String, String>>(
                headerFile.readText(Charsets.UTF_8), object : TypeToken<MutableMap<String, String>>() {}.type
            )

            val job = viewModelScope.launch(Dispatchers.IO) {

               val info= getDownloadInfo(task.oriUrl, headerParam)
                updateFileNameAndType(id, info.fileName,info.fileType)
                DownloadManager().startDownload(info,
                    dir, task.oriUrl, headerParam
                ) { taskId: String, progress: Int, status: cn.com.lushunming.models.DownloadProgressStatus ->

                    if (progress == 100) {
                        updateStatus(id, model.DownloadStatus.COMPLETED)
                    }
                    when (status) {
                        is cn.com.lushunming.models.DownloadProgressStatus.Done -> {
                            updateStatus(
                                id, model.DownloadStatus.COMPLETED
                            )
                            updateProgress(id, progress)
                        }

                        is cn.com.lushunming.models.DownloadProgressStatus.Error -> {
                            updateStatus(
                                id, model.DownloadStatus.ERROR, status.throwable.message
                            )
                            jobMap.remove(id)
                        }

                        cn.com.lushunming.models.DownloadProgressStatus.None -> updateStatus(
                            id, model.DownloadStatus.PENDING
                        )

                        is cn.com.lushunming.models.DownloadProgressStatus.Progress -> {
                            updateStatus(
                                id, model.DownloadStatus.DOWNLOADING
                            )
                            updateProgress(id, progress)
                        }

                    }
                }

            }
            jobMap[id] = job

            getTaskList()
        }
    }

    fun pauseDownload(id: String) {


        viewModelScope.launch {
            //取消协程
            val job = jobMap[id]
            job?.cancel()
            jobMap.remove(id)
            updateStatus(id, model.DownloadStatus.PAUSED)
            getTaskList()
        }
    }

    /**
     * 请求文件获取文件类型，文件大小，是否可以分片，以及文件名
     */
    suspend fun getDownloadInfo(urlParam: String, headerParam: MutableMap<String, String>): DownloadInfo {

        headerParam.put(HttpHeaders.Range, "bytes=0-1024")
        val res = HttpClientUtil.get(urlParam, headerParam)

        val contentType = res.headers[HttpHeaders.ContentType]
        val acceptRanges = res.status.value == 206
        val isM3u8 = res.bodyAsText().startsWith("#EXTM3U")

        // 优先从 Content-Disposition 头中获取文件名
        var fileName =
            res.headers[HttpHeaders.ContentDisposition]?.split(";")?.find { it.trim().startsWith("filename=") }
                ?.substringAfter("filename=")?.trim('"', ' ')

        // 如果 Content-Disposition 中未找到文件名，则从 URL 中提取
        if (fileName.isNullOrEmpty()) {
            fileName = urlParam.substringAfterLast('/')
        }
        val fileType = getFileType(contentType, isM3u8);

        return DownloadInfo(fileType, acceptRanges, fileName)


    }

    private fun getFileType(
        contentType: String?,
        isM3u8: Boolean,
    ): String {
        return if (isM3u8) {
            "M3u8"
        } else if (contentType.isNullOrBlank()) {
            "Unknown";
        } else if (contentType.startsWith("video")) {
            "Video"
        } else {
            contentType.split("/")[0]
        }

    }


    data class DownloadInfo(val fileType: String, val acceptRanges: Boolean, val fileName: String)


}