package com.epicorebiosystems.rehydrate.modelData

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.epicorebiosystems.rehydrate.appInjectionSupport.EpicoreCHViewState
import com.epicorebiosystems.rehydrate.fileHandler.FileManager
import com.epicorebiosystems.rehydrate.networkManager.AvgSweatVolumeSodiumConcentration
import com.epicorebiosystems.rehydrate.networkManager.DayIntakeLossData
import com.epicorebiosystems.rehydrate.networkManager.NetworkManager
import com.epicorebiosystems.rehydrate.networkManager.UserHistoryStats
import com.epicorebiosystems.rehydrate.topBarViews.InfoScreens
import com.epicorebiosystems.rehydrate.topBarViews.NotificationData
import com.epicorebiosystems.rehydrate.topBarViews.NotificationLocation
import com.epicorebiosystems.rehydrate.topBarViews.NotificationShowOptions
import com.epicorebiosystems.rehydrate.topBarViews.NotificationType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.Locale


class ModelData : ViewModel() {

    private val _state = MutableStateFlow(EpicoreCHViewState())
    val state = _state.asStateFlow()

    val initialPreviewBottleName = "Preview Icon - Can Water - 9"

    var applicationContext: Context? = null

    val networkManager = NetworkManager(chViewModel = this)
    val fileManager = FileManager(chViewModel = this)

    private lateinit var bottles: ArrayList<BottleData>
    lateinit var bottleList: ArrayList<BottleData>
    private lateinit var searchBottleList: List<BottleData>
    lateinit var bottlePreviewIcons: ArrayList<BottleData>

    // Preferences data store vars
    lateinit var dataStore: PreferencesDataStore

    // Bottle list searches
    private var bottleListSearchQuery by mutableStateOf("")
    private lateinit var bottlesFlow: Flow<List<BottleData>>
    lateinit var bottleListSearchResults: StateFlow<List<BottleData>>

    // Users Lat/Long for CSV file upload
    var currLatLong: LatAndLong? = null

    // Imperial/metric conversion class
    var userPrefsData = UserPrefsData()

    // Application in foreground(true)/background(false)
    var applicationInForeground = false
    var applicationSwitchToForeground = false

    // Used for Manual Bottle Entry
    var waterAmountEnterManual = "0"
    var sodiumAmountEnterManual = "0"
    var manualUserBottle = BottleData(id = 0, name = "", image_name = initialPreviewBottleName, barcode = "", sodiumAmount = 0F, sodiumSize = "mg", waterAmount = 0F, waterSize = "oz")

    // Total water/sodium intake
    var totalWaterAmount: Double = 0.0
    var totalSodiumAmount: Double = 0.0

    // Array of the user's bottle intake items.
    internal val _updateCurrentUserIntakeItems = MutableStateFlow(false)
    var updateCurrentUserIntakeItems: StateFlow<Boolean> = _updateCurrentUserIntakeItems
    var currentUserIntakeItems =  mutableListOf<BottleData>()

    // Connectivity states
    private val _csvFileIsUploading = MutableStateFlow(false)
    var csvFileIsUploading: StateFlow<Boolean> = _csvFileIsUploading

    var isNetworkConnected = false
    var isCHDeviceConnected = false

    val _isSensorConnected = MutableStateFlow(false)
    var isSensorConnected: StateFlow<Boolean> = _isSensorConnected

    val _isSaveButtonShowing = MutableStateFlow(false)
    var isSaveButtonShowing: StateFlow<Boolean> = _isSaveButtonShowing

    //val _updateUserInfoFromDevice = MutableStateFlow(false)
    //var updateUserInfoFromDevice: StateFlow<Boolean> = _updateUserInfoFromDevice

    var onboardingComplete = MutableStateFlow( false)

    val _isSweatTimerLollipopClose = MutableStateFlow(false)
    var isSweatTimerLollipopClose: StateFlow<Boolean> = _isSweatTimerLollipopClose

    val _isActivityTimerLollipopClose = MutableStateFlow(false)
    var isActivityTimerLollipopClose: StateFlow<Boolean> = _isActivityTimerLollipopClose

    val _isSkinTempTimerLollipopClose = MutableStateFlow(false)
    var isSkinTempTimerLollipopClose: StateFlow<Boolean> = _isSkinTempTimerLollipopClose

    var isTabButtonPressed = MutableStateFlow( false)

    // Notification system
    var showNotification = MutableStateFlow( false)
    var notificationData = NotificationData(id = "empty_notification", title = "Notification Title", detail = "Notification detail text for the user.", type = NotificationType.Error, notificationLocation = NotificationLocation.Middle, showOnce = true, showSeconds = NotificationShowOptions.showClose, appUrl = null)
    var notificationStateString = mutableStateOf("")

    var showAppUpdateAvailable = false  // used for app updates available

    var currentUnits = mutableStateOf(1)
    var updateSecondTime = 0

    // Used for new bottle intake border so users know new item
    val newBottlesItemsAdded = mutableListOf<Int>()

    // Data Sharing switch
    var switchShareAnonymousDataEnterprise = true
    var switchShareAnonymousDataEpicore = true

    // Array of the user's bottle menu items. Loaded from JSON data stored in userBottleMenuItems when application starts
    var currentBottleMenuItems =  mutableListOf<BottleData>()

    var onboardingStep = 1       // Used for Initial Setup view (5 Steps)

    var userExists = false
    var userExistsKeystore = false

    var continueWithOnboarding = true

    var updateUserSuccess = false
    var isUpdateUserCalled = false

    var isCreateAccountFlow = mutableStateOf( false)

    // Loaded from PreferencesDataStore
    var serverSettings = mutableStateOf(0)      // 1 is staging, 0 is production

    var usersEmailAddress = mutableStateOf("")  // epicoretest004@gmail.com

    var enterpriseId = mutableStateOf("")       // EBS-TES1

    var onboardingEnterpriseId = mutableStateOf("")
    var onboardingEnterpriseName = mutableStateOf("")
    var onboardingSiteName = mutableStateOf("")

    var CH_EnterpriseName = mutableStateOf("")
    var CH_SiteName = mutableStateOf("")
    var jwtEnterpriseID = mutableStateOf("")    // EBS
    var jwtSiteID = mutableStateOf("")          // TES1
    var deviceSN = mutableStateOf("")           // CHTEST00
    var CH_UserRole = mutableStateOf("")

    var currentAuthAPIServer = mutableStateOf(0)      // Last user Auth info
    var currentAuthUserId = mutableStateOf("")
    var currentAuthUserEmail = mutableStateOf("")
    var currentAuthUserRole = mutableStateOf("")

    var userWeightLb = mutableStateOf("165")
    var userWeightKg = mutableStateOf("75")
    var userHeightFt = mutableStateOf("5")
    var userHeightIn = mutableStateOf("9")
    var userHeightCm = mutableStateOf("175")
    var userGender = mutableStateOf("Male")

    var onboardingWeightLb = mutableStateOf("165")
    var onboardingWeightKg = mutableStateOf("75")
    var onboardingHeightFt = mutableStateOf("5")
    var onboardingHeightIn = mutableStateOf("9")
    var onboardingHeightCm = mutableStateOf("175")
    var onboardingGender = mutableStateOf("Male")

    // Module button water intake
    var buttonPressWaterIntakeVolumeInMl = mutableStateOf(500)
    var buttonPressWaterIntakeState = mutableStateOf(true)

    // Passive water loss state
    var userPassiveLossState = mutableStateOf(true)

    var oldUserHeightFt = ""
    var oldUserHeightIn = ""
    var oldUserHeightCm = ""
    var oldUserWeightLb = ""
    var oldUserWeightKg = ""
    var oldUserGender = ""

    var userHistoryStats: UserHistoryStats? = null
    var userAvgSweatSodiumConcentration: AvgSweatVolumeSodiumConcentration? = null

    var isSweatDataDownloadProgressAlertShowing = false

//    var sweatDataPreviousDayDownloadingCompleted = mutableStateOf(true)
//    var sweatDataMultiDaySyncWithSensorCompleted = mutableStateOf(true)
//    var historicalSweatDataDownloadCompleted = mutableStateOf(true)

    var sweatDataCurrentDayDownloadingCompleted = true
    var sweatDataMultiDaySyncWithSensorCompleted = true
    var historicalSweatDataDownloadCompleted = true

    var isCurrentUserSession = true
    var isUserSessionToDisplay = false

//    var timeToSyncHistoricalData = mutableStateOf(false)
    var timeToSyncHistoricalData = false

    // Last sync time for connectivity view
    var syncDate: Date? = null
    var syncFileTime = 0L
    var updateDate: Date? = null

    var initDeviceOnce = mutableStateOf(false)

    var networkUploadSuccess = mutableStateOf(false)
    var networkUploadFailed = mutableStateOf(false)
    var networkUploadFailedMsg = ""

    var userTotalBottleMenuItems = mutableStateOf(0)   // Max is 25 local bottles (max not implemented yet). Increment when new one added

    var lastCheckAppUpdate = mutableStateOf(0)

    var currentBottleCounts = mutableMapOf<Int, String>()
    var currentBottleListSelections = mutableMapOf<Int, String>()

    var newUserBottle = BottleData(id = 0, name = "", image_name = initialPreviewBottleName, barcode = "", sodiumAmount = 0F, sodiumSize = "mg", waterAmount = 0F, waterSize = "oz")

    // Write this out in @Composable after add
    var userBottleMenuItems = String() // String to read/write JSON of users Bottle Menu Items

    // Used for new bottle intake border so users know new item
    var newBottlesAdded = ArrayList<Int>()

    var infoPopupScreen: InfoScreens = InfoScreens.APP_OVERVIEW

    // Used for Android keystore - access and refresh tokens
    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    lateinit var encryptedPreferences: EncryptedSharedPreferences

    val _qrPairingState = MutableStateFlow(PairState())
    var qrPairingState: StateFlow<PairState> = _qrPairingState

    // ok = 0
    // At Risk = 1
    // Dehydrated = 2
    internal val _sweatDashboardViewStatus = MutableStateFlow(0)
    var sweatDashboardViewStatus: StateFlow<Int> = _sweatDashboardViewStatus

    var deviceRSSI = 0

    // Used by History view - chart data
    var weeklyOrMonthlyDataArray = mutableListOf<DayIntakeLossData>()

    // Sweat status packet
    var fluidDeficitInOz: Double = 0.0
    var sweatSodiumDeficitInMg: Short = 0
    var sweatVolumeDeficitInMl : Short = 0
    var sweatVolumeTotalLossInMl : UShort = 0u
    var sweatSodiumTotalLossInMg : UShort = 0u
    var fluidTotalIntakeInMl : UShort = 0u
    var sodiumTotalIntakeInMg : UShort = 0u
    var averageSkinTempInF: Double = 0.0
    var currentTEWLInMl: Int = 0

    var capSodiumValue = mutableStateOf(0)

    var scrollSettingsView = false

    init {
        Log.d("ModelData", "init() called")
        userPrefsData.chViewModel = this
    }

    // Loading of predefined Bottles JSON
    fun initializeModelData() {
        bottles = loadJSON(applicationContext!!, "drinks.json")
        if (getCurrentLocale() == "ja_JP") {
            bottleList = loadJSON(applicationContext!!, "preset_bottle_list_jap.json")
            searchBottleList = loadJSON(applicationContext!!, "preset_bottle_list_jap.json")
        }
        else {
            bottleList = loadJSON(applicationContext!!, "preset_bottle_list.json")
            searchBottleList = loadJSON(applicationContext!!, "preset_bottle_list.json")
        }
        bottlePreviewIcons = loadJSON(applicationContext!!, "preview_bottles.json")

        // Set up search bottle list
        bottlesFlow = flowOf(searchBottleList)
        bottleListSearchResults =
            snapshotFlow { bottleListSearchQuery }
                .combine(bottlesFlow) { searchQuery, bottles ->
                    when {
                        searchQuery.isNotEmpty() -> bottles.filter { bottle ->
                            bottle.name.contains(searchQuery, ignoreCase = true)
                        }

                        else -> bottles
                    }
                }.stateIn(
                    scope = viewModelScope,
                    initialValue = emptyList(),
                    started = SharingStarted.WhileSubscribed(5_000)
                )

        currentBottleMenuItems = readJSONfromFile("users_bottle_list.json").toMutableList()

        dataStore = PreferencesDataStore(applicationContext!!)

        // Initialize Android Keystore for access tokens
        encryptedPreferences = EncryptedSharedPreferences.create("ch_secured_values",
            masterKey,
            applicationContext!!,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM) as EncryptedSharedPreferences

        onboardingComplete.value = getOnBoardingComplete()
    }

    fun destroyModelData() {
        networkManager.destroyNetworkData()
        //networkManager.ebsDeviceMonitor.onEvent(DisconnectEvent)
        //networkManager.ebsDeviceMonitor.disconnect()
        //networkManager.ebsDeviceMonitor.stopScanningJob()
    }

    // ***********************************
    // Methods used to handle bottle list searches
    fun onSearchQueryChange(newQuery: String) {
        bottleListSearchQuery = newQuery
    }

    // ***********************************
    // Methods used to handle bottle intake

    fun addIntakeBottle(bottle: BottleData) {
        // Only add if unique bottle id
        var appendBottle = true
        for (i in 0 until currentUserIntakeItems.size) {
            if (bottle.id == currentUserIntakeItems[i].id) {
                appendBottle = false
                val keyExists = currentBottleCounts.containsKey(currentUserIntakeItems[i].id)
                if (keyExists) {
                    var value = currentBottleCounts[currentUserIntakeItems[i].id]!!.toInt()
                    value += 1
                    currentBottleCounts[currentUserIntakeItems[i].id] = value.toString()
                } else {
                    currentBottleCounts[currentUserIntakeItems[i].id] = "2"
                }
            }
        }
        if (appendBottle) {
            currentUserIntakeItems.add(bottle)
            currentBottleCounts[bottle.id] = "1"
        }
        // Handle the intake icon state
        _updateCurrentUserIntakeItems.value = _updateCurrentUserIntakeItems.value == false
        _isSaveButtonShowing.value = currentBottleCounts.isNotEmpty()
    }

    fun removeIntakeBottle(bottle: BottleData) {
        for (i in 0 until currentUserIntakeItems.size) {
            if (bottle.id == currentUserIntakeItems[i].id) {
                if (currentBottleCounts.isNotEmpty()) {
                    val keyExists = currentBottleCounts.containsKey(bottle.id)
                    if (keyExists) {
                        var value = currentBottleCounts[bottle.id]!!.toInt()
                        value -= 1
                        if (value == 0) {
                            currentBottleCounts.remove(bottle.id)
                            currentUserIntakeItems.removeAt(i)
                        } else {
                            currentBottleCounts[bottle.id] = value.toString()
                        }
                    }
                }
                _updateCurrentUserIntakeItems.value = _updateCurrentUserIntakeItems.value == false
                break
            }
        }
        _isSaveButtonShowing.value = currentBottleCounts.isNotEmpty()
    }

    fun savePhysiologyChangedValues(userWeightLb: String, userWeightKg: String, heightFt: String, heightIn: String, heightCm: String, gender: String) {
        updateUserWeightLb(userWeightLb)
        updateUserHeightFt(heightFt)
        updateUserHeightIn(heightIn)
        updateUserGender(gender)
        updateUserHeightCm(heightCm)
        updateUserWeightKg(userWeightKg)
    }

    fun getSweatSodiumString(): String {
        var sweatString = ""
        val sodiumConcentrationMm = userAvgSweatSodiumConcentration?.data?.sodium_concentration_mm ?: 0.0
        val sweatVolumeMl = userAvgSweatSodiumConcentration?.data?.sweat_volume_ml ?: 0.0

        // Sodium concentration
        sweatString = if (sodiumConcentrationMm <= 15.0) {
            "low sodium concentration"
        } else if (sodiumConcentrationMm <= 40.0) {
            "moderate sodium concentration"
        } else {
            "high sodium concentration"
        }

        sweatString += " and "

        // Sweat volume conversion: ml to oz
        val mlConversionToOz = sweatVolumeMl / 29.574
        sweatString += if (mlConversionToOz <= 21.0) {
            "low volume of loss."
        } else if (mlConversionToOz <= 48.0) {
            "moderate volume of loss."
        } else {
            "high volume of loss."
        }

        return sweatString
    }

    @Suppress("DEPRECATION")
    fun getCurrentLocale(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationContext!!.resources.configuration.locales.get(0).toString()
        } else {
            applicationContext!!.resources.configuration.locale.toString()
        }
    }

    fun Locale.isMetric(): Boolean {
        Log.d("getCurrentLocale", country.toUpperCase(this))
        return when (country.toUpperCase(this)) {
            "US", "LR", "MM" -> false
            else -> true
        }
    }

    fun isMetricRegion(): Boolean {
        val metric = Locale.getDefault().isMetric()
        return metric
    }

    // ***********************************
    // Methods used to update user preferences

    fun updateUnits(unit: Int) {
        currentUnits.value = unit
        viewModelScope.launch {
            dataStore.saveUnits(unit)
        }
    }

    fun updateEmailAddress(email: String) {
        viewModelScope.launch {
            dataStore.saveUserEmailAddress(email)
        }
    }

 //   fun updateUserBottleMenuItems(bottles: String) {
 //       viewModelScope.launch {
 //           dataStore.saveUserBottleMenuItems(bottles)
 //       }
 //   }

    fun updateUserTotalBottleMenuItems(num: Int) {
        viewModelScope.launch {
            dataStore.saveUserTotalBottleMenuItems(num)
        }
    }

    fun updatePassiveWaterLossState(state: Boolean) {
        viewModelScope.launch {
            dataStore.savePassiveWaterLossState(state)
        }
    }

    fun updateButtonPressWaterIntakeState(state: Boolean) {
        viewModelScope.launch {
            dataStore.saveButtonPressWaterIntakeState(state)
        }
    }

    fun updateButtonPressWaterIntakeValue(value: Int) {
        viewModelScope.launch {
            dataStore.saveButtonPressWaterIntakeValue(value)
        }
    }

    fun updateOnBoardingComplete(done: Boolean) {
        viewModelScope.launch {
            dataStore.saveOnBoardingComplete(done)
        }

        if (done) {
            fileManager.writeFile("onboarding.out", "done")
        }
        else {
            fileManager.deleteFile("onboarding.out")
        }
        onboardingComplete.value = done
    }

    fun getOnBoardingComplete(): Boolean {
        onboardingComplete.value = fileManager.fileExists("onboarding.out")
        return onboardingComplete.value
    }

    fun updateIsCreateAccountFlow(done: Boolean) {
        if (done) {
            val outputString = "${usersEmailAddress.value}*${enterpriseId.value}"
            fileManager.writeFile("createaccount.out", outputString)
        }
        else {
            fileManager.deleteFile("createaccount.out")
        }
        isCreateAccountFlow.value = done
    }

    fun getIsCreateAccountFlow(): Boolean {
        isCreateAccountFlow.value = fileManager.fileExists("createaccount.out")
        if (isCreateAccountFlow.value) {
            val fileString = fileManager.readFile("createaccount.out").toString()
            val splitCode = fileString.split("*")
            usersEmailAddress.value = splitCode[0]
            if (!userExists) {
                onboardingEnterpriseId.value = splitCode[1]
            }
        }
        return isCreateAccountFlow.value
    }

    fun updateCreateAccountUserInfo() {
        isCreateAccountFlow.value = fileManager.fileExists("createaccount.out")
        if (isCreateAccountFlow.value) {
            val fileString = fileManager.readFile("createaccount.out").toString()
            val splitCode = fileString.split("*")
            usersEmailAddress.value = splitCode[0]
            if (!userExists) {
                onboardingEnterpriseId.value = splitCode[1]
            }
        }
    }

    fun updateServerSettings(server: Int) {
        if (server == 1) {
            fileManager.writeFile("staging.out", "staging")
        }
        else {
            fileManager.deleteFile("staging.out")
        }
        serverSettings.value = server
    }

    fun getServerSettings() {
        if (fileManager.fileExists("staging.out")) {
            serverSettings.value = 1
        }
        else {
            serverSettings.value = 0
        }
    }

    fun updateJwtSiteId(jwtSiteId: String) {
        viewModelScope.launch {
            dataStore.saveJwtSiteId(jwtSiteId)
        }
    }

    fun updateEnterpriseName(eName: String) {
        viewModelScope.launch {
            dataStore.saveEnterpriseName(eName)
        }
    }

    fun updateSiteName(siteName: String) {
        viewModelScope.launch {
            dataStore.saveSiteName(siteName)
        }
    }

    fun updateJwtEnterpriseId(jwtEId: String) {
        viewModelScope.launch {
            dataStore.saveJwtEnterpriseId(jwtEId)
        }
    }

    fun updateEnterpriseId(eId: String) {
        viewModelScope.launch {
            dataStore.saveEnterpriseId(eId)
        }
    }
/*
    fun updateServerSettings(server: Int) {
        viewModelScope.launch {
            dataStore.saveServerSettings(server)
        }
    }
*/
    fun updateCurrentAuthAPIServer(server: Int) {
        viewModelScope.launch {
            dataStore.saveCurrentAuthAPIServer(server)
        }
    }

    fun updateCurrentAuthUserEmail(email: String) {
        viewModelScope.launch {
            dataStore.saveCurrentAuthUserEmail(email)
        }
    }

    fun updateCurrentAuthUserRole(jwtUserRole: String) {
        viewModelScope.launch {
            dataStore.saveCurrentAuthUserRole(jwtUserRole)
        }
    }

    fun updateUserId(userId: String) {
        viewModelScope.launch {
            dataStore.saveUserId(userId)
        }
    }

    fun updateCHDeviceName(deviceName: String) {
        viewModelScope.launch {
            dataStore.saveCHDeviceName(deviceName)
        }
    }

    private fun updateUserWeightLb(weight: String) {
        viewModelScope.launch {
            dataStore.saveUserWeightLb(weight)
        }
    }

    private fun updateUserWeightKg(weight: String) {
        viewModelScope.launch {
            dataStore.saveUserWeightKg(weight)
        }
    }

    private fun updateUserHeightFt(feet: String) {
        viewModelScope.launch {
            dataStore.saveUserHeightFt(feet)
        }
    }

    private fun updateUserHeightIn(inches: String) {
        viewModelScope.launch {
            dataStore.saveUserHeightIn(inches)
        }
    }

    private fun updateUserHeightCm(cm: String) {
        viewModelScope.launch {
            dataStore.saveUserHeightCm(cm)
        }
    }

    private fun updateUserGender(gender: String) {
        viewModelScope.launch {
            dataStore.saveUserGender(gender)
        }
    }

    fun updateNotificationState(jsonState: String) {
        viewModelScope.launch {
            dataStore.saveNotificationState(jsonState)
        }
    }

    fun saveLastAppUpdateNotificationState(date: Int) {
        viewModelScope.launch {
            dataStore.saveLastAppUpdateNotificationState(date)
        }
    }

    fun updateSodiumDeficitCap(cap: Int) {
        viewModelScope.launch {
            dataStore.saveUserSodiumDeficitCap(cap)
        }
    }

    fun clearUserDataStore(includeUserAuth: Boolean) {
        viewModelScope.launch {
            dataStore.saveCHDeviceName("")
            dataStore.saveOnBoardingComplete(false)
            if (includeUserAuth) {
                // Used for user exist test
                dataStore.saveUserEmailAddress("")
                dataStore.saveCurrentAuthUserEmail("")
                dataStore.saveEnterpriseName("")
                dataStore.saveJwtEnterpriseId("")
                dataStore.saveJwtSiteId("")
                dataStore.saveEnterpriseId("")
                dataStore.saveUserId("")
                dataStore.saveCurrentAuthAPIServer(0)
                dataStore.saveCurrentAuthUserRole("")
            }
            dataStore.saveUserWeightLb("165")
            dataStore.saveUserWeightKg("75")
            dataStore.saveUserHeightFt("5")
            dataStore.saveUserHeightIn("7")
            dataStore.saveUserHeightCm("175")
            dataStore.saveUserGender("Male")
        }
    }

    fun resetModelDataMutables() {
        deviceSN.value = ""
        isCHDeviceConnected = false
        isNetworkConnected = false
        _isSensorConnected.value = false
        initDeviceOnce.value = false
        _qrPairingState.value = PairState(showErrorMsg = false, showLoading = false, pairedMoveView = false)

        enterpriseId.value = ""
        onboardingEnterpriseId.value = ""
        onboardingEnterpriseName.value = ""
        onboardingSiteName.value = ""
    }

    fun writeJSONtoFile(fileName: String, bottles: List<BottleData>) {
        var gson = Gson()
        var jsonString:String = gson.toJson(bottles)
        applicationContext!!.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    }

    fun readJSONfromFile(fileName: String): List<BottleData> {
        var gson = Gson()
        val listBottleType = object : TypeToken<List<BottleData>>() {}.type

        val file = File(applicationContext!!.filesDir, fileName)
        var fileExists = file.exists()
        if (!fileExists) {
            return ArrayList<BottleData>()
        }

        val bufferedReader: BufferedReader = file.bufferedReader()
        val inputJSONString = bufferedReader.use { it.readText() }

        var bottles: MutableList<BottleData> = gson.fromJson(inputJSONString, listBottleType)
        //bottles.forEachIndexed { idx, bottle -> Log.i("data", "> Item $idx:\n$bottle") }

        return bottles
    }

    fun setCsvFileIsUploading(fileUploading: Boolean) {
        _csvFileIsUploading.value = fileUploading
    }

    fun isTestAccount(): Boolean {
        return (usersEmailAddress.value == "epicoretest000@gmail.com")
    }

    fun showNotification() {
        showNotification.value = true
    }

    fun hideNotification() {
        showNotification.value = false
    }

    fun setSweatDashboardViewStatus(hydrationStatus: Int) {
        _sweatDashboardViewStatus.value = hydrationStatus
    }
}

private fun loadJSON(context: Context, fileName: String): ArrayList<BottleData> {
    val jsonFileString = getJsonDataFromAsset(context, fileName)
    if (jsonFileString == null) {
        Log.i("data", "jsonFileString is null - can't find bottle json files in assets.")
        return ArrayList<BottleData>()
    }

    val gson = Gson()
    val listBottleType = object : TypeToken<List<BottleData>>() {}.type

    //bottles.forEachIndexed { idx, bottle -> Log.i("data", "> Item $idx:\n$bottle") }

    return gson.fromJson(jsonFileString, listBottleType)
}

private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}