# Predictive DocV Android SDK v4

Learn how to integrate the Predictive Document Verification (DocV) Android SDK into your Android application. 

> Note: Document verification services will be disabled for older SDK versions soon. All SDK integrations must be updated to version 3.1.0 or later to meet compliance requirements.

## Minimum Requirements

Before getting started, check that your development environment meets the following requirements:

- Android SDK Version 22 (OS Version 5.1) and later

The DocV Android SDK v4 is compiled with:

- `compileSdkVersion 33`
- Java 11

> Note: Auto Capture feature requires Android SDK Version 28 (OS Version 9.0) and later with at least 3 GB of RAM. If the device does not meet these requirements, only the manual capture feature will be available.

## Integration

To add the DocV SDK to your application, include the Socure DocV SDK Maven repository in your `build.gradle` file at the end of the `allprojects > repositories` section:

```
allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

In your module level `build.gradle` file, add the following Socure DocV SDK dependency and replace `x.y.z` with the DocV Android SDK version:

```
 dependencies {
      implementation 'com.github.socure-inc:socure-docv:x.y.z'
 }
 ```

## Configuration and integration

For instructions on how to configure the SDK, see the [Android SDK documentation](https://developer.socure.com/docs/sdks/docv/android-sdk/v3-and-v4/quick-start) on DevHub.
