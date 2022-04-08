# Android SDK

# Version: 2.0.12 - Release Date : Mar 2022 

Release Notes: https://github.com/socure-inc/socure-docv-sdk-android/releases/tag/2.0.12

# Implementation

The Socure Document Verification SDK (“SDK”) provides a framework to add image capture and upload services to your mobile application. The SDK supports image capture for both Android and iOS. This guide covers only Android.
The SDK includes:

# SDK Minimum Requirements

The Socure Android SDK requires Android `minSdkVersion 22`.

| Feature                           | Minimum Requirements |
| --------------------------------- | -------------------- |
| Document and Selfie Auto Capture       | Android 8 and above     |


# Android Setup

**Step 1: Add the Socure dependency**

Add the following:

1. Add the maven URL in the root/project level `build.gradle` :

```
buildscript {
	..............
	dependencies {
		........................
		classpath 'com.github.dcendents:android-maven-gradle-plugin:1.5'
	}
}

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
2. Add the following to the `build.gradle` of the module/library that is using the framework:
```
//Top of file
apply plugin: 'com.github.dcendents.android-maven'

group="com.github.socure-inc"
````

```
dependencies {
    implementation 'com.github.socure-inc:android-sdk:2.0.12'
}
```



**Step 2 : Update Permissions**

Make the following permission updates for Android in the Android.manifest:

Feature: Capture Document

```
<uses-permission android:name="android.permission.CAMERA"/>
```

Feature: Pass information to the SDK
```
<uses-permission android:name="android.permission.INTERNET"/>
```

Permissions:

```
<uses-permission android:name="android.permission.CAMERA"></uses-permission>
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-feature android:name="android.hardware.camera.flash" android:required="false" />
<uses-feature android:name="android.hardware.camera" />
<uses-permission android:name="android.permission.VIBRATE" />
```

**Step 3: Add API Keys***

Use the dashboard to find the Socure SDK key to be added in the application.

```
<string name="socurePublicKey" translatable="false">YOUR_KEY_HERE</string>
```

Add the API Key in **string.xml** under the resources.

**Step 4: Intent Implementation**

Add the Intent for the invocation class and the SDK class for the document capture.
Assuming that you are calling the capture in the MainActivity class, here are the Activity classes for each of the different functionalities.

- PassportScannerActivity: For scanning the passport type documents.
- LicenseScannerActivity: For scanning the license/card type documents.
- SelfieActivity: For scanning the self-portrait.
- UploadActivity: For uploading the documents.

**Note**: Please note that the request code has to be defined by the customer, we are taking a constant value, but the request code is just for the comparison later in the handling, the customer can define it based on their implementation.

**Scanning the Passport**:<br/>
*Java*
```
private final int SCAN_PASSPORT_CODE = 210
Intent scanPassportIntent = new Intent(this, PassportScannerActivity.class)
startActivityForResult(scanPassportIntent, SCAN_PASSPORT_CODE)
```
*Kotlin*
```
private const val SCAN_PASSPORT_CODE = 210
val scanPassportIntent = Intent(this@DocumentSelectionActivity, PassportScannerActivity::class.java)
startActivityForResult(scanPassportIntent, SCAN_PASSPORT_CODE)
```

**Scanning the License**:<br/>
*Java*
```
private static final int SCAN_DRIVER_LICENSE_CODE = 300;
Intent scanLicenseIntent = new Intent(MainActivity.this, LicenseScannerActivity.class);
startActivityForResult(scanLicenseIntent, SCAN_DRIVER_LICENSE_CODE);

```
*Kotlin*
```
private const val SCAN_DRIVER_LICENSE_CODE = 300
val scanLicenseIntent = Intent(this@DocumentSelectionActivity, LicenseScannerActivity::class.java)
startActivityForResult(scanLicenseIntent, SCAN_DRIVER_LICENSE_CODE)
```

**Capturing the Selfie**:<br/>
*Java*
```
private static final int SCAN_SELFIE_CODE = 400;
Intent selfieScanIntent = new Intent(MainActivity.this, SelfieActivity.class);
startActivityForResult(selfieScanIntent, SCAN_SELFIE_CODE);
```
*Kotlin*
```
private const val SCAN_SELFIE_CODE = 400
val selfieScanIntent = Intent(this@MainActivity, SelfieActivity::class.java)
startActivityForResult(selfieScanIntent, SCAN_SELFIE_CODE)
```

