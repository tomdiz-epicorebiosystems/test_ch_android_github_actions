package com.epicorebiosystems.rehydrate.modelData

import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datadog.android.rum.GlobalRumMonitor
import com.datadog.android.rum.RumErrorSource
import com.epicorebiosystems.rehydrate.BuildConfig
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.HistoricalSweatDataPacket
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.SweatStatusPacket
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.SysInfoPacket
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.UARTConfiguration
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.UARTMacro
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.UARTPersistentDataSource
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.UserInfo
import com.epicorebiosystems.rehydrate.nordicsemi.uart.repository.UARTRepository
import com.epicorebiosystems.rehydrate.nordicsemi.uart.repository.UART_SERVICE_UUID
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.ClearOutputItems
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.DisconnectEvent
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.MacroInputSwitchClick
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnAddConfiguration
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnConfigurationSelected
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnCreateMacro
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnDeleteConfiguration
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnDeleteMacro
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnEditConfiguration
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnEditFinish
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnEditMacro
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnRunInput
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnRunMacro
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OpenLogger
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.UARTViewEvent
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.UARTViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import no.nordicsemi.android.kotlin.ble.scanner.errors.ScanFailedError
import no.nordicsemi.android.kotlin.ble.scanner.errors.ScanningFailedException
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScanningState
import java.time.format.DateTimeFormatter
import javax.inject.Inject


private const val FILTER_RSSI = -50 // [dBm]

data class DevicesScanFilter(
    val filterUuidRequired: Boolean,
    val filterNearbyOnly: Boolean,
    val filterWithNames: Boolean
)

data class PairState(
    val showLoading: Boolean = false,
    val showErrorMsg: Boolean = false,
    var pairedMoveView: Boolean = false
)

@HiltViewModel
class EBSDeviceMonitor @Inject constructor(
//    @ApplicationContext
//    private val context: Context,
    private val uartRepository: UARTRepository,
    private val scannerRepository: ScannerRepositoryCustomized,
    private val dataSource: UARTPersistentDataSource,
) : ViewModel() {

    private var uuid: ParcelUuid? = ParcelUuid(UART_SERVICE_UUID)

    lateinit var chViewModel: ModelData

    private val _uartState = MutableStateFlow(UARTViewState())
    val uartState = _uartState.asStateFlow()

    private var sweatDataLogStartEpochTime: UInt = 0u

    private val filterConfig = MutableStateFlow(
        DevicesScanFilter(
            filterUuidRequired = true,
            filterNearbyOnly = false,
            filterWithNames = true
        )
    )

    private var currentJob: Job? = null

    private val _scanState = MutableStateFlow<ScanningState>(ScanningState.Loading)
    val scanState = _scanState.asStateFlow()

    var chDevicesFound = 0  // 0 - no devices found,  > 0 number devices found, -1 scan error
    var scanErrString = ""

    init {
        uartRepository.setOnScreen(true)

        viewModelScope.launch {
            if (uartRepository.isRunning.firstOrNull() == false) {
                scanBluetoothDevice()
            }
        }

        uartRepository.data.onEach {
            _uartState.value = _uartState.value.copy(uartManagerState = it)
        }.launchIn(viewModelScope)

        dataSource.getConfigurations().onEach {
            _uartState.value = _uartState.value.copy(configurations = it)
        }.launchIn(viewModelScope)

        uartRepository.lastConfigurationName.onEach {
            it?.let {
                _uartState.value = _uartState.value.copy(selectedConfigurationName = it)
            }
        }.launchIn(viewModelScope)
    }

    fun scanBluetoothDevice() {
        launchScanning()
    }

    fun onDeviceSelected(device: ServerDevice) {
        //Log.d("onDeviceSelected", device.name.toString())
        uartRepository.launch(device)
    }

    fun setButtonPressWaterIntakeVolumeInMl() {
        if (chViewModel.isSensorConnected.value) {
            val volumeToSetInMl: UShort = if (chViewModel.buttonPressWaterIntakeState.value) chViewModel.buttonPressWaterIntakeVolumeInMl.value.toUShort() else 0u

            val waterVolumeToSetData: ByteArray = volumeToSetInMl.toLittleEndianByteArray()
            val setButtonPressWaterIntakeVolumeCmd: ByteArray = byteArrayOf(0x4F).plus(waterVolumeToSetData)
            onEvent(OnRunInput(setButtonPressWaterIntakeVolumeCmd))
        }
    }

    fun UShort.toLittleEndianByteArray(): ByteArray {
        return ByteArray(2) { i -> ((this.toInt() shr (i * 8)) and 0xFF).toByte() }
    }

    fun setSweatSensingStartTimestamp() {
        val currentMoment: Instant = Clock.System.now()
        val currentEpochTimeBytes = (currentMoment.epochSeconds.toInt()).toByteArray()

        Log.d("SensingStartTimestamp", "${currentMoment.epochSeconds.toInt()}")

        // Add user ID and site ID here
        val userIDShort = if(chViewModel.currentAuthUserId.value.length < 8) "        " else chViewModel.currentAuthUserId.value.substring(0, 8)
        val userIDData = userIDShort.toByteArray()

        val siteIDDataLength = chViewModel.enterpriseId.value.length
        val paddedZeroArray: ByteArray = ByteArray(12-siteIDDataLength) {0x0}
        val siteIDData = chViewModel.enterpriseId.value.toByteArray().plus(paddedZeroArray)

        val setTimeStampCommandBytes: ByteArray = byteArrayOf(0x54).plus(currentEpochTimeBytes).plus(userIDData).plus(siteIDData)
        onEvent(OnRunInput(setTimeStampCommandBytes))
    }

    fun generateCurrentTimeStamp(): String {
        // Get the current time as an Instant
        val currentInstant = Clock.System.now()

        // Convert to LocalDateTime in the system's default time zone
        val localDateTime = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())

        // Define the formatter for ISO 8601 with dashes in the time
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss")

        // Format and return as a string
        return localDateTime.toJavaLocalDateTime().format(formatter)
    }

    fun onEvent(event: UARTViewEvent) {
        //Log.d("ONEVENT", "**** ${event} ****")
        when (event) {
            is OnCreateMacro -> addNewMacro(event.macro)
            OnDeleteMacro -> deleteMacro()
            DisconnectEvent -> disconnect()
            is OnRunMacro -> runMacro(event.macro)
            is OnEditMacro -> onEditMacro(event)
            OnEditFinish -> onEditFinish()
            is OnConfigurationSelected -> onConfigurationSelected(event)
            is OnAddConfiguration -> onAddConfiguration(event)
            OnDeleteConfiguration -> deleteConfiguration()
            OnEditConfiguration -> onEditConfiguration()
            ClearOutputItems -> uartRepository.clearItems()
            OpenLogger -> uartRepository.openLogger()
            is OnRunInput -> sendText(event.cmd)
            MacroInputSwitchClick -> onMacroInputSwitch()
            else -> {}
        }
    }

    private fun runMacro(macro: UARTMacro) {
        uartRepository.runMacro(macro)
    }

    private fun sendText(cmd: ByteArray) {
        uartRepository.sendText(cmd)
    }

    private fun onMacroInputSwitch() {
        _uartState.value = _uartState.value.copy(isInputVisible = !uartState.value.isInputVisible)
    }

    private fun onEditConfiguration() {
        val isEdited = _uartState.value.isConfigurationEdited
        _uartState.value = _uartState.value.copy(isConfigurationEdited = !isEdited)
    }

    private fun onAddConfiguration(event: OnAddConfiguration) {
        viewModelScope.launch(Dispatchers.IO) {
            dataSource.saveConfiguration(UARTConfiguration(null, event.name))
            _uartState.value = _uartState.value.copy(selectedConfigurationName = event.name)
        }
        saveLastConfigurationName(event.name)
    }

    private fun onEditMacro(event: OnEditMacro) {
        _uartState.value = _uartState.value.copy(editedPosition = event.position)
    }

    private fun onEditFinish() {
        _uartState.value = _uartState.value.copy(editedPosition = null)
    }

    private fun onConfigurationSelected(event: OnConfigurationSelected) {
        saveLastConfigurationName(event.configuration.name)
    }

    private fun saveLastConfigurationName(name: String) {
        viewModelScope.launch {
            uartRepository.saveConfigurationName(name)
        }
    }

    private fun addNewMacro(macro: UARTMacro) {
        viewModelScope.launch(Dispatchers.IO) {
            _uartState.value.selectedConfiguration?.let {
                val macros = it.macros.toMutableList().apply {
                    set(_uartState.value.editedPosition!!, macro)
                }
                val newConf = it.copy(macros = macros)
                dataSource.saveConfiguration(newConf)
                _uartState.value = _uartState.value.copy(editedPosition = null)
            }
        }
    }

    private fun deleteConfiguration() {
        viewModelScope.launch(Dispatchers.IO) {
            _uartState.value.selectedConfiguration?.let {
                dataSource.deleteConfiguration(it)
            }
        }
    }

    private fun deleteMacro() {
        viewModelScope.launch(Dispatchers.IO) {
            _uartState.value.selectedConfiguration?.let {
                val macros = it.macros.toMutableList().apply {
                    set(_uartState.value.editedPosition!!, null)
                }
                val newConf = it.copy(macros = macros)
                dataSource.saveConfiguration(newConf)
                _uartState.value = _uartState.value.copy(editedPosition = null)
            }
        }
    }

    fun disconnect() {
        // Clear all the flags for downloading and uploading upon disconnection.
        chViewModel.sweatDataCurrentDayDownloadingCompleted = true
        setCurrentDayDownloadingCompletedFlagForHistoricalData(true)

        chViewModel.sweatDataMultiDaySyncWithSensorCompleted = true
        chViewModel.historicalSweatDataDownloadCompleted = true
        chViewModel.setCsvFileIsUploading(false)

        clearDuplicateHash()

        setFileReadyUploadFlag(false)
        resetSweatDataLogCSVText()

        uartRepository.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        uartRepository.setOnScreen(false)
    }

    fun stopScanningJob() {
        currentJob?.cancel()
    }

    // BLE scanning code
    private fun launchScanning() {

        if (currentJob != null && !currentJob!!.isCancelled) {
            currentJob?.cancel()
        }

//        ScanningState.DevicesDiscovered(emptyList())

        val aggregator = BleScanResultAggregator()
        currentJob = scannerRepository.getScannerState()
            .map { aggregator.aggregate(it) }
            .filter { it.isNotEmpty() }
            .combine(filterConfig) { result, config ->
                result.applyFilters(config)
            }
            .onStart { _scanState.value = ScanningState.Loading }
            .cancellable()
            .onEach {
                _scanState.value = ScanningState.DevicesDiscovered(it)
                //Log.d("EBSDEVICE FOUND", "${it.size}")
                //Log.d("EBSDEVICE CONN", "${chViewModel.isCHDeviceConnected}")
                if (it.isNotEmpty()) {
                    chDevicesFound = it.size
                    for (bleDevice in it) {
//                        Log.d("EBSDEVICE NAME", "${bleDevice.device.name}, ${chDevicesFound}")
                        if (chViewModel.deviceSN.value == bleDevice.device.name.toString()) {
                            chViewModel.deviceRSSI = bleDevice.highestRssi
                            setDeviceSN(chViewModel.deviceSN.value)
                            if (!chViewModel.isSensorConnected.value) {
                                //Log.d("EBSDEVICE CONNECT", "connecting...")
                                GlobalRumMonitor.get().addError("EBSDEVICE CONNECT", RumErrorSource.LOGGER, null, mapOf("deviceSN" to "$chViewModel.deviceSN.value"))
                                onDeviceSelected(bleDevice.device)
                                chViewModel.updateCHDeviceName(chViewModel.deviceSN.value)
                                chViewModel._isSensorConnected.value = true
                                // If disconnects will be set to false again
                                delay(3000)  // the delay of 3 seconds

                                // Get system and user information command - to get firmware version for onboarding flow (isCHArmband)
                                val getSystemAndUserInfoCommandBytes : ByteArray = byteArrayOf(0x50)
                                onEvent(OnRunInput(getSystemAndUserInfoCommandBytes))

                                chViewModel._qrPairingState.value =
                                    chViewModel._qrPairingState.value.copy(pairedMoveView = true)
                                break
                            }
                        }
                    }
                } else {
                    // No devices found scanning
                    chDevicesFound = 0
                }
            }
            .catch { e ->
                _scanState.value = (e as? ScanningFailedException)?.let {
                    //Log.d("EBSDEVICE SCAN ERROR", "${it.errorCode.value}")
                    GlobalRumMonitor.get().addError("EBSDEVICE SCAN ERROR", RumErrorSource.LOGGER, null, mapOf("deviceSN" to "${it.errorCode.value}"))
                    ScanningState.Error(it.errorCode.value)
                } ?: ScanningState.Error(ScanFailedError.UNKNOWN.value)
                chDevicesFound = -1 // device scan error
                scanErrString = "Bluetooth error code = ${_scanState.value}"
                chViewModel._qrPairingState.value =
                    chViewModel._qrPairingState.value.copy(showErrorMsg = true)
            }
            .launchIn(viewModelScope)
    }

    // This can't be observed in View Model Scope, as it can exist even when the
    // scanner is not visible. Scanner state stops scanning when it is not observed.
    // .stateIn(viewModelScope, SharingStarted.Lazily, ScanningState.Loading)
    private fun List<BleScanResults>.applyFilters(config: DevicesScanFilter) =
        filter {
            !config.filterUuidRequired || it.lastScanResult?.scanRecord?.serviceUuids?.contains(
                uuid
            ) == true
        }
            .filter { !config.filterNearbyOnly || it.highestRssi >= FILTER_RSSI }
            .filter { !config.filterWithNames || it.device.hasName }

    fun setFilterUuid(uuid: ParcelUuid?) {
        this.uuid = uuid
        if (uuid == null) {
            filterConfig.value = filterConfig.value.copy(filterUuidRequired = false)
        }
    }

    fun setFilter(config: DevicesScanFilter) {
        this.filterConfig.value = config
    }

    fun refresh() {
        launchScanning()
    }

    fun scanDeviceCurrentDayData(uartStateMgr: UARTViewState) { //, chViewModel: ModelData) {
        //Log.d("scanPreviousDayDeviceData", "scanPreviousDayDeviceData() called")

        chViewModel.sweatDataCurrentDayDownloadingCompleted = false
        setCurrentDayDownloadingCompletedFlagForHistoricalData(false)

        chViewModel.sweatDataMultiDaySyncWithSensorCompleted = false

        // Already downloading
        if (!uartStateMgr.uartManagerState.sweatDataLogDownloadCompleted) {
            //Log.d("scanDeviceData", "sweatDataLogDownloadCompleted is false")
            return
        }

        // This would start the multi-day data sync between sensor, app and cloud, start with current day's data.
        if (!chViewModel._isSensorConnected.value) {
            setFileReadyUploadFlag(false)
            chViewModel.sweatDataCurrentDayDownloadingCompleted = true
            setCurrentDayDownloadingCompletedFlagForHistoricalData(true)

            chViewModel.setCsvFileIsUploading(false)
            return
        }

        // Set the header of data file based on type of module connected and get ready for data download.
        resetSweatDataLogCSVText()

        val scanCurrentDayDeviceDataCommandBytes: ByteArray = byteArrayOf(0x52, 0x00.toByte())
        onEvent(OnRunInput(scanCurrentDayDeviceDataCommandBytes))
    }

    fun scanDevicePreviousDayData(uartStateMgr: UARTViewState) {
        //Log.d("SYNCING", "scanDevicePreviousDayData() called")

        if (!uartStateMgr.uartManagerState.sweatDataLogDownloadCompleted) {
            //Log.d("SYNCING", "sweatDataLogDownloadCompleted is false")
            return
        }

        if (uartStateMgr.uartManagerState.missingServices) {
            //Log.d("SYNCING", "scanDeviceCurrentDayData_sensor_not_connected")
            return
        }

        if (!chViewModel._isSensorConnected.value) {
            //Log.d("scanDeviceData", "scandevicedata_sensor_not_connected")

//            uartStateMgr.uartManagerState.fileReadyUpload = false
            setFileReadyUploadFlag(false)

            chViewModel.sweatDataMultiDaySyncWithSensorCompleted = true
            chViewModel.setCsvFileIsUploading(false)

            return
        }

        val scanPreviousDayDeviceDataCommandBytes: ByteArray = byteArrayOf(0x52, 0xA5.toByte())
        onEvent(OnRunInput(scanPreviousDayDeviceDataCommandBytes))
    }

    //
    // Used for file upload file
    //

    fun setApplicationContext(context: Context) {
        uartRepository.applicationContext = context
    }

    fun setDeviceSN(deviceSN: String) {
        uartRepository.setDeviceSN(deviceSN)
    }

    fun setLatitudeLongitude(lat: Double, long: Double) {
        uartRepository.setLatitudeLongitude(lat, long)
    }

    fun setUserId(id: String) {
        uartRepository.setUserId(id)
    }

    fun setPassiveWaterLoss(state: Boolean) {
        uartRepository.setPassiveWaterLoss(state)
    }

    fun setUserInfoForCSVFile(gender: String, heightCm: Int, weightKg: Int) {
        uartRepository.setUserInfoForCSVFile(gender, heightCm, weightKg)
    }

    fun setEnterpriseId(eid: String) {
        uartRepository.setEnterpriseId(eid)
    }

    fun setAppVersion(version: String) {
        uartRepository.setAppVersion(version)
    }

    fun setBuildNumber(number: String) {
        uartRepository.setBuildNumber(number)
    }

    fun setSweatDataLogDownloadCompletedFlag(flag: Boolean) {
        uartRepository.setSweatDataLogDownloadCompletedFlag(flag)
    }

    fun setFileReadyUploadFlag(flag: Boolean) {
        uartRepository.setFileReadyUploadFlag(flag)
    }

    fun resetSweatDataLogCSVText() {
        uartRepository.resetSweatDataLogCSVText()
    }

    fun getSweatDataLogFileName(): String {
        return uartRepository.getSweatDataLogFileName()
    }

    fun getHistoricalSweatDataForPlot(): MutableList<HistoricalSweatDataPacket> {
        return uartRepository.getHistoricalSweatDataForPlot()
    }

    fun getCurrentHistoricalSweatDataDownloadIndex(): UShort {
        return uartRepository.getCurrHistoricalSweatDataDownloadIndex()
    }

    fun getSweatStatusPacket(): SweatStatusPacket? {
        return uartRepository.getSweatStatusPacket()
    }

    fun getUserInfoFromDevice(): UserInfo? {
        return uartRepository.getUserInfoFromDevice()
    }

    fun getSysInfo(): SysInfoPacket? {
        return uartRepository.getSysInfo()
    }

    fun getDeviceName(): String? {
        return (if (chViewModel.isTestAccount()) "CHTEST00" else uartRepository.getDeviceName())
    }

    fun clearHistoricalDataSet() {
        sweatDataLogStartEpochTime = 0u
        uartRepository.clearHistoricalSweatDataSetForPlot()
    }

    fun createFileUpload() {
        uartRepository.createFileUpload()
    }

    fun getSweatDataLogStartEpochTime(): UInt {
        return uartRepository.getSweatDataLogStartEpochTime()
    }

    fun getIsCHArmband(): Boolean {
        if (BuildConfig.QA_TESTING) {
            // This is for QA testing specific onboarding routes
            if (chViewModel.deviceSN.value == "CHTEST01") {
                return true
            } else if (chViewModel.deviceSN.value == "CHTEST00") {
                return false
            }
        }

        return uartRepository.getIsCHArmband()
    }

    fun getSessionUserID(): String? {
        return uartRepository.getSessionUserID()
    }

    fun getSessionSiteID(): String? {
       return uartRepository.getSessionSiteID()
    }

    fun getCurrentUserSession(): Boolean {
        return uartRepository.getCurrentUserSession()
    }

    fun getDisplayUserSession(): Boolean {
        return uartRepository.getDisplayUserSession()
    }

    fun getCurrentRecordingDuration(): UShort {
        return uartRepository.getCurrentRecordingDuration()
    }

    fun getAlertStatus(): UByte {
        return uartRepository.getAlertStatus()
    }

    fun Int.toByteArray(): ByteArray {
        return byteArrayOf(
            (this and 0xFF).toByte(),
            ((this shr 8) and 0xFF).toByte(),
            ((this shr 16) and 0xFF).toByte(),
            ((this shr 24) and 0xFF).toByte()
        )
    }

    fun UShort.toByteArray(): ByteArray {
        return byteArrayOf(
            (this.toUInt() and 255u).toByte(),
            (this.toUInt() shr 8).toByte()
        )
    }

    fun clearDuplicateHash() {
        uartRepository.clearUploadDataDuplicateHasMap()
    }

    fun getDemoSweatDataLogCSVText(): String {
        return uartRepository.getDemoSweatDataLogCSVText()
    }

    fun setDemoOnboardingFlow(mode: Boolean) {
        uartRepository.setDemoOnboardingFlow(mode)
    }

    fun setCurrentDayDownloadingCompletedFlagForHistoricalData(state: Boolean) {
        uartRepository.setCurrentDayDownloadingCompletedFlag(state)
    }

}
