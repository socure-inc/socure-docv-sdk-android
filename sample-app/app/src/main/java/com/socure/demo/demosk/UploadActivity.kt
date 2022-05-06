package com.socure.demo.demosk

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.socure.demo.demosk.databinding.UploadActivityBinding
import com.socure.idplus.SDKAppDataPublic
import com.socure.idplus.error.SocureSdkError
import com.socure.idplus.interfaces.Interfaces.UploadCallback
import com.socure.idplus.model.ScanResult
import com.socure.idplus.model.UploadResult
import com.socure.idplus.upload.ImageUploader
import com.socure.idplus.util.Constants
import com.socure.idplus.util.ImageUtil.toBitmap
import com.socure.idplus.util.mergeBooleanWithAnd
import kotlinx.android.synthetic.main.activity_main.backImage
import kotlinx.android.synthetic.main.activity_main.frontImage
import kotlinx.android.synthetic.main.activity_main.selfieImage
import kotlinx.android.synthetic.main.upload_activity.*

class UploadActivity : AppCompatActivity(), UploadCallback {

    private lateinit var binding: UploadActivityBinding

    private var imageUploader: ImageUploader? = null

    private var uploadSuccessLiveData: MutableLiveData<Boolean> = MutableLiveData()

    var socurePublicKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UploadActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            socurePublicKey = intent?.getSerializableExtra(resources.getString(R.string.socurePublicKey)) as String
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }

        SDKAppDataPublic.successfulScanningResult?.barcodeData?.postalCode =
            SDKAppDataPublic.successfulScanningResult?.barcodeData?.postalCode?.take(5)

        SDKAppDataPublic.successfulScanningResult?.licenseFrontImage?.toBitmap()
            ?.let { frontImage.setImageBitmap(it) }
            ?: SDKAppDataPublic.successfulScanningResult?.passportImage?.toBitmap()?.let {
                frontImage.setImageBitmap(it)
            }

        SDKAppDataPublic.successfulScanningResult?.licenseBackImage?.toBitmap()
            ?.let { backImage.setImageBitmap(it) }

        SDKAppDataPublic.selfieScanResult?.imageData?.toBitmap()
            ?.let { selfieImage.setImageBitmap(it) }

        close.setOnClickListener {
            onBackPressed()
        }

        pressToUpload.setOnClickListener {
            simpleProgressBar.visibility = View.VISIBLE
            val uploadUrl = if (BuildConfig.DEBUG) resources.getString(R.string.rel_upload_url) else resources.getString(R.string.rel_upload_url)
            imageUploader = ImageUploader(this.baseContext, socurePublicKey, uploadUrl)
            imageUploader?.imageUploader(this.baseContext)

            uploadDocuments()
        }

        pressToUploadDoc.setOnClickListener {
            simpleProgressBar.visibility = View.VISIBLE
            val uploadUrl =
                if (BuildConfig.DEBUG) resources.getString(R.string.rel_upload_url) else resources.getString(
                    R.string.rel_upload_url
                )
            imageUploader = ImageUploader(this.baseContext, socurePublicKey, uploadUrl)
            imageUploader?.imageUploader(this.baseContext)

            uploadDocumentsAlone()
        }

        pressToUploadSelfie.isEnabled = false
        pressToUploadSelfie.isClickable = false

        uploadSuccessLiveData.postValue(false)

        mergeBooleanWithAnd(uploadSuccessLiveData).observe(this, androidx.lifecycle.Observer
        {
            if (it == true) {
                runOnUiThread {
                    val responseText = Gson().toJson(SDKAppDataPublic.uploadResult)
                    uploadResponseState.text = "Upload Response State: Success"
                    uploadResponseValue.text = "Upload Response Value: $responseText"
                    simpleProgressBar.visibility = View.GONE
                    Log.d(TAG, "Success response: $responseText")

                    enableSelfieUpload()
                }
            }
        })
    }

    private fun enableSelfieUpload() {
        pressToUploadSelfie.isEnabled = true
        pressToUploadSelfie.isClickable = true
        pressToUploadSelfie.setOnClickListener {
            uploadSelfie()
        }
    }

    private fun uploadSelfie() {
        simpleProgressBar.visibility = View.VISIBLE
        imageUploader?.uploadSelfie(
            object : UploadCallback {
                override fun onDocumentUploadError(error: SocureSdkError?) {
                    Log.e(TAG, "onDocumentUploadError: ${error?.toJSON()}")
                    var cancelCause = "onDocumentUploadError"
                    error?.let { cancelCause = it.toJSON() }
                    Toast.makeText(this@UploadActivity, cancelCause, Toast.LENGTH_SHORT)
                        .show()
                    uploadSuccessLiveData.postValue(false)
                    showError("Upload Error")
                }

                override fun onSocurePublicKeyError(error: SocureSdkError?) {
                    Toast.makeText(
                        this@UploadActivity,
                        "onDocumentUploadError: " + error.toString(),
                        Toast.LENGTH_SHORT
                    ).show();
                    Log.e(TAG, "onSocurePublicKeyError: ${error?.toJSON()}")
                    uploadSuccessLiveData.postValue(false)
                    showError("Socure Public Key Error")
                }

                override fun documentUploadFinished(uploadResult: UploadResult?) {
                    val responseText = Gson().toJson(SDKAppDataPublic.uploadResult)
                    uploadResponseState.text = "Selfie Upload Response State: Success"
                    uploadResponseValue.text = "Selfie Upload Response Value: $responseText"
                    simpleProgressBar.visibility = View.GONE
                    Log.d(TAG, "Success response: $responseText")
                    Toast.makeText(
                        this@UploadActivity,
                        "Selfie uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            SDKAppDataPublic.uploadResult?.uuid,
            SDKAppDataPublic.selfieScanResult?.imageData,
            getDocType()
        )
    }

    private fun getDocType(): String {
        return when (SDKAppDataPublic.successfulScanningResult?.documentType) {
            ScanResult.DocumentType.PASSPORT -> {
                Constants.DOCUMENTTYPE_PASSPORT
            }
            else -> {
                Constants.DOCUMENTTYPE_LICENSE
            }
        }
    }

    private fun uploadDocuments() {
        if (SDKAppDataPublic.successfulScanningResult?.passportImage==null && SDKAppDataPublic.successfulScanningResult?.licenseFrontImage == null) {
            Toast.makeText(this, "Front cannot be null", Toast.LENGTH_LONG).show()
        } else
            if (SDKAppDataPublic.successfulScanningResult?.documentType == ScanResult.DocumentType.PASSPORT) {
                if (SDKAppDataPublic.selfieScanResult?.imageData != null) {
                    Log.i(TAG, "uploadPassport called with selfie")

                    imageUploader?.uploadPassport(
                        this,
                        SDKAppDataPublic.successfulScanningResult?.passportImage,
                        SDKAppDataPublic.selfieScanResult?.imageData
                    )
                } else {
                    Log.i(TAG, "uploadPassport without selfie")

                    imageUploader?.uploadPassport(
                        this,
                        SDKAppDataPublic.successfulScanningResult?.passportImage
                    )
                }
            } else {
                if (SDKAppDataPublic.selfieScanResult?.imageData != null) {
                    Log.i(TAG, "uploadLicense with selfie")

                    imageUploader?.uploadLicense(
                        this,
                        SDKAppDataPublic.successfulScanningResult?.licenseFrontImage,
                        SDKAppDataPublic.successfulScanningResult?.licenseBackImage,
                        SDKAppDataPublic.selfieScanResult?.imageData
                    )
                } else {
                    Log.i(TAG, "uploadLicense without selfie")

                    imageUploader?.uploadLicense(
                        this,
                        SDKAppDataPublic.successfulScanningResult?.licenseFrontImage,
                        SDKAppDataPublic.successfulScanningResult?.licenseBackImage
                    )
                }
            }
    }


    private fun uploadDocumentsAlone() {
        if (SDKAppDataPublic.successfulScanningResult?.passportImage == null && SDKAppDataPublic.successfulScanningResult?.licenseFrontImage == null) {
            Toast.makeText(this, "Front cannot be null", Toast.LENGTH_LONG).show()
        } else
            if (SDKAppDataPublic.successfulScanningResult?.documentType == ScanResult.DocumentType.PASSPORT) {
                imageUploader?.uploadPassport(
                    this,
                    SDKAppDataPublic.successfulScanningResult?.passportImage
                )
            } else {
                imageUploader?.uploadLicense(
                    this,
                    SDKAppDataPublic.successfulScanningResult?.licenseFrontImage,
                    SDKAppDataPublic.successfulScanningResult?.licenseBackImage
                )
            }
    }

    companion object {
        private val TAG = UploadActivity::class.java.simpleName
    }

    override fun documentUploadFinished(uploadResult: UploadResult?) {
        Log.i(TAG, "documentUploadFinished")
        SDKAppDataPublic.uploadResult = uploadResult
        uploadSuccessLiveData.postValue(true)
    }

    override fun onDocumentUploadError(error: SocureSdkError?) {
        Log.e(TAG, "onDocumentUploadError: ${error?.toJSON()}")
        var cancelCause = "onDocumentUploadError"
        error?.let {
            cancelCause = it.toJSON()
        }
        Toast.makeText(this,cancelCause,Toast.LENGTH_SHORT).show()

        uploadSuccessLiveData.postValue(false)
        showError("Upload Error")

    }

    override fun onSocurePublicKeyError(error: SocureSdkError?) {
        Toast.makeText(this,"onDocumentUploadError: " + error.toString(),Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onSocurePublicKeyError: ${error?.toJSON()}")
        uploadSuccessLiveData.postValue(false)
        showError("Socure Public Key Error")
    }

    private fun showError(msg: String) {
        simpleProgressBar.visibility = View.GONE
        uploadResponseState.text = "Upload Response State: Error"
        uploadResponseValue.text = "Upload Response Value: $msg"
    }
}