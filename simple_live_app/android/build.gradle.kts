allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
// 部分插件（如 auto_orientation_v2、dynamic_color）按 AGP 9 内置 Kotlin 编写，未应用 Kotlin 插件，
// 在 AGP 8 下会报 kotlin {} 无法解析。对含 Kotlin 源码但未应用 Kotlin 插件的库自动补上。
subprojects {
    pluginManager.withPlugin("com.android.library") {
        if (!pluginManager.hasPlugin("org.jetbrains.kotlin.android") &&
            file("src/main/kotlin").exists()
        ) {
            pluginManager.apply("org.jetbrains.kotlin.android")
            // Kotlin 目标与该库的 Java 目标保持一致，避免 JVM-target 不一致报错
            val androidExt = extensions.getByType(com.android.build.gradle.LibraryExtension::class.java)
            tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile::class.java).configureEach {
                compilerOptions.jvmTarget.set(
                    org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(
                        androidExt.compileOptions.targetCompatibility.toString()
                    )
                )
            }
        }
    }
}
subprojects {
    project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
