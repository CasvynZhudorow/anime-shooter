plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.application)
}

application {
    mainClass.set("com.animeshooter.DesktopLauncher")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.libgdx.backend.lwjgl3)
    implementation(libs.libgdx.platform)
    implementation(libs.libgdx.box2d.platform)
    implementation(libs.libgdx.freetype.platform)
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf("-Xjvm-default=all", "-Xopt-in=kotlin.RequiresOptIn")
        }
    }
    
    run {
        workingDir = project.rootDir
    }
}