package com.sashio.ops.data

import com.sashio.ops.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import kotlinx.serialization.Serializable

@Serializable
data class SupabasePaymentStatus(
    val reference: String,
    val status: String
)

@Serializable
data class AdminProfileRow(
    val id: String,
    val email: String,
    val full_name: String? = null,
    val role: String = "client",
    val phone_number: String? = null,
    val company_name: String? = null,
    val suspended: Boolean = false
)

@Serializable
data class SupabaseProfile(
    val id: String,
    val email: String,
    val full_name: String? = null,
    val role: String = "client",
    val phone_number: String? = null,
    val company_name: String? = null
)

object SupabaseProvider {
    // Populated at build time from SUPABASE_URL / SUPABASE_ANON_KEY in .env
    // via the Secrets Gradle Plugin. Set real values in a local .env file
    // (git-ignored) - never commit real credentials to .env.example.
    // Not private: SashioViewModel.loginWithEmail() checks SUPABASE_URL
    // directly to decide whether to fall back to offline demo login when
    // Supabase isn't configured yet. Safe to expose - anon key/URL are
    // public-by-design credentials, not secrets.
    val SUPABASE_URL = BuildConfig.SUPABASE_URL
    val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
