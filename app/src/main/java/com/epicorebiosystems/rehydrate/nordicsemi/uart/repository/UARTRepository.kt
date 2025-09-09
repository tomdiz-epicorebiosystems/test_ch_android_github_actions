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

import android.content.Context
import android.os.Build
import android.util.Log
import com.epicorebiosystems.rehydrate.modelData.isCHArmBand
import com.epicorebiosystems.rehydrate.nordicsemi.service.DisconnectAndStopEvent
import com.epicorebiosystems.rehydrate.nordicsemi.service.ServiceManager
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.ConfigurationDataSource
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.HistoricalSweatDataPacket
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.SweatLogDataType
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.SweatStatusPacket
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.SysInfoPacket
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.UARTMacro
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.UARTServiceData
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.UserInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import no.nordicsemi.android.common.core.simpleSharedFlow
import no.nordicsemi.android.common.logger.BleLoggerAndLauncher
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import java.io.File
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.roundToInt
import java.security.MessageDigest
import kotlin.math.round

private const val numOfSweatLogEntriesPerPacket = 8
private const val sizeofSweatLogEntry = 20
private const val sizeOfSweatDataWaveformEntries = 80

@Singleton
class UARTRepository @Inject internal constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val configurationDataSource: ConfigurationDataSource,
) {
    private var logger: BleLoggerAndLauncher? = null

    private val _data = MutableStateFlow(UARTServiceData())
    val data = _data.asStateFlow()

    private val _stopEvent = simpleSharedFlow<DisconnectAndStopEvent>()
    internal val stopEvent = _stopEvent.asSharedFlow()

    private val _command = simpleSharedFlow<ByteArray>()
    internal val command = _command.asSharedFlow()

    val isRunning = data.map { it.connectionState?.state == GattConnectionState.STATE_CONNECTED }

    val lastConfigurationName = configurationDataSource.lastConfigurationName

    private var isOnScreen = false
    private var isServiceRunning = false

    private var deviceSN = ""

    private var currentHistoricalSweatDataDownloadIndex: UShort = 0u

    private var currentRecordingDuration: UShort = 0u

    private var alertStatus: UByte = 0u

    //
    // Used for file upload file
    //
    // Set the data log file header before downloading starts in case there is an out of order packet which break the
    private var sweatDataLogCSVText =
        "TimeStamp(s),Data Type,Sweat Volume Loss Local (uL),Sweat Sodium Level (mM),Recommended fluid intake (mL),Recommended sodium intake (mg),Total sweat loss (mL),Total sodium loss (mg),TEWL (mL), Body Temp (C),Ambient Temp (C),Activity Score,Batt Level(mV)\n"
    private var sweatDataLogFileName = ""
    private var logDurationInSeconds: UShort = 0u
    private var sweatDataLogStartEpochTime: UInt = 0u
    private var sweatDataLogStartEpochTimeString = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var userId = ""
    private var enterpriseId = ""
    private var appVersion = ""
    private var buildNumber = ""
    private var subjectHeightInCm = 175
    private var subjectWeightInKg = 75
    private var subjectGender = "Male"

    private var sweatDataLogSessionUserHeigthtInCm : UByte = 175u
    private var sweatDataLogSessionUserWeightInKg : UShort = 75u
    private var sweatDataLogSessionUserGender : String = "M"

    private var passiveWaterLoss = true

    private var isCHArmBandConnected = false

    private var subjectBSAInM2 = 1.903

    private var deviceName: String? = null

    var applicationContext: Context? = null

    private var sweatStatusPacket: SweatStatusPacket? = null
    private var userInfo: UserInfo? = null
    private var sysInfo: SysInfoPacket? = null

    private var historicalSweatDataSetForPlot: MutableList<HistoricalSweatDataPacket> = mutableListOf()
    private var initialSweatDataSetForPlot: MutableList<HistoricalSweatDataPacket> = mutableListOf()

    private var sensorResetReason: UInt = 0xFFFFFFFF.toUInt()
    private var sensorResetErrorId: UInt = 0xFFFFFFFF.toUInt()
    private var sensorResetErrorCode: UInt = 0xFFFFFFFF.toUInt()
    private var sensorResetLineNum: UInt = 0xFFFFFFFF.toUInt()
    private var sensorResetErrorFileName: String = ""
    private var sweatDataLogSessionUserID: String = ""
    private var sweatDataLogSessionSiteID: String = ""

    private var demoSweatDataLogCSVText: String = ""

    // Used for module already in session support
    private var isCurrentUserSession = true
    private var isUserSessionToDisplay = false

    private var currentRecordingStartEpochTime: UInt = 0u

    // Stores SHA-1 hashes as keys to track duplicates
    private val uploadDataDuplicateHashMap: MutableMap<String, Boolean> = mutableMapOf()
    private var sweatDataAdded = false

    private var isDemoOnboardingFlow = false

    private var currentDayDownloadingCompletedFlagForHistoricalData = true

    fun setOnScreen(isOnScreen: Boolean) {
        this.isOnScreen = isOnScreen

        if (shouldClean()) clean()
    }

    fun setServiceRunning(serviceRunning: Boolean) {
        this.isServiceRunning = serviceRunning

        if (shouldClean()) clean()
    }

    private fun shouldClean() = !isOnScreen && !isServiceRunning

    fun launch(device: ServerDevice) {
        //logger = loggerFactory.createNordicLogger(context, "Epicore CH", "UART", device.address)
        deviceName = device.name
        serviceManager.startService(UARTService::class.java, device)
    }

    fun onConnectionStateChanged(connectionState: GattConnectionStateWithStatus?) {
        _data.value = _data.value.copy(connectionState = connectionState)
        Log.d("ConnectionStateChanged", "${connectionState?.state}")
    }

    fun onBatteryLevelChanged(batteryLevel: Int) {
        //_data.value = _data.value.copy(batteryLevel = batteryLevel)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun onNewMessageReceived(value: ByteArray) {
//        _data.value = _data.value.copy(messages = _data.value.messages + UARTRecord(String(value), UARTRecordType.OUTPUT))
        val uartByteArray = value.toUByteArray()

        when(uartByteArray[1]) {
            // This is real time status packet.
            0x30.toUByte() -> {
                val packetNo = uartByteArray[0]
                val timeStamp = (uartByteArray[2].toUShort() + (uartByteArray[3].toUInt() shl 8)).toUShort()
                val batteryVoltageInV = ((uartByteArray[4].toUShort() + (uartByteArray[5].toUInt() shl 8)).toDouble() / 1000.0).toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()
                val bodyTempSkinInFahrenheit = (((uartByteArray[6].toUShort() + (uartByteArray[7].toUInt() shl 8)).toDouble() * 0.005) * 1.8 + 32.0).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
                val bodyTempAirInFahrenheit = (((uartByteArray[8].toUShort() + (uartByteArray[9].toUInt() shl 8)).toDouble() * 0.005) * 1.8 + 32.0).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
                val localSweatVolumeInUl = uartByteArray[10]
                val localSweatChlorideLevelInMM = uartByteArray[11]

                val currentTEWLInMl = if (passiveWaterLoss) getCurrentTEWLInMl(timeStamp) else 0

                val sweatVolumeDeficitInMl = ((uartByteArray[12].toUShort() + (uartByteArray[13].toUInt() shl 8)).toShort() + currentTEWLInMl).toShort()
                val sweatSodiumDeficitInMg = (uartByteArray[14].toUShort() + (uartByteArray[15].toUInt() shl 8)).toShort()
                val sweatVolumeTotalLossInMl = ((uartByteArray[16].toUShort() + (uartByteArray[17].toUInt() shl 8)).toUShort() + currentTEWLInMl.toUShort()).toUShort()
                val sweatSodiumTotalLossInMg = (uartByteArray[18].toUShort() + (uartByteArray[19].toUInt() shl 8)).toUShort()
                val fluidTotalIntakeInMl = (uartByteArray[20].toUShort() + (uartByteArray[21].toUInt() shl 8)).toUShort()
                val sodiumTotalIntakeInMg = (uartByteArray[22].toUShort() + (uartByteArray[23].toUInt() shl 8)).toUShort()
                val hydrationStatus = uartByteArray[24]
                alertStatus = uartByteArray[25]
                val averageSkinTempRaw = (uartByteArray[26].toUShort() + (uartByteArray[27].toUInt() shl 8)).toShort()

                val averageSkinTempInF = ((averageSkinTempRaw.toDouble() * 0.005) * 1.8 + 32.0).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
                val sweatVolumeTotalLossInOz = (sweatVolumeTotalLossInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
                val fluidTotalIntakeInOz = (fluidTotalIntakeInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
                val fluidDeficitInOz = (sweatVolumeDeficitInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()

                currentRecordingDuration = timeStamp
                currentRecordingStartEpochTime = getTimeIntervalSince1970() - currentRecordingDuration.toUInt()

                sweatStatusPacket = SweatStatusPacket(packetNo,
                    timeStamp, batteryVoltageInV, bodyTempSkinInFahrenheit, bodyTempAirInFahrenheit, localSweatVolumeInUl, localSweatChlorideLevelInMM,
                    sweatVolumeDeficitInMl, sweatSodiumDeficitInMg, sweatVolumeTotalLossInMl, sweatSodiumTotalLossInMg, fluidTotalIntakeInMl,sodiumTotalIntakeInMg,
                    hydrationStatus, alertStatus, averageSkinTempRaw,
                    sweatVolumeTotalLossInOz, fluidTotalIntakeInOz, fluidDeficitInOz, averageSkinTempInF, currentTEWLInMl, currentRecordingDuration
                )

                _data.value = _data.value.copy(sweatStatusUpdate = true)

                //Log.d("SweatStatusPacket", "$timeStamp, $batteryVoltageInV, $bodyTempSkinInFahrenheit")
            }

            // This is historical sweat data packet for plot display purpose
            0x40.toUByte() -> {
                //val packetNo = uartByteArray[0]

                for (i in 0..<numOfSweatLogEntriesPerPacket) {
                    val timeStamp =
                        (uartByteArray[2 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[3 + i* sizeofSweatLogEntry].toUInt() shl 8)).toUShort()

                    // Reach the end of the historical log
                    if (timeStamp == 0xFFFF.toUShort()) {

                        // Sort all the downloaded records and find the download index for next partial download.
                        historicalSweatDataSetForPlot = historicalSweatDataSetForPlot.toMutableSet().toMutableList()
                        historicalSweatDataSetForPlot.sortBy { it.timeStamp }

                        val downloadedRecordCount = historicalSweatDataSetForPlot.size
                        if (downloadedRecordCount > 0) {
                            val downloadIndex = (historicalSweatDataSetForPlot[downloadedRecordCount-1].timeStamp / 20u + 1u).toUShort()
//                            currentHistoricalSweatDataDownloadIndex = downloadIndex
                            //Log.d("currHistoricalDwnIndex2", "$downloadIndex")
                        }

                        _data.value = _data.value.copy(historicalSweatDataUpToDate = true)

                        break
                    }
                    else {
                        val batteryVoltageInV =
                            ((uartByteArray[4 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[5 + i* sizeofSweatLogEntry].toUInt() shl 8)).toDouble() / 1000.0).toBigDecimal()
                                .setScale(2, RoundingMode.DOWN).toDouble()
                        val dataType = uartByteArray[6 + i* sizeofSweatLogEntry]
                        val bodyTemperatureSkinInC = ((uartByteArray[7 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[8 + i* sizeofSweatLogEntry].toUInt() shl 8)).toDouble() * 0.005).toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()
                        val bodyTemperatureAirInC = ((uartByteArray[9 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[10 + i* sizeofSweatLogEntry].toUInt() shl 8)).toDouble() * 0.005).toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()
                        val activityCounts = uartByteArray[11 + i* sizeofSweatLogEntry]

                        val currentTEWLInMlToPlot = if (passiveWaterLoss) getCurrentTEWLInMl(timeStamp) else 0

                        val sweatVolumeDeficitInMl = ((uartByteArray[14 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[15 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort() + currentTEWLInMlToPlot).toShort()
                        val sweatSodiumDeficitInMg = (uartByteArray[16 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[17 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()

                        val sweatVolumeTotalLossInMl = ((uartByteArray[18 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[19 + i* sizeofSweatLogEntry].toUInt() shl 8)).toUShort() + currentTEWLInMlToPlot.toUShort()).toUShort()
                        val sweatSodiumTotalLossInMg = (uartByteArray[20 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[21 + i* sizeofSweatLogEntry].toUInt() shl 8)).toUShort()

                        val sweatVolumeDeficitInOz = (sweatVolumeDeficitInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
                        val sweatVolumeTotalLossInOz = (sweatVolumeTotalLossInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()

                        val fluidTotalIntakeInOz = (sweatVolumeTotalLossInMl.toDouble() - sweatVolumeDeficitInMl.toDouble()) / 29.574
                        val sodiumTotalIntakeInMg = (sweatSodiumTotalLossInMg.toShort() - sweatSodiumDeficitInMg).toUShort()

                        if(dataType.toInt() == SweatLogDataType.DATA_SWEAT.eventType) {
                            historicalSweatDataSetForPlot.add(HistoricalSweatDataPacket(timeStamp, sweatVolumeDeficitInOz, sweatSodiumDeficitInMg,
                                sweatVolumeTotalLossInOz, sweatSodiumTotalLossInMg, fluidTotalIntakeInOz, sodiumTotalIntakeInMg,
                                bodyTemperatureSkinInC, bodyTemperatureAirInC, activityCounts))
                        }
                    }

                    //Log.d("CHART_HISTORY", "${historicalSweatDataSetForPlot.size}")
                }
            }

            // This is sweat raw signal waveform packet
            0x42.toUByte() -> {
                var currentSweatDataWaveformSamples = IntArray(80) { 1500 }
                for (i in 0..<sizeOfSweatDataWaveformEntries) {
                    currentSweatDataWaveformSamples[i] = ((uartByteArray[7 + 2*i].toInt() + (uartByteArray[8 + 2*i].toInt() shl 8)).toDouble() * 3600.0 / 4096.0).toInt()
                }

                _data.value = _data.value.copy(sweatDataWaveformSamplesInMv = currentSweatDataWaveformSamples)

                //Log.d("sweatDataWaveformSamplesInMv", "")
            }

            // This is sensor system information and user personal information packet
            0x50.toUByte() -> {
                val fwRevMajor = uartByteArray[2].toInt()
                val fwRevMinor = uartByteArray[3].toInt()
                val fwRevisionString = "v" + fwRevMajor.toString() + "." + "%02d".format(fwRevMinor)
                val brownOutResetCounter = uartByteArray[4]
                val sweatSensingOngoing = (uartByteArray[5] != 0.toUByte())

                isCHArmBandConnected = isCHArmBand(fwRevisionString)

                val batteryVoltageInV = ((uartByteArray[6].toUShort() + (uartByteArray[7].toUInt() shl 8)).toDouble() / 1000.0).toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()
                val bodyTemperatureSkinInF = (((uartByteArray[8].toUShort() + (uartByteArray[9].toUInt() shl 8)).toDouble() * 0.005) * 1.8 + 32.0).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
                val bodyTemperatureAirInF = (((uartByteArray[10].toUShort() + (uartByteArray[11].toUInt() shl 8)).toDouble() * 0.005) * 1.8 + 32.0).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()

                // Parse user personal information meta data
                if(uartByteArray[44] != 0xFF.toUByte()) {
                    val subjectGender = if (uartByteArray[44] == 0.toUByte()) "Male" else "Female"
                    val heightInInches = uartByteArray[45].toDouble() / 2.54
                    val subjectHeightFeet = (heightInInches / 12.0).toInt()
                    val subjectHeightInches = (heightInInches % 12.0).roundToInt()
                    val subjectHeightInCm = uartByteArray[45].toInt()
                    val subjectWeightInKg = ((uartByteArray[46].toUShort() + (uartByteArray[47].toUInt() shl 8)).toUShort()).toInt()
                    val subjectWeightInLb = (subjectWeightInKg.toDouble() / 0.453592).roundToInt()

                    val subjectAge = uartByteArray[48]
                    val subjectClothCode = uartByteArray[49]

                    userInfo = UserInfo(subjectGender,
                        subjectHeightFeet,
                        subjectHeightInches,
                        subjectHeightInCm,
                        subjectWeightInLb,
                        subjectWeightInKg,
                        subjectClothCode
                    )

                    sysInfo = SysInfoPacket(fwRevisionString, brownOutResetCounter, sweatSensingOngoing,
                        batteryVoltageInV, bodyTemperatureSkinInF, bodyTemperatureAirInF)

                    _data.value = _data.value.copy(userInfoUpdate = true)
                }

                //Log.d("SYS_INFO_PACKET", "$fwRevisionString")
            }

            // This is sweat data log packet
            0x41.toUByte() -> {

                var sweatVolumeDeficitInMl: Short = 0
                var sweatSodiumDeficitInMg: Short = 0
                var eventWaterIntakeInMl: UShort = 0u
                var eventSodiumIntakeInMg: UShort = 0u
                var eventLocalSweatRate: Double = 0.0
                var eventDurationForSweatRate: UShort = 0u
                var eventLocalVolumeLossForSweatRateCalculation: UByte = 0u
                var eventFluidicsNotClippedStatus: UByte = 0u
                var eventGSROffBodyStatus: UByte = 0u
                var eventGSRSweatOnsetChannelRawInMv: Short = 0
                var eventGSRSweatFluidicsChannelRawInMv: Short = 0

                val packetNum = uartByteArray[0]
                for (i in 0..<numOfSweatLogEntriesPerPacket) {
                    val timeStamp = (uartByteArray[2 + i * sizeofSweatLogEntry].toUShort() + (uartByteArray[3 + i * sizeofSweatLogEntry].toUInt() shl 8)).toUShort()

                    if ((timeStamp > logDurationInSeconds) && (timeStamp != (0xFFFF.toUShort()))) {
                        logDurationInSeconds = timeStamp
                    }

                    var currentTEWLToLog = getCurrentTEWLInMl(timeStamp)

                    val batteryVoltageInV = ((uartByteArray[4].toUShort() + (uartByteArray[5].toUInt() shl 8)).toDouble() / 1000.0).toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()

                    val batteryVoltageInMv =
                        uartByteArray[4 + i* sizeofSweatLogEntry].toUInt() + (uartByteArray[5 + i* sizeofSweatLogEntry].toUInt() shl 8)
                    val dataType = uartByteArray[6 + i* sizeofSweatLogEntry].toInt()
                    val bodyTemperatureSkinInC = ((uartByteArray[7 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[8 + i* sizeofSweatLogEntry].toUInt() shl 8)).toDouble() * 0.005).toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()
                    val bodyTemperatureAirInC = ((uartByteArray[9 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[10 + i* sizeofSweatLogEntry].toUInt() shl 8)).toDouble() * 0.005).toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()
                    val activityCounts = uartByteArray[11 + i* sizeofSweatLogEntry]

                    val localSweatVolumeUl = uartByteArray[12 + i* sizeofSweatLogEntry]
                    val localSweatChlorideLevel = uartByteArray[13 + i* sizeofSweatLogEntry]

                    val gsrLargeSkinElectrodeSignalRawInMv = localSweatVolumeUl * 5u
                    val gsrWellSignalRawInMv = uartByteArray[9 + i* sizeofSweatLogEntry] * 8u
                    val gsrZeroDepthElectrodeSignalRawInMv = uartByteArray[10 + i* sizeofSweatLogEntry] * 8u

                    val sweatVolumeTotalLossInMl = (uartByteArray[18 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[19 + i* sizeofSweatLogEntry].toUInt() shl 8)).toUShort()
                    val sweatSodiumTotalLossInMg = (uartByteArray[20 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[21 + i* sizeofSweatLogEntry].toUInt() shl 8)).toUShort()

                    // This is the regular sweat data log entry
                    if(dataType == SweatLogDataType.DATA_SWEAT.eventType) {
                        sweatVolumeDeficitInMl = (uartByteArray[14 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[15 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()
                        sweatSodiumDeficitInMg = (uartByteArray[16 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[17 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()
                    }

                    // This is one of the events: nudge alert, dehydration alarm
                    else if((dataType == SweatLogDataType.EVENT_NUDGE_ALERT.eventType) || (dataType.toInt() == SweatLogDataType.EVENT_DEHYDRATION_ALARM.eventType)) {
                        sweatVolumeDeficitInMl = (uartByteArray[14 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[15 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()
                        sweatSodiumDeficitInMg = (uartByteArray[16 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[17 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()
                    }

                    // This is intake event
                    else if(dataType == SweatLogDataType.EVENT_HYDRATION_INTAKE.eventType) {
                        eventWaterIntakeInMl = (uartByteArray[14 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[15 + i* sizeofSweatLogEntry].toUInt() shl 8)).toUShort()
                        eventSodiumIntakeInMg = (uartByteArray[16 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[17 + i* sizeofSweatLogEntry].toUInt() shl 8)).toUShort()
                    }

                    // This is GPS location capture event
                    else if(dataType == SweatLogDataType.EVENT_GPS_LOCATION.eventType) {

                    }

                    // This is fluidics not clipped event
                    else if(dataType == SweatLogDataType.EVENT_FLUIDICS_NOT_CLIPPED.eventType) {
                        sweatVolumeDeficitInMl = (uartByteArray[14 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[15 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()
                        sweatSodiumDeficitInMg = (uartByteArray[16 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[17 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()
                        eventFluidicsNotClippedStatus = uartByteArray[13 + i* sizeofSweatLogEntry]
                    }

                    else if(dataType == SweatLogDataType.EVENT_GSR_OFF_BODY.eventType) {
                        sweatVolumeDeficitInMl = (uartByteArray[14 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[15 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()
                        sweatSodiumDeficitInMg = (uartByteArray[16 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[17 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()
                        eventGSROffBodyStatus = uartByteArray[13 + i* sizeofSweatLogEntry]
                    }

                    else if((dataType == SweatLogDataType.EVENT_GSR_SWEAT_ONSET.eventType) || (dataType == SweatLogDataType.EVENT_GSR_SODIUM_READING_AVAILABLE.eventType) || (dataType == SweatLogDataType.EVENT_GSR_SODIUM_READING_MAX_UPDATE.eventType)) {
                        eventGSRSweatOnsetChannelRawInMv = (uartByteArray[14 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[15 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()
                        eventGSRSweatFluidicsChannelRawInMv = (uartByteArray[16 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[17 + i* sizeofSweatLogEntry].toUInt() shl 8)).toShort()
                    }

                    // This is one of the events: seal break, plateau, saturation, persistent dropout or sweat rate update. Sweat rate and related event data are recorded
                    else if(dataType != 0xFF) {
                        eventLocalSweatRate = (uartByteArray[14 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[15 + i* sizeofSweatLogEntry].toUInt() shl 8)).toDouble() / 100000.0
                        eventDurationForSweatRate = (uartByteArray[16 + i* sizeofSweatLogEntry].toUShort() + (uartByteArray[17 + i* sizeofSweatLogEntry].toUInt() shl 8)).toUShort()
                        eventLocalVolumeLossForSweatRateCalculation = uartByteArray[13 + i* sizeofSweatLogEntry]
                    }

                    //Log.d("SWEATDATAPACKET", "$packetNum, $timeStamp, $dataType, $batteryVoltageInMv, $bodyTemperatureSkinInC, $bodyTemperatureAirInC, $activityCounts")

                    val uartServiceData = data.value

                    // End of sweat data log is reached, save all the buffered data from downloading to a CSV file
                    if (timeStamp == 0xFFFF.toUShort() && (!uartServiceData.sweatDataLogDownloadCompleted)) {

                        _data.value = _data.value.copy(sweatDataLogDownloadCompleted = true)

                        if (isCurrentUserSession && currentHistoricalSweatDataDownloadIndex <= 0u) {
                            _data.value = _data.value.copy(historicalSweatDataUpToDate = true)
                        }

                        if (sweatDataAdded) {
                            createFileUpload()
                        }
                        //else {
                        //    Log.d("DUPLICATE TEST", "*** NO FILE UPLOAD ***")
                        //}
                        sweatDataAdded = false
                    }
                    // The sweat data log packet is still coming, append the data to the existing buffer
                    else if (!uartServiceData.sweatDataLogDownloadCompleted) {
                        var sweatDataString = ""

                        if (isCHArmBandConnected) {
                            // This is regular periodic sweat data
                            if (dataType == SweatLogDataType.DATA_SWEAT.eventType)
                            {
                                sweatDataString = "${timeStamp},${dataType},${gsrLargeSkinElectrodeSignalRawInMv},${gsrZeroDepthElectrodeSignalRawInMv},${localSweatChlorideLevel},${gsrWellSignalRawInMv},${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                            // This is intake event recorded from the app.
                            else if (dataType == SweatLogDataType.EVENT_HYDRATION_INTAKE.eventType)
                            {
                                sweatDataString = "${timeStamp},${dataType},${eventWaterIntakeInMl},${eventSodiumIntakeInMg},0,0,${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                            // This is GPS location event recorded from the app.
                            else if (dataType == SweatLogDataType.EVENT_GPS_LOCATION.eventType) {
                            }

                            // This is one of the events: nudge alert, dehydration alarm.
                            else if((dataType == SweatLogDataType.EVENT_DEHYDRATION_ALARM.eventType) || (dataType == SweatLogDataType.EVENT_NUDGE_ALERT.eventType)) {
                                sweatDataString =
                                    "${timeStamp},${dataType},${localSweatVolumeUl},${localSweatChlorideLevel},0,0,${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                            else if(dataType == SweatLogDataType.EVENT_FLUIDICS_NOT_CLIPPED.eventType) {
                                sweatDataString =
                                    "${timeStamp},${dataType},${localSweatVolumeUl},${eventFluidicsNotClippedStatus},0,0,${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                            else if(dataType == SweatLogDataType.EVENT_GSR_OFF_BODY.eventType) {
                                sweatDataString =
                                    "${timeStamp},${dataType},${localSweatVolumeUl},${eventGSROffBodyStatus},0,0,${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                            else if ((dataType == SweatLogDataType.EVENT_GSR_SWEAT_ONSET.eventType) || (dataType == SweatLogDataType.EVENT_GSR_SODIUM_READING_AVAILABLE.eventType) || (dataType == SweatLogDataType.EVENT_GSR_SODIUM_READING_MAX_UPDATE.eventType)) {
                                sweatDataString =
                                    "${timeStamp},${dataType},${localSweatVolumeUl},${eventGSROffBodyStatus},${eventGSRSweatOnsetChannelRawInMv},${eventGSRSweatFluidicsChannelRawInMv},${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                            // This is one of the events: seal break, plateau, saturation, persistent dropout or sweat rate update.
                            else if (dataType != 0xFF) {
                                sweatDataString =
                                    "${timeStamp},${dataType},${localSweatVolumeUl},${eventLocalSweatRate},${eventDurationForSweatRate},${eventLocalVolumeLossForSweatRateCalculation},${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                        }
                        else {
                            // This is regular periodic sweat data
                            if (dataType == SweatLogDataType.DATA_SWEAT.eventType) {
                                sweatDataString =
                                    "${timeStamp},${dataType},${localSweatVolumeUl},${localSweatChlorideLevel},${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${bodyTemperatureAirInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                            // This is intake event recorded from the app.
                            else if (dataType == SweatLogDataType.EVENT_HYDRATION_INTAKE.eventType) {
                                sweatDataString =
                                    "${timeStamp},${dataType},${eventWaterIntakeInMl},${eventSodiumIntakeInMg},${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${bodyTemperatureAirInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                            // This is GPS location event recorded from the app.
                            else if (dataType == SweatLogDataType.EVENT_GPS_LOCATION.eventType) {
                            }

                            // This is one of the events: nudge alert, dehydration alarm.
                            else if ((dataType == SweatLogDataType.EVENT_DEHYDRATION_ALARM.eventType) || (dataType == SweatLogDataType.EVENT_NUDGE_ALERT.eventType)) {
                                sweatDataString =
                                    "${timeStamp},${dataType},${localSweatVolumeUl},${localSweatChlorideLevel},${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${bodyTemperatureAirInC},${activityCounts},${batteryVoltageInMv}\n"

                            } else if (dataType == SweatLogDataType.EVENT_FLUIDICS_NOT_CLIPPED.eventType) {
                                sweatDataString =
                                    "${timeStamp},${dataType},${localSweatVolumeUl},${eventFluidicsNotClippedStatus},${sweatVolumeDeficitInMl},${sweatSodiumDeficitInMg},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${bodyTemperatureAirInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                            // This is one of the events: seal break, plateau, saturation, persistent dropout or sweat rate update.
                            else if (dataType != 0xFF) {
                                sweatDataString =
                                    "${timeStamp},${dataType},${localSweatVolumeUl},${eventLocalSweatRate},${eventDurationForSweatRate},${eventLocalVolumeLossForSweatRateCalculation},${sweatVolumeTotalLossInMl},${sweatSodiumTotalLossInMg},${currentTEWLToLog},${bodyTemperatureSkinInC},${bodyTemperatureAirInC},${activityCounts},${batteryVoltageInMv}\n"
                            }

                        }

//                        // Use current day sweat data to fill the chart data faster
//                        if (!uartServiceData.fileReadyUpload && isCurrentUserSession && currentHistoricalSweatDataDownloadIndex <= 0u) {
//                            val sweatVolumeTotalLossToDisplayInMl = sweatVolumeTotalLossInMl.toDouble() + (if (passiveWaterLoss) currentTEWLToLog.toDouble() else 0.0)
//                            val sweatVolumeTotalLossInOz = round(sweatVolumeTotalLossToDisplayInMl * 0.033814 * 10) / 10
//                            val sweatVolumeDeficitToDisplayInMl = sweatVolumeDeficitInMl.toDouble() + (if (passiveWaterLoss) currentTEWLToLog.toDouble() else 0.0)
//                            val sweatVolumeDeficitInOz = round(sweatVolumeDeficitToDisplayInMl * 0.033814 * 10) / 10
//                            val fluidTotalIntakeInOz = (eventWaterIntakeInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
//                            val sodiumTotalIntakeInMg = (sweatSodiumTotalLossInMg.toShort() - sweatSodiumDeficitInMg).toUShort()
//
//                            println(sweatVolumeTotalLossToDisplayInMl)
//                            println(timeStamp)
//
//                            initialSweatDataSetForPlot.add(HistoricalSweatDataPacket(timeStamp, sweatVolumeDeficitInOz, sweatSodiumDeficitInMg,
//                                sweatVolumeTotalLossInOz, sweatSodiumTotalLossInMg, fluidTotalIntakeInOz, sodiumTotalIntakeInMg,
//                                bodyTemperatureSkinInC, bodyTemperatureAirInC, activityCounts))
//                        }

                        if (checkForDuplicate(sweatDataString)) {
                            sweatDataLogCSVText += sweatDataString
                            sweatDataAdded = true

                            // Use current day sweat data to fill the chart data faster
                            if (!uartServiceData.fileReadyUpload && isCurrentUserSession && currentHistoricalSweatDataDownloadIndex <= 0u && !currentDayDownloadingCompletedFlagForHistoricalData) {
                                val sweatVolumeTotalLossToDisplayInMl = sweatVolumeTotalLossInMl.toDouble() + (if (passiveWaterLoss) currentTEWLToLog.toDouble() else 0.0)
                                val sweatVolumeTotalLossInOz = round(sweatVolumeTotalLossToDisplayInMl * 0.033814 * 10) / 10
                                val sweatVolumeDeficitToDisplayInMl = sweatVolumeDeficitInMl.toDouble() + (if (passiveWaterLoss) currentTEWLToLog.toDouble() else 0.0)
                                val sweatVolumeDeficitInOz = round(sweatVolumeDeficitToDisplayInMl * 0.033814 * 10) / 10
                                val fluidTotalIntakeInOz = (eventWaterIntakeInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
                                val sodiumTotalIntakeInMg = (sweatSodiumTotalLossInMg.toShort() - sweatSodiumDeficitInMg).toUShort()

                                initialSweatDataSetForPlot.add(HistoricalSweatDataPacket(timeStamp, sweatVolumeDeficitInOz, sweatSodiumDeficitInMg,
                                    sweatVolumeTotalLossInOz, sweatSodiumTotalLossInMg, fluidTotalIntakeInOz, sodiumTotalIntakeInMg,
                                    bodyTemperatureSkinInC, bodyTemperatureAirInC, activityCounts))
                            }

                        }
                        //else {
                        //    Log.d("DUPLICATE TEST", "*** FOUND DUPLICATE ***")
                        //}

                        //Log.d("SWEATDATAPACKET", "$sweatDataString")
                    }
                }
            }

            // This is logging session information packet
            0x52.toUByte() -> {
                sweatDataLogStartEpochTime = uartByteArray[2].toUInt() + (uartByteArray[3].toUInt() shl 8) + (uartByteArray[4].toUInt() shl 16) + (uartByteArray[5].toUInt() shl 24)

                // If recording start timestamp was not set, give the best estimate of 8am of previous day.
                if(sweatDataLogStartEpochTime == 0xFFFFFFFF.toUInt()) {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val todayStartOfDay = calendar.time
                    sweatDataLogStartEpochTime = ((todayStartOfDay.time / 1000) - (16 * 3600)).toUInt()
                }

                //_data.value = _data.value.copy(sweatDataLogDownloadCompleted = false)

                sweatDataLogStartEpochTimeString = generateTimeStampStringFromEpoch(sweatDataLogStartEpochTime)

                // Parse the reset error information if available
                if((uartByteArray.size > 6) && (uartByteArray.size <= 142)) {
                    sensorResetReason = uartByteArray[6].toUInt() + (uartByteArray[7].toUInt() shl 8) + (uartByteArray[8].toUInt() shl 16) + (uartByteArray[9].toUInt() shl 24)
                    sensorResetErrorId = uartByteArray[10].toUInt() + (uartByteArray[11].toUInt() shl 8) + (uartByteArray[12].toUInt() shl 16) + (uartByteArray[13].toUInt() shl 24)
                    sensorResetErrorCode = uartByteArray[14].toUInt() + (uartByteArray[15].toUInt() shl 8) + (uartByteArray[16].toUInt() shl 16) + (uartByteArray[17].toUInt() shl 24)
                    sensorResetLineNum = uartByteArray[18].toUInt() + (uartByteArray[19].toUInt() shl 8) + (uartByteArray[20].toUInt() shl 16) + (uartByteArray[21].toUInt() shl 24)

                    val resetErrorFileNameData = uartByteArray.sliceArray(22 until 142)

                    // Retrieve subject ID if it has been set in the sensor already
                    if (resetErrorFileNameData[0] != 0xFF.toUByte()) {
                        sensorResetErrorFileName = resetErrorFileNameData.toByteArray().toString(Charsets.UTF_8).trim { it.isLetterOrDigit()/* <= ' ' */}
                    }
                    else {
                        sensorResetErrorFileName = ""
                    }

                }

                else {

                    sensorResetReason = uartByteArray[6].toUInt() + (uartByteArray[7].toUInt() shl 8) + (uartByteArray[8].toUInt() shl 16) + (uartByteArray[9].toUInt() shl 24)
                    sensorResetErrorId = uartByteArray[10].toUInt() + (uartByteArray[11].toUInt() shl 8) + (uartByteArray[12].toUInt() shl 16) + (uartByteArray[13].toUInt() shl 24)
                    sensorResetErrorCode = uartByteArray[14].toUInt() + (uartByteArray[15].toUInt() shl 8) + (uartByteArray[16].toUInt() shl 16) + (uartByteArray[17].toUInt() shl 24)
                    sensorResetLineNum = uartByteArray[18].toUInt() + (uartByteArray[19].toUInt() shl 8) + (uartByteArray[20].toUInt() shl 16) + (uartByteArray[21].toUInt() shl 24)

                    val resetErrorFileNameData = uartByteArray.sliceArray(22 until 142)

                    // Retrieve subject ID if it has been set in the sensor already
                    sensorResetErrorFileName = if (resetErrorFileNameData[0] != 0xFF.toUByte()) {
                        resetErrorFileNameData.toByteArray().toString(Charsets.UTF_8).trim { it.isLetterOrDigit()/* <= ' ' */}
                    } else {
                        ""
                    }

                    // Set the individual session's user height/weight/gender to current user's information if session physiology info is not supported
                    if (uartByteArray.size <= 162) {
                        sweatDataLogSessionUserHeigthtInCm = subjectHeightInCm.toUByte()
                        sweatDataLogSessionUserWeightInKg = subjectWeightInKg.toUShort()
                        sweatDataLogSessionUserGender = subjectGender
                    }

                    // If included, retrieve user height/weight/gender informatoin from each individual session
                    else //if (byteArray.count > 162)
                    {
                        if(uartByteArray[162].toInt() != 0xFF) {
                            sweatDataLogSessionUserGender = if (uartByteArray[162].toInt() == 0) "M" else "F"
                            sweatDataLogSessionUserHeigthtInCm = uartByteArray[163]
                            sweatDataLogSessionUserWeightInKg = (uartByteArray[164].toInt() + (uartByteArray[165].toInt() shl 8)).toUShort()
                        }

                        // User information has not been set yet, use default values instead
                        else {
                            sweatDataLogSessionUserGender = "M"
                            sweatDataLogSessionUserHeigthtInCm = 175u
                            sweatDataLogSessionUserWeightInKg = 75u
                        }
                    }

                    // Retrieve the session user ID
                    var sessionUserIDData = uartByteArray.sliceArray(142 until 150)
                    if (sessionUserIDData[0] != 0xFF.toUByte()) {
                        sweatDataLogSessionUserID = sessionUserIDData.toByteArray().toString(Charsets.UTF_8)//.trim { it.isLetterOrDigit()/* <= ' ' */}
                    }

                    // Retrieve the session site ID
                    var sessionSiteIDData = uartByteArray.sliceArray(150 until 162)

                    if (sessionSiteIDData[0] != 0xFF.toUByte()) {

                        var numberOfZeroesAtTheEnd = 0

                        for (i in 0..sessionSiteIDData.size-1) {
                            if (sessionSiteIDData[i] == 0.toUByte()) {
                                numberOfZeroesAtTheEnd++
                            }
                        }

                        sweatDataLogSessionSiteID = sessionSiteIDData.sliceArray (0 until (sessionSiteIDData.size-numberOfZeroesAtTheEnd)).toByteArray().toString(Charsets.UTF_8)
                    }

                    //val shortUserId = userId.substring(0, 8)
                    //Log.d("******* SessionUserID", "$sweatDataLogSessionUserID")
                    //Log.d("******* SessionSiteID", "$sweatDataLogSessionSiteID")
                    //Log.d("******* userId", "$shortUserId")

                    val uartServiceData = data.value
                    if (uartServiceData.sweatDataLogDownloadCompleted) {
                        if (isDemoOnboardingFlow) {
                            _data.value = _data.value.copy(isAlreadyInSession = false)
                            isCurrentUserSession = true
                            isUserSessionToDisplay = true
                            //Log.d("*****isAlreadyInSession", "TRUE")
                        }

                        else if ((userId.substring(0, 8) != sweatDataLogSessionUserID) || (enterpriseId != sweatDataLogSessionSiteID)) {
                            _data.value = _data.value.copy(isAlreadyInSession = true)
                            isCurrentUserSession = false
                            isUserSessionToDisplay = false
                            //Log.d("*****isAlreadyInSession", "TRUE")
                        }

                        else {
                            _data.value = _data.value.copy(isAlreadyInSession = false)
                            isCurrentUserSession = true
                            isUserSessionToDisplay = true
                            //Log.d("****isAlreadyInSession", "FALSE")
                        }
                    }
                }

                _data.value = _data.value.copy(sweatDataLogDownloadCompleted = false)

                // Reset log duration counter when the download starts
                logDurationInSeconds = 0u

                //Log.d("LOGGING_PACKET", "$logDurationInSeconds")
            }

            // This is acknowledge response packet for user height/weight/gender update with the app
            0x55.toUByte() -> {
                _data.value = _data.value.copy(sweatUserInfoSetResponseReceived = true)
            }

            // This is acknowledge response packet for user intake event log with the app
            0x57.toUByte() -> {
                _data.value = _data.value.copy(intakeLogResponseReceived = true)
            }

            // This is acknowledge response packet for sensor name set/update with the engineering app
            // TODO: Might need to remove it since the sensor name should not be set/updated with the regular app
            0x59.toUByte() -> {
                _data.value = _data.value.copy(sweatSensorNameSetResponseReceived = true)
            }

            else -> {
                print("undefined")
            }
        }
    }

    fun getTimeIntervalSince1970(): UInt {
        val currentMoment: Instant = Clock.System.now()
        return (currentMoment.epochSeconds.toUInt())
    }

    fun onNewMessageSent(value: ByteArray) {
//        _data.value = _data.value.copy(messages = _data.value.messages + UARTRecord(value.toString(), UARTRecordType.INPUT))
    }

    fun sendText(cmd: ByteArray) {
        _command.tryEmit(cmd)
    }

    fun runMacro(macro: UARTMacro) {
        if (macro.command == null) {
            return
        }
//        _command.tryEmit(macro.command.parseWithNewLineChar(macro.newLineChar))
    }

    fun clearItems() {
//        _data.value = _data.value.copy(messages = emptyList())
    }

    fun openLogger() {
        logger?.launch()
    }

    fun log(priority: Int, message: String) {
        // NOTE: Commenting this out remove UART logging. Helps view local app logging
        //logger?.log(priority, message)
    }

    fun onMissingServices() {
        _data.value = _data.value.copy(missingServices = true)
        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    suspend fun saveConfigurationName(name: String) {
        configurationDataSource.saveConfigurationName(name)
    }

    fun disconnect() {
        // Clear all the flags for downloading and uploading upon disconnection.
        setSweatDataLogDownloadCompletedFlag(true)

        // Reset upload duplicates cache and upload all data after device disconnect
        initialSweatDataSetForPlot.clear()

        _stopEvent.tryEmit(DisconnectAndStopEvent())
    }

    private fun clean() {
        logger = null
        _data.value = UARTServiceData()
    }

    fun clearHistoricalSweatDataSetForPlot() {
        historicalSweatDataSetForPlot.clear()
        initialSweatDataSetForPlot.clear()
        currentHistoricalSweatDataDownloadIndex = 0u
        sweatDataLogStartEpochTime = 0u
    }

    fun setSweatDataLogDownloadCompletedFlag(flag: Boolean) {
        _data.value = _data.value.copy(sweatDataLogDownloadCompleted = flag)
    }

    fun setFileReadyUploadFlag(flag: Boolean) {
        _data.value = _data.value.copy(fileReadyUpload = flag)
    }

    fun resetSweatDataLogCSVText() {
        // Reset the data log file header to get ready for next upload.
        sweatDataLogCSVText = if (!isCHArmBandConnected) {
            "TimeStamp(s),Data Type,Sweat Volume Loss Local (uL),Sweat Sodium Level (mM),Recommended fluid intake (mL),Recommended sodium intake (mg),Total sweat loss (mL),Total sodium loss (mg),TEWL (mL), Body Temp (C),Ambient Temp (C),Activity Score,Batt Level(mV)\n"
        } else {
            "TimeStamp(s),Data Type,Large Skin Electrode Voltage (scaled mV),Zero Depth Electrode Voltage (mV),Sweat Sodium Level (mM),Well Voltage (scaled mV),Recommended fluid intake (mL),Recommended sodium intake (mg),Total sweat loss (mL),Total sodium loss (mg),TEWL (mL), Body Temp (C),Activity Score,Batt Level(mV)\n"
        }
    }

    private fun generateCurrentTimeStamp(): String {
        // Get the current instant
        val currentInstant = Clock.System.now()

        // Convert the instant to LocalDateTime in the system's default time zone
        val localDateTime = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())

        // Format the LocalDateTime in the desired format
        return "${localDateTime.date}T${localDateTime.time.toString().replace(":", "-")}"
    }

    private fun generateTimeStampStringFromEpoch(epochTime: UInt): String {
        // Convert epoch time to Instant
        val instant = Instant.fromEpochSeconds(epochTime.toLong())

        // Convert Instant to LocalDateTime in the system's default time zone
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        // Define the formatter for ISO 8601 format with time zone offset
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

        // Format the LocalDateTime with offset
        return localDateTime.toJavaLocalDateTime().atZone(TimeZone.currentSystemDefault().toJavaZoneId()).format(formatter)
    }

    fun getHistoricalSweatDataForPlot(): MutableList<HistoricalSweatDataPacket> {
        if (isCurrentUserSession && currentHistoricalSweatDataDownloadIndex <= 0u) {
            deduplicateAndSortByTimestamp(initialSweatDataSetForPlot)
            return initialSweatDataSetForPlot
        }
        else {
            initialSweatDataSetForPlot.clear()
            return historicalSweatDataSetForPlot
        }
    }

    private fun deduplicateAndSortByTimestamp(dataSet: MutableList<HistoricalSweatDataPacket>) {
        val seenTimestamps = mutableSetOf<UShort>()
        val deduplicated = dataSet.filter { seenTimestamps.add(it.timeStamp) }
        dataSet.clear()
        dataSet.addAll(deduplicated.sortedBy { it.timeStamp })
    }

    fun createFileUpload() {
        val v2FileFormatHeader = "CH3 DataFile Revision,,,,,,,,,,,\n" + (if (isCHArmBandConnected) "4" else "3") + ",,,,,,,,,,,\n" + ",,,,,,,,,,,\n"

        // Add the log start Epoch time at the end of the data file
        val startEpochTimeHeaderString = "\nSweat Log Start Epoch Time (s),Sweat Log Start Local Time,Log Duration (s)"
        val startEpochTimeString = "${sweatDataLogStartEpochTime},$sweatDataLogStartEpochTimeString,${logDurationInSeconds}"

        val sensorSubjectSiteIDHeaderString = "Sensor ID,User ID,Enterprise and Site Code"

        val sensorSubjectSiteIDString = "$deviceSN,$userId,$enterpriseId"

        val subjectLocationHeaderStringV2 = "Current location latitude,Current location longtitude,,,,,,,,,,\n"
        val subjectLocationStringV2 = "$latitude,$longitude,,,,,,,,,,\n"

        val subjectLocationHeaderString = "Location latitude,Location longtitude"
        val subjectLocationString = "$latitude,$longitude"

        val subjectPhysiologyDataHeaderString = "Clothing,Height (cm),Weight (kg),Biological Sex"
        val subjectClothingType = "N/A"
        //val subjectPhysiologyDataString = subjectClothingType + "," + "${uartServiceData.userInfo?.subjectHeightInCm}" + "," + "${uartServiceData.userInfo?.subjectWeightInKg}" + "," + uartServiceData.userInfo?.subjectGender
        val subjectPhysiologyDataString = "$subjectClothingType,$subjectHeightInCm,$subjectWeightInKg," + (if (subjectGender == "Male") "M" else "F")

        // Firmware and app version/build information
        val versionHeader = "Sensor hardware version,Sensor firmware version,App version,Phone model,Phone OS version"
        val sensorHardwareVersionString = "3"
        val sensorFirmwareVersionString = sysInfo?.fwRevisonString ?: "0"

        val appBuildVersionString = "v$appVersion build $buildNumber"
        val phoneModelString = Build.MANUFACTURER + Build.MODEL + Build.DEVICE
        val phoneOSString = Build.VERSION.SDK_INT
        val versionString = "$sensorHardwareVersionString,$sensorFirmwareVersionString,$appBuildVersionString,$phoneModelString,$phoneOSString"

        // Append sensor reset reason at the end if an unexpected reset occured during recording
        var sensorResetDebugHeader = ""
        var sensorResetDebugString = ""

        if (sensorResetReason != 0xFFFFFFFF.toUInt()) {
            sensorResetDebugHeader = "Sensor Reset Reason Code"
            sensorResetDebugString = "$sensorResetReason"

            if (sensorResetErrorId != 0xFFFFFFFF.toUInt()) {
                sensorResetDebugHeader += "," + "Reset Error ID"
                sensorResetDebugString += ",$sensorResetErrorId"

                if (sensorResetErrorCode != 0xFFFFFFFF.toUInt()) {
                    sensorResetDebugHeader += "," + "Reset Error Code" + "," + "Reset Line Number" + "," + "Reset File Name"
                    sensorResetDebugString += ",$sensorResetErrorCode,$sensorResetLineNum,$sensorResetErrorFileName"
                }
            }
        }

        val sweatLogMetaDataString = v2FileFormatHeader + subjectLocationHeaderStringV2 +
                subjectLocationStringV2 + ",,,,,,,,,,," + startEpochTimeHeaderString + "," + sensorSubjectSiteIDHeaderString + "," + subjectLocationHeaderString + "," + subjectPhysiologyDataHeaderString + "\n" +
                startEpochTimeString + "," + sensorSubjectSiteIDString + "," + subjectLocationString + "," + subjectPhysiologyDataString + "\n" +
                versionHeader + "," + sensorResetDebugHeader + "\n" +
                versionString + "," + sensorResetDebugString + "\n" + ",,,,,,,,,,,\n"

        // Remove new line at end of file
        val removeNewline = sweatDataLogCSVText.last()
        if (removeNewline == '\n') {
            sweatDataLogCSVText = sweatDataLogCSVText.dropLast(1)
        }

        // **** Handle file upload to server
        // On iOS BLEManager.bleSingleton.subjectID == ""
        val sweatDataLogFileIDString = /*BLEManager.bleSingleton.subjectID + "_" + */"_$deviceSN"
        sweatDataLogFileName =
            "sweatLog_" + sweatDataLogFileIDString + "_" + generateCurrentTimeStamp() + ".csv"
        val fullSweatDataLogCSVText = sweatLogMetaDataString + sweatDataLogCSVText

        // DEMO-DEMO mode support
        demoSweatDataLogCSVText = fullSweatDataLogCSVText

        resetSweatDataLogCSVText()

        File(applicationContext?.filesDir, sweatDataLogFileName).writeBytes(fullSweatDataLogCSVText.toByteArray())

        //Log.d("SWEATDATAPACKET", "timeStamp - $timeStamp")
        Log.d("fileReadyUpload", "fileReadyUpload - $sweatDataLogFileName")

        _data.value = _data.value.copy(fileReadyUpload = true)
    }

    //
    // Used for file upload file
    //

    fun setDeviceSN(device: String) {
        deviceSN = device
    }

    fun setLatitudeLongitude(lat: Double, long: Double) {
        if (lat != 0.0 && long != 0.0) {
            latitude = lat
            longitude = long
        }
    }

    fun setUserId(id: String) {
        userId = id
    }

    fun setPassiveWaterLoss(state: Boolean) {
        passiveWaterLoss = state
    }

    fun setUserInfoForCSVFile(gender: String, heightCm: Int, weightKg: Int) {
        subjectHeightInCm = heightCm
        subjectWeightInKg = weightKg
        subjectGender = gender

        setSubjectBSA(heightCm, weightKg)
    }

    fun setEnterpriseId(eid: String) {
        enterpriseId = eid
    }

    fun setAppVersion(version: String) {
        appVersion = version
    }

    fun setBuildNumber(number: String) {
        buildNumber = number
    }

    fun getSweatDataLogFileName(): String {
        return sweatDataLogFileName
    }

    fun getCurrHistoricalSweatDataDownloadIndex(): UShort {
        return currentHistoricalSweatDataDownloadIndex
    }

    fun getSweatStatusPacket(): SweatStatusPacket? {
        return sweatStatusPacket
    }

    fun getUserInfoFromDevice(): UserInfo? {
        return userInfo
    }

    fun getSysInfo(): SysInfoPacket? {
        return sysInfo
    }

    fun getDeviceName(): String? {
        return deviceName
    }

    fun getIsCHArmband(): Boolean {
        return isCHArmBandConnected
    }

    fun getSweatDataLogStartEpochTime(): UInt {
        return currentRecordingStartEpochTime
    }

    fun getSessionUserID(): String? {
        return sweatDataLogSessionUserID
    }

    fun getSessionSiteID(): String? {
        return sweatDataLogSessionSiteID
    }

    fun setSubjectBSA(heightCm: Int, weightKg: Int) {
        subjectBSAInM2 = 0.007184 * (weightKg.toDouble()).pow(0.425) * (heightCm.toDouble()).pow(0.725)
    }

    fun getCurrentTEWLInMl(duration: UShort): Int {
        return (0.00486526 * subjectBSAInM2 * (if (duration <= 900u) 0.0 else (duration.toDouble() - 900))).toInt()
    }

    fun getCurrentUserSession(): Boolean {
        return isCurrentUserSession
    }

    fun getDisplayUserSession(): Boolean {
        return isUserSessionToDisplay
    }

    fun getCurrentRecordingDuration(): UShort {
        return currentRecordingDuration
    }

    fun getAlertStatus(): UByte {
        return alertStatus
    }

    /**
     * Checks if the input text is a duplicate (by SHA-1 hash).
     * Returns true if it's a new entry (and stores it), or false if it's already seen.
     */
    fun checkForDuplicate(text: String): Boolean {
        val hashedKey = sha1Hash(text)

        return if (uploadDataDuplicateHashMap.containsKey(hashedKey)) {
            false  // Duplicate
        } else {
            uploadDataDuplicateHashMap[hashedKey] = true  // Store new hash
            true
        }
    }

    /**
     * Generates SHA-1 hash of a given string.
     */
    private fun sha1Hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val result = digest.digest(input.toByteArray())
        return result.joinToString("") { "%02x".format(it) }
    }

    /**
     * Clear the duplicate hash map to get ready for the next full upload.
     */
    fun clearUploadDataDuplicateHasMap() {
        uploadDataDuplicateHashMap.clear()
    }

    // Support for DEMO-DEMO uploads to datadog
    fun getDemoSweatDataLogCSVText(): String {
        return demoSweatDataLogCSVText
    }

    // Set/clear demo mode flag for skipping session information comparison for data file storage to Datadog log
    fun setDemoOnboardingFlow(mode: Boolean) {
        isDemoOnboardingFlow = mode
    }

    fun setCurrentDayDownloadingCompletedFlag(state: Boolean) {
        currentDayDownloadingCompletedFlagForHistoricalData = state
    }

}