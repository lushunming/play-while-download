package cn.com.lushunming.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import org.openani.mediamp.compose.MediampPlayerSurface
import org.openani.mediamp.compose.rememberMediampPlayer
import org.openani.mediamp.features.AudioLevelController
import org.openani.mediamp.playUri

@Composable
fun Video(url: String, onClose: () -> Unit) {
    val windowState = rememberWindowState(width = 900.dp, height = 700.dp)
    val player = rememberMediampPlayer()
    var isPlaying by remember { mutableStateOf(true) }
    var volume by remember { mutableStateOf(1.0f) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration: Long by remember { mutableStateOf(0L) }
    var isMuted by remember { mutableStateOf(false) }

    // 更新播放状态
    LaunchedEffect(player) {
        player.playUri(url)

        // 监听播放进度
        while (true) {
            currentPosition = player.getCurrentPositionMillis()
            duration = player.getCurrentMediaProperties()?.durationMillis ?: 0L
            delay(1000) // 每秒更新一次
        }
    }

    Window(
        onCloseRequest = onClose, title = "视频播放", state = windowState, onPreviewKeyEvent = { event ->
            when {
               (event.key == Key.DirectionLeft && event.type == KeyEventType.KeyDown) -> {
                   val currentPosition = player.getCurrentPositionMillis()
                   player.seekTo(currentPosition - 10000) // 快进10秒
                    true
                }

                (event.key == Key.DirectionRight && event.type == KeyEventType.KeyDown) -> {
                    val currentPosition = player.getCurrentPositionMillis()
                    player.seekTo(currentPosition + 10000) // 快进10秒
                    true
                }

                (event.key == Key.Spacebar && event.type == KeyEventType.KeyUp) -> {
                    if (isPlaying) {
                        player.pause()
                    } else {
                        player.resume()
                    }
                    isPlaying = !isPlaying
                    true
                }

                (event.key == Key.VolumeUp && event.type == KeyEventType.KeyUp) -> {
                    volume = minOf(1.0f, volume + 0.1f)
                    player.features[AudioLevelController]?.setVolume(volume)
                    true
                }

                (event.key == Key.VolumeDown && event.type == KeyEventType.KeyUp) -> {
                    volume = maxOf(0.0f, volume - 0.1f)
                    player.features[AudioLevelController]?.setVolume(volume)
                    true
                }

                else -> false
            }
        }) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 视频播放区域
                Box(modifier = Modifier.weight(1f)) {
                    MediampPlayerSurface(player, Modifier.fillMaxSize())
                }

                // 控制栏
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 播放/暂停按钮
                    IconButton(onClick = {
                        if (isPlaying) {
                            player.pause()
                        } else {
                            player.resume()
                        }
                        isPlaying = !isPlaying
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放"
                        )
                    }

                    // 后退按钮
                    IconButton(onClick = {
                        player.skip(-10000)
                    }) {
                        Icon(Icons.Default.Replay10, contentDescription = "后退10秒")
                    }

                    // 快进按钮
                    IconButton(onClick = {
                        player.skip(10000)
                    }) {
                        Icon(Icons.Default.Forward10, contentDescription = "快进10秒")
                    }

                    // 静音按钮
                    IconButton(onClick = {
                        isMuted = !isMuted
                        player.features[AudioLevelController]?.setVolume(if (isMuted) 0f else volume)
                    }) {
                        Icon(
                            imageVector = if (isMuted || volume == 0f) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "取消静音" else "静音"
                        )
                    }

                    // 音量滑块
                    Slider(
                        value = volume, onValueChange = { newVolume ->
                            volume = newVolume
                            player.features[AudioLevelController]?.setVolume(volume)
                            isMuted = volume == 0f
                        }, valueRange = 0f..1f, modifier = Modifier.width(100.dp)
                    )


                }

                // 进度条
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() / duration * 100 else 0f,
                    onValueChange = { progress ->
                        val newPosition = (progress / 100 * duration).toLong()
                        player.seekTo(newPosition)
                    },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 时间显示
                    Text(
                        text = "${formatTime(currentPosition)} ",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )  // 时间显示
                    Text(
                        text = "${formatTime(duration)} ",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                }

            }
        }
    }
}

// 辅助函数：格式化时间显示
private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
