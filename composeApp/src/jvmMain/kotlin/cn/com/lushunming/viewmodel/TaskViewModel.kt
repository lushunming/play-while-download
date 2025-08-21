package cn.com.lushunming.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.com.lushunming.service.TaskService
import cn.com.lushunming.util.Constant.jobMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Task
import java.io.File


class TaskViewModel() : ViewModel() {
    val service = TaskService()
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

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

    fun deleteTask(id: String) {
        viewModelScope.launch {
            jobMap.remove(id)
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

    fun startDownload(task: Task, path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = task.id

            service.updateStatus(id, model.DownloadStatus.DOWNLOADING)

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
                        headerFile.readText(Charsets.UTF_8),
                        object : TypeToken<MutableMap<String, String>>() {}.type
                    )
                    cn.com.lushunming.server.startDownload(
                        dir, task.oriUrl, headerParam
                    ) { taskId: String, progress: Int ->
                        updateProgress(id, progress)
                        if (progress == 100) {
                            service.updateStatus(id, model.DownloadStatus.COMPLETED)
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
            jobMap.remove(id)
            service.updateStatus(id, model.DownloadStatus.PAUSED)
            getTaskList()
        }
    }


}