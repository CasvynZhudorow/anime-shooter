plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.libgdx.core)
    implementation(libs.libgdx.box2d)
    implementation(libs.libgdx.freetype)
    implementation(libs.ashley)
    
    // Desktop natives for testing
    testImplementation(libs.libgdx.platform)
    testImplementation(libs.libgdx.box2d.platform)
    testImplementation(libs.libgdx.freetype.platform)
    
    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf("-Xjvm-default=all", "-Xopt-in=kotlin.RequiresOptIn")
        }
    }
    
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}