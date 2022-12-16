package com.socure.demo.demosk

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.socure.idplus.SDKAppDataPublic
import com.socure.idplus.model.ScanResult
import com.socure.idplus.scanner.consent.ConsentActivity
import com.socure.idplus.scanner.license.LicenseBackScannerActivity
import com.socure.idplus.scanner.license.LicenseFrontScannerActivity
import com.socure.idplus.scanner.license.LicenseScannerActivity
import com.socure.idplus.scanner.passport.PassportScannerActivity
import com.socure.idplus.scanner.selfie.SelfieActivity
import com.socure.idplus.util.ImageUtil.toBitmap
import com.socure.idplus.util.KEY_ERROR
import com.socure.idplus.util.KEY_SESSION_ID
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MultiplePermissionsListener {

    var allPermissionChecked: Boolean = false

    private val permissions = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Please set the SDK public key by calling this function if it's not set via the strings.xml
        // SDKAppDataPublic.setSocureSdkKey("REPLACE ME WITH YOUR SOCURE PUBLIC KEY")

        btn_show_consent.setOnClickListener {
            Log.d(TAG, "Calling showConsent")
            val intent = Intent(this@MainActivity, ConsentActivity::class.java)
            startActivityForResult(intent, SHOW_CONSENT_CODE)
        }

        scanIDButton.setOnClickListener {
            val passingIntent = Intent(this@MainActivity, LicenseScannerActivity::class.java)
            passingIntent.putExtra(status, 0)
            passingIntent.putExtra(documentTypeTitle, "Driver's License")
            passingIntent.putExtra(autoFilling, true)
            startActivityForResult(passingIntent, SCAN_DRIVER_LICENSE_CODE)
        }

        scanFrontButton.setOnClickListener {
            val passingIntent = Intent(this@MainActivity, LicenseFrontScannerActivity::class.java)
            passingIntent.putExtra(status, 0)
            passingIntent.putExtra(documentTypeTitle, "Driver's License")
            passingIntent.putExtra(autoFilling, true)
            startActivityForResult(passingIntent, SCAN_DRIVER_LICENSE_CODE)
        }

        scanBackButton.setOnClickListener {
            val passingIntent = Intent(this@MainActivity, LicenseBackScannerActivity::class.java)
            passingIntent.putExtra(status, 0)
            passingIntent.putExtra(documentTypeTitle, "Driver's License")
            passingIntent.putExtra(autoFilling, true)
            startActivityForResult(passingIntent, SCAN_DRIVER_LICENSE_CODE)
        }

        scanPassportButton.setOnClickListener {
            val scanPassportIntent = Intent(this@MainActivity, PassportScannerActivity::class.java)
            scanPassportIntent.putExtra(autoFilling, true)
            startActivityForResult(scanPassportIntent, SCAN_PASSPORT_CODE)
        }

        pressToTakeSelfie.setOnClickListener {
            val passingIntent = Intent(this@MainActivity, SelfieActivity::class.java)
            passingIntent.putExtra(status, 0)
            passingIntent.putExtra(autoFilling, true)
            startActivityForResult(passingIntent, SELFIE_CODE)
        }

        pressGoToUpload.setOnClickListener {
            startActivityForResult(Intent(this@MainActivity, UploadActivity::class.java), UPLOAD_ACTIVITY)
        }

        Dexter.withContext(this)
            .withPermissions(permissions)
            .withListener(this)
            .onSameThread()
            .check()
    }

    private fun showMessageOKCancel(okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_request_message))
            .setPositiveButton(R.string.ok, okListener)
            .setNegativeButton("cancel", null)
            .create()
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            Log.d(TAG, "onActivityResult called - requestCode: $requestCode,  resultCode: $resultCode, " +
                    "sessionId: ${data?.getStringExtra(KEY_SESSION_ID)}, any error: ${data?.getStringExtra(KEY_ERROR)}")

            if (requestCode == SHOW_CONSENT_CODE) {
                val errorMessage = data?.getStringExtra(KEY_ERROR)
                val sessionId = data?.getStringExtra(KEY_SESSION_ID)
                Log.d(TAG, "onActivityResult called after showConsent: sessionId: $sessionId, errorMessage: $errorMessage")

                errorMessage?.let {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            if (requestCode == SCAN_PASSPORT_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val scanResult = ScanResult(ScanResult.DocumentType.PASSPORT)
                        val passportImage = SDKAppDataPublic.successfulScanningResult?.passportImage
                        val mrzData = SDKAppDataPublic.successfulScanningResult?.mrzData

                        //ACCESS Returned Data

                        passportImage?.toBitmap()?.let { frontImage.setImageBitmap(it) }
                        backImage.setImageBitmap(null)

                        val passportCaptureMode = SDKAppDataPublic.successfulScanningResult?.captureType?.get(PASSPORT)
                        passportCaptureMode?.let {
                            frontImageText.text = "Front - ${getCaptureType(passportCaptureMode)}"
                        }?: kotlin.run {
                            frontImageText.text = "Front"
                        }

                        Log.d(TAG, "mrz extraction success: ${SDKAppDataPublic.successfulScanningResult?.dataExtracted}")

                        val errorString = data.getStringExtra("error")
                        errorString?.let{
                            Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                }
            }

            if (requestCode == SCAN_DRIVER_LICENSE_FRONT_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        /* val passingIntent = Intent(this@MainActivity, LicenseBackScannerActivity::class.java)
                         passingIntent.putExtra(IntentKeys.status, 0)
                         passingIntent.putExtra(IntentKeys.documentTypeTitle, "Driver's License")
                         passingIntent.putExtra(IntentKeys.autoFilling, true)
                         AppCoreData.getInstance().isAutoFilling = true
                         AppCoreData.getInstance().passportScan = false
                         startActivityForResult(passingIntent, SCAN_DRIVER_LICENSE_BACK_CODE)*/

                        //ACCESS Returned Data
                    }
                } else {
                }
            }

            if (requestCode == SCAN_DRIVER_LICENSE_BACK_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {

                        //Access Returned data
                    }
                } else {
                }
            }

            if (requestCode == SCAN_DRIVER_LICENSE_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {

                        val a = ScanResult(ScanResult.DocumentType.LICENSE)
                        SDKAppDataPublic.successfulScanningResult?.licenseFrontImage?.toBitmap()
                            ?.let { frontImage.setImageBitmap(it) }
                        SDKAppDataPublic.successfulScanningResult?.licenseBackImage?.toBitmap()
                            ?.let { backImage.setImageBitmap(it) }
                        val d = SDKAppDataPublic.successfulScanningResult?.barcodeData

                        val licFrontCaptureType = SDKAppDataPublic.successfulScanningResult?.captureType?.get(LIC_FRONT)
                        licFrontCaptureType?.let {
                            frontImageText.text = "Front - ${getCaptureType(licFrontCaptureType)}"
                        }?: kotlin.run {
                            frontImageText.text = "Front"
                        }

                        val licBackCaptureType = SDKAppDataPublic.successfulScanningResult?.captureType?.get(LIC_BACK)
                        licBackCaptureType?.let {
                                backImageText.text = "Back - ${getCaptureType(licBackCaptureType)}"
                            }?: kotlin.run {
                            backImageText.text = "Back"
                        }

                        Log.d(TAG, "barcode extraction success: ${SDKAppDataPublic.successfulScanningResult?.dataExtracted}")

                        val errorString = data.getStringExtra("error")
                        errorString?.let{
                            Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                }
            }
            val permissions = arrayOf<String>(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE
            )

            if (requestCode == INFO_LICENSE_SCANNED_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    val intent = Intent(this, SelfieActivity::class.java)
                    startActivityForResult(intent, SELFIE_CODE)
                }
            }

            if (requestCode == SELFIE_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    SDKAppDataPublic.userContent = true
                    SDKAppDataPublic.selfieScanResult?.imageData?.toBitmap()
                        ?.let { selfieImage.setImageBitmap(it) }
                    val selfieCaptureType = SDKAppDataPublic.selfieScanResult?.captureType?.get(SELFIE)
                    selfieCaptureType?.let {
                        selfieImageText.text = "Selfie - ${getCaptureType(selfieCaptureType)}"
                    }
                }
            }

            if(resultCode == Activity.RESULT_CANCELED){
                var cancelCause = "Result Cancelled"
                data?.let{
                    cancelCause = it.getStringExtra("error") ?: ""
                }
                Toast.makeText(this,cancelCause,Toast.LENGTH_SHORT).show();
            }
            super.onActivityResult(requestCode, resultCode, data)
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val SHOW_CONSENT_CODE = 100
        private const val SCAN_PASSPORT_CODE = 200
        private const val SCAN_DRIVER_LICENSE_CODE = 300
        private const val SCAN_DRIVER_LICENSE_FRONT_CODE = 310
        private const val SCAN_DRIVER_LICENSE_BACK_CODE = 320
        private const val SELFIE_CODE = 240
        private const val UPLOAD_ACTIVITY = 100
        private const val INFO_LICENSE_SCANNED_CODE = 260
        private const val API_ERROR = 400
        private const val TAG = "SDLT_MA"

        //CaptureType keys
        private const val LIC_FRONT = "lic_front"
        private const val LIC_BACK = "lic_back"
        private const val PASSPORT = "passport"
        private const val SELFIE = "selfie"


        var status = "status"
        var documentTypeTitle = "document_type_title"
        var autoFilling = "auto_filling"
    }

    private fun getCaptureType(captureType: Int): String {
        return when (captureType) {
            0 -> "Auto"
            else -> "Manual"
        }
    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        if (report?.areAllPermissionsGranted() == true) {
            allPermissionChecked = true
        } else {
            allPermissionChecked = false
            setResult(Activity.RESULT_CANCELED)
            //finish()

            showMessageOKCancel(DialogInterface.OnClickListener { _, _ ->
                Dexter.withContext(this)
                    .withPermissions(permissions)
                    .withListener(this)
                    .onSameThread()
                    .check()
            })
        }
    }

    override fun onPermissionRationaleShouldBeShown(
        p0: MutableList<PermissionRequest>?,
        p1: PermissionToken?
    ) {
        p1?.continuePermissionRequest()
    }
}