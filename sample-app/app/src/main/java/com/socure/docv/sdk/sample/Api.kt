package com.socure.docv.sdk.sample

import com.socure.docv.capturesdk.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

private const val BASE_URL = "https://service.socure.com/"

fun transaction(): TransactionService {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpBuilder().getBuilder())
        .build().create(TransactionService::class.java)
}

interface TransactionService {
    @POST("api/5.0/documents/request")
    suspend fun createTransaction(
        @HeaderMap headers: Map<String, String>,
        @Body request: TransactionRequest
    ): TransactionResponse
}

data class TransactionResponse(
    val data: Data
) {

    data class Data(
        val eventId: String,
        val authToken: String,
        val docvTransactionToken: String,
        val qrcode: String,
        val url: String
    )
}

data class TransactionRequest(
    val config: TransactionConfig,
    val previousReferenceId: String? = null
) {

    data class TransactionConfig(
        val useCaseKey: String,
        val language: String = "en"
    )
}

class OkHttpBuilder {
    fun getBuilder(): OkHttpClient {
        with(OkHttpClient.Builder()) {
            readTimeout(120L, TimeUnit.SECONDS)
            connectTimeout(120L, TimeUnit.SECONDS)
            writeTimeout(120L, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
                addInterceptor(loggingInterceptor)
            }
            return build()
        }
    }
}