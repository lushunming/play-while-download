package cn.com.lushunming.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.com.lushunming.service.TaskService
import cn.com.lushunming.util.Constant.jobMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Task
import java.io.File
import kotlin.collections.set


class TaskViewModel(private val service: TaskService) : ViewModel() {
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

    fun startDownload(id: String,path: String) {



        if (jobMap[id] != null) {
            //TODO  call.respondText("已经在下载")
            return
        }
        if (task.oriUrl.contains("m3u8")) {

            val dir = path + File.separator + id
            val job = CoroutineScope(Dispatchers.IO).launch {
                File(dir).mkdirs()
                val headerFile = File(dir, "header.tmp")
                val headerParam = Gson().fromJson<MutableMap<String, String>>(
                    headerFile.readText(Charsets.UTF_8),
                    object : TypeToken<MutableMap<String, String>>() {}.type
                )
                cn.com.lushunming.server.startDownload(
                    dir, task.oriUrl, headerParam
                )
            }
            jobMap[id] = job
        }
        getTaskList()
    }

    fun pauseDownload(id: String) {
        getTaskList()
    }


}