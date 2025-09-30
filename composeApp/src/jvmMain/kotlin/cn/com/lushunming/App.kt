package cn.com.lushunming

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.com.lushunming.service.DownloadService
import cn.com.lushunming.util.Paths
import cn.com.lushunming.viewmodel.TaskViewModel
import cn.com.lushunming.views.Download
import cn.com.lushunming.views.Setting
import com.example.compose.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import java.awt.Desktop

// 定义应用的页面
enum class AppScreen {
    /*  Home, */Downloads, Settings
}


@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(module {
            single { TaskViewModel() }
            single { DownloadService(get()) }
        })
    }) {
        AppTheme {
            // 当前显示的页面
            var currentScreen by remember { mutableStateOf(AppScreen.Downloads) }


            LaunchedEffect(Unit) {

            }
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // 左侧导航栏 - 固定在左侧
                NavigationRail(
                    modifier = Modifier.fillMaxHeight().width(80.dp) // 设置固定宽度
                ) {
                    /*NavigationRailItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "主页") },
                    label = { Text("主页") },
                    selected = currentScreen == AppScreen.Home,
                    onClick = { currentScreen = AppScreen.Home })*/
                    NavigationRailItem(
                        icon = {
                        Icon(
                            Icons.Default.Download, contentDescription = "下载"
                        )
                    },
                        label = { Text("下载") },
                        selected = currentScreen == AppScreen.Downloads,
                        onClick = { currentScreen = AppScreen.Downloads })
                    NavigationRailItem(
                        icon = {
                        Icon(
                            Icons.Default.Settings, contentDescription = "设置"
                        )
                    },
                        label = { Text("设置") },
                        selected = currentScreen == AppScreen.Settings,
                        onClick = { currentScreen = AppScreen.Settings })

                    NavigationRailItem(icon = {
                        Icon(
                            Icons.Default.FileOpen, contentDescription = "打开日志"
                        )
                    }, label = { Text("打开日志") }, selected = false, onClick = {
                        Desktop.getDesktop().open(Paths.userDataRoot())
                    })
                }
                val stateVertical = rememberScrollState(0)
                // 右侧内容区域 - 占据剩余空间
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f)

                ) {
                    when (currentScreen) {
                        // AppScreen.Home -> HomeScreen()
                        AppScreen.Downloads -> Download()
                        AppScreen.Settings -> Setting()
                    }
                }

            }
        }
    }
}/*@Composable
fun HomeScreen() {
    var showContent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { showContent = !showContent }) {
            Text("点击我!")
        }
        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting")
            }
        }
    }
}*/
