package cn.com.lushunming.util

import dev.dirs.UserDirectories
import java.io.File

object Paths {
    private const val ApplicationName = "play-while-download"

    private val userDataDir = getUserDataDir()

    private fun getUserDataDir() = run {
        File(
            UserDirectories.get().documentDir, ApplicationName
        )
    }

    private fun File.check(): File {
        if (!exists()) {
            mkdirs()
        }
        return this
    }

    fun root(): File {
        return userDataDir.resolve("data")
    }

    fun userDataRoot(): File {
        return userDataDir
    }

    fun doh(): File {
        return cache("doh").check()
    }

    private fun cache(path: String): File {
        return root().resolve("cache").resolve(path)
    }

    fun db(): String {
        val path = userDataRoot().resolve("db").check().resolve("m3u8-proxy")
        return "jdbc:h2:${path}"

    }
    fun log(): String {
        val path = userDataRoot().resolve("logs").check()
        return "$path"

    }


}