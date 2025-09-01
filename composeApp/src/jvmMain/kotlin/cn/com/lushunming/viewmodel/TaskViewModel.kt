package cn.com.lushunming.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.com.lushunming.service.ConfigService
import cn.com.lushunming.service.TaskService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

    fun updateStatus(id: String, status: DownloadStatus, message: String? = "") {
        viewModelScope.launch {
            service.updateStatus(id, status, message)
            getTaskList()
        }
    }

    fun startDownload(task: Task, path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = task.id

            updateStatus(id, model.DownloadStatus.DOWNLOADING)

            if (jobMap[id] != null) {
                //TODO  call.respondText("已经在下载")
                return@launch
            }
            if (task.oriUrl.contains("m3u8")) {

                val dir = path + File.separator + id
                val job = viewModelScope.launch(Dispatchers.IO) {
                    File(dir).mkdirs()
                    val headerFile = File(dir, "header.tmp")
                    val headerParam = Gson().fromJson<MutableMap<String, String>>(
                        headerFile.readText(Charsets.UTF_8), object : TypeToken<MutableMap<String, String>>() {}.type
                    )
                    cn.com.lushunming.server.startDownload(
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
            }
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


}