package cn.com.lushunming.service

import cn.com.lushunming.util.Util
import cn.com.lushunming.viewmodel.TaskViewModel
import model.DownloadStatus
import model.Task
import org.slf4j.LoggerFactory
import java.io.File


class DownloadService(val viewModel: TaskViewModel) {
    val logger = LoggerFactory.getLogger(DownloadService::class.java)

    fun addTask(
        urlParam: String, downloadPath: String,

        headerParam: MutableMap<String, String>, id: String, fileName: String, url: String
    ) {


        //   val info = getDownloadInfo(urlParam, headerParam);
        val type = "Unknown";

        val task = Task(
            id, fileName, url, urlParam, type, 0, DownloadStatus.DOWNLOADING
        )
        val dir = downloadPath + File.separator + Util.md5(urlParam)
        File(dir).mkdirs()
        val headerFile = File(dir, "header.tmp")
        headerFile.writeText(Util.json(headerParam))
        viewModel.startDownload(task, dir)


        viewModel.addTask(
            Task(
                id, fileName, url, urlParam, type, 0, DownloadStatus.DOWNLOADING
            )
        )
    }


}