**Document Upload Service*:
<br/>
*Java*
```
//ImageUploader constructor without key
ImageUploader imageUploader = new ImageUploader(this, null);

//ImageUploader constructor with socure public key from resources

ImageUploader imageUploader = new ImageUploader(this, getApplicationContext().getString(R.string.socurePublicKey));

//ImageUploader configuration after constructor

imageUploader.imageUploader(this);

//Initiate the UploadLicense service with selfie
imageUploader.uploadLicense(Interfaces.UploadCallback uploadCallback, byte[] front, byte[] back, byte[] selfie);

//Initiate the UploadLicense service without selfie
imageUploader.uploadLicense(Interfaces.UploadCallback uploadCallback, byte[] front, byte[] back);

//Initiate the UploadPassport service with selfie
imageUploader.uploadPassport(Interfaces.UploadCallback uploadCallback, byte[] front, byte[] selfie);

//Initiate the UploadPassport service without selfie
imageUploader.uploadPassport(Interfaces.UploadCallback uploadCallback, byte[] front);
 ```
*Kotlin*
```
//ImageUploader constructor without key
val imageUploader = ImageUploader(this@Activity);

//ImageUploader constructor with socure public key from resources
imageUploader = ImageUploader(this.baseContext, socurePublicKey)

//ImageUploader configuration after constructor
 imageUploader?.imageUploader(this.baseContext)

//Initiate the UploadLicense service with selfie
imageUploader?.uploadLicense(uploadCallback : Interfaces.UploadCallback, front : ByteArray, back: ByteArray, selfie: ByteArray)

//Initiate the UploadLicense service without selfie
imageUploader?.uploadLicense(uploadCallback : Interfaces.UploadCallback, front : ByteArray, back: ByteArray)

//Initiate the UploadPassport service with selfie
imageUploader?.uploadPassport(uploadCallback : Interfaces.UploadCallback, front : ByteArray, selfie: ByteArray)

//Initiate the UploadPassport service without selfie
imageUploader?.uploadPassport(uploadCallback : Interfaces.UploadCallback, front : ByteArray)
 ```

**Uploader Callback**:
<br/>
*Java*
```
//ImageUploader Callback
Should add the implementation to the activity to handle the callbacks and override the methods

Public class Activity implements Interfaces.UploadCallback

@Override
public void onDocumentUploadError(SocureSdkError error) {
	//returns there is an error with the upload
}

@Override
public void onSocurePublicKeyError(SocureSdkError error) {
	//returns if there is no key to upload
}

@Override
public void documentUploadFinished(UploadResult uploadResult) {
	//success return
}
```
*Kotlin*
```
class Activity : Interfaces.UploadCallback

override fun onDocumentUploadError(error:SocureSdkError) {
	//returns there is an error with the upload
}

override fun onSocurePublicKeyError(error:SocureSdkError) {
	//returns if there is no key to upload
}

override fun documentUploadFinished(uploadResult : UploadResult) {
	//success return
}
```

**Handling the response for the Scans**

The response for the scanner methods have to be implemented in the onActivityResult method using the request code used while starting the activity.<br/>
Java
```
@Override
   protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
   	try {
       	if (requestCode == SCAN_PASSPORT_CODE) {
           	if (resultCode == RESULT_OK) {
              	ScanResult result = SDKAppDataPublic.INSTANCE.getSuccessfulScanningResult();
           	} else {
               	Toast.makeText(this, "Scan Passport error", Toast.LENGTH_LONG).show();
           	}
       	}
```
Kotlin
```
override fun onActivityResult(requestCode : Int, resultCode : Int, intent : Intent = null) {
   	try {
       		if (requestCode == SCAN_PASSPORT_CODE) {
           		if (resultCode == RESULT_OK) {
              			val result = SDKAppDataPublic.INSTANCE.getSuccessfulScanningResult();
           		} else {
               			Toast.makeText(this, "Scan Passport error", Toast.LENGTH_LONG).show();
           		}
       		}
	} catch...
```

**Response for MRZ Data**:

```
SDKAppDataPublic.INSTANCE.getSuccessfulScanningResult().getMrzData()
Kotlin
SDKAppDataPublic.successfulScanningResult?.mrzData
```

Response for Barcode Data:<br/>
Java
```
SDKAppDataPublic.INSTANCE.getSuccessfulScanningResult().getBarcodeData()
```
Kotlin
```
SDKAppDataPublic.successfulScanningResult?.barcodeData
```

**Optional: Ensure User permissions**

Use the code below in Capture Activity class, so you can obtain runtime permissions needed from the user to initialize the scanner.

*Java*<br/>
```
/Defined as final in the activity class where the scan is being invoked to confirm if the user has the required permissions.
final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE};

//Method to check for permissions.
private boolean checkPermission(){
 for(String permission : permissions){
   	if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
  	return false;
   }
   return true;
}
// while capturing an image, we recommend that you check if the user has granted the permissions.
if (checkPermission()) {
imageUploader = ImageUploader.ImageUploaderFactory.create(this);
}else{
// To get the required permissions if not granted
   ActivityCompat.requestPermissions(MainActivity.this, permissions, 101);
}
```
*Kotlin*
```
//Defined as final in the activity class where the scan is being invoked to confirm if the user has the required permissions.
val permissions = arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE)

//Method to check for permissions.
private fun checkPermission() : Boolean {
 permissions.forEach {
	if(checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED){
		return false
	}
	return true
}
// while capturing an image, we recommend that you check if the user has granted the permissions.
if (checkPermission()) {
imageUploader = ImageUploader.ImageUploaderFactory.create(this);
}else{
// To get the required permissions if not granted
   ActivityCompat.requestPermissions(this@MainActivity, permissions, 101);
}
```

**Getting Image Byte Array for Upload or Preview**

You can retrieve the captured image on your side as well to show a document preview or to store the image. The image data is stored in the instance and can be extracted using the following methods.

*Java*<br/>
```
//License Front Image
SDKAppDataPublic.INSTANCE.getSuccessfulScanningResult().getLicenseFrontImage()

//License Back Image
SDKAppDataPublic.INSTANCE.getSuccessfulScanningResult().getLicenseBackImage()

//Passport Image
SDKAppDataPublic.INSTANCE.getSuccessfulScanningResult().getPassportImage()

//Selfie Image
SDKAppDataPublic.INSTANCE.getSuccessfulScanningResult().getSelfieImage()
```
*Kotlin*
```
//License Front Image
SDKAppDataPublic.successfulScanningResult?.licenseFrontImage

//License Back Image
SDKAppDataPublic.successfulScanningResult?.licenseBackImage

//Passport Image
SDKAppDataPublic.successfulScanningResult?.passportImage

//Selfie Image
SDKAppDataPublic.successfulScanningResult?.selfieImage
```

**Capture Type**

The way image has been captured (whether auto or manual) can be known using 'captureType' map in ```SDKAppDataPublic.successfulScanningResult? ``` for kotlin and ```SDKAppDataPublic.INSTANCE.getSuccessfulScanningResult()``` for java.

0 - Document was captured automatically

1 - Document was captured manually

Document name(Key) as License Front - "lic_front", License Back - "lic_back", Passport - "passport", Selfie - "selfie"

**Getting barcode/MRZ data**

To get the barcode/MRZ data in the callback class, you can use the getter methods of the respective object.<br/>
*Java*
```
@Override
public void handleBarcodeData(BarcodeData barcodeData)
{
  barcodeData.getFirstName();
  barcodeData.getSurName();
  barcodeData.getFullName();
  barcodeData.getAddress();
  barcodeData.getCity();
  barcodeData.getState();
  barcodeData.getPostalCode();
  barcodeData.getIssueDate();
  barcodeData.getExpirationDate();
  barcodeData.getDOB();
  barcodeData.getDocumentNumber();
}
@Override
public void handleMRZData(MrzData mrzData)
{
  mrzData.getFirstName();
  mrzData.getSurName();
  mrzData.getFullName();
  mrzData.getExpirationDate();
  mrzData.getDOB();
  mrzData.getDocumentNumber();
  mrzData.getIssuingCountry();
}
```
*Kotlin*
```
override fun handleBarcodeData(barcodeData : BarcodeData)
{
  barcodeData.FirstName
  barcodeData.SurName
  barcodeData.FullName
  barcodeData.Address
  barcodeData.City
  barcodeData.State
  barcodeData.PostalCode
  barcodeData.IssueDate
  barcodeData.ExpirationDate
  barcodeData.DOB
  barcodeData.DocumentNumber
}
override fun handleMRZData(mrzData : MrzData)
{
  mrzData.FirstName
  mrzData.SurName
  mrzData.FullName
  mrzData.ExpirationDate
  mrzData.DOB
  mrzData.DocumentNumber
  mrzData.IssuingCountry
}
```
*ErrorHandling*
```
Error Classes
com.socure.idplus.error.DocumentUploadError 	-> Handles the exception if there is an error during the document upload process
com.socure.idplus.error.InternetConnectionError -> Handles the error for internet connection issues.
com.socure.idplus.error.DocumentScanError 	-> JSON string is returned by result from ActivityForResult (see below)
com.socure.idplus.error.DocumentScanFailedError -> JSON string is returned by result from ActivityForResult (see below) 
com.socure.idplus.error.SelfieScanError 	-> JSON string is returned by result from ActivityForResult (see below)
com.socure.idplus.error.SelfieScanFailedError	-> JSON string is returned by result from ActivityForResult (see below)
com.socure.idplus.error.SocureSdkError		-> The above classes are derived from the SocureSdkError.

```
*Returned Errors From Activities For Result*
*Java*
```
public void onActivityResult(int requestCode, int resultCode, Intent data){
	if (resultCode == Activity.RESULT_OK) {
		//Handle returned data from activity
	} else {
		String err = data.getStringExtra("error");
		Log.i("ERROR",err);

		//err is a json string. such as -> "{\"type\":\"com.socure.idplus.error.DocumentScanError\",\"message\":\"Scan cancelled by user\"}"
		//To extract values you can utilize a JSON parser such as org.json.JSONObject
		JSONObject jObj = new JSONObject(err);
                Log.i("ERROR","THE MESSAGE IS " + jObj.getString("message"));
	}
}
```
*Kotlin*
```
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	if (resultCode == Activity.RESULT_OK) {
		//Handle returned data from activity
	} else {
		val err = data?.getStringExtra("error")
		Log.i("ERROR",err)

		//err is a json string. such as -> "{\"type\":\"com.socure.idplus.error.DocumentScanError\",\"message\":\"Scan cancelled by user\"}"
		//To extract values you can utilize a JSON parser such as org.json.JSONObject
		var jObj = JSONObject(err)
                Log.i("ERROR","THE MESSAGE IS ${jObj.getString("message")}")
	}
}

```
**Notes:** 
Barcode/MRZ extraction status can now be obtained from dataExtracted variable in ```SDKAppDataPublic.successfulScanningResult? ``` for kotlin and ```SDKAppDataPublic.INSTANCE.getSuccessfulScanningResult()``` for java.

# Customizations

Socure offers `styles.xml` , `strings.xml` and `config.json` along with the package that has the option to override any of the texts and color scheme for the SDK components.

| File             | Description                                                                                         |
| ---------------- | ---------------------------------------------------------------------------------------------------- |
| styles.xml    | You can add this file in the project to change the display color and size of the capture components. |
| strings.xml | You can add this file in the project to change the texts for the capture screen components.        |
| config.json     | You can add this file in the project to change the capture properties as defined below.             |


Please note, `config.json` must be included in the assets folder of your project.

## Document Capture Customizations

| Property Name                    | Description                                                        | Allowed Values | Default |
| -------------------------------- | ------------------------------------------------------------------ | -------------- |:-------:|
| show_cropper                     | Displays the capture frame for documents.                          | Boolean        |   true   |
| only_manual_capture              | To disable the auto-capture of documents.                          | Boolean        |   false    |
| manual_timeout                   | Timeout after which the option for manual capture pops.            | Number >1      |   10    |
| document_show_confirmation_screen| Shows document preview from Socure to confirm the captured images. | Boolean        |   true   |
| enable_flash_capture             | Enable the flash image capture.                                    | Boolean        |   false    |
| enable_help                      | Shows a help button at the bottom for the additional help text.    | Boolean        |   true   |
| barcode_check                    | Toggle the barcode check, if disabled, the SDK will stop looking for the barcode at the back of the document after 3 seconds, capturing only using the edge detection.    | Boolean        |   true   |


## Selfie Capture Customizations

| Property Name                  | Description                                                     | Allowed Values | Default |
| ------------------------------ | --------------------------------------------------------------- | -------------- |:-------:|
| selfie_manual_capture          | Disable the auto-capture.                                       | Boolean        |   false    |
| selfie_manual_timeout          | Timeout after which the option for manual capture pops.         | Number >1      |   10    |
| selfie_show_confirmation_screen| Shows selfie preview from Socure to confirm.                    | Boolean        |   true   |
| selfie_enable_help             | Shows a help button at the bottom for the additional help text. | Boolean        |   true   |

## OkHttp3 & Retrofit dependencies

```
def retrofitVersion = '2.9.0'
implementation "com.squareup.retrofit2:retrofit:$retrofitVersion"
implementation "com.squareup.retrofit2:converter-gson:$retrofitVersion"

def okHttpVersion = '4.9.3'
implementation "com.squareup.okhttp3:okhttp:$okHttpVersion"
implementation "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"
```
