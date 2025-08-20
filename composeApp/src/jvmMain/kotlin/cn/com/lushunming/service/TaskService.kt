package cn.com.lushunming.service

import model.DownloadStatus
import model.Task
import model.Tasks
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class TaskService {


    suspend fun getTaskList(): List<Task> {
        return DatabaseFactory.dbQuery {
            Tasks.selectAll().map { toTask(it) }
        }
    }


    suspend fun addTask(task: Task) {
        DatabaseFactory.dbQuery {
            Tasks.insert {
                it[Tasks.id] = task.id
                it[Tasks.name] = task.name
                it[Tasks.url] = task.url
                it[Tasks.oriUrl] = task.oriUrl
                it[Tasks.type] = task.type
                it[Tasks.progress] = task.progress
                it[Tasks.status] = task.status.ordinal
            }
        }
    }

    suspend fun updateProgress(id: String, progress: Int) {
        DatabaseFactory.dbQuery {
            Tasks.update(where = { Tasks.id eq id }) {
                it[Tasks.progress] = progress

            }
        }
    }

    suspend fun getTaskById(id: String): Task? {

        return DatabaseFactory.dbQuery {
            Tasks.selectAll().where { Tasks.id eq id }.map { toTask(it) }.singleOrNull()
        }
    }

    private fun toTask(row: ResultRow): Task = Task(
        id = row[Tasks.id], name = row[Tasks.name], url = row[Tasks.url], type = row[Tasks.type],
        progress = row[Tasks.progress],
        status = DownloadStatus.entries[row[Tasks.status]],
        oriUrl = row[Tasks.oriUrl],
    )

    suspend fun deleteTask(id: String?) {
        id ?: return
        return DatabaseFactory.dbQuery {
            Tasks.deleteWhere { Tasks.id eq id }
        }

    }

    fun updateStatus(id: String, downloading: DownloadStatus) {


        DatabaseFactory.dbQuery {
            Tasks.update(where = { Tasks.id eq id }) {
                it[Tasks.status] = downloading.ordinal
            }
        }
    }
}