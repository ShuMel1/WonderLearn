package com.compose.wonderlearn.data.analytics

/** "android" or "ios", attached to each event so game usage can be split by platform. */
expect val platformName: String

/** The user-visible app version, e.g. "1.0.0". */
expect val appVersionName: String
