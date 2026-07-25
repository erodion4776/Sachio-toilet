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
data class SupabaseProfile(
    val id: String,
    val email: String,
    val full_name: String? = null,
    val selected_role: String = "client",
    val job_title: String? = null
)

object SupabaseProvider {
    // Populated at build time from SUPABASE_URL / SUPABASE_ANON_KEY in .env
    // via the Secrets Gradle Plugin. Set real values in a local .env file
    // (git-ignored) - never commit real credentials to .env.example.
    private val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY

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
