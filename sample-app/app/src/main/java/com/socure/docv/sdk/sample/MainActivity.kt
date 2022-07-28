package com.socure.docv.sdk.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.socure.docv.capturesdk.api.SocureDocVHelper
import com.socure.docv.capturesdk.common.utils.ResultListener
import com.socure.docv.capturesdk.common.utils.ScanError
import com.socure.docv.capturesdk.common.utils.ScannedData

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.actionStartSocureDocvSDK).setOnClickListener {
            //optional camera permission check before launching Socure SDK
            if (isCameraPermissionGranted()) {
                launchSocureSdk()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // initiate Socure SDK
    private fun launchSocureSdk() {
        startForResult.launch(
            SocureDocVHelper.getIntent(this, "YOUR_SOCURE_API_KEY", null)
        )
    }

    // handle response from Socure SDK
    private var startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            result.data?.let {
                SocureDocVHelper.getResult(it, object : ResultListener {
                    override fun onSuccess(scannedData: ScannedData) {
                        Toast.makeText(
                            this@MainActivity,
                            "Success -> Session ID: ${scannedData.sessionId}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onError(scanError: ScanError) {
                        Toast.makeText(
                            this@MainActivity,
                            "Failure: ${scanError.statusCode} -> ${scanError.errorMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
        }

    // is camera permission granted
    private fun isCameraPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    // handles permission response
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                launchSocureSdk()
            } else {
                Snackbar.make(
                    findViewById(R.id.actionStartSocureDocvSDK),
                    "Require Camera access",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(
                        "Allow"
                    ) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                    }
                    .show()
            }
        }

}