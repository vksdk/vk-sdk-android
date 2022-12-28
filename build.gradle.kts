@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

buildscript {
    repositories {
        mavenCentral()
        google()
    }
}

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.dokka) apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    group = project.property("GROUP").toString()
    version = project.property("VERSION_NAME").toString()

    afterEvaluate {
        if (path != rootProject.path) {
            extensions.configure<com.android.build.gradle.LibraryExtension>("android") {
                (this as ExtensionAware).extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions>("kotlinOptions") {
                    @Suppress("SuspiciousCollectionReassignment")
                    freeCompilerArgs += "-Xexplicit-api=warning"
                }
            }
        }
    }
}

apply(from = "tasks.gradle.kts")