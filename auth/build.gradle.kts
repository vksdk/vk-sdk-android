@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.dokka)
    `maven-publish`
}

android {
    namespace = "com.petersamokhin.vksdk.android.auth"

    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        buildToolsVersion = libs.versions.buildToolsVersion.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        aarMetadata {
            minCompileSdk = libs.versions.minSdk.get().toInt()
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            consumerProguardFile("consumer-rules.pro")
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.browser)

    testImplementation(libs.junit)

    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.extJunit)
    androidTestImplementation(libs.espressoCore)
    androidTestImplementation(libs.mockito)
}

apply(from = "$rootDir/gradle/mavenpublish.gradle")