package com.epicorebiosystems.rehydrate.appInjectionSupport

import android.app.Activity
import android.app.Application
import android.util.Log
import com.datadog.android.Datadog
import com.datadog.android.DatadogSite
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.Rum
import com.datadog.android.rum.RumConfiguration
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy
import com.datadog.android.rum.tracking.ComponentPredicate
import com.datadog.android.trace.AndroidTracer
import com.datadog.android.trace.Trace
import com.datadog.android.trace.TraceConfiguration
import com.epicorebiosystems.rehydrate.BuildConfig
import com.epicorebiosystems.rehydrate.MainActivity
import com.epicorebiosystems.rehydrate.nordicsemi.uart.UartServer
import dagger.hilt.android.HiltAndroidApp
import io.opentracing.util.GlobalTracer
import javax.inject.Inject

@HiltAndroidApp
class EpicoreCHApplication : Application() {

    @Inject
    lateinit var uartServer: UartServer

    override fun onCreate() {
        super.onCreate()

        val clientToken = "pubf3783864fd448e436b950c3002d081a9"
        val environmentName = "prod"
        val appVariantName = BuildConfig.BUILD_TYPE
        
        val configuration = Configuration.Builder(
            clientToken = clientToken,
            env = environmentName,
            variant = appVariantName
        ).useSite(DatadogSite.US5).build()
        
        Datadog.initialize(this, configuration, TrackingConsent.GRANTED)

        val applicationId = "c9d66e77-93b8-4da4-bbce-2273eebc62cc"
        val rumConfiguration = RumConfiguration.Builder(applicationId)
            .trackUserInteractions()
            .trackBackgroundEvents(true)
            .useViewTrackingStrategy(
                ActivityViewTrackingStrategy(
                    trackExtras = true,
                    componentPredicate = object : ComponentPredicate<Activity> {
                    override fun accept(component: Activity): Boolean {
                        return component !is MainActivity
                    }

                    override fun getViewName(component: Activity): String? = null
                }))
            .build()
        Rum.enable(rumConfiguration)

        if (Datadog.isInitialized()) {
            Datadog.setVerbosity(Log.INFO)

            val traceConfig = TraceConfiguration.Builder().build()
            Trace.enable(traceConfig)

            val tracer = AndroidTracer.Builder().build()
            GlobalTracer.registerIfAbsent(tracer)
        }

        uartServer.start(this)
    }
}