package cn.com.lushunming

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cn.com.lushunming.server.startServer
import cn.com.lushunming.service.DatabaseFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun main() {
    CoroutineScope(Dispatchers.IO).launch {
        DatabaseFactory.connectAndMigrate()
        startServer(
            args = emptyArray()
        );
    }
    application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "play-while-download",
    ) {


        App()

    }

}}