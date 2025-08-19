import org.jetbrains.compose.desktop.application.dsl.TargetFormat
val hikariCpVersion = "5.0.1"
val flywayVersion = "8.5.4"
val logback_version= "1.4.14"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    id("io.ktor.plugin") version "3.2.2"
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("io.ktor:ktor-server-cors")
            implementation("io.ktor:ktor-server-core")
/*
            implementation("io.ktor:ktor-server-swagger")
*/
            implementation("io.ktor:ktor-server-call-logging")
            implementation("io.ktor:ktor-server-content-negotiation")
            implementation("io.ktor:ktor-serialization-gson")
            implementation("io.ktor:ktor-server-netty")
           /* implementation("io.ktor:ktor-server-config-yaml")
            implementation("io.ktor:ktor-server-websockets")*/
            implementation("io.ktor:ktor-server-thymeleaf")

            implementation("io.ktor:ktor-client-core")
            implementation("io.ktor:ktor-client-okhttp")
            implementation("io.ktor:ktor-client-okhttp-jvm:3.2.2")
            implementation("io.ktor:ktor-client-logging")
            //db
            implementation("org.jetbrains.exposed:exposed-core:1.0.0-beta-5")
            implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0-beta-5")
            implementation("com.h2database:h2:2.2.224")
            implementation("com.zaxxer:HikariCP:${hikariCpVersion}")
            implementation("org.flywaydb:flyway-core:${flywayVersion}")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation("ch.qos.logback:logback-classic:${logback_version}")
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "cn.com.lushunming.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "cn.com.lushunming"
            packageVersion = "1.0.0"
        }
    }
}
