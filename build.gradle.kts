buildscript {
    repositories {
        mavenCentral()
        google()
    }
}

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.android.extensions) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.publish) apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    group = project.property("GROUP").toString()
    version = project.property("VERSION_NAME").toString()
}

apply(from = "tasks.gradle.kts")