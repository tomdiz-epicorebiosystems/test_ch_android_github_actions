package com.epicorebiosystems.rehydrate

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.datadog.android.compose.NavigationViewTrackingEffect
import com.datadog.android.compose.trackClick
import com.datadog.android.rum.GlobalRumMonitor
import com.datadog.android.rum.RumErrorSource
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.HandleMenuAddOption
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.isValidDeviceSerialNumber
import com.epicorebiosystems.rehydrate.networkManager.ApiServerInfo
import com.epicorebiosystems.rehydrate.networkManager.ConnectionState
import com.epicorebiosystems.rehydrate.networkManager.connectivityState
import com.epicorebiosystems.rehydrate.nordicsemi.uart.repository.UART_SERVICE_UUID
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.DisconnectEvent
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnRunInput
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleIdentify
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleMainView
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleManuallyView
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleScanView
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleUnresponsive
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.toByteArray
import com.epicorebiosystems.rehydrate.sharedViews.EditEnterpriseScreen
import com.epicorebiosystems.rehydrate.sharedViews.EpicoreLegalScreens
import com.epicorebiosystems.rehydrate.sharedViews.EpicoreRequiredView
import com.epicorebiosystems.rehydrate.sharedViews.PhysiologyInformationView
import com.epicorebiosystems.rehydrate.sharedViews.QrScannerScreen
import com.epicorebiosystems.rehydrate.sharedViews.VerifyPhysiologyInfoView
import com.epicorebiosystems.rehydrate.tabViews.IntakeViews.AddMenuItemScreen
import com.epicorebiosystems.rehydrate.tabViews.IntakeViews.BottleListScreen
import com.epicorebiosystems.rehydrate.tabViews.IntakeViews.EnterBottleManuallyScreen
import com.epicorebiosystems.rehydrate.tabViews.IntakeViews.IntakeScreen
import com.epicorebiosystems.rehydrate.tabViews.SettingsScreen
import com.epicorebiosystems.rehydrate.tabViews.historyViews.HistoryScreen
import com.epicorebiosystems.rehydrate.tabViews.insightViews.InsightsScreen
import com.epicorebiosystems.rehydrate.tabViews.settingsViews.ComplianceScreen
import com.epicorebiosystems.rehydrate.tabViews.settingsViews.DebugScreen
import com.epicorebiosystems.rehydrate.tabViews.settingsViews.LegalRegulatoryScreen
import com.epicorebiosystems.rehydrate.tabViews.settingsViews.LicenseScreen
import com.epicorebiosystems.rehydrate.tabViews.settingsViews.PrivacyScreen
import com.epicorebiosystems.rehydrate.tabViews.settingsViews.SensorInformationScreen
import com.epicorebiosystems.rehydrate.tabViews.settingsViews.TermsConditionsScreen
import com.epicorebiosystems.rehydrate.tabViews.todayViews.TodayScreen
import com.epicorebiosystems.rehydrate.topBarViews.InformationViews
import com.epicorebiosystems.rehydrate.topBarViews.NotificationConstants
import com.epicorebiosystems.rehydrate.topBarViews.NotificationData
import com.epicorebiosystems.rehydrate.topBarViews.NotificationLocation
import com.epicorebiosystems.rehydrate.topBarViews.NotificationShowOptions
import com.epicorebiosystems.rehydrate.topBarViews.NotificationType
import com.epicorebiosystems.rehydrate.ui.theme.EpicoreCHTheme
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScanningState
import java.util.Date
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.system.exitProcess


// Tab route navigation
sealed class TabScreen(var isSelected: Boolean, val route: String?, val title: Int?, val selectedImageId: Int?, val unSelectedImageId: Int?) {
    data object History : TabScreen(false, "history",
        R.string.tab_history, R.drawable.icon_tab_history_on, R.drawable.icon_tab_history_off)
    data object Today : TabScreen(true, "today",
        R.string.tab_today, R.drawable.icon_tab_today_on, R.drawable.icon_tab_today_off)
    data object Intake : TabScreen(false, "intake",null,null, null)
    data object Insights : TabScreen(false, "insights",
        R.string.tab_insights, R.drawable.icon_tab_insights_on, R.drawable.icon_tab_insights_off)
    data object Settings : TabScreen(false, "settings",
        R.string.tab_settings, R.drawable.icon_tab_settings_on, R.drawable.icon_tab_settings_off)
}

// Settings route navigation
sealed class SettingsSubScreens(val route: String?) {
    data object LegalRegulatory : SettingsSubScreens("legal_regulatory")
    data object TermsConditions : SettingsSubScreens("terms_conditions")
    data object PrivacyPolicy : SettingsSubScreens("privacy_policy")
    data object Debug : SettingsSubScreens("debug_screen")
    data object Compliance : SettingsSubScreens("compliance")
    data object License : SettingsSubScreens("license")
    data object SensorInformation : SettingsSubScreens("sensor_information")
    data object PairNewModuleMain : SettingsSubScreens("pair_new_module_main")
    data object PairNewModuleScanView : SettingsSubScreens("pair_new_module_scan_view")
    data object PairNewModuleIdentify : SettingsSubScreens("pair_new_module_identify")
    data object PairNewModuleManuallyView : SettingsSubScreens("pair_new_module_manually")
    data object PairNewModuleUnresponsiveView : SettingsSubScreens("pair_new_module_unresponsive")
    data object PairNewModuleQrScanView : SettingsSubScreens("pair_new_module_qrscan")
    data object VerifyPhysioloyInfoView : SettingsSubScreens("verify_user_physiology")
}

// Intake route navigation
sealed class IntakeScreens(val route: String?) {
    data object AddMenuItem : IntakeScreens("add_menu_item")
    data object EnterBottleManually : IntakeScreens("enter_bottle_manually")
    data object BottleListAdd : IntakeScreens("bottle_list_add")
}

