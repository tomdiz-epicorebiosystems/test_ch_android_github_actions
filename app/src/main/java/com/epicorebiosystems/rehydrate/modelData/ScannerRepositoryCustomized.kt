package com.epicorebiosystems.rehydrate.modelData

import android.annotation.SuppressLint
import dagger.hilt.android.scopes.ViewModelScoped
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanMode
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScannerSettings
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import javax.inject.Inject

@ViewModelScoped
class ScannerRepositoryCustomized @Inject internal constructor(
    private val nordicScanner: BleScanner
) {

    @SuppressLint("MissingPermission")
    fun getScannerState() = nordicScanner.scan(settings = BleScannerSettings(scanMode = BleScanMode.SCAN_MODE_LOW_LATENCY))
}