rootProject.buildFileName = "build.gradle.kts"
rootProject.name = "vk-sdk-android"

include(
    "auth"

    // Do NOT include ":examples:*" here;
    // they are individual projects.
    // To test, use `./gradlew publish` and test repository `./build/localMaven`
)