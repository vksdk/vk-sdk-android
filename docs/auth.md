# VK SDK Android — Auth

For the detailed information about the VK auth process, see the official documentation: [https://vk.com/dev/access_token](https://vk.com/dev/access_token)

## Ways to show the login page
If user had installed the official VK App, you can ask for the access without user need to enter the login and password.
But [Authorization code flow](https://vk.com/dev/authcode_flow_user) is not supported either by the VK App or by official SDK.
Otherwise, WebView will be used and user will be used to enter their credentials only the first time.

Using the VK SDK Android — Auth feature, you can:
- use `login` method and way to show the page will be chosen automatically
- force to use the WebView using the `VkAuth.loginWithWebView` methods
- force to use the VK App using the `VkAuth.loginWithApp` methods
    - exception will be thrown if app is not installed, but you can manually check this fact using the method `VkAuth.isVkAppInstalled`
    
## Ways to retrieve the auth result
In both cases, with the VK App or with WebView, some Activities will be opened using the `startActivityForResult` method.

### Use the inline callback
`androidx.fragment.app.FragmentActivity` (e.g. `AppCompatActivity`) is required as the first param:
```kotlin
class MainActivity : androidx.appcompat.app.AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    
        someButton.setOnClickListener {
            VkAuth.login(this, /* Parameters */) { result ->
                when (result) {
                    is VkAuthResult.AccessToken -> {
                        /* do something with result.accessToken */
                    }
                    is VkAuthResult.Error -> {
                        /* do something with result.error */
                    }
                }
            }
        }
    }
}
```

### Use the onActivityResult
Use any activity as the first param, but override the `onActivityResult`:
```kotlin
class MainActivity : android.app.Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    
        someButton.setOnClickListener {
            VkAuth.login(this, /* Parameters */)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = VkAuth.parseResult(requestCode, resultCode, data) 
            ?: return super.onActivityResult(requestCode, resultCode, data)
        
        when (result) {
            is VkAuthResult.AccessToken -> {
                /* do something with result.accessToken */
            }
            is VkAuthResult.Error -> {
                /* do something with result.error */
            }
        }
    }
}
```

## Parameters
```kotlin
// For the first way
val activity: Activity = this

// For the second way
val activity: FragmentActivity = this

// From here: https://vk.com/apps?act=manage
// Choose the app and get the ID from here: https://vk.com/editapp?id=XXX
// Or go here: https://vk.com/editapp?id=XXX&section=options and see the App ID
val appId = 1

// See: https://vk.com/dev/implicit_flow_user
val responseType = VkAuth.ResponseType.AccessToken 

// See: https://vk.com/dev/authcode_flow_user
// Only needed if you use server-side auth
// Only supported using WebView; ignored by the VK App
val responseType = VkAuth.ResponseType.Code 

// See: https://vk.com/dev/permissions
// Can be empty, so token will be valid for the one day 
// and you will be able to retrieve the basic user info.
// You can use the comma-separated string values
val scopes = "offline,email"

// Or int values
val scopes = 65536 + 4194304

// Or pre-defined constants
val scopes = listOf(VkAuth.Scope.Offline, VkAuth.Scope.Email)

// Redirect URL after the successful or unsuccessful auth
// This page will not be shown.
// Default is VkAuth.VK_REDIRECT_URI_DEFAULT = "https://oauth.vk.com/blank.html"
val redirectUri = "YourCustomRedirectUri"

// The display type of the auth page
// Default is VkAuth.Display.Mobile
// .Android and .Ios are the private values used by the official VK Apps
// because official apps also use WebView
// Only supported using WebView; ignored by the VK App
val display = VkAuth.Display.Mobile // this is the default and recommended value

// An arbitrary string that will be returned together with authorization result.
// Only supported using WebView; ignored by the VK App
val state = "test_1234" // empty by default

// Sets that permissions request should not be skipped even if a user is already authorized.
// If set to false, the web page will not be shown
val revoke = true // true by default

// See: https://vk.com/dev/versions
val apiVersion = 5.103 // 5.103 by default

// if activity is FragmentActivity, you can use the callback
val callback = object: ResultListener {
    override fun onResult(result: VkAuthResult) {
        // do something with result.accessToken, result.code or result.error 
    }
}
// or for Kotlin
val callback = { result: VkAuthResult ->
    // do something with result.accessToken, result.code or result.error
}
```

You can pass all the parameters directly:
```kotlin
VkAuth.login(
    activity, 
    appId,
    responseType,
    // all the other parameters are optional
    scopes,
    redirectUri,
    display,
    state,
    revoke,
    apiVersion,
    callback // only if you want to get the inline result
)
```

Or use `VkAuth.AuthParams`:

```kotlin
val params = VkAuth.AuthParams(
    appId,
    responseType,
    // all the other parameters are optional
    scopes,
    redirectUri,
    display,
    state,
    revoke,
    apiVersion
)

VkAuth.login(activity, params, callback)
```

## Handle the result
[*`VkAuthResult`*](https://vksdk.github.io/vk-sdk-android/0.0.x/auth/com.petersamokhin.vksdk.android.auth.model/-vk-auth-result/) is a sealed class.

If `responseType` is `VkAuth.ResponseType.AccessToken`, you will get the `VkAuthResult.AccessToken`.
If `responseType` is `VkAuth.ResponseType.Code`, you will get the `VkAuthResult.Code`.

If auth is unsuccesfull and some error occured, you will get the `VkAuthResult.Error`.
Check the `error`, `errorReason` and `errorDescription` fields of the result.
But if page wasn't shown and some error occurred before the auth process, these fields will be empty and `exception` field will contain the exception.

Example for `AccessToken`:
```kotlin
val params = VkAuth.AuthParams(
    appId,
    VkAuth.ResponseType.AccessToken
)

VkAuth.login(activity, params) { result ->
    when (result) {
        is VkAuthResult.AccessToken -> {
            /* do something with result.accessToken, result.expiresIn, result.userId, etc. */
        }
        is VkAuthResult.Error -> {
            /* do something with result.error */
        }
    }
}
```


Example for `Code`:
```kotlin
val params = VkAuth.AuthParams(
    appId,
    VkAuth.ResponseType.Code
)

VkAuth.login(activity, params) { result ->
    when (result) {
        is VkAuthResult.Code -> {
            /* do something with result.code, result.state */
        }
        is VkAuthResult.Error -> {
            /* do something with result.error, etc. or with result.exception */
        }
    }
}
```