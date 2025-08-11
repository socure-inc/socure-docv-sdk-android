# Predictive DocV Android SDK v5

Learn how to quickly integrate with the Predictive Document Verification (DocV) Android SDK v5.

## Table of Contents

- [Getting started](#getting-started)
- [Step 1: Generate a transaction token](#step-1-generate-a-transaction-token)
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
  - Android SDK Version 22 (OS Version 5.1) and later

The DocV SDK is compiled with the following:
- `compileSdkVersion: 34`
- `Java: 11`
- Gradle: `7.5` or higher

## Step 1: Generate a transaction token and configure the Capture App

To initiate the verification process, generate a transaction token (`docvTransactionToken`) by calling the Document Request endpoint v5. We strongly recommend that customers generate this token via a server-to-server API call and then pass it to the DocV SDK to ensure the security of their API key and any data they send to Socure. 

### Call the Document Request endpoint

1. From your backend, make a `POST` request to the [`/documents/request`](https://developer.socure.com/reference#tag/Predictive-Document-Verification/operation/DocumentRequestV5) endpoint specifying the following information in the `config` object:

| Parameter   | Required | Description                                                                                                                                                                                                                                                                                                                                                     |
|------------------|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `language`       | Optional     | Determines the language package for the UI text on the Capture App.  <br/><br/> **Note**: Socure can quickly add support for new language requirements. For more information, contact [support@socure.com](mailto:support@socure.com). |
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
  }
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

```
allprojects {
    repositories {
        ...
        maven { url 'https://sdk.socure.com' }
    }
}
```

In your module level `build.gradle` file, add the following Socure DocV SDK dependency and replace `x.y.z` with the DocV Android SDK version number (for example: `5.0.0`):

```
dependencies {
    implementation 'com.socure.android:docv-capture:5.0.0'
}
```

### Camera and file permissions

The DocV SDK requires camera and file permission to capture identity documents. Upon the first invocation of the SDK, your app will request camera and file permission from the user.

>Note: We recommend you check for camera and file permissions before calling the Socure DocV SDK’s launch API.

Ensure that your app manifest has been set up properly to request the following required permissions:

```
<uses-feature android:name="android.hardware.camera" />

<!-- Declare permissions -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
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
| `docVTransactionToken`    | String  | The transaction token retrieved from the API response of the [`/documents/request`](https://developer.socure.com/reference#tag/Predictive-Document-Verification/operation/DocumentRequestV5) endpoint. Required to initiate the document verification session. |
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
| `CAMERA_PERMISSION_DECLINED`    | Permissions to open the camera declined by the user      |
| `USER_CANCELED`                 | Scan canceled by the user                                   |
| `NO_INTERNET_CONNECTION`        | No internet connection                                      |
| `UNKNOWN`                       | Unknown error                                               |


## Step 3: Fetch the verification results 

When the consumer successfully completes the document capture and upload process, call the ID+ endpoint fetch the results. See the [API Reference documentation](https://developer.socure.com/reference#tag/ID+/operation/ID+) on DevHub for more information. 
