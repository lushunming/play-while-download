package db.migration

import cn.com.lushunming.models.Config
import cn.com.lushunming.util.Constant
import model.Tasks
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class V1 : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        transaction {
            SchemaUtils.create(Config, Tasks)
            Config.insert {
                it[Config.proxy] = "http://127.0.0.1:1080"
                it[Config.open] = 0
                it[Config.downloadPath] = Constant.downloadPath
            }
        }
    }
}