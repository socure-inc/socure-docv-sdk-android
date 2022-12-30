# Predictive DocV Android SDK v2

The Predictive Document Verification (DocV) Android SDK v2 provides a framework to add image capture and upload services to your mobile application.

>Note: All SDK v2 integrations must be updated to version 2.2.2 or later to meet compliance requirements. Document verification services will be disabled for older SDK versions soon.

## Minimum Requirements

- Android SDK is compiled with `minSdkVersion 22` and `compilerSDKVersion 32`
- Document and Selfie Auto Capture features require Android 8 and later

Additionally, OkHttp3 and Retrofit dependencies require the following: 

- okHttp version `4.9.3`
- retrofit version `2.9.0` 

```
def retrofitVersion = '2.9.0'
implementation "com.squareup.retrofit2:retrofit:$retrofitVersion"
implementation "com.squareup.retrofit2:converter-gson:$retrofitVersion"

def okHttpVersion = '4.9.3'
implementation "com.squareup.okhttp3:okhttp:$okHttpVersion"
implementation "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"
```

## Installation

To install the DocV Android SDK, complete the following steps:

 - [Add SDK dependencies](#add-sdk-dependencies)
 - [Update permissions](#update-permissions)
 - [Add your SDK key](#add-your-sdk-key)

### Add SDK dependencies

In your root `build.gradle` file, add the maven URL: 

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

Add the following to the `build.gradle` of the module/library that uses the framework:

```
dependencies {
   		 implementation 'com.github.socure-inc:android-sdk:2.1.1'
}
```

### Update permissions

Make the following permission updates for Android in the Android.manifest:

```java
<uses-permission android:name="android.permission.CAMERA"></uses-permission>
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-feature android:name="android.hardware.camera.flash" android:required="false" />
<uses-feature android:name="android.hardware.camera" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### Add your SDK key

To add your SDK key, go to [Admin Dashboard](https://developer.socure.com/docs/) and copy your Socure public key, then add the key to your application using one of the following methods: 

> **Note:** The key is validated when the SDK is initialized. If you do not set your application public key using one of the methods below, an `"Empty Key"` error will be returned.

- **Method 1**: Configure the key in the `string.xml` as a resource:

```
<string name="socurePublicKey" translatable="false">YOUR_KEY_HERE</string>
```

- **Method 2**: Use the function `SDKAppDataPublic.setSocureSdkKey(publicKey: String)` to set the public key before initiating the scan process. Note that this method is given preference over the adding the key in `string.xml`. 

## Configuration and usage

For instructions on how to configure the SDK, see the [Android SDK documentation](https://developer.socure.com/docs/sdks/docv/android-sdk/android-sdk-v2).
