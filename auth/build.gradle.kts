plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(Config.Android.compileSdk)
    defaultConfig {
        minSdkVersion(Config.Android.minSdk)
        targetSdkVersion(Config.Android.compileSdk)
        buildToolsVersion(Config.Android.buildToolsVersion)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // applicationId = "${project.property("GROUP").toString()}.auth"
        versionCode = 1
        versionName = "0.0.1"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            consumerProguardFile("consumer-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    sourceSets {
        val main by getting {}
        val test by getting {}
        val androidTest by getting {}
        main.java.srcDirs("src/main/kotlin")
        test.java.srcDirs("src/test/kotlin")
        androidTest.java.srcDirs("src/androidTest/kotlin")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8", Config.Versions.Kotlin.kotlin))
    
    implementation("androidx.appcompat:appcompat:${Config.Versions.Android.appCompat}")
    implementation("androidx.core:core-ktx:${Config.Versions.Android.coreKtx}")
    implementation("androidx.constraintlayout:constraintlayout:${Config.Versions.Android.constraintLayout}")
    
    testImplementation("junit:junit:${Config.Versions.Test.junit}")

    androidTestImplementation("androidx.test:runner:${Config.Versions.AndroidTest.runner}")
    androidTestImplementation("androidx.test.ext:junit:${Config.Versions.AndroidTest.extJunit}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Config.Versions.AndroidTest.espressoCore}")
    androidTestImplementation("org.mockito:mockito-android:${Config.Versions.Test.mockito}")
}

apply(from = "$rootDir/gradle/mavenpublish.gradle")