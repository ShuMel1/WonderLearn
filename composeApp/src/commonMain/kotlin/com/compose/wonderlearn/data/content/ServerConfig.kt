package com.compose.wonderlearn.data.content

/** Set once the service is deployed. Blank means release builds use bundled content only. */
const val PRODUCTION_BASE_URL = "https://wonderlearn-api.onrender.com"

/** True in developer builds, so debug runs talk to a server on the developer's machine. */
expect val isDebugBuild: Boolean

/** Cleartext is permitted for these hosts in the Android network security config. */
expect val devServerBaseUrl: String

val serverBaseUrl: String
  get() = if (isDebugBuild) devServerBaseUrl else PRODUCTION_BASE_URL
