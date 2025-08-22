package cn.com.lushunming

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cn.com.lushunming.server.startServer
import cn.com.lushunming.service.DatabaseFactory
import cn.com.lushunming.util.Paths
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import play_while_download.composeapp.generated.resources.Res
import play_while_download.composeapp.generated.resources.icon_main

fun main() {

    CoroutineScope(Dispatchers.IO).launch {
        System.setProperty("logback.configurationFile", Res.getUri("files/logback.xml"));
        System.setProperty("LOG_HOME", Paths.log());
        DatabaseFactory.connectAndMigrate()
        startServer(
            args = emptyArray()
        );
    }
    application {

        Window(
            onCloseRequest = ::exitApplication, icon = BitmapPainter(
                imageResource(Res.drawable.icon_main)
            ), title = "play-while-download"
        ) {


            App()

        }

    }
}