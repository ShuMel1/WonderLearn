package com.compose.wonderlearn.shared

import io.ktor.resources.Resource

/**
 * Route definitions shared by client and server, so a path or parameter change is a compile
 * error on both sides rather than a runtime mismatch.
 */
@Resource("/")
class RootRoute

@Resource("/health")
class HealthRoute

@Resource("/privacy")
class PrivacyRoute

@Resource("/support")
class SupportRoute

@Resource("/v1/content/manifest")
class ContentManifestRoute(val since: Long? = null)

@Resource("/v1/events")
class EventsRoute

@Resource("/v1/events/summary")
class EventsSummaryRoute
