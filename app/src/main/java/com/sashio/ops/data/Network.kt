package com.sashio.ops.data

import com.sashio.ops.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// --- API Request / Response Models ---
// bookingId must be the booking's Supabase UUID (public.bookings.id), not the
// local Room Long id - the Edge Function looks the booking up in Postgres.
data class CreatePaymentRequest(
    val bookingId: String,
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

// Talks to real Supabase Edge Functions (see supabase/functions/). The
// Paystack SECRET key lives only inside those functions, never in the app.
interface SashioApiService {
    @POST("initialize-payment")
    suspend fun createPayment(@Body request: CreatePaymentRequest): CreatePaymentResponse

    // Note: there is no client-side "paystackWebhook" call. Paystack calls
    // the paystack-webhook Edge Function directly from its own servers once
    // a real payment succeeds - the app never triggers that itself.
}

class NetworkProvider(
    private val database: SashioDatabase
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Every Supabase Edge Function call must include the anon key so
    // Supabase's gateway accepts the request (this is not a secret - it's
    // the same public key used to init the Supabase client).
    private class SupabaseAuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                .build()
            return chain.proceed(request)
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(SupabaseAuthInterceptor())
        .build()

    val apiService: SashioApiService = Retrofit.Builder()
        .baseUrl("${BuildConfig.SUPABASE_URL}/functions/v1/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(SashioApiService::class.java)
}
