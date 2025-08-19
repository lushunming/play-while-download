package cn.com.lushunming

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cn.com.lushunming.server.startServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.wait

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "play-while-download",
    ) {

        App()

    }
}