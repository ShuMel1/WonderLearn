package com.compose.wonderlearn.data.content

const val PRODUCTION_BASE_URL = "https://wonderlearn-api.onrender.com"

/**
 * Flip to true while working on the server itself, to point debug builds at a machine on the
 * desk instead of the deployed service. Release builds always use [PRODUCTION_BASE_URL].
 */
private const val USE_LOCAL_SERVER = false

expect val isDebugBuild: Boolean

/** Cleartext is permitted for these hosts in the Android network security config. */
expect val devServerBaseUrl: String

val serverBaseUrl: String
  get() = if (USE_LOCAL_SERVER && isDebugBuild) devServerBaseUrl else PRODUCTION_BASE_URL
