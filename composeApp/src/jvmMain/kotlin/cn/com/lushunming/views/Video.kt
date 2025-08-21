package cn.com.lushunming.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import chaintech.videoplayer.util.LocalWindowState


@Composable
fun Video(url: String, onClose: () -> Unit) {
    val windowState = rememberWindowState(width = 900.dp, height = 700.dp)
    CompositionLocalProvider(LocalWindowState provides windowState) {
        Window(onCloseRequest = onClose, title = "视频播放", state = windowState) {

            val playerHost = remember { MediaPlayerHost(mediaUrl = url) }
            playerHost.setVolume(0.5f)
            VideoPlayerComposable(
                modifier = Modifier.fillMaxSize(), playerHost = playerHost
            )
        }
    }

}