// Shared route navigation
sealed class SharedScreens(val route: String?) {
    data object EditEnterprise : SharedScreens("edit_enterprise")
    data object ScanEnterpriseQRCode : SharedScreens("scan_qr_enterprise_view")
    data object ScanDeviceQRCode : SharedScreens("scan_qr_device_view")
    data object EditPhysiology : SharedScreens("edit_physiology")
}

// Info popup route navigation
sealed class InfoPopupScreens(val route: String?) {
    data object InformationViews : SharedScreens("info_popup_view")
    data object EpicoreRequiredTroubleshootingView : SharedScreens("required_legal_troubleshooting_view")
    data object EpicoreRequiredFaqView : SharedScreens("required_legal_faq_view")
}

// Global variables used by application
enum class IntakeButtonState {
    INTAKE_UP, INTAKE_DOWN, INTAKE_ADD, INTAKE_SAVE, INTAKE_UPDATE, INTAKE_CANCEL
}

val tabItems = listOf(
    TabScreen.Today,
    TabScreen.History,
    TabScreen.Insights,
    TabScreen.Settings
)

var selectedTabItem = tabItems[0]
var isIntakeTabSelected = false

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val chViewModel = ModelData()
    val context = this@MainActivity

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*
        // Handle In-App updates
        val appUpdateManager: AppUpdateManager? = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
        //chViewModel.showAppUpdateAvailable = true
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                Log.d("APP_UPDATE", "Epicore CH update available")
                chViewModel.showAppUpdateAvailable = true
            } else {
                Log.d("APP_UPDATE", "No Epicore CH update available")
                chViewModel.showAppUpdateAvailable = false
            }
        }
