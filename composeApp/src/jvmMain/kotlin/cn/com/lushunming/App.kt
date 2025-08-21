package cn.com.lushunming

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.com.lushunming.server.startServer
import cn.com.lushunming.service.DatabaseFactory
import cn.com.lushunming.views.Download
import cn.com.lushunming.views.Setting
import com.example.compose.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

// 定义应用的页面
enum class AppScreen {
    /*  Home, */Downloads, Settings
}
val LightPrimary = Color(color = 0xFF6750A4)
val LightOnPrimary = Color(color = 0xFFFFFFFF)
val LightPrimaryContainer = Color(color = 0xFFEADDFF)
val LightOnPrimaryContainer = Color(color = 0xFF21005D)
val LightInversePrimary = Color(color = 0xFFD0BCFF)
val LightSecondary = Color(color = 0xFF625B71)
val LightOnSecondary = Color(color = 0xFFFFFFFF)
val LightSecondaryContainer = Color(color = 0xFFE8DEF8)
val LightOnSecondaryContainer = Color(color = 0xFF1D192B)
val LightTertiary = Color(color = 0xFF7D5260)
val LightOnTertiary = Color(color = 0xFFFFFFFF)
val LightTertiaryContainer = Color(color = 0xFFFFD8E4)
val LightOnTertiaryContainer = Color(color = 0xFF31111D)
val LightBackground = Color(color = 0xFFFFFBFE)
val LightOnBackground = Color(color = 0xFF1C1B1F)
val LightSurface = Color(color = 0xFFFFFBFE)
val LightOnSurface = Color(color = 0xFF1C1B1F)
val LightSurfaceVariant = Color(color = 0xFFE7E0EC)
val LightOnSurfaceVariant = Color(color = 0xFF49454F)
val LightInverseSurface = Color(color = 0xFF313033)
val LightInverseOnSurface = Color(color = 0xFFF4EFF4)
val LightSurfaceTint = Color(color = 0xFF6750A4)
val LightError = Color(color = 0xFFB3261E)
val LightOnError = Color(color = 0xFFFFFFFF)
val LightErrorContainer = Color(color = 0xFFF9DEDC)
val LightOnErrorContainer = Color(color = 0xFF410E0B)
val LightOutline = Color(color = 0xFF79747E)
val LightOutlineVariant = Color(color = 0xFFCAC4D0)
val LightScrim = Color(color = 0xFF4B484E)





@Composable
@Preview
fun App() {
    AppTheme{
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

/*@Composable
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
