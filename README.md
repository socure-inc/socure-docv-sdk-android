# Predictive DocV Android SDK v3

The Predictive Document Verification (DocV) Android SDK v3 provides a framework to add image capture
and upload services to your mobile application.

> Note: Document verification services will be disabled for older SDK versions soon. All SDK v3 integrations must be updated to version **3.1.0 or later** to meet compliance requirements.

## Minimum Requirements

- Android SDK Version 22 (OS Version 5.1) and later
- Android SDK is compiled with `compileSdkVersion 32` and Java 11

> Note: Document and Selfie Auto Capture features require Android SDK Version 28 (OS Version 9.0) and later with at least 3 GB of RAM. If the device does not meet these requirements, only the manual capture feature will be available.

## Add SDK dependencies

In your root `build.gradle` file, at the end of the `allprojects` > `repositories` section, add the
Socure DocV SDK Maven repository.

```
allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

In your module level `build.gradle` file, add the following Socure DocV SDK dependency:

```
 dependencies {
      implementation 'com.github.socure-inc:socure-docv:x.y.z'
 }
 ```

## Configuration and integration

For instructions on how to configure the SDK, see
the [Android SDK documentation](https://developer.socure.com/docs/sdks/docv/android-sdk/android-sdk-v3)
on DevHub.