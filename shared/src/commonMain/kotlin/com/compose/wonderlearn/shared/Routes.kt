package com.compose.wonderlearn.shared

import io.ktor.resources.Resource

/**
 * Route definitions shared by client and server, so a path or parameter change is a compile
 * error on both sides rather than a runtime mismatch.
 */
@Resource("/health")
class HealthRoute

@Resource("/v1/content/manifest")
class ContentManifestRoute(val since: Long? = null)
