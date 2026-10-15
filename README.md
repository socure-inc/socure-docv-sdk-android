# Predictive DocV Android SDK v5

Learn how to quickly integrate with the Predictive Document Verification (DocV) Android SDK v5.

## Table of Contents

- [Getting started](#getting-started)
  - [Datadog telemetry (v5.7.0 or later)](#upgrading-to-android-docv-sdk-v570-or-later-datadog-telemetry)
  - [Upgrading to Android DocV SDK v5.4.3 or later](#upgrading-to-android-docv-sdk-v543-or-later)
  - [Upgrading to Android DocV SDK v5.4.0 or later](#upgrading-to-android-docv-sdk-v540-or-later)
  - [Using the Device SDK independently](#using-the-device-sdk-independently)
  - [16KB page support](#16kb-page-support)
- [Step 1: Generate a transaction token](#step-1-generate-a-transaction-token-and-configure-the-capture-app)
  - [Call the Document Request endpoint](#call-the-document-request-endpoint)
- [Step 2: Add the DocV Android SDK](#step-2-add-the-docv-android-sdk)
  - [Camera and file permissions](#camera-and-file-permissions)
  - [Initialize and launch the SDK](#initialize-and-launch-the-sdk)
  - [Handle the response](#handle-the-response)
- [Step 3: Fetch the verification results](#step-3-fetch-the-verification-results)

## Getting started

Before you begin, ensure you have the following: 

- Get a valid [ID+ key from Admin Dashboard](https://developer.socure.com/docs/admin-dashboard/developers/id-plus-keys) to authenticate API requests.
- Get a valid [SDK key from Admin Dashboard](https://developer.socure.com/docs/admin-dashboard/developers/sdk-keys) to initialize and authenticate the DocV Android SDK.
- Add your IP address to the [allowlist in Admin Dashboard](https://developer.socure.com/docs/admin-dashboard/developers/allowlist).
- Check that your development environment meets the following requirements:
  - Android SDK Version 23 (OS Version 6) and later

### Android SDK integration notes

The DocV SDK is compiled with the following:
- `compileSdkVersion: 36`
- `Java: 17`

#### Upgrading to Android DocV SDK v5.7.0 or later: Datadog telemetry

Beginning with Android DocV SDK v5.7.0, the SDK reports diagnostic telemetry to Socure through Datadog. This helps Socure investigate capture and upload issues on your integration faster. Telemetry is on by default and requires no code changes or new permissions. There is no API or configuration option to opt out of telemetry; see [When it is off](#how-it-runs-alongside-your-app) for the only case in which it is not sent.

> **Note:** If your app does not use Datadog, no action is required. If it does, read [If your app already uses Datadog](#if-your-app-already-uses-datadog) before upgrading.

##### What is reported

- **Capture-flow events:** the same steps the SDK already reports to Socure's analytics, such as screens shown, captures, retakes, uploads, and step timings.
- **Errors and failures:** camera, upload, and API failures, and API call durations.
- **SDK crashes and ANRs** that happen while the capture flow is on screen and involve SDK code.

Sensitive identifiers (tokens, reference IDs, public keys, device IDs) are replaced with `[REDACTED]` before anything leaves the device. Telemetry consists of events, errors, and timings only.

##### How it runs alongside your app

- **Isolated instance:** The SDK runs its own named Datadog instance (`socure-docv-android`). It never uses or reconfigures your app's default Datadog instance, so your Datadog data, configuration, and credentials are unaffected. Socure's logs do not reach your RUM sessions.
- **No process-wide crash reporting:** Datadog's own crash reporting is disabled on the SDK's instance, and RUM is not included, so it does not compete with your RUM crash reporting. While the capture flow is open, the SDK briefly watches for crashes in its own code. Every crash is still passed on to your app's existing crash handler, and your handler is restored when the flow ends.
- **Lazy start:** The SDK's Datadog instance starts only when the capture flow starts.
- **Region:** SocureGov sessions are sent to Datadog's FedRAMP site (US1-FED). All other sessions are sent to US1.
- **When it is off:** Telemetry is not sent when you route the SDK through your own DocV proxy URL (`docvBaseUrl` in `SocureDocVContext`). This is the only case in which telemetry is disabled; the SDK provides no separate opt-out.

##### Dependencies added to your app

The SDK declares the following Datadog artifacts at `runtime` scope with soft versions, so they resolve like any normal dependency. Datadog also pulls in `dd-sdk-android-internal` 3.13.1. The SDK uses Datadog Logs only; it does not declare RUM, tracing, or OpenTelemetry artifacts.

| Artifact | Version |
|---|---|
| `com.datadoghq:dd-sdk-android-core` | 3.13.1 |
| `com.datadoghq:dd-sdk-android-logs` | 3.13.1 |

Datadog is **not** shaded or relocated. Your app and the DocV SDK share one `com.datadoghq` classpath, and exactly one version of it is compiled into your APK.

##### If your app already uses Datadog

| Requirement | Detail |
|---|---|
| Datadog version the SDK embeds | `dd-sdk-android` **3.13.1** |
| Supported `dd-sdk-android` range for your app | **3.6.0 – 3.13.1**, with all `com.datadoghq:*` artifacts on the same version |
| Required | Every `com.datadoghq:*` artifact in your build must resolve to the **same** version |

Datadog publishes no BOM and removes cross-artifact APIs in minor releases, so nothing aligns its artifacts for you. Gradle picks the newest version by default, so adding the DocV SDK can raise your Datadog artifacts to 3.13.1, including artifacts you pinned lower on purpose. An artifact the SDK does not declare, such as `dd-sdk-android-rum`, stays where you put it. That split is the most common cause of build failures:

```groovy
// DON'T: declares one artifact, leaves the rest to chance
dependencies {
    implementation 'com.socure.android:docv-capture:5.7.0'
    implementation 'com.datadoghq:dd-sdk-android-rum:3.5.0'
}
```

Here `rum` stays at 3.5.0 while `core` resolves up to 3.13.1, and the build fails in your own RUM code:

```
ERROR: R8: Missing class com.datadog.android.core.internal.net.HttpSpec$ContentType
```

To keep all artifacts aligned, add the following `constraints` block to your app module's `build.gradle`:

```groovy
dependencies {
    constraints {
        // Must be >= 3.6.0. Use 3.13.1 to match the DocV SDK unless you have a reason not to.
        def ddVersion = '3.13.1'
        ['dd-sdk-android-core', 'dd-sdk-android-internal', 'dd-sdk-android-logs',
         'dd-sdk-android-rum', 'dd-sdk-android-trace', 'dd-sdk-android-trace-api',
         'dd-sdk-android-trace-internal', 'dd-sdk-android-trace-otel'].each {
            implementation("com.datadoghq:$it") { version { strictly(ddVersion) } }
        }
    }
}
```

Kotlin DSL:

```kotlin
dependencies {
    constraints {
        val ddVersion = "3.13.1"   // must be >= 3.6.0
        listOf("dd-sdk-android-core", "dd-sdk-android-internal", "dd-sdk-android-logs",
               "dd-sdk-android-rum", "dd-sdk-android-trace", "dd-sdk-android-trace-api",
               "dd-sdk-android-trace-internal", "dd-sdk-android-trace-otel").forEach {
            implementation("com.datadoghq:$it") { version { strictly(ddVersion) } }
        }
    }
}
```

All eight artifacts are listed on purpose. A constraint on an artifact your build does not use is harmless, while constraining only the artifacts you can see is what leaves a split set behind.

Confirm that every Datadog artifact resolves to the same version (`->` arrows are expected; differing final versions are not):

```bash
./gradlew :app:dependencies --configuration releaseRuntimeClasspath | grep datadoghq
```

| Where you are | What to do |
|---|---|
| Not using Datadog | Nothing. |
| On 3.13.1 | Nothing. |
| On 3.6.0 – 3.12.x, all artifacts aligned | Nothing required; Gradle raises you to 3.13.1. To stay on your version, use the `constraints` block with your version and keep all eight artifacts aligned. |
| Below 3.6.0 | Move to 3.6.0 or later. Your app keeps working, but **the SDK's telemetry silently stops** (no crash, no error, nothing in logcat). |
| On 2.x | Migrate to 3.6.0 or later before integrating, or pin the whole set at 3.6.0 or later. A split set across 2.x and 3.x is not supported. |
| Declaring only `dd-sdk-android-rum` | Use the `constraints` block. |
| Using `failOnVersionConflict()` | Add the `constraints` block, or exempt `com.datadoghq`. |
| Pinning only `dd-sdk-android-core` lower, leaving the SDK's `dd-sdk-android-logs` at 3.13.1 | This is a mixed set, not the aligned setup the 3.6.0 – 3.13.1 range covers. Align everything with the `constraints` block (recommended). If you must pin `-core` alone, keep it at **3.11.0 or later**; below that, expect runtime errors on Datadog's log write path, even though 3.6.0 – 3.10.x is fine when every artifact is aligned. |

##### Verifying your integration

A successful build does not prove that telemetry works: every version mismatch that disables the SDK's telemetry builds cleanly. After integrating, check that telemetry actually arrives:

1. **Check alignment:** run the `dependencies` command above and confirm all Datadog artifacts resolve to one version.
2. **Check your own integration still reports:** your logs, traces, and RUM views should appear in your Datadog org as before.
3. **Check the SDK's instance initialized:** in a debug build, enable Datadog verbosity with `Datadog.setVerbosity(Log.VERBOSE)` and complete one verification through the capture flow. You should see batch upload lines prefixed with `[socure-docv-android]`:

   ```
   [socure-docv-android]: Batch abc123… [13636 bytes] (Logs Request) sent successfully.
   ```

   Your own instance's lines are prefixed `[_dd.sdk_core.default]`. Because the SDK's instance starts lazily, `[socure-docv-android]` lines appear only after the capture flow starts.

##### Troubleshooting

| Symptom | Likely cause |
|---|---|
| R8: `Missing class com.datadog.android.core.internal.net.HttpSpec$ContentType` | Your `rum` artifact is below `core`. Align all eight artifacts. |
| Errors in your own Datadog 2.x code after integrating the SDK | The SDK raised `core`, `internal`, and `logs` to 3.x while your other artifacts stayed on 2.x. Migrate to 3.6.0 or later and align all eight artifacts. |
| Build fails on a Datadog version conflict | `failOnVersionConflict()` combined with the SDK's soft 3.13.1. Add the `constraints` block. |
| Your Datadog data stopped after integrating the SDK | Unaligned `com.datadoghq` artifacts. Check alignment first. |
| `[socure-docv-android]` batches never appear | The capture flow never started, or Datadog resolved below 3.6.0. |
| `Requested to forward error log to RUM, but RUM feature is not registered.` in logcat | Expected and harmless. It does not affect your RUM data. |

Versions above 3.13.1 are untested. When opening a support case about telemetry or Datadog, include the output of the `dependencies` command above, your DocV SDK version, which Datadog features you enable (Logs, Traces, RUM), and whether you use `failOnVersionConflict()`, `resolutionStrategy.force`, or `constraints` on `com.datadoghq`.

#### Upgrading to Android DocV SDK v5.4.3 or later

The Socure SDK uses the TensorFlow Lite 1.4.1 runtime since version 5.4.3 in order to support:
- arm64-v8a
- armeabi-v7a
    
If you would like to continue using the newer TensorFlow 2.1.1 with SDK 5.4.3, this will require a custom Gradle configuration on your side:
```gradle
configurations.configureEach {
    resolutionStrategy {
        force 'com.google.ai.edge.litert:litert:2.1.1'
    }
}
```

#### Upgrading to Android DocV SDK v5.4.0 or later

This version is required to use **Kotlin Version 2.1.0 or later**. This is required to support the latest language features and library dependencies utilized in this version.
When upgrading to the **Android DocV v5.4.0 SDK or later**, you must also update the **Device SDK** to at least **v4.8.0**.

The Socure SDK uses the TensorFlow Lite runtime since version 5.4.0:

```gradle
implementation 'com.google.ai.edge.litert:litert:2.1.0'
```

This version supports only the following CPU architectures:
- arm64-v8a
- x86_64

As a result, the library does not include native .so binaries for the following architectures:
- armeabi-v7a
- x86

##### Option 1 — Support only arm64-v8a

If your application targets only 64-bit ARM devices, you should exclude unsupported architectures from your APK by configuring ABI filters in your app’s `build.gradle` file:

```gradle
android {
    defaultConfig {
        ndk {
            abiFilters 'arm64-v8a'
        }
    }
}
```

This ensures that unsupported architectures are not packaged into the application.


##### Option 2 — Support both armeabi-v7a and arm64-v8a

If your application must support both 32-bit and 64-bit ARM devices, you can force the use of an earlier TensorFlow LiteRT version that still includes support for armeabi-v7a.

Add the following configuration to your app-level `build.gradle`:

```gradle
configurations.configureEach {
    resolutionStrategy {
        force 'com.google.ai.edge.litert:litert:1.4.1'
    }
}
```

This forces Gradle to resolve the TensorFlow LiteRT dependency to version 1.4.1, which includes native libraries for both:
- armeabi-v7a
- arm64-v8a



#### Using the Device SDK independently

If your application integrates the Device SDK **outside of DocV** (for example, for standalone device capture or validation), you must install the Device SDK **alongside DocV** to avoid runtime errors.

#### 16KB page support

Beginning with Android DocV SDK v5.1.1, support for **16KB memory pages** is included.

> **Note:** This feature requires **Android Gradle Plugin (AGP) v8.5.1 or higher**.  
> AGP 8.5.1 also requires **Gradle v8.7 or higher**.

## Step 1: Generate a transaction token and configure the Capture App

To initiate the verification process, generate a transaction token (`docvTransactionToken`) by calling the Document Request endpoint v5. We strongly recommend that customers generate this token via a server-to-server API call and then pass it to the DocV SDK to ensure the security of their API key and any data they send to Socure. 

### Call the Document Request endpoint

1. From your backend, make a `POST` request to the [`/documents/request`](https://developer.socure.com/reference#tag/Predictive-Document-Verification/operation/DocumentRequestV5) endpoint specifying the following information in the `config` object:

| Parameter   | Required | Description                                                                                                                                                                                                                                                                                                                                                     |
|------------------|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `language`       | Optional     | Determines the language package for the UI text on the Capture App. If you send `auto` as the language parameter, the Capture App uses the device's default language as the language to display the Capture App.  <br/><br/> **Note**: Socure can quickly add support for new language requirements. For more information, contact [support@socure.com](mailto:support@socure.com). |
| `useCaseKey`     | Optional     | Deploys a customized Capture App flow on a per-transaction basis. Replace the `customer_use_case_key` value with the name of the flow you created in [Admin Dashboard](https://developer.socure.com/docs/sdks/docv/capture-app/customize-capture-app). <br/><br/> - If this field is empty, the Capture App will use the flow marked as **Default** in Admin Dashboard. <br/> - If the value provided is incorrect, the SDK will return an `Invalid Request` error. |

>Note: We recommend including as much consumer PII in the body of the request as possible to return the most accurate results. 

```bash
curl --location 'https://service.socure.com/api/5.0/documents/request' \
--header 'Content-Type: application/json' \
--header 'Authorization: SocureApiKey a182150a-363a-4f4a-xxxx-xxxxxxxxxxxx' \
--data '{
  "config": {
    "useCaseKey": "customer_use_case_key", 
    ...
  },
  "firstName": "Dwayne",
  "surName": "Denver",
  "dob": "1975-04-02",
  "mobileNumber": "+13475550100",
  "physicalAddress": "200 Key Square St",
  "physicalAddress2": null,
  "city": "Brownsville",
  "state": "TN",
  "zip": "38012",
  "country": "US"
}'
```

2. When you receive the API response, collect the `docvTransactionToken`. This value is required to initialize the DocV Android SDK and fetch the DocV results. 

```json
{
  "referenceId": "123ab45d-2e34-46f3-8d17-6f540ae90303",
    "data": {
      "eventId": "zoYgIxEZUbXBoocYAnbb5DrT",
      "customerUserId": "121212",
      "docvTransactionToken" : "78d1c86d-03a3-4e11-b837-71a31cb44142", 
      "qrCode": "data:image/png;base64,iVBO......K5CYII=",
      "url": "https://verify-v2.socure.com/#/t/c5e71062-26d5-478c-8441-b434fcc565d0"
    }
}
```


## Step 2: Add the DocV Android SDK 

To add the DocV SDK to your application, include the Socure DocV SDK Maven repository in your `build.gradle` file at the end of the `allprojects > repositories` section:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://sdk.socure.com' }
    }
}
```

In your module level `build.gradle` file, add the following Socure DocV SDK dependency. The example uses the latest DocV Android SDK version, `5.7.0`; replace it if you need a different version:

```gradle
dependencies {
    implementation 'com.socure.android:docv-capture:5.7.0'
}
```

### Camera and file permissions

The DocV SDK requires camera and file permission to capture identity documents. Upon the first invocation of the SDK, your app will request camera and file permission from the user.

>Note: We recommend you check for camera and file permissions before calling the Socure DocV SDK’s launch API.

Ensure that your app manifest has been set up properly to request the following required permissions:

```xml
<uses-feature android:name="android.hardware.camera" />

<!-- Declare permissions -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Initialize and launch the SDK

To enable the DocV SDK functionality, add the following code to your app: 

```kotlin
class Activity : AppCompatActivity() {

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        ...

        startForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            result.data?.let { data ->
                SocureSdk.getResult(data) { sdkResult ->
                    Log.d(TAG, "onResult called: $sdkResult")

                    if (sdkResult is SocureDocVSuccess) {
                        Log.d(
                            TAG,
                            "success: ${sdkResult.deviceSessionToken}"
                        )
                    } else {
                        val error = sdkResult as? SocureDocVFailure
                        Log.d(
                            TAG,
                            "error: ${error?.deviceSessionToken}, " +
                            "${error?.error}"
                        )
                    }
                }
            }
        }

        // Launch the SDK using the intent
        startForResult.launch(
            SocureSdk.getIntent(
                context,
                SocureDocVContext(
                    docvTransactionToken,
                    SDKKey,
                    useSocureGov
                )
            )
        )
    }
}
```

#### `startForResult.launch` function parameters

The following table lists the arguments passed to the `startForResult.launch` function through `SocureSdk.getIntent`: 

| Parameter                | Type    | Description                                                                                                                                                                                                                                            |
|--------------------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `docvTransactionToken`    | String  | The transaction token retrieved from the API response of the [`/documents/request`](https://developer.socure.com/reference#tag/Predictive-Document-Verification/operation/DocumentRequestV5) endpoint. Required to initiate the document verification session. |
| `SDKKey`                 | String  | The unique SDK key obtained from [Admin Dashboard](https://developer.socure.com/docs/admin-dashboard/developers/sdk-keys) used to authenticate the SDK.                                                                                                      |
| `useSocureGov`            | Boolean | A Boolean flag indicating whether to use the GovCloud environment. It defaults to `false`. This is only applicable for customers provisioned in the SocureGov environment.                                                                             |


### Handle the response

Your app can receive response callbacks from the `startForResult` function when the flow either completes successfully or returns with an error. The SDK represents these outcomes with the `SocureDocVSuccess` and `SocureDocVFailure` classes.

#### Success

If the consumer successfully completes the verification flow and the captured images are uploaded to Socure's servers, the SDK returns a `SocureDocVSuccess` result. This result contains a `deviceSessionToken`, a unique identifier for the session that can be used for accessing device details about the specific session.

```kotlin
if (result is SocureDocVSuccess) {
    Log.d(TAG, "success: ${result.deviceSessionToken}")
}
```

#### Failure

If the consumer exits the flow without completing it or an error occurs, the SDK returns a `SocureDocVFailure` result. This result contains both the `deviceSessionToken` and `SocureDocVError`, which provides specific details about the reason for failure.  

```kotlin
val error = result as? SocureDocVFailure
Log.d(TAG, "error: ${error?.deviceSessionToken}, ${error?.error}")
```

When the SDK returns a failure, it provides a `SocureDocVError` enum with specific error cases relevant to the Capture App flow:

```kotlin
enum class SocureDocVError {
    SESSION_INITIATION_FAILURE,
    SESSION_EXPIRED,
    INVALID_PUBLIC_KEY,
    INVALID_DOCV_TRANSACTION_TOKEN,
    DOCUMENT_UPLOAD_FAILURE,
    CONSENT_DECLINED,
    GUARDIAN_CONSENT_DECLINED,
    CAMERA_PERMISSION_DECLINED,
    USER_CANCELED, 
    NO_INTERNET_CONNECTION, 
    UNKNOWN
}
```

The following table lists the error values that can be returned by the `SocureDocVError` enum:

| Enum Case                      | Error Description                                           |
|---------------------------------|-------------------------------------------------------------|
| `SESSION_INITIATION_FAILURE`    | Failed to initiate the session                              |
| `SESSION_EXPIRED`               | Session expired                                             |
| `INVALID_PUBLIC_KEY`            | Invalid or missing SDK key                           |
| `INVALID_DOCV_TRANSACTION_TOKEN`| Invalid transaction token                                   |
| `DOCUMENT_UPLOAD_FAILURE`       | Failed to upload the documents                              |
| `CONSENT_DECLINED`              | Consent declined by the user                                            |
| `GUARDIAN_CONSENT_DECLINED`     | Consent declined by the guardian                               |
| `CAMERA_PERMISSION_DECLINED`    | Permissions to open the camera declined by the user      |
| `USER_CANCELED`                 | Scan canceled by the user                                   |
| `NO_INTERNET_CONNECTION`        | No internet connection                                      |
| `UNKNOWN`                       | Unknown error                                               |


## Step 3: Fetch the verification results 

When the consumer successfully completes the document capture and upload process, call the ID+ endpoint to fetch the results. See the [API Reference documentation](https://developer.socure.com/reference#tag/ID+/operation/ID+) on DevHub for more information. 
