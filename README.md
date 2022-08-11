# Predictive DocV Android SDK v3

The Predictive Document Verification (DocV) Android SDK v3 provides a framework to add image capture and upload services to your mobile application. 

## Minimum Requirements

- Android SDK Version 22 (OS Version 5.1) and later
- Android SDK is compiled with `compileSdkVersion 32` and Java 11

## Configuration and integration

The DocV Android SDK v3 allows integration as simple as writing a single line of code:
```
SocureDocVHelper.getIntent(context, socure_sdk_key, config)
```

| Argument           | Description                                                                                                                                                                                                                                                                                                       |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `SocureDocVHelper` | A SDK helper class.                                                                                                                                                                                                                                                                                         |
| `socure_sdk_key`   | The unique SDK key obtained from Admin Dashboard. For more information on SDK keys, see the [Getting Started](https://developer.socure.com/docs/).                                                                                                                          |
| `context`          | Activity context                                                                                                                                                                                                                                                                                                  |
| `config`           | An optional JSON string or null value that specifies a custom flow. The `your_custom_flow_name` value specifies the name of the flow (created in Admin Dashboard) that the DocV SDK should use.  <br /> <br />`"{'flow':{'name':'your_custom_flow_name'}}"` <br /> <br />If the value is `null`, the DocV SDK will fetch the default flow from Admin Dashboard. |

Before you can use the DocV Android SDK v3, you must perform the following steps:

- [Add SDK dependencies](#add-sdk-dependencies)
- [Camera permissions](#camera-permissions)
- [Launch Socure DocV SDK](#launch-socure-docv-sdk)

### Add SDK dependencies

In your root `build.gradle` file, at the end of the `allprojects` > `repositories` section, add the Socure DocV SDK Maven repository. 

``` {4}
allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

In your module level `build.gradle` file, add the following Socure DocV SDK dependency:

``` {2}
 dependencies {
      implementation 'com.github.socure-inc:socure-docv:x.y.z'
 }
 ```

### Camera permissions

The DocV Android SDK requires camera permission to capture identity documents. Upon the first invocation of the SDK, your app will request camera permission from the user.

**Note**: We recommend you check for camera permissions before calling the Socure DocV SDK’s launch API. 

#### Required permissions

Ensure that your app manifest has been set up properly to request the following required permissions:

```
<uses-feature android:name="android.hardware.camera" />

<!-- Declare permissions -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

```

### Launch Socure DocV SDK

To launch the Socure DocV SDK, call the launch function using `ActivityResultLauncher`. 

```
val startForResult: ActivityResultLauncher<Intent> = registerForActivityResult(...)
...
startForResult.launch(SocureDocVHelper.getIntent(context, socure_sdk_key, config))

```

Once the Socure DocV SDK is successfully launched, you will be able to set up a listener for Callback Events and customize most aspect of the interface. For more information, see the [Android SDK documentation](https://developer.socure.com/docs/sdks/docv/android-sdk/android-sdk-v3/) on DevHub. 
