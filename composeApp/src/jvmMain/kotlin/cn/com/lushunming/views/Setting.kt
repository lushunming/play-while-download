package cn.com.lushunming.views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.com.lushunming.models.AppConfig
import cn.com.lushunming.service.ConfigService
import cn.com.lushunming.viewmodel.ConfigViewModel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Preview
@Composable
fun Setting() {

    val configService = ConfigService()
    val configViewModel = ConfigViewModel(configService)
    val config by configViewModel.config.collectAsState()
    // 状态管理
    var proxyAddress by remember { mutableStateOf("") }
    var enableProxy by remember { mutableStateOf(false) }
    var downloadDirectory by remember {
        mutableStateOf(
            FileSystemView.getFileSystemView().homeDirectory.absolutePath
        )
    }

    // 编辑时填充表单
    LaunchedEffect(config) {
        proxyAddress = config.proxy
        enableProxy = config.open == 1
        downloadDirectory = config.downloadPath
    }

    // 是否显示目录选择器
    var showDirectoryChooser by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding().fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 表单容器
        Card(
            modifier = Modifier.fillMaxWidth(0.8f).padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 代理服务器地址
                OutlinedTextField(
                    value = proxyAddress,
                    onValueChange = { proxyAddress = it },
                    label = { Text("代理服务器地址") },
                    placeholder = { Text("例如: http://127.0.0.1:7890") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 是否开启代理
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("开启代理")
                    Switch(
                        checked = enableProxy, onCheckedChange = { enableProxy = it })
                }

                // 下载目录
                OutlinedTextField(
                    value = downloadDirectory,
                    onValueChange = { downloadDirectory = it },
                    label = { Text("下载目录") },
                    readOnly = true,
                    trailingIcon = {
                        TextButton(onClick = { showDirectoryChooser = true }) {
                            Text("选择")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 保存按钮
                Button(
                    onClick = {

                        // 这里可以添加保存设置的逻辑
                        println("保存设置：")
                        println("代理地址: $proxyAddress")
                        println("开启代理: $enableProxy")
                        println("下载目录: $downloadDirectory")
                        configViewModel.saveConfig(
                            AppConfig(
                                proxy = proxyAddress,
                                open = if (enableProxy) 1 else 0,
                                downloadPath = downloadDirectory,
                                id = config.id
                            )
                        )

                    }, modifier = Modifier.align(Alignment.End).padding(top = 16.dp)
                ) {
                    Text("保存")
                }
            }
        }
    }

    // 处理目录选择
    if (showDirectoryChooser) {
        LaunchedEffect(Unit) {
            val fileChooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                dialogTitle = "选择下载目录"
                currentDirectory = File(downloadDirectory)
            }

            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                downloadDirectory = fileChooser.selectedFile.absolutePath
            }

            showDirectoryChooser = false
        }
    }
}
