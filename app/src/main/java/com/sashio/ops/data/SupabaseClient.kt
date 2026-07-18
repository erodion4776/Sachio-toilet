package com.sashio.ops.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseProfile(
    val id: String,
    val email: String,
    val full_name: String? = null,
    val selected_role: String = "client",
    val job_title: String? = null
)

object SupabaseProvider {
    // PLACEHOLDERS FOR CREDENTIALS - TO BE REPLACED LIVE BY THE USER
    const val SUPABASE_URL = "YOUR_SUPABASE_URL"
    const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"

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