*/
        // If BLE is turned off - have user turn on in settings and re-run app
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled) {

            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.bluetooth)
            builder.setMessage(R.string.ch_requires_bluetooth)
            builder.setNegativeButton((R.string.exit),
                DialogInterface.OnClickListener { _, _ ->
                    finishAndRemoveTask()
                })
            builder.setPositiveButton((R.string.settings),
                DialogInterface.OnClickListener { _, _ ->
                    val i = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    context.startActivity(i)
                    exitProcess(0)
                })
            val alertDialog = builder.show()
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.setCancelable(false)
            return
        }

        if (savedInstanceState == null) {
            setContent {
                val permissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
//                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
                } else {
                    rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
                }

                if (permissionsState.allPermissionsGranted) {
                    EpicoreCHApp(chViewModel)
                }
                else {
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(key1 = lifecycleOwner,
                        effect = {
                            val observer = LifecycleEventObserver { _, event ->
                                if(event == Lifecycle.Event.ON_START) {
                                    permissionsState.launchMultiplePermissionRequest()
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)

                            onDispose {
                                lifecycleOwner.lifecycle.removeObserver(observer)
                            }
                        })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d("MainActivity", "onResume")
        chViewModel.applicationInForeground = true
        chViewModel.applicationSwitchToForeground = true

        // Update the BLE connection interval when the app goes from background to foreground to speed up downloading in case connection interval is long.
        if (chViewModel.isCHDeviceConnected) {
            // Start device status
            val getDeviceStatusCommandBytes: ByteArray = byteArrayOf(0x51, 0xA5.toByte())
            chViewModel.networkManager.ebsDeviceMonitor.onEvent(OnRunInput(getDeviceStatusCommandBytes))
        }

        // If the module is already in a session notification is showing then reset to hide
        if (chViewModel.notificationData.id == NotificationConstants.BLE_SESSION_RUNNING_NOTIFICATION) {
             chViewModel.hideNotification()
            chViewModel.notificationData = NotificationData(id = "empty_notification", title = "Notification Title", detail = "Notification detail text for the user.", type = NotificationType.Error, notificationLocation = NotificationLocation.Middle, showOnce = true, showSeconds = NotificationShowOptions.showClose, appUrl = null)
        }

    }

    override fun onPause() {
        super.onPause()

        Log.d("MainActivity", "onPause")
        chViewModel.applicationInForeground = false
    }

    override fun onDestroy() {
        super.onDestroy()

        chViewModel.destroyModelData()
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun EpicoreCHApp(chViewModel: ModelData) {
    EpicoreCHTheme {
        chViewModel.applicationContext = LocalContext.current
        chViewModel.initializeModelData()

        //val onBoardingComplete = chViewModel.dataStore.getOnBoardingComplete.collectAsState(initial = false)
        val onBoardingComplete = chViewModel.onboardingComplete.collectAsStateWithLifecycle()

        chViewModel.currentUnits = chViewModel.dataStore.getUnits.collectAsState(initial = 1) as MutableState<Int>

        // Load last device connected if onboarding is completed
        if(onBoardingComplete.value) {
            chViewModel.deviceSN =
                chViewModel.dataStore.getCHDeviceName.collectAsState(initial = "") as MutableState<String>
        }

        // Load saved userInfo
        chViewModel.userWeightLb = chViewModel.dataStore.getUserWeightLb.collectAsState(initial = "") as MutableState<String>
        chViewModel.userWeightKg = chViewModel.dataStore.getUserWeightKg.collectAsState(initial = "") as MutableState<String>
        chViewModel.userHeightFt = chViewModel.dataStore.getUserHeightFt.collectAsState(initial = "") as MutableState<String>
        chViewModel.userHeightIn = chViewModel.dataStore.getUserHeightIn.collectAsState(initial = "") as MutableState<String>
        chViewModel.userHeightCm = chViewModel.dataStore.getUserHeightCm.collectAsState(initial = "") as MutableState<String>
        chViewModel.userGender = chViewModel.dataStore.getUserGender.collectAsState(initial = "") as MutableState<String>
        chViewModel.enterpriseId = chViewModel.dataStore.getEnterpriseId.collectAsState(initial = "") as MutableState<String>
        chViewModel.jwtEnterpriseID = chViewModel.dataStore.getJwtEnterpriseId.collectAsState(initial = "") as MutableState<String>
        chViewModel.CH_EnterpriseName = chViewModel.dataStore.getEnterpriseName.collectAsState(initial = "") as MutableState<String>
        chViewModel.CH_SiteName = chViewModel.dataStore.getSiteName.collectAsState(initial = "") as MutableState<String>
        chViewModel.jwtSiteID = chViewModel.dataStore.getJwtSiteId.collectAsState(initial = "") as MutableState<String>
        chViewModel.CH_UserRole = chViewModel.dataStore.getCurrentAuthUserRole.collectAsState(initial = "") as MutableState<String>
        chViewModel.usersEmailAddress = chViewModel.dataStore.getUserEmailAddress.collectAsState(initial = "") as MutableState<String>
        chViewModel.userTotalBottleMenuItems = chViewModel.dataStore.getUserTotalBottleMenuItems.collectAsState(initial = 0) as MutableState<Int>
        chViewModel.lastCheckAppUpdate = chViewModel.dataStore.getLastAppUpdateNotificationState.collectAsState(initial = 0) as MutableState<Int>
        chViewModel.userPassiveLossState = chViewModel.dataStore.getPassiveWaterLossState.collectAsState(initial = true) as MutableState<Boolean>
        chViewModel.buttonPressWaterIntakeVolumeInMl = chViewModel.dataStore.getButtonPressWaterIntakeValue.collectAsState(initial = 500) as MutableState<Int>
        chViewModel.buttonPressWaterIntakeState = chViewModel.dataStore.getButtonPressWaterIntakeState.collectAsState(initial = true) as MutableState<Boolean>

        // Load server to use
        chViewModel.getServerSettings()

        // Load last user Auth too server - user exist have token/refresh token
        chViewModel.currentAuthAPIServer = chViewModel.dataStore.getCurrentAuthAPIServer.collectAsState(initial = 0) as MutableState<Int>
        chViewModel.currentAuthUserId = chViewModel.dataStore.getUserId.collectAsState(initial = "") as MutableState<String>
        chViewModel.currentAuthUserEmail = chViewModel.dataStore.getCurrentAuthUserEmail.collectAsState(initial = "") as MutableState<String>
        chViewModel.currentAuthUserRole = chViewModel.dataStore.getCurrentAuthUserRole.collectAsState(initial = "") as MutableState<String>

        // Load notifications state
        chViewModel.notificationStateString = chViewModel.dataStore.getNotificationState.collectAsState(initial = "") as MutableState<String>

        // Load sodium cap
        chViewModel.capSodiumValue = chViewModel.dataStore.getUserSodiumCap.collectAsState(initial = 0) as MutableState<Int>

        val connection by connectivityState()
        val isNetworkConnected = connection === ConnectionState.Available
        chViewModel.isNetworkConnected = isNetworkConnected == true

        // If you want to skip onboarding you need to update PreferencesDataStore.getOnBoardingComplete()
        // init value, that is currently set to 'false' (true will skip) and above val onBoardingComplete to true.
        val ebsMonitor: EBSDeviceMonitor = hiltViewModel()
        ebsMonitor.chViewModel = chViewModel
        ebsMonitor.setApplicationContext(LocalContext.current)
        ebsMonitor.setAppVersion(BuildConfig.VERSION_NAME)
        ebsMonitor.setBuildNumber(BuildConfig.VERSION_CODE)
        chViewModel.networkManager.ebsDeviceMonitor = ebsMonitor
        chViewModel.networkManager.apiServerInfo = ApiServerInfo(chViewModel)

        ebsMonitor.setFilterUuid(ParcelUuid(UART_SERVICE_UUID))

        // Clear device scan array
        ScanningState.DevicesDiscovered(emptyList())

        if (!onBoardingComplete.value) {
            Scaffold(
                modifier = Modifier.fillMaxSize().semantics { testTagsAsResourceId = true },
                backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)
            ) { innerPadding ->
                OnboardingNavHost(
                    chViewModel,
                    ebsMonitor,
                    modifier = Modifier.padding(innerPadding))
            }
        } else {
            BottomBarWithIntakeFab(chViewModel, ebsMonitor)
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun BottomBarWithIntakeFab(chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor) {
    var intakeButtonState by rememberSaveable { mutableStateOf(IntakeButtonState.INTAKE_UP) }
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = BottomSheetValue.Collapsed
        )
    )
    val navController = rememberNavController().apply {
        NavigationViewTrackingEffect(navController = this)
    }
    var hideBottomBar by rememberSaveable { mutableStateOf(false) }
    var userHistoryState by remember { mutableStateOf(true) }
    var userAvgSweatState by remember { mutableStateOf(true) }
    var refreshTokenState by remember { mutableStateOf(true) }
    //var initDeviceOnce by remember { mutableStateOf(false) }
    var nextSaveIconGlow = remember { mutableIntStateOf(R.drawable.bottom_bar_0_save_copy_10) }
    val isSaveButtonShowing by chViewModel.isSaveButtonShowing.collectAsState()
    val uartStateMgr = ebsMonitor.uartState.collectAsState().value
    val historicalScope  = rememberCoroutineScope()
    var initSyncTimeOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val fileUploadScope  = rememberCoroutineScope()

    val sweatDataDownloadScope = rememberCoroutineScope()

    val timer = Timer()

    var lastIntakeSaveButtonClickTime by remember { mutableStateOf(0L) }

    val clickDisablePeroid: Long = 2000L

    // Start sweat data log downloading (from sensor) and uploading (to cloud) right after the screen appears the first time or screen switch.
    // Limit the frequency/rate of data sync while switching back to Today view to once per 30 seconds.
    LaunchedEffect(Unit) {

        // If the sensor is disconnected for more than 5 seconds, restart scanning and re-connect
        timer.schedule(0L, 5000L) {
            if (!chViewModel.isSensorConnected.value) { // && (ebsMonitor.getCurrentUserSession() && ebsMonitor.getDisplayUserSession())) {
                ebsMonitor.scanBluetoothDevice()
            }
        }

        while(true) {

            // Run file upload every 30 seconds after completed
            var timeSinceLastFileUploading = if (chViewModel.syncFileTime == 0L) 60000L else System.currentTimeMillis() - chViewModel.syncFileTime
            //Log.d("SWEAT_DATA_DOWNLOAD", "Time since last data sync: $timeSinceLastFileUploading")
            if (((timeSinceLastFileUploading > 30000L) || chViewModel.applicationSwitchToForeground) && (chViewModel.isCHDeviceConnected) && chViewModel.initDeviceOnce.value && chViewModel.applicationInForeground) {
                if (chViewModel.sweatDataMultiDaySyncWithSensorCompleted && chViewModel.historicalSweatDataDownloadCompleted) {
                    Log.d("SWEAT_DATA_DOWNLOAD", "Start file upload commands")

                    chViewModel.applicationSwitchToForeground = false
                    sweatDataDownloadScope.launch {
                        // Start file upload
//                        ebsMonitor.scanPreviousDayDeviceData(uartStateMgr, chViewModel)
                        ebsMonitor.scanDeviceCurrentDayData(uartStateMgr)

                        // Add a timeout here for the data downloading/uploading so that it won't hang the system.
                        delay(17000L)    // orig: 15000 on iOS
                        Log.d("SWEAT_DATA_DOWNLOAD", "Delay complete!")

                        if (!uartStateMgr.uartManagerState.sweatDataLogDownloadCompleted) {
                            ebsMonitor.setSweatDataLogDownloadCompletedFlag(true)
                        }

                        if (!chViewModel.sweatDataCurrentDayDownloadingCompleted) {
                            chViewModel.sweatDataCurrentDayDownloadingCompleted = true
                        }

                        if (!chViewModel.sweatDataMultiDaySyncWithSensorCompleted) {
                            chViewModel.sweatDataMultiDaySyncWithSensorCompleted = true
                        }

                        if (chViewModel.csvFileIsUploading.value) {
                            chViewModel.setCsvFileIsUploading(false)
                        }

                    }
                }
            }

            // Once sensor connects do setup
            if (chViewModel.isCHDeviceConnected && !chViewModel.initDeviceOnce.value) {
                Log.d("SENSOR_CONNECTED", "Initialize...")

                // Get system and user information command
                val getSystemAndUserInfoCommandBytes : ByteArray = byteArrayOf(0x50)
                ebsMonitor.onEvent(OnRunInput(getSystemAndUserInfoCommandBytes))

                delay(100L)

                ebsMonitor.setSweatSensingStartTimestamp()

                delay(100L)

                // Start device status
                val getDeviceStatusCommandBytes : ByteArray = byteArrayOf(0x51, 0x00)
                ebsMonitor.onEvent(OnRunInput(getDeviceStatusCommandBytes))

                delay(100L)

//                // Get system and user information command
//                val getSystemAndUserInfoCommandBytes : ByteArray = byteArrayOf(0x50)
//                ebsMonitor.onEvent(OnRunInput(getSystemAndUserInfoCommandBytes))

                chViewModel.initDeviceOnce.value = true
            }

            if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
                if (chViewModel.timeToSyncHistoricalData && chViewModel.isCHDeviceConnected && chViewModel.initDeviceOnce.value) {
                    if (chViewModel.historicalSweatDataDownloadCompleted && chViewModel.sweatDataMultiDaySyncWithSensorCompleted) {
                        Log.d("MainActivity", "Download historical data")
                        historicalScope.launch {

                            chViewModel.historicalSweatDataDownloadCompleted = false
                            chViewModel.timeToSyncHistoricalData = false

                            // Get latest device historical sweat data array
                            var currentHistoricalSweatDataDownloadIndex =
                                ebsMonitor.getCurrentHistoricalSweatDataDownloadIndex()

                            // If there are many (>5) missing packets, re-downloading historical data from the beginning
                            Log.d(
                                "SWEAT_HISTORICAL_DATA",
                                "${currentHistoricalSweatDataDownloadIndex}, ${ebsMonitor.getHistoricalSweatDataForPlot().size}"
                            )
                            if ((currentHistoricalSweatDataDownloadIndex > (ebsMonitor.getHistoricalSweatDataForPlot().size.toUShort() + 5u))
                                && (currentHistoricalSweatDataDownloadIndex > ((ebsMonitor.getHistoricalSweatDataForPlot().size.toUShort()) * 11u / 10u))
                            ) {
//                            Log.d("SWEAT_HISTORICAL_DATA", "${currentHistoricalSweatDataDownloadIndex}, ${ebsMonitor.getHistoricalSweatDataForPlot().size}")
                                currentHistoricalSweatDataDownloadIndex = 0u
                            }

                            val startHistoricalDataDownloadCommandBytes: ByteArray =
                                byteArrayOf(
                                    0x52,
                                    0x5A
                                ).plus(currentHistoricalSweatDataDownloadIndex.toByteArray())
                            ebsMonitor.onEvent(OnRunInput(startHistoricalDataDownloadCommandBytes))

                            //Log.d("currHistoricalDwnIndex", "$currentHistoricalSweatDataDownloadIndex")

                            // Start timer for file sync
                            if (!initSyncTimeOnce) {
                                initSyncTimeOnce = true
                            }

                            delay(10000L)    // orig: 10000 like iOS

                            if (!chViewModel.historicalSweatDataDownloadCompleted) {
                                chViewModel.historicalSweatDataDownloadCompleted = true
                            }
                        }
                    }
                }
            }

            delay(1000L)

        }
    }

    if(uartStateMgr.uartManagerState.historicalSweatDataUpToDate) {
        Log.d("SWEAT_HISTORICAL_DATA", "History Data download completed!")
        uartStateMgr.uartManagerState.historicalSweatDataUpToDate = false
        chViewModel.historicalSweatDataDownloadCompleted = true
    }

    // File upload trigger once timestamp 0xFFFFF is reached and file created
    if (uartStateMgr.uartManagerState.fileReadyUpload) {
        chViewModel.syncDate = Date()

        fileUploadScope.launch {
            // The following code is to support multi-day data downloading from sensor and uploading to cloud.
            if (!chViewModel.sweatDataCurrentDayDownloadingCompleted) {

                ebsMonitor.setFileReadyUploadFlag(false)
                chViewModel.sweatDataCurrentDayDownloadingCompleted = true

                // Only upload the file to cloud if the session's user ID and site ID match with the current user information.
                if((chViewModel.currentAuthUserId.value.substring(0, 8) == ebsMonitor.getSessionUserID()) && (chViewModel.enterpriseId.value == ebsMonitor.getSessionSiteID())) {
                    //Log.d("fileReadyUpload", "uploadSensorCSVFile - ${ebsMonitor.getSweatDataLogFileName()}")
                    chViewModel.networkManager.uploadSensorCSVFile(
                        context,
                        ebsMonitor.getSweatDataLogFileName()
                    )

                    chViewModel.isCurrentUserSession = true
                    chViewModel.isUserSessionToDisplay = true

                    // Continue to download previous day's data
                    ebsMonitor.scanDevicePreviousDayData(uartStateMgr)

                }

                // No match, not current user's session, terminate the data downloading and remove the file downloaded from sensor
                else {
                    chViewModel.isCurrentUserSession = false
                    chViewModel.isUserSessionToDisplay = false

                    chViewModel.sweatDataMultiDaySyncWithSensorCompleted = true

                    // Force disconnect the module from the app
                    ebsMonitor.onEvent(DisconnectEvent)
                    ebsMonitor.disconnect()
                    ebsMonitor.stopScanningJob()
                }

                chViewModel.setCsvFileIsUploading(false)

            }

            // This is for previous day's data.
            else {

                ebsMonitor.setFileReadyUploadFlag(false)

                // Only upload the file to cloud if the session's user ID and site ID match with the current user information.
                if((chViewModel.currentAuthUserId.value.substring(0, 8) == ebsMonitor.getSessionUserID()) && (chViewModel.enterpriseId.value == ebsMonitor.getSessionSiteID())) {
                    //Log.d("fileReadyUpload", "uploadSensorCSVFile - ${ebsMonitor.getSweatDataLogFileName()}")
                    chViewModel.networkManager.uploadSensorCSVFile(
                        context,
                        ebsMonitor.getSweatDataLogFileName()
                    )
                }

                // No match, remove the file downloaded from sensor.
                else {
//                    delay(1000L)
//                    chViewModel.fileManager.deleteFile(ebsMonitor.getSweatDataLogFileName())
                }

                chViewModel.sweatDataMultiDaySyncWithSensorCompleted = true
                chViewModel.setCsvFileIsUploading(false)

            }

            delay(1000L)
            chViewModel.fileManager.deleteFile(ebsMonitor.getSweatDataLogFileName())

        }
    }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        bottomBar = {
            if (!hideBottomBar) {
                BottomAppBar(
                    modifier = Modifier
                        .height(65.dp),
                    backgroundColor = Color.White,
                    elevation = 0.dp
                ) {
                    BottomNav(chViewModel, navController = navController, updateIntakeState = { newIntakeState ->
                        intakeButtonState = newIntakeState
                    })
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        floatingActionButton = {
            if (!hideBottomBar) {
                FloatingActionButton(
                    modifier = Modifier.size(85.dp),
                    backgroundColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    onClick = trackClick(targetName = "Intake button pressed. state = $intakeButtonState") {
                        chViewModel.newBottlesItemsAdded.clear()
                        HandleMenuAddOption(chViewModel, intakeButtonState)
                        selectedTabItem.isSelected = false
                        isIntakeTabSelected = true
                        TabScreen.Intake.route?.let {
                            navController.navigate(it) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        if (intakeButtonState == IntakeButtonState.INTAKE_SAVE) {

                            // Check to see if the button is pressed multiple times by accident
                            if (SystemClock.elapsedRealtime() - lastIntakeSaveButtonClickTime > clickDisablePeroid) {

                                lastIntakeSaveButtonClickTime = SystemClock.elapsedRealtime()

                                // Send intake to the device
                                val fluidIntakeTotalInMl =
                                    chViewModel.totalWaterAmount.toInt().toUShort()
                                val sodiumIntakeTotalInMg =
                                    chViewModel.totalSodiumAmount.toInt().toUShort()
                                if (fluidIntakeTotalInMl != 0.toUShort() || sodiumIntakeTotalInMg != 0.toUShort()) {
                                    val intakeFluid = fluidIntakeTotalInMl.toByteArray()
                                    val intakeSodium = sodiumIntakeTotalInMg.toByteArray()
                                    val recordIntakeCommandBytes: ByteArray =
                                        byteArrayOf(0x57, 0x03).plus(intakeFluid).plus(intakeSodium)
                                    ebsMonitor.onEvent(OnRunInput(recordIntakeCommandBytes))

                                    GlobalRumMonitor.get().addError(
                                        "INTAKE_SAVE()",
                                        RumErrorSource.LOGGER,
                                        null,
                                        mapOf(
                                            "fluidIntakeTotalInMl" to "$fluidIntakeTotalInMl",
                                            "sodiumIntakeTotalInMg" to "$sodiumIntakeTotalInMg"
                                        )
                                    )

                                    chViewModel.totalWaterAmount = 0.0
                                    chViewModel.totalSodiumAmount = 0.0
                                    chViewModel.currentUserIntakeItems.clear()
                                }
                                intakeButtonState = IntakeButtonState.INTAKE_UP
                            }

                            else {
                                Log.d("IntakeSave", "Intake save button pressed too fast")
                            }
                        }
                        else {
                            intakeButtonState = IntakeButtonState.INTAKE_DOWN
                        }
                        chViewModel._isSaveButtonShowing.value = false
                        TabScreen.Intake.route?.let { navController.navigate(it) }
                    },
                    contentColor = Color.White//Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.size(width = 100.dp, height = 100.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (intakeButtonState) {
                            IntakeButtonState.INTAKE_UP -> {
                                Image(
                                    painterResource(id = R.drawable.bttn_trackintake_u),
                                    modifier = Modifier.testTag("image_intake_button_up"),
                                    contentDescription = "image_intake_button_up",
                                )
                                LaunchedEffect(bottomSheetScaffoldState) {
                                    bottomSheetScaffoldState.bottomSheetState.collapse()
                                }
                            }

                            IntakeButtonState.INTAKE_DOWN -> {
                                Image(
                                    painterResource(id = R.drawable.bttn_trackintake_d),
                                    modifier = Modifier.testTag("image_intake_button_down"),
                                    contentDescription = "image_intake_button_down",
                                )
                            }

                            IntakeButtonState.INTAKE_CANCEL -> {
                                Image(
                                    painterResource(id = R.drawable.bttn_trackintake_x),
                                    modifier = Modifier.testTag("image_intake_button_cancel"),
                                    contentDescription = "image_intake_button_cancel",
                                )
                                LaunchedEffect(bottomSheetScaffoldState) {
                                    bottomSheetScaffoldState.bottomSheetState.collapse()
                                }
                            }

                            IntakeButtonState.INTAKE_ADD -> {
                                Image(
                                    painterResource(id = R.drawable.bttn_trackintake_d___add),
                                    modifier = Modifier.testTag("image_intake_button_add"),
                                    contentDescription = "image_intake_button_add",
                                )
                                LaunchedEffect(bottomSheetScaffoldState) {
                                    bottomSheetScaffoldState.bottomSheetState.collapse()
                                }
                            }

                            IntakeButtonState.INTAKE_SAVE -> {
                                // Need to start animation of PNG here
                                var pngCount = 0
                                LaunchedEffect(Unit) {
                                    while(isSaveButtonShowing) {
                                        delay(500L)

                                        when (pngCount) {
                                            0 -> nextSaveIconGlow.value = R.drawable.bottom_bar_0_save_copy_10
                                            1 -> nextSaveIconGlow.value = R.drawable.bottom_bar_0_save_copy_11
                                            2 -> nextSaveIconGlow.value = R.drawable.bottom_bar_0_save_copy_12
                                            3 -> nextSaveIconGlow.value = R.drawable.bottom_bar_0_save_copy_13
                                            4 -> nextSaveIconGlow.value = R.drawable.bottom_bar_0_save_copy_14
                                            5 -> nextSaveIconGlow.value = R.drawable.bottom_bar_0_save_copy_15
                                            6 -> nextSaveIconGlow.value = R.drawable.bottom_bar_0_save_copy_16
                                            7 -> nextSaveIconGlow.value = R.drawable.bottom_bar_0_save_copy_17
                                        }

                                        pngCount += 1
                                        if (pngCount > 7) {
                                            pngCount = 0
                                        }
                                    }
                                }

                                Image(painterResource(nextSaveIconGlow.value),
                                    modifier = Modifier.testTag("image_intake_button_save"),
                                    contentDescription = "image_intake_button_save")
                            }

                            IntakeButtonState.INTAKE_UPDATE -> {
                                Image(
                                    painterResource(id = R.drawable.bttn_trackintake_u),
                                    modifier = Modifier.testTag("image_intake_button_update"),
                                    contentDescription = "image_intake_button_update",
                                )
                            }

                        }
                    }
                }
            }
        }
    ) {
        Surface(
            color = colorResource(if (chViewModel.sweatDashboardViewStatus.value == 0) R.color.BgStatusHydrated else if (chViewModel.sweatDashboardViewStatus.value == 1) R.color.BgStatusAtRisk else R.color.BgStatusDehydrated)
        ) {

            NavHost(navController, startDestination = TabScreen.Today.route!!, modifier = Modifier.testTag("tabbar_bottombarwithintakefab")) {

                // Today
                composable(TabScreen.Today.route) {
                    LaunchedEffect(refreshTokenState) {

                        //chViewModel._updateUserInfoFromDevice.value = true

                        // Clear create account flow flag in case user logs out
                        chViewModel.updateIsCreateAccountFlow(false)
                        // Reset create account - if user exists check for 2 enterprises
                        chViewModel.onboardingEnterpriseId.value = ""
                        chViewModel.onboardingEnterpriseName.value = ""

                        // Add support for test account here
                        if (!chViewModel.isTestAccount()) {
                            // if JWT Token invalid force logout
                            val isValidToken = chViewModel.networkManager.isTokenValid()
                            if (!isValidToken) {
                                chViewModel.networkManager.logOutUser()
                                chViewModel.onboardingStep = 1
                                chViewModel.updateOnBoardingComplete(false)
                                ebsMonitor.onEvent(DisconnectEvent)
                                ebsMonitor.disconnect()
                                ebsMonitor.stopScanningJob()
                                chViewModel.clearUserDataStore(true)
                                chViewModel.resetModelDataMutables()
                                ebsMonitor.clearHistoricalDataSet()
                                chViewModel.updateServerSettings(chViewModel.serverSettings.value)
                                ebsMonitor.scanBluetoothDevice()
                            } else {
                                val ret = chViewModel.networkManager.getNewRefreshToken()
                                // only call API if no error returned
                                //if (ret == null) {
                                //Log.d("LaunchedEffect", "***** refreshTokenState called")
                                refreshTokenState != refreshTokenState

                                // Make sure not empty value - MutableState takes some time now and than
                                val userHeightCm = chViewModel.userHeightCm.value.toIntOrNull() ?: 0
                                val userWeightKg = chViewModel.userWeightKg.value.toIntOrNull() ?: 0
                                val userGender = chViewModel.userGender.value.ifEmpty {
                                    "Male"
                                }

                                ebsMonitor.setUserInfoForCSVFile(userGender, userHeightCm, userWeightKg)
                                if (!chViewModel.updateUserSuccess && chViewModel.isUpdateUserCalled) {
                                    val userInfo: Map<String, Any> = mapOf(
                                        "height" to chViewModel.userHeightCm.value,
                                        "weight" to chViewModel.userWeightKg.value,
                                        "biologicalSex" to if (userGender == "Male") "male" else "female")

                                    chViewModel.networkManager.updateUser(enterpriseId = chViewModel.jwtEnterpriseID.value, siteId = chViewModel.jwtSiteID.value, userInfo = userInfo)
                                }
                                else {
                                    chViewModel.networkManager.getUserInfo()
                                }
                            }
                        }
                    }

                    if (intakeButtonState == IntakeButtonState.INTAKE_SAVE) {
                        chViewModel.currentUserIntakeItems.clear()
                        chViewModel.newBottlesItemsAdded.clear()
                        chViewModel.totalWaterAmount = 0.0
                        chViewModel.totalSodiumAmount = 0.0
                        intakeButtonState = IntakeButtonState.INTAKE_UP
                    }

                    chViewModel.isTabButtonPressed.value = true

                    TodayScreen(chViewModel, ebsMonitor, navController, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                // History
                composable(TabScreen.History.route!!) {
                    LaunchedEffect(userHistoryState) {
                        val ret = chViewModel.networkManager.getUserHistoryStats()
                        // only call API if no error returned
                        if (ret == null) {
                            //Log.d("LaunchedEffect", "***** getUserHistoryStats called")
                            userHistoryState != userHistoryState
                        }
                    }

                    if (intakeButtonState == IntakeButtonState.INTAKE_SAVE) {
                        chViewModel.currentUserIntakeItems.clear()
                        chViewModel.newBottlesItemsAdded.clear()
                        chViewModel.totalWaterAmount = 0.0
                        chViewModel.totalSodiumAmount = 0.0
                        intakeButtonState = IntakeButtonState.INTAKE_UP
                    }

                    chViewModel.isTabButtonPressed.value = true

                    HistoryScreen(chViewModel, ebsMonitor, navController, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                // Intake
                composable(TabScreen.Intake.route!!) {
                    IntakeScreen(
                        chViewModel = chViewModel,
                        ebsDeviceMonitor = ebsMonitor,
                        navController = navController,
                        bottomSheetScaffoldState,
                        items = tabItems,
                        onItemClick = { item -> navController.navigate(item.route!!) },
                        updateIntakeState = { newIntakeState ->
                            intakeButtonState = newIntakeState
                        },
                        updateHideBottomBar = { viewState ->
                            hideBottomBar = viewState
                        })
                }

                // Insights
                composable(TabScreen.Insights.route!!) {
                    LaunchedEffect(userAvgSweatState) {
                        val ret = chViewModel.networkManager.getAvgSweatVolumeSodiumConcentration()
                        // only call API if no error returned
                        if (ret == null) {
                            //Log.d("LaunchedEffect", "***** getAvgSweatVolumeSodiumConcentration called")
                            userAvgSweatState != userAvgSweatState
                        }
                    }

                    if (intakeButtonState == IntakeButtonState.INTAKE_SAVE) {
                        chViewModel.currentUserIntakeItems.clear()
                        chViewModel.newBottlesItemsAdded.clear()
                        chViewModel.totalWaterAmount = 0.0
                        chViewModel.totalSodiumAmount = 0.0
                        intakeButtonState = IntakeButtonState.INTAKE_UP
                    }

                    chViewModel.isTabButtonPressed.value = true

                    InsightsScreen(chViewModel, ebsMonitor, navController, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                // Settings
                composable(TabScreen.Settings.route!!) {

                    if (intakeButtonState == IntakeButtonState.INTAKE_SAVE) {
                        chViewModel.currentUserIntakeItems.clear()
                        chViewModel.newBottlesItemsAdded.clear()
                        chViewModel.totalWaterAmount = 0.0
                        chViewModel.totalSodiumAmount = 0.0
                        intakeButtonState = IntakeButtonState.INTAKE_UP
                    }

                    chViewModel.isTabButtonPressed.value = true

                    chViewModel.oldUserHeightFt = chViewModel.userHeightFt.value
                    chViewModel.oldUserHeightIn = chViewModel.userHeightIn.value
                    chViewModel.oldUserHeightCm = chViewModel.userHeightCm.value
                    chViewModel.oldUserWeightLb = chViewModel.userWeightLb.value
                    chViewModel.oldUserWeightKg = chViewModel.userWeightKg.value
                    chViewModel.oldUserGender = chViewModel.userGender.value

                    SettingsScreen(chViewModel = chViewModel, ebsMonitor, navController = navController, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                composable(SettingsSubScreens.PrivacyPolicy.route!!) {
                    PrivacyScreen(navController = navController, chViewModel, ebsMonitor)
                }

                composable(SettingsSubScreens.TermsConditions.route!!) {
                    TermsConditionsScreen(navController = navController, chViewModel, ebsMonitor)
                }

                composable(SettingsSubScreens.LegalRegulatory.route!!) {
                    LegalRegulatoryScreen(navController = navController, chViewModel, ebsMonitor)
                }

                composable(SettingsSubScreens.Debug.route!!) {
                    DebugScreen(chViewModel = chViewModel, ebsMonitor, navController = navController)
                }

                composable(SettingsSubScreens.Compliance.route!!) {
                    ComplianceScreen(navController = navController, chViewModel, ebsMonitor)
                }

                composable(SettingsSubScreens.License.route!!) {
                    LicenseScreen(navController = navController, chViewModel, ebsMonitor)
                }

                composable(SettingsSubScreens.SensorInformation.route!!) {
                    SensorInformationScreen(chViewModel = chViewModel, ebsMonitor, navController = navController , updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                composable(SettingsSubScreens.PairNewModuleMain.route!!) {
                    Step3PairModuleMainView(chViewModel = chViewModel, ebsDeviceMonitor = ebsMonitor, isNewPair = true, navController = navController, isOnboarding = false, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                composable(SettingsSubScreens.PairNewModuleScanView.route!!) {
                    Step3PairModuleScanView(chViewModel = chViewModel, ebsDeviceMonitor = ebsMonitor, isNewPair = true, isOnboarding = false, navController = navController)
                }

                composable(SettingsSubScreens.PairNewModuleIdentify.route!!) {
                    Step3PairModuleIdentify(chViewModel = chViewModel, ebsMonitor = ebsMonitor, isNewPair = true, navController = navController, isOnboarding = false, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                composable(SettingsSubScreens.PairNewModuleManuallyView.route!!) {
                    Step3PairModuleManuallyView(chViewModel = chViewModel, ebsDeviceMonitor = ebsMonitor, isNewPair = true, isOnboarding = false, navController = navController)
                }

                composable(SettingsSubScreens.PairNewModuleUnresponsiveView.route!!) {
                    Step3PairModuleUnresponsive(chViewModel, navController = navController, isNewPair = true, isOnboarding = false, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                composable(SettingsSubScreens.VerifyPhysioloyInfoView.route!!) {
                    VerifyPhysiologyInfoView(chViewModel = chViewModel, ebsDeviceMonitor = ebsMonitor, navController = navController, isOnboarding = false, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                composable(SettingsSubScreens.PairNewModuleQrScanView.route!!) {
                    QrScannerScreen(navController, Modifier.fillMaxSize(), onQrCodeScanned = { qrCode ->
                        chViewModel._qrPairingState.value = chViewModel._qrPairingState.value.copy(showLoading = true, showErrorMsg = false)
                        if (isValidDeviceSerialNumber(qrCode)) {
                            chViewModel.deviceSN.value = qrCode
                            navController.navigateUp()
                        }
                    })
                }

                // Intake subviews
                composable(IntakeScreens.AddMenuItem.route!!) {
                    AddMenuItemScreen(
                        navController = navController,
                        updateIntakeState = { newIntakeState ->
                            intakeButtonState = newIntakeState
                        })
                }

                composable(IntakeScreens.BottleListAdd.route!!) {
                    BottleListScreen(
                        chViewModel = chViewModel,
                        navController = navController,
                        updateIntakeState = { newIntakeState ->
                            intakeButtonState = newIntakeState
                        })
                }

                composable(IntakeScreens.EnterBottleManually.route!!) {
                    EnterBottleManuallyScreen(
                        chViewModel = chViewModel,
                        navController = navController,
                        updateIntakeState = { newIntakeState ->
                            intakeButtonState = newIntakeState
                        })
                }

                // Shared Screens
                composable(SharedScreens.EditEnterprise.route!!) {
                    EditEnterpriseScreen(navController = navController, ebsDeviceMonitor = ebsMonitor, chViewModel = chViewModel, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                composable(SharedScreens.EditPhysiology.route!!) {
                    PhysiologyInformationView(navController = navController, chViewModel = chViewModel, ebsMonitor = ebsMonitor, showHeading = false, onBoarding = false, isEditing = true, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                // Info popup views
                composable(InfoPopupScreens.InformationViews.route!!) {
                    InformationViews(chViewModel = chViewModel, ebsMonitor, navController, chViewModel.infoPopupScreen, updateHideBottomBar = { viewState ->
                        hideBottomBar = viewState
                    })
                }

                composable(InfoPopupScreens.EpicoreRequiredTroubleshootingView.route!!) {
                    EpicoreRequiredView(chViewModel = chViewModel, navController, EpicoreLegalScreens.TROUBLESHOOTING)
                }

                composable(InfoPopupScreens.EpicoreRequiredFaqView.route!!) {
                    EpicoreRequiredView(chViewModel = chViewModel, navController, EpicoreLegalScreens.FAQ)
                }

            }
        }
    }
}

@Composable
fun BottomNav(chViewModel: ModelData, navController: NavController, updateIntakeState: (IntakeButtonState) -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination
    BottomNavigation(
        modifier = Modifier
            .height(100.dp),
        backgroundColor = Color.White,
        elevation = 0.dp
    ) {
        tabItems.forEachIndexed { index, it ->
            if (index == 2) {
                // Empty BottomNavigationItem - used for spacing around intake
                BottomNavigationItem(
                    icon = {},
                    label = {},
                    selected = false,
                    onClick = {},
                    enabled = false
                )
            }
            BottomNavigationItem(
                icon = {
                     if (it.isSelected) {
                        Image(painterResource(id = it.selectedImageId!!),
                            modifier = Modifier.testTag("image_tab_selected"),
                            contentDescription = "image_tab_selected",
                        )
                    }
                    else {
                        Image(painterResource(id = it.unSelectedImageId!!),
                            modifier = Modifier.testTag("image_tab_unselected"),
                            contentDescription = "image_tab_unselected",
                        )
                    } },
                label = {
                    if (it.isSelected) {
                        Text(stringResource(it.title!!), fontSize = 11.sp, fontFamily = OswaldFonts, color = colorResource(R.color.intake_button_color))
                    }
                    else {
                        Text(stringResource(it.title!!), fontSize = 11.sp, fontFamily = OswaldFonts, color = colorResource(R.color.TabLabelOff))
                    } },
                selected = currentRoute?.hierarchy?.any { it.route == it.route } == true,
                selectedContentColor = colorResource(R.color.intake_button_color),
                unselectedContentColor = Color.Gray,
                onClick = {
                    if (!chViewModel._isSaveButtonShowing.value) {
                        updateIntakeState(IntakeButtonState.INTAKE_UP)
                        it.isSelected = true
                        if (!isIntakeTabSelected) {
                            selectedTabItem.isSelected = false
                        }
                        isIntakeTabSelected = false
                        selectedTabItem = it
                        navController.navigate(it.route!!) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select tabItems
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // re-selecting the same item
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}