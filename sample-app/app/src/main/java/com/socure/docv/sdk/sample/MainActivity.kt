package com.socure.docv.sdk.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.socure.docv.capturesdk.api.SocureDocVContext
import com.socure.docv.capturesdk.api.SocureDocVError
import com.socure.docv.capturesdk.api.SocureSdk
import com.socure.docv.capturesdk.common.utils.ResultListener
import com.socure.docv.capturesdk.common.utils.SocureDocVFailure
import com.socure.docv.capturesdk.common.utils.SocureDocVSuccess
import com.socure.docv.capturesdk.common.utils.SocureResult
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

private const val TAG = "MainActivity"
private const val ID_PLUS_KEY = "YOUR_ID_PLUS_KEY"
private const val PUBLIC_KEY = "YOUR_PUBLIC_KEY"

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
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                transaction()
                    .createTransaction(
                        createHeaderMap(),
                        TransactionRequest(
                            config = TransactionRequest.TransactionConfig(
                                useCaseKey = "socure_default"
                            ),
                        )
                    )
            }.onSuccess {
                startForResult.launch(
                    SocureSdk.getIntent(
                        this@MainActivity,
                        SocureDocVContext(
                            it.data.docvTransactionToken,
                            PUBLIC_KEY,
                            false
                        )
                    )
                )
            }.onFailure {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to get transaction token",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    // handle response from Socure SDK
    private var startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            result.data?.let {
                SocureSdk.getResult(it) { result ->
                    Log.d(TAG, "onResult called: $result")
                    if (result is SocureDocVSuccess) {
                        Toast.makeText(
                            this@MainActivity,
                            "onSuccess called: ${result.sessionToken}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val error = result as? SocureDocVFailure
                        Toast.makeText(
                            this@MainActivity,
                            "onError called: ${error?.sessionToken}, ${error?.error}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    private fun createHeaderMap(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "SocureApiKey".plus(" ")
            .plus(ID_PLUS_KEY)
        headerMap["content-type"] = "application/json"
        return headerMap
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
