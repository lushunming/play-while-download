package cn.com.lushunming.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.com.lushunming.service.ConfigService
import cn.com.lushunming.util.Constant.jobMap
import cn.com.lushunming.viewmodel.ConfigViewModel
import cn.com.lushunming.viewmodel.TaskViewModel
import model.DownloadStatus
import model.Task
import java.io.File

@Composable
fun Download() {
    //下载任务

    val taskViewModel = viewModel { TaskViewModel() }
    val downloadTasks by taskViewModel.tasks.collectAsState()

    //配置
    val configService = ConfigService()
    val configViewModel = ConfigViewModel(configService)
    val config by configViewModel.config.collectAsState()

    var urlForVideoWindow by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        for (task in downloadTasks) {
            if(task.status!= DownloadStatus.COMPLETED){
                taskViewModel.startDownload(task, config.downloadPath)
            }

        }
    }

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding().fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "下载管理",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 下载任务列表
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(downloadTasks, key = { it.id }) { task ->
                    DownloadTaskItem(task = task, onStart = {
                        // 启动下载任务
                        val path = config.downloadPath
                        taskViewModel.startDownload(task, path)

                    }, onPause = {
                        // 暂停下载任务
                        taskViewModel.pauseDownload(task.id)
                        //取消协程
                        val id = task.id
                        val job = jobMap[id]
                        job?.cancel()
                    }, onDelete = {
                        //取消协程
                        val id = task.id
                        val job = jobMap[id]
                        job?.cancel()
                        //删除文件
                        val path = config.downloadPath
                        val dir = path + File.separator + id
                        File(dir).deleteRecursively()
                        // 删除下载任务
                        taskViewModel.deleteTask(task.id)

                        //TODO 提示删除成功

                    }, onPlay = {
                        // 播放下载的文件
                        urlForVideoWindow=task.url
                        println("播放文件: ${task.name}")


                    })
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.Start).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = state
                )
            )
        }

        // 添加新下载任务的按钮
        FloatingActionButton(
            onClick = {
                // 添加新下载任务
                //viewModel.addTask();
            }, modifier = Modifier.align(Alignment.End).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加下载任务")
        }
    }

    urlForVideoWindow?.let { url ->
        Video(url) { urlForVideoWindow = null }
    }
}




@Composable
fun DownloadTaskItem(
    task: Task, onStart: () -> Unit, onPause: () -> Unit, onDelete: () -> Unit, onPlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp), border = BorderStroke(1.dp, color =Blue)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            // 任务名称和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(status = task.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { task.progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = when (task.status) {
                    DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primary
                    DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                    DownloadStatus.PAUSED -> MaterialTheme.colorScheme.secondary
                    DownloadStatus.ERROR -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            Text(
                text = "${task.progress}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 播放按钮（仅当下载完成时可用）
                IconButton(
                    onClick = onPlay, enabled = (task.progress >= 5||task.type.contains("mp4"))
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "播放",
                        tint = if (task.progress >= 5||task.type.contains("mp4")) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }

                // 启动/暂停按钮
                IconButton(
                    onClick = {
                        if (task.status == DownloadStatus.DOWNLOADING) {
                            onPause()
                        } else {
                            onStart()
                        }
                    }, enabled = task.status != DownloadStatus.COMPLETED
                ) {
                    Icon(
                        if (task.status == DownloadStatus.DOWNLOADING) Icons.Default.Pause
                        else Icons.Default.Start,
                        contentDescription = if (task.status == DownloadStatus.DOWNLOADING) "暂停" else "开始",
                        tint = if (task.status != DownloadStatus.COMPLETED) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }

                // 删除按钮
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: DownloadStatus) {
    val (backgroundColor, text) = when (status) {
        DownloadStatus.PENDING -> Pair(Color.Gray, "等待中")
        DownloadStatus.DOWNLOADING -> Pair(MaterialTheme.colorScheme.primary, "下载中")
        DownloadStatus.PAUSED -> Pair(MaterialTheme.colorScheme.secondary, "已暂停")
        DownloadStatus.COMPLETED -> Pair(MaterialTheme.colorScheme.tertiary, "已完成")
        DownloadStatus.ERROR -> Pair(MaterialTheme.colorScheme.error, "错误")
    }

    Surface(
        color = backgroundColor, shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
