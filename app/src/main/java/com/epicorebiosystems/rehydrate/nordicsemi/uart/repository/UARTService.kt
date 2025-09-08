/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.epicorebiosystems.rehydrate.nordicsemi.uart.repository

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.IntentCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.epicorebiosystems.rehydrate.nordicsemi.service.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.Mtu
import no.nordicsemi.android.kotlin.ble.profile.battery.BatteryLevelParser
import java.util.UUID
import javax.inject.Inject

val UART_SERVICE_UUID: UUID = UUID.fromString("4C570001-3033-4843-2045-524F43495045")
internal val UART_RX_CHARACTERISTIC_UUID = UUID.fromString("4C570002-3033-4843-2045-524F43495045")
internal val UART_TX_CHARACTERISTIC_UUID = UUID.fromString("4C570003-3033-4843-2045-524F43495045")

internal val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
internal val BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
@AndroidEntryPoint
internal class UARTService() : NotificationService() {

    @Inject
    lateinit var repository: UARTRepository

    private var client: ClientBleGatt? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        repository.setServiceRunning(true)

        val device = IntentCompat.getParcelableExtra(intent!!, com.epicorebiosystems.rehydrate.nordicsemi.service.DEVICE_DATA, ServerDevice::class.java)!!

        startGattClient(device)

        repository.stopEvent
            .onEach { disconnect() }
            .launchIn(lifecycleScope)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            START_NOT_STICKY
        } else {
            START_REDELIVER_INTENT
        }
    }

    private fun startGattClient(device: ServerDevice) = lifecycleScope.launch {
        val client = ClientBleGatt.connect(this@UARTService, device, logger = { p, s -> repository.log(p, s) })
        this@UARTService.client = client

        if (!client.isConnected) {
            return@launch
        }

        try {
            client.requestMtu(Mtu.max)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val services = client.discoverServices()
            configureGatt(services)
        } catch (e: Exception) {
            repository.onMissingServices()
        }

        client.connectionStateWithStatus
            .filterNotNull()
            .onEach { repository.onConnectionStateChanged(it) }
            .onEach { stopIfDisconnected(it.state, it.status) }
            .filterNotNull()
            .launchIn(lifecycleScope)

//        try {
//            client.requestConnectionPriority(BleGattConnectionPriority.HIGH)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

    }

    private suspend fun configureGatt(services: ClientBleGattServices) {
        val uartService = services.findService(UART_SERVICE_UUID)!!
        val rxCharacteristic = uartService.findCharacteristic(UART_RX_CHARACTERISTIC_UUID)!!
        val txCharacteristic = uartService.findCharacteristic(UART_TX_CHARACTERISTIC_UUID)!!

        val batteryService = services.findService(BATTERY_SERVICE_UUID)

        batteryService?.findCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)?.getNotifications()
            ?.mapNotNull { BatteryLevelParser.parse(it) }
            ?.onEach { repository.onBatteryLevelChanged(it) }
            ?.catch { it.printStackTrace() }
            ?.launchIn(lifecycleScope)

        txCharacteristic.getNotifications()
            .onEach { repository.onNewMessageReceived((it.value)) }
            .onEach { repository.log(10, "Received: ${String(it.value)}") }
            .catch { it.printStackTrace() }
            .launchIn(lifecycleScope)

        repository.command
            .onEach { rxCharacteristic.splitWrite(DataByteArray(it), getWriteType(rxCharacteristic)) }
            .onEach { repository.onNewMessageSent(it) }
            .onEach { repository.log(10, "Sent: $it") }
            .catch { it.printStackTrace() }
            .launchIn(lifecycleScope)
    }

    private fun getWriteType(characteristic: ClientBleGattCharacteristic): BleWriteType {
//        return if (characteristic.properties.contains(BleGattProperty.PROPERTY_WRITE)) {
        return if (characteristic.properties.contains(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE)) {
            BleWriteType.NO_RESPONSE
        } else {
            BleWriteType.DEFAULT
        }
    }

    private fun stopIfDisconnected(connectionState: GattConnectionState, connectionStatus: BleGattConnectionStatus) {
        if (connectionState == GattConnectionState.STATE_DISCONNECTED && !connectionStatus.isLinkLoss) {
            repository.disconnect()
            stopSelf()
        }
    }

    private fun disconnect() {
        client?.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.setServiceRunning(false)
    }

}