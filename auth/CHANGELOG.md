# Change log

Version 1.2.0 *(2023-11-14)*
----------------------------

* Support VK custom redirect URL auth with custom tabs
* Bump dependencies

Version 1.1.0 *(2022-12-29)*
----------------------------

* Expose the activity result launcher and take it as a param.
* Fix `AuthMode.RequireWebView` ignored for `ResponseType.Code`.

Version 1.0.1 *(2022-12-29)*
----------------------------

* Switch to manual unregistering of the activity result launchers.

Version 1.0.0 *(2022-12-29)*
----------------------------

* Bump `minSdk` to `21`
* Support Chrome Custom Tabs (thanks @DrRey in #4)
* Migrate to Activity Result API (thanks @DrRey in #5)
* Simplify the API
* Migrate to Robolectric tests from `androidTest`
* Bump the dependencies

Version 0.0.2 *(2020-04-27)*
----------------------------

* Fixed instrumental tests
* Fixed showing web page errors and retry dialog on Lollipop+
* Added return of the error description after the web page error

Version 0.0.1 *(2020-04-27)*
----------------------------

First release.