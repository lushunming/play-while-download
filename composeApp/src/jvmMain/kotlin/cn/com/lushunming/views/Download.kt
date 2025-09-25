package cn.com.lushunming.views


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.com.lushunming.server.ProxyServer
import cn.com.lushunming.service.ConfigService
import cn.com.lushunming.service.DownloadService
import cn.com.lushunming.service.TaskService
import cn.com.lushunming.util.Constant.port
import cn.com.lushunming.util.DownloadUtil.logger
import cn.com.lushunming.util.Util
import cn.com.lushunming.viewmodel.ConfigViewModel
import cn.com.lushunming.viewmodel.TaskViewModel
import model.DownloadStatus
import model.Task

import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.inject
import java.awt.Desktop
import java.io.File
import java.net.URI

@Composable
fun Download() {
    //下载任务
    //val taskViewModel = koinViewModel<TaskViewModel>()
    val taskViewModel = koinInject<TaskViewModel>()
    //  val taskViewModel = viewModel { TaskViewModel() }
    val downloadTasks by taskViewModel.tasks.collectAsState()
    val taskService = TaskService();
    //配置
    val configService = ConfigService()
    val configViewModel = ConfigViewModel(configService)
    val config by configViewModel.config.collectAsState()

    var urlForVideoWindow by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        for (task in downloadTasks) {
            if (task.status != DownloadStatus.COMPLETED) {
                taskViewModel.startDownload(task, config.downloadPath + File.separator + task.id)
            }

        }
    }

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer).safeContentPadding().fillMaxSize()
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
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
                        val path = config.downloadPath+ File.separator + task.id
                        taskViewModel.startDownload(task, path)

                    }, onPause = {
                        // 暂停下载任务
                        taskViewModel.pauseDownload(task.id)

                    }, onDelete = {

                        // 删除下载任务
                        taskViewModel.deleteTask(task.id, config.downloadPath)

                        //TODO 提示删除成功

                    }, onPlay = {
                        // 播放下载的文件
                        urlForVideoWindow = task.url
                        println("播放文件: ${task.name}")


                    }, onOpenFolder = {
                        val dir = config.downloadPath + File.separator + task.id
                        Desktop.getDesktop().open(File(dir))
                    })
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.Start).fillMaxHeight(), adapter = rememberScrollbarAdapter(
                    scrollState = state
                )
            )
        }

        // 添加新下载任务的按钮
        FloatingActionButton(
            onClick = {
                // 添加新下载任务
                //viewModel.addTask();
                showDialog = true

            }, modifier = Modifier.align(Alignment.End).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加下载任务")
        }

        if (showDialog) {
            DownloadDialog(onConfirmation = {

                val path = config.downloadPath

                val urlParam = it
                val headerParam = mutableMapOf<String, String>()
                val url = ProxyServer().buildProxyUrl(urlParam, headerParam, port)
                val id = Util.md5(urlParam)
                val old = taskService.getTaskById(id)
                if (old == null) {

                    val taskProcess: DownloadService by inject(DownloadService::class.java)
                    taskProcess.addTask(urlParam, path, headerParam, id, "", url)
                    showDialog = false
                }

            }, onDismissRequest = { showDialog = false }

            )
        }
    }

    urlForVideoWindow?.let { url ->
        logger.info("视频URl：$url")
        Video(url) { urlForVideoWindow = null }
    }
}


@Composable
fun DownloadTaskItem(
    task: Task,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onDelete: () -> Unit,
    onPlay: () -> Unit,
    onOpenFolder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp), border = BorderStroke(1.dp, color = Blue)
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
                progress = { task.progress / 100f }, modifier = Modifier.fillMaxWidth(), color = when (task.status) {
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
                // 打开目录
                IconButton(
                    onClick = onOpenFolder, enabled = true
                ) {
                    Icon(
                        Icons.Default.FolderOpen, contentDescription = "打开目录",

                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),

                        )
                }
                // 播放按钮（仅当下载完成时可用）
                when (task.type) {
                    "Video" -> {
                        IconButton(
                            onClick = onPlay, enabled = (task.progress >= 5)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "播放",
                                tint = if (task.progress >= 5) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                        IconButton(
                            onClick = {
                                Desktop.getDesktop().browse(
                                    URI.create(
                                        "http://localhost:3800/video?url=${
                                            Util.base64Encode(
                                                task.url.toByteArray(
                                                )
                                            )
                                        }&type=${task.type}"
                                    )
                                )
                            }, enabled = (task.progress >= 5)
                        ) {
                            Icon(
                                Icons.Outlined.PlayArrow,
                                contentDescription = "播放",
                                tint = if (task.progress >= 5) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }

                    "M3u8" -> {
                        IconButton(
                            onClick = onPlay, enabled = (task.progress >= 5)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "播放",
                                tint = if (task.progress >= 5) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                        IconButton(
                            onClick = {
                                Desktop.getDesktop().browse(
                                    URI.create(
                                        "http://localhost:3800/video?url=${
                                            Util.base64Encode(
                                                task.url.toByteArray(
                                                )
                                            )
                                        }&type=${task.type}"
                                    )
                                )
                            }, enabled = (task.progress >= 5)
                        ) {
                            Icon(
                                Icons.Outlined.PlayArrow,
                                contentDescription = "播放",
                                tint = if (task.progress >= 5) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }

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
                        Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadDialog(
    onDismissRequest: () -> Unit, onConfirmation: (url: String) -> Unit
) {

    var downloadURL by remember { mutableStateOf("") }


    BasicAlertDialog(onDismissRequest = { onDismissRequest() }) {


        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.wrapContentHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 代理服务器地址
                OutlinedTextField(
                    value = downloadURL,
                    onValueChange = { newValue -> downloadURL = newValue },
                    label = { Text("下载链接") },
                    modifier = Modifier.padding(2.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("取消")
                    }
                    TextButton(
                        onClick = { onConfirmation(downloadURL) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("开始下载")
                    }
                }
            }
        }
    }


}


