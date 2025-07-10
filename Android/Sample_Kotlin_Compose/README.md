
# 🧩 Mapsted SDK Integration Guide (Android)

This guide walks you through integrating the Mapsted SDK into your Android project.

---

## 🔧 1. Add Maven Repository & Import SDKs

In your **project-level** `build.gradle` file, ensure the Maven repository is added:

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url "https://sdk.mapsted.com/maven"
        }
    }
}
```

In your **app-level** `build.gradle` file, add the SDK dependencies:

```gradle
dependencies {
    implementation 'com.mapsted:sdk-core:<version>'
    implementation 'com.mapsted:sdk-map:<version>'
    implementation 'com.mapsted:sdk-map-ui:<version>'
    // Add other SDK modules if needed
}
```

Replace `<version>` with the specific version provided by Mapsted.

---

## 🔐 2. Authentication: License File or Login-Based Access

You can choose one of the following approaches for SDK access:

### Option A: License File (No Login)
- Add the `assets` folder to your project (if not already present).
- Place the `license.key` file provided by Mapsted into the `assets` directory.

```plaintext
app/
└── src/
    └── main/
        └── assets/
            └── license.key
```

### Option B: Login-Based Access
- Use your credentials via API as per the authentication flow defined by your backend or Mapsted support.

---

## 📜 3. Add Required Permissions

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```

*Note:* Some permissions may vary depending on SDK modules used (e.g., BLE, Wi-Fi, etc.).

---

## 🧱 4. Add Layout Containers

In your activity/fragment XML layout (e.g., `map_ui.xml`), add containers for the base map and UI overlays:

```xml
<FrameLayout
    android:id="@+id/fl_base_map"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

<FrameLayout
    android:id="@+id/fl_map_ui"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

Optional:
```xml
<FrameLayout
    android:id="@+id/notification_container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_gravity="top" />
```

---

## 🧩 5. Extend Demo Application

Create your custom `Application` class and extend the SDK setup:

```kotlin
class DemoApplication : MapstedBaseApplication() {
    private lateinit var coreApi: CoreApi

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.d(TAG, "onActivityCreated: " + activity.localClassName)
            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                Log.d(TAG, "onActivityDestroyed: " + activity.localClassName)
            }
        })
        coreApi = MapstedSdk.initialize(this) // or with license key
    }

    fun getCoreApi(context: Context): CoreApi {
        return coreApi
    }
}
```

And update your `AndroidManifest.xml`:

```xml
<application
    android:name=".DemoApplication"
    ... >
```

---

## 🚀 6. Initialize SDKs in Activity

In your main `Activity` (e.g., `MapActivity.kt`):

```kotlin
val coreApi = (application as DemoApplication).getCoreApi(this)
val mapApi = MapstedMapApi.newInstance(application, coreApi)
val mapUiApi = MapstedMapUiApi.newInstance(application, mapApi)

// Set up parameters and initialize map
mapUiApi.setup().initialize(params, callback, locationServicesCallback)
```

Ensure you handle lifecycle events (`onDestroy`, `onBackPressed`, `onConfigurationChanged`) accordingly.

---

## ✅ You're Done!

The Mapsted SDK should now be integrated and ready to use.  
For advanced configuration (e.g., overlays, positioning, routing), refer to the official Mapsted developer documentation or contact support.
