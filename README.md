# VK SDK Android
![cover](docs/images/android_cover.png)

<p align="center">Some Android-specific features built into Unofficial VK SDK for Android</p>

---

![Build](https://github.com/vksdk/vk-sdk-android/workflows/Release/badge.svg) ![Documentation](https://github.com/vksdk/vk-sdk-android/workflows/Documentation/badge.svg) [![Kotlin 1.3.72](https://img.shields.io/badge/Kotlin-1.8.0-blue.svg?style=flat)](http://kotlinlang.org) [![API version](https://img.shields.io/badge/API%20version-5.113-blue?style=flat&logo=vk&logoColor=white)](https://vk.com/dev/versions)
[![GitHub license](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat)](https://github.com/vksdk/vk-sdk-android/blob/master/LICENSE)

The official VK Android SDK is monstrous, has poor API, does not contain some important features and their code leaves much to be desired.
This is the reason why this SDK is created.
See the available features below.

See the documentation: [https://vksdk.github.io/vk-sdk-android](https://vksdk.github.io/vk-sdk-android)

## Auth
[![Android minSdkVersion](https://img.shields.io/badge/minSdkVersion-21-yellowgreen)](https://img.shields.io/badge/minSdkVersion-16-yellowgreen) [![Android targetSdkVersion](https://img.shields.io/badge/targetSdkVersion-33-green)](https://img.shields.io/badge/targetSdkVersion-33-green)

Latest version:  [![maven-central](https://img.shields.io/badge/Maven%20Central-1.1.0-yellowgreen?style=flat)](https://search.maven.org/search?q=g:com.petersamokhin.vksdk.android)

[Authorization code flow](https://vk.com/dev/authcode_flow_user) is not supported by the official VK SDK and by the official app.
But it is supported by this auth feature.

Easiest way to authorize user with VK and get the token:

```kotlin
// From here: https://vk.com/apps?act=manage
// Choose the app and get the ID from here: https://vk.com/editapp?id=XXX
// Or go here: https://vk.com/editapp?id=XXX&section=options and see the App ID

val callback = { result: VkAuthResult ->
    when (result) {
        is VkAuthResult.AccessToken -> {
            // do something with result.accessToken
        }
        is VkAuthResult.Error -> {
            // do something with result.error
        }
    }
}

// before activity.onCreate
val launcher = VkAuth.register(activity, callback)

// somewhere onClick
VkAuth.login(activity, launcher, params)
```

## Install
Library is uploaded to the Maven Central Repository.

Add the following line to your dependencies:
```groovy
implementation "com.petersamokhin.vksdk.android:auth:$vkSdkAndroidVersion"
```

For other information and for details, see the documentation: [https://vksdk.github.io/vk-sdk-android/auth/](https://vksdk.github.io/vk-sdk-android/auth/)

## License
See the [License](https://github.com/vksdk/vk-sdk-android/blob/master/LICENSE)