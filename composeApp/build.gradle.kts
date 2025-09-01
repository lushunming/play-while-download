import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val hikariCpVersion = "5.0.1"
val flywayVersion = "8.5.4"
val logback_version = "1.4.14"
val koin_version = "4.0.3"
version = "1.0.3"
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    id("io.ktor.plugin") version "3.2.2"
    id("dev.hydraulic.conveyor") version "1.12"
}
dependencies {


    // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}
configurations.all {
    attributes {
        // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}
kotlin {
    jvm()
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.JETBRAINS)
    }
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
            /*
                        implementation("io.ktor:ktor-server-thymeleaf")
            */

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
            implementation("network.chaintech:compose-multiplatform-media-player:1.0.42")
            implementation("io.insert-koin:koin-compose:${koin_version}")
            implementation("io.insert-koin:koin-compose-viewmodel:${koin_version}")
            implementation("io.insert-koin:koin-compose-viewmodel-navigation:${koin_version}")
            implementation("io.insert-koin:koin-core:${koin_version}")
            // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.

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
            packageName = "play-while-download"
            packageVersion = "1.0.2"
            modules(
                "java.compiler",
                "java.instrument",
                "java.management",
                "java.naming",
                "java.net.http",
                "java.prefs",
                "java.sql",
                "jdk.jfr",
                "jdk.jsobject",
                "jdk.unsupported",
                "jdk.unsupported.desktop",
                "jdk.xml.dom"
            )

            windows {
                iconFile.set(project.file("src/jvmMain/composeResources/drawable/icon.ico"))
                dirChooser = true
                upgradeUuid = "161FA5A0-A30B-4568-9E84-B3CD637CC8FE"
                shortcut = true
            }

            linux {
                iconFile.set(project.file("src/jvmMain/composeResources/drawable/icon_main.png"))
            }

            macOS {
                iconFile.set(project.file("src/jvmMain/composeResources/drawable/icon.icns"))
            }

        }
    }
}
tasks.register<Exec>("convey") {
    val dir = layout.buildDirectory.dir("packages")
    outputs.dir(dir)
    commandLine("conveyor", "make", "--output-dir", dir.get(), "site")
    dependsOn( "jvmJar","writeConveyorConfig")
}