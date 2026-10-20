package com.thupo

import io.sentry.kotlin.multiplatform.Sentry

object MonitoringManager {
    fun initMonitoring() {
        val dsn = BuildKonfig.SENTRY_DSN
        if (dsn.isNotBlank()) {
            Sentry.init { options ->
                options.dsn = dsn
                options.tracesSampleRate = 1.0
                options.environment = if (BuildKonfig.IS_DEBUG) "development" else "production"
            }
        }
    }

    fun logError(throwable: Throwable) {
        if (BuildKonfig.SENTRY_DSN.isNotBlank()) {
            Sentry.captureException(throwable)
        }
    }
}
