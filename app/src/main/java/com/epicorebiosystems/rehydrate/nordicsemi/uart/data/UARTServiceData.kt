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

package com.epicorebiosystems.rehydrate.nordicsemi.uart.data

import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus

data class UARTServiceData(
    val connectionState: GattConnectionStateWithStatus? = null,
    val missingServices: Boolean = false,
    val sweatDataWaveformSamplesInMv: IntArray = IntArray(80) { 1500 },
    var sweatStatusUpdate: Boolean = false,
    var userInfoUpdate: Boolean = false,
    var fileReadyUpload: Boolean = false,
    var sweatDataLogDownloadCompleted: Boolean = true,
    val sweatUserInfoSetResponseReceived: Boolean = false,
    var isAlreadyInSession: Boolean = false,
    var intakeLogResponseReceived: Boolean = false,
    val sweatSensorNameSetResponseReceived: Boolean = false,
    var historicalSweatDataUpToDate: Boolean = false
) {

    val disconnectStatus = if (missingServices) {
        BleGattConnectionStatus.NOT_SUPPORTED
    } else {
        connectionState?.status ?: BleGattConnectionStatus.UNKNOWN
    }

//    val displayMessages = messages
}

data class UARTRecord(
    val text: String,
    val type: UARTRecordType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class UARTRecordType {
    INPUT, OUTPUT
}

data class HistoricalSweatDataPacket(
    val timeStamp: UShort,
    val sweatVolumeDeficitInOz: Double,
    val sweatSodiumDeficitInMg: Short,
    val sweatVolumeLossWholeBodyInOz: Double,
    val sweatSodiumLossWholeBodyInMg: UShort,
    val fluidTotalIntakeInOz: Double,
    val sodiumTotalIntakeInMg: UShort,
    val bodyTemperatureSkinInC: Double,
    val bodyTemperatureAirInC: Double,
    val activityCounts: UByte
)

enum class SweatLogDataType(val eventType: Int) {
    DATA_SWEAT(0),
    EVENT_DEHYDRATION_ALARM(1),
    EVENT_NUDGE_ALERT(2),
    EVENT_HYDRATION_INTAKE(3),

    EVENT_GPS_LOCATION(4),
    EVENT_PATCH_SATURATION(5),
    EVENT_PATCH_PLATEAU(6),
    EVENT_PATCH_SEALBREAK(7),
    EVENT_PERSISTENT_DROPOUT(8),
    EVENT_SWEAT_RATE_UPDATE(9),
    EVENT_FLUIDICS_NOT_CLIPPED(10),
    EVENT_REC_NOT_POSSIBLE(11),

    EVENT_GSR_SWEAT_ONSET(20),
    EVENT_GSR_SODIUM_READING_AVAILABLE(21),
    EVENT_GSR_SODIUM_READING_MAX_UPDATE(22),
    EVENT_GSR_OFF_BODY(23)
}

data class SweatStatusPacket(
    val packetNum : UByte,
    val timeStamp : UShort,
    val batteryVoltageInV :Double,
    val bodyTemperatureSkinInF: Double,
    val bodyTemperatureAirInF :Double,

    val localSweatVolumeInUl : UByte,
    val localSweatChlorideLevelInMM : UByte,
    val sweatVolumeDeficitInMl : Short,
    val sweatSodiumDeficitInMg : Short,
    val sweatVolumeTotalLossInMl : UShort,
    val sweatSodiumTotalLossInMg : UShort,
    val fluidTotalIntakeInMl : UShort,
    val sodiumTotalIntakeInMg : UShort,
    val hydrationStatus : UByte,
    val alertStatus : UByte,
    val averageSkinTempRaw : Short,

    // Derived values for display purpose
    val sweatVolumeTotalLossInOz: Double,
    val fluidTotalIntakeInOz: Double,
    val fluidDeficitInOz: Double,
    val averageSkinTempInF: Double,
    val currentTEWLInMl: Int,

    val currentRecordingDuration: UShort,
)

data class SysInfoPacket(
    val fwRevisonString : String,
    val brownOutResetCounter : UByte,
    val sweatSensingOngoing : Boolean,
    val batteryVoltageInV : Double,
    val bodyTemperatureSkinInF : Double,
    val bodyTemperatureAirInF : Double,
)

data class UserInfo(
    val subjectGender : String,
    val subjectHeightFeet : Int,
    val subjectHeightInches : Int,
    val subjectHeightInCm : Int,
    val subjectWeightInLb : Int,
    val subjectWeightInKg : Int,
    val subjectClothCode : UByte
)