# VK SDK Android — Auth

For the detailed information about the VK auth process, see the official documentation: [https://vk.com/dev/access_token](https://vk.com/dev/access_token)

## Ways to show the login page
If user had installed the official VK App, you can ask for the access without user need to enter the login and password.
But [Authorization code flow](https://vk.com/dev/authcode_flow_user) is not supported either by the VK App or by official SDK.
Otherwise, WebView will be used and user will be used to enter their credentials only the first time.

Using the VK SDK Android — Auth feature, you can:
- use `login` method and way to show the page will be chosen automatically
- force to use the WebView using the `VkAuth.AuthMode.RequireWebView` method
- force to use the VK App using the `VkAuth.AuthMode.RequireApp` methods
    - exception will be thrown if app is not installed, but you can manually check this fact using the
      method `VkAuth.isVkAppInstalled`

## Ways to retrieve the auth result
In both cases, with the VK App or with WebView, some Activities will be opened using the `startActivityForResult` method.<br/>
Using Custom Tabs, `onNewIntent` listener will be added.

### Use the inline callback

`androidx.activity.ComponentActivity` (e.g. `AppCompatActivity`) is required as the first param:

```kotlin
class MainActivity : androidx.appcompat.app.AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        // must be called before onCreate
        VkAuth.register(this) { result ->
            Log.e("vk_auth", result.toString())
        }
        super.onCreate(savedInstanceState)

        someButton.setOnClickListener {
            VkAuth.login(this, params)
        }
    }
}
```

### Chrome Custom Tabs
To support auth via Chrome Custom Tabs, you need:

#### Use a custom redirect URI on your website
1. First of all, you need to add the redirect URI to the app settings.
   For this, go here: https://vk.com/apps?act=manage
   Choose your app, and then go to the "Settings": https://vk.com/editapp?id=XXX&section=options
   And add your redirect URI to the "Authorized redirect URI:" field.

2. On your website, create a manifest:
   `https://domain.com/.well-known/assetlinks.json`
    ```json
    [
      {
        "relation": [
          "delegate_permission/common.handle_all_urls"
        ],
        "target": {
          "namespace": "android_app",
          "package_name": "com.example.android",
          "sha256_cert_fingerprints": [
            "./gradlew signingReport -> SHA:256"
          ]
        }
      }
    ]
    ```

3. Make your auth activity (from where you will do the auth) discoverable:
    ```xml
    <activity android:name=".auth.YourAuthActivity"
        android:exported="true" 
        android:launchMode="singleTop">
    
        <intent-filter android:autoVerify="true" tools:targetApi="m">
            <action android:name="android.intent.action.VIEW" />
    
            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
    
            <data android:scheme="https" />
            <data android:scheme="http" />
            <data android:host="example.com" />
        </intent-filter>
    </activity>
    ```

4. In your auth activity, just before `onCreate`, call `VkAuth.register(this)`.

Done! Now your user will have a greater & secure experience.<br/>
They will not have to enter their passwords, if they logged into VK in their browser.

## Parameters
```kotlin
val activity: ComponentActivity = this

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
// But it has to be overridden in order to support the Custom Tabs.
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
val apiVersion = "5.113" // 5.113 by default
```

Use `VkAuth.AuthParams`:
```kotlin
val params = VkAuth.AuthParams(
    clientId = appId,
    responseType = responseType,
    
    // required for Custom Tabs
    redirectUri = redirectUri,
    
    // all the other parameters are optional
    scope = scopes,
    display = display,
    state = state,
    revoke = revoke,
    apiVersion = apiVersion,
)

VkAuth.login(activity, params, authMode)
```

Auth modes:
```kotlin
/**
 * Use this as a param to [VkAuth.login] to specify the behavior
 */
public enum class AuthMode {
    /**
     * VK official app will be used if [VkAuth.isVkAppInstalled],
     * otherwise an error will be thrown
     */
    RequireApp,

    /**
     * VK official app will not be used even if it is available, it will not be checked.
     * Otherwise, if the Chrome Custom Tabs are available, they will be used.
     * Otherwise, a WebView will be used.
     */
    RequireWeb,

    /**
     * VK official app will not be used even if it is available, it will not be checked.
     * Chrome Custom Tabs will not be used even if it is available, it will not be checked.
     * A WebView will be always used.
     */
    RequireWebView,

    /**
     * If the VK official app is available, it will be used.
     * Otherwise, if the Chrome Custom Tabs are available, they will be used.
     * Otherwise, a WebView will be used.
     */
    Auto
}
```

## Handle the result
[*`VkAuthResult`*](https://vksdk.github.io/vk-sdk-android/1.x/auth/com.petersamokhin.vksdk.android.auth.model/-vk-auth-result/)is a sealed class.

If `responseType` is `VkAuth.ResponseType.AccessToken`, you will get the `VkAuthResult.AccessToken`.
If `responseType` is `VkAuth.ResponseType.Code`, you will get the `VkAuthResult.Code`.

If auth is unsuccesfull and some error occured, you will get the `VkAuthResult.Error`.
Check the `error`, `errorReason` and `errorDescription` fields of the result.
But if page wasn't shown and some error occurred before the auth process, these fields will be empty and `exception` field will contain the exception.

Example for `AccessToken`:
```kotlin
val params = VkAuth.AuthParams(
    clientId = appId,
    responseType = VkAuth.ResponseType.AccessToken
)

val callback = { result: VkAuthResult ->
    when (result) {
        is VkAuthResult.AccessToken -> {
            /* do something with result.accessToken, result.expiresIn, result.userId, etc. */
        }
        is VkAuthResult.Error -> {
            /* do something with result.error */
        }
    }
}

// before activity.onCreate
VkAuth.register(activity, callback)

// somewhere onClick
VkAuth.login(activity, params)
```

Example for `Code`:
```kotlin
val params = VkAuth.AuthParams(
    clientId = appId,
    responseType = VkAuth.ResponseType.Code
)

val callback = { result: VkAuthResult ->
    when (result) {
        is VkAuthResult.Code -> {
            /* do something with result.code, result.state */
        }
        is VkAuthResult.Error -> {
            /* do something with result.error, etc. or with result.exception */
        }
    }
}

// before activity.onCreate
VkAuth.register(activity, callback)

// somewhere onClick
VkAuth.login(activity, params)
```