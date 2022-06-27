# Document Verification Android SDK

The Document Verification Android SDK provides a framework to add image capture and upload services to your native Android applications. The SDK supports image capture for both Android and iOS. This guide covers only Android.

## Minimum Requirements

| Feature                          | Minimum Requirements |
|----------------------------------|----------------------|
| Android SDK                      | `minSdkVersion 22`   |
| Document and Selfie Auto Capture | Android 8 and above  |

### OkHttp3 and Retrofit dependencies

| Feature  | Version |
|----------|---------|
| okHttp   | 4.9.3   |
| retrofit | 2.9.0   |

```
def retrofitVersion = '2.9.0'
implementation "com.squareup.retrofit2:retrofit:$retrofitVersion"
implementation "com.squareup.retrofit2:converter-gson:$retrofitVersion"

def okHttpVersion = '4.9.3'
implementation "com.squareup.okhttp3:okhttp:$okHttpVersion"
implementation "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"
```

## Installation

To install the DocV Android SDK, you must perform the following steps:
 - [Add Socure to the build.gradle](#add-socure-to-the-buildgradle)
 - [Update permissions](#update-permissions)
 - [Add API keys](#add-api-keys)

### Add Socure to the build.gradle

You must add the following to your `build.gradle`:

- Add the maven URL in the root/project level `build.gradle`:

```
(NOT IN THE `buildscript` code block above. allprojects is a sibling to build script)


allprojects {
  	  	repositories {
      	 		 .......

       			 maven {
           				 url "https://jitpack.io"
     		 	 }
    		}
}
```

- Add the following to the `build.gradle` of the module/library that uses the framework:

```
dependencies {
   		 implementation 'com.github.socure-inc:android-sdk:2.0.13'
}
```

### Update permissions

Make the following permission updates for Android in the Android.manifest:

```
<uses-permission android:name="android.permission.CAMERA"></uses-permission>
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-feature android:name="android.hardware.camera.flash" android:required="false" />
<uses-feature android:name="android.hardware.camera" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### Add API keys

Use the Admin Dashboard to find the Socure public key.

- Add the key to your application:

```
<string name="socurePublicKey" translatable="false">YOUR_KEY_HERE</string>
```
- Add the API key to `string.xml` as a resource.

### Configuration and usage

For instructions on how to configure the SDK, see the [Android SDK documentation](https://developer.socure.com/docs/sdks/docv/android-sdk).