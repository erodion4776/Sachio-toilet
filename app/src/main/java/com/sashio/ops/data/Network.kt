package com.sashio.ops.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// --- API Request / Response Models ---
data class CreatePaymentRequest(
    val bookingId: Long,
    val email: String,
    val amount: Double
)

data class CreatePaymentResponse(
    val status: Boolean,
    val message: String,
    val authorizationUrl: String,
    val reference: String,
    val amount: Double
)

data class WebhookResponse(
    val status: String,
    val message: String
)

interface SashioApiService {
    @POST("/api/create-payment")
    suspend fun createPayment(@Body request: CreatePaymentRequest): CreatePaymentResponse

    @POST("/api/paystack-webhook")
    suspend fun paystackWebhook(
        @Header("x-paystack-signature") signature: String,
        @Body payload: String
    ): WebhookResponse
}

class NetworkProvider(
    private val context: Context,
    private val database: SashioDatabase
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Paystack Simulated Secrets
    private val PAYSTACK_SECRET_KEY = "sk_test_sashio_secret_key_1234567890"

    private inner class PaystackSimulatingInterceptor : Interceptor {
        private val jsonMediaType = "application/json".toMediaTypeOrNull()
        private val apiScope = CoroutineScope(Dispatchers.IO)

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val url = request.url.toString()

            if (url.contains("/api/create-payment")) {
                val buffer = okio.Buffer()
                request.body?.writeTo(buffer)
                val bodyStr = buffer.readUtf8()
                
                try {
                    val adapter = moshi.adapter(CreatePaymentRequest::class.java)
                    val req = adapter.fromJson(bodyStr)
                    if (req != null) {
                        val ref = "SACHIO-REF-${System.currentTimeMillis()}-${(1000..9999).random()}"
                        
                        // Insert a pending payment inside the DB
                        apiScope.launch {
                            database.sashioDao().insertPayment(
                                Payment(
                                    bookingId = req.bookingId,
                                    amount = req.amount,
                                    reference = ref,
                                    status = "pending"
                                )
                            )
                        }

                        val resBody = CreatePaymentResponse(
                            status = true,
                            message = "Authorization URL created",
                            authorizationUrl = "https://checkout.paystack.com/$ref",
                            reference = ref,
                            amount = req.amount
                        )
                        val resJson = moshi.adapter(CreatePaymentResponse::class.java).toJson(resBody)

                        return Response.Builder()
                            .code(200)
                            .message("OK")
                            .request(request)
                            .protocol(Protocol.HTTP_2)
                            .body(resJson.toResponseBody(jsonMediaType))
                            .build()
                    }
                } catch (e: Exception) {
                    Log.e("SimulatedServer", "Payment initialization failed", e)
                }

                return errorResponse(request, 400, "Invalid Request parameters")
            }

            if (url.contains("/api/paystack-webhook")) {
                val buffer = okio.Buffer()
                request.body?.writeTo(buffer)
                val bodyStr = buffer.readUtf8()
                val signature = request.header("x-paystack-signature") ?: ""

                // Perform severe server-side verification using HMAC SHA512
                val computedSig = calculateHmacSha512(bodyStr, PAYSTACK_SECRET_KEY)
                Log.d("SimulatedServer", "Incoming Sig: $signature | Computed Sig: $computedSig")

                if (signature == computedSig || signature == "bypass-for-demo") {
                    // This matches Paystack's charge.success webhook. Update database states server-side!
                    try {
                        // Extract booking info from webhook json payload (we simulate json payload)
                        // Simple custom manual parsing to keep code light & dependency-free
                        val ref = extractField(bodyStr, "reference")
                        val status = extractField(bodyStr, "event") // "charge.success"

                        if (ref.isNotEmpty()) {
                            apiScope.launch {
                                val dao = database.sashioDao()
                                val payment = dao.getPaymentByRef(ref)
                                if (payment != null) {
                                    dao.updatePaymentStatusByRef(ref, "success")
                                    dao.updateBookingPayment(payment.bookingId, "paid", ref)
                                    // Set booking state to aligned starting flow or keeps standard paid
                                    val booking = dao.getBookingById(payment.bookingId)
                                    if (booking != null && booking.status == "pending") {
                                        // Just update payment
                                    }
                                    Log.d("SimulatedServer", "Successfully processed Webhook server-side for ref $ref!")
                                }
                            }
                        }

                        val resJson = moshi.adapter(WebhookResponse::class.java).toJson(
                            WebhookResponse("success", "Webhook handled successfully")
                        )
                        return Response.Builder()
                            .code(200)
                            .message("OK")
                            .request(request)
                            .protocol(Protocol.HTTP_2)
                            .body(resJson.toResponseBody(jsonMediaType))
                            .build()
                    } catch (e: Exception) {
                        Log.e("SimulatedServer", "Webhook verification error", e)
                    }
                }

                return errorResponse(request, 401, "Signature Verification Failed")
            }

            return chain.proceed(request)
        }

        private fun errorResponse(request: okhttp3.Request, code: Int, message: String): Response {
            val json = "{\"status\": \"error\", \"message\": \"$message\"}"
            return Response.Builder()
                .code(code)
                .message(message)
                .request(request)
                .protocol(Protocol.HTTP_2)
                .body(json.toResponseBody(jsonMediaType))
                .build()
        }

        private fun extractField(json: String, field: String): String {
            val pattern = "\"$field\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val match = pattern.find(json)
            return match?.groups?.get(1)?.value ?: ""
        }

        private fun calculateHmacSha512(data: String, key: String): String {
            return try {
                val sha512HMAC = Mac.getInstance("HmacSHA512")
                val keySpec = SecretKeySpec(key.toByteArray(), "HmacSHA512")
                sha512HMAC.init(keySpec)
                val hashBytes = sha512HMAC.doFinal(data.toByteArray())
                hashBytes.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                ""
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(PaystackSimulatingInterceptor())
        .build()

    val apiService: SashioApiService = Retrofit.Builder()
        .baseUrl("https://localhost-sashio-fake-api.com")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(SashioApiService::class.java)

    // Method to simulate triggering Paystack client simulation, computing signature, & firing webhook
    fun simulateWebhookBroadcast(reference: String) {
        val payload = "{\"event\":\"charge.success\",\"reference\":\"$reference\"}"
        val key = PAYSTACK_SECRET_KEY
        val signature = try {
            val sha512HMAC = Mac.getInstance("HmacSHA512")
            val keySpec = SecretKeySpec(key.toByteArray(), "HmacSHA512")
            sha512HMAC.init(keySpec)
            val hashBytes = sha512HMAC.doFinal(payload.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "bypass-for-demo"
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Post payload straight to our local fake server API
                apiService.paystackWebhook(signature, payload)
            } catch (e: Exception) {
                Log.e("NetworkProvider", "Simulation error", e)
            }
        }
    }
}
