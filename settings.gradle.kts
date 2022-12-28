rootProject.buildFileName = "build.gradle.kts"
rootProject.name = "vk-sdk-android"

include(
    "auth"

    // Do NOT include ":examples:*" here;
    // they are individual projects.
    // To test, use `./gradlew publish` and test repository `./build/localMaven`
)

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}