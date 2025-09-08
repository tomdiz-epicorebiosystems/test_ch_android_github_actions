package com.epicorebiosystems.rehydrate.tabViews.todayViews

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.datadog.android.compose.ExperimentalTrackingApi
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.getUserLocation
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.DisconnectEvent
import com.epicorebiosystems.rehydrate.sharedViews.BgStatusView
import com.epicorebiosystems.rehydrate.tabItems
import com.epicorebiosystems.rehydrate.topBarViews.AppUpdateUrls
import com.epicorebiosystems.rehydrate.topBarViews.NotificationConstants
import com.epicorebiosystems.rehydrate.topBarViews.NotificationData
import com.epicorebiosystems.rehydrate.topBarViews.NotificationLocation
import com.epicorebiosystems.rehydrate.topBarViews.NotificationShowOptions
import com.epicorebiosystems.rehydrate.topBarViews.NotificationType
import com.epicorebiosystems.rehydrate.topBarViews.NotificationView
import com.epicorebiosystems.rehydrate.topBarViews.TopBarView
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTrackingApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TodayScreen(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController, updateHideBottomBar: (Boolean) -> Unit) {
    var isSuggestedIntakeExpanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var isBluetoothEnabled = remember { mutableStateOf(true) }

    DisposableEffect(Unit) {

        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

        val mBluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                        BluetoothAdapter.STATE_OFF -> {
                            isBluetoothEnabled.value = false
                            //Log.i("Bluetooth", "State OFF")

                            ebsDeviceMonitor.onEvent(DisconnectEvent)
                            ebsDeviceMonitor.disconnect()
                            
                        }
                        BluetoothAdapter.STATE_ON -> {
                            isBluetoothEnabled.value = true
                            //Log.i("Bluetooth", "State ON")

                            chViewModel._isSensorConnected.value = false
                            chViewModel.isCHDeviceConnected = false

                            chViewModel.initDeviceOnce.value = false

                        }
                    }

                }
            }
        }

        context.registerReceiver(mBluetoothStateReceiver, intentFilter)

        onDispose {
            context.unregisterReceiver(mBluetoothStateReceiver)
        }
    }

    // Get users GPS location
    chViewModel.currLatLong = getUserLocation(LocalContext.current)
    Log.d("LAT_LONG_LOCATION", "${chViewModel.currLatLong!!.latitude},${chViewModel.currLatLong!!.longitude}")
    ebsDeviceMonitor.setLatitudeLongitude(chViewModel.currLatLong!!.latitude, chViewModel.currLatLong!!.longitude)
    ebsDeviceMonitor.setEnterpriseId(chViewModel.enterpriseId.value)
    ebsDeviceMonitor.setUserId(chViewModel.currentAuthUserId.value)
    ebsDeviceMonitor.setDeviceSN(chViewModel.deviceSN.value)
    ebsDeviceMonitor.setPassiveWaterLoss(chViewModel.userPassiveLossState.value)

    Scaffold (
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopBarView(chViewModel, ebsDeviceMonitor, navController, updateHideBottomBar = { viewState ->
                updateHideBottomBar(viewState)
            })

            NotificationView(chViewModel)

        }
    ) {
        BgStatusView(chViewModel, ebsDeviceMonitor)

        BoxWithConstraints() {
            val widthModifier = maxWidth - 20.dp
            LazyColumn(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .height(if (!isSuggestedIntakeExpanded) 1220.dp else 1640.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // See if we need to show app update notification
                if (chViewModel.showAppUpdateAvailable) {
                    // Only run once a day
                    val calendar: Calendar = Calendar.getInstance()
                    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                    if (chViewModel.lastCheckAppUpdate.value != currentDay) {
                        val applicationInfo = chViewModel.applicationContext!!.applicationInfo
                        val stringId = applicationInfo.labelRes
                        val appName = if (stringId == 0)
                            applicationInfo.nonLocalizedLabel.toString()
                        else
                            chViewModel.applicationContext!!.getString(stringId)

                        chViewModel.notificationData = NotificationData(
                            id = NotificationConstants.APP_UPDATE_AVAIL_NOTIFICATION,
                            title = "New $appName Application",
                            detail = "There is a new $appName Application available in Google Play.",
                            type = NotificationType.Info,
                            notificationLocation = NotificationLocation.Top,
                            showOnce = true,
                            showSeconds = NotificationShowOptions.showNoClose,
                            appUrl = AppUpdateUrls.epicoreCH_GooglePlayLink
                        )
                        chViewModel.showNotification()
                        chViewModel.saveLastAppUpdateNotificationState(currentDay)
                    }
                    chViewModel.showAppUpdateAvailable = false
                }

                if (!isBluetoothEnabled.value) {
                    item {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = {
                                isBluetoothEnabled.value = false
                            },
                            title = {
                                Text(stringResource(R.string.bluetooth))
                            },
                            text = {
                                Text(stringResource(R.string.turn_on_bluetooth))
                            },
                            confirmButton = {
                                Button(
                                    onClick = trackClick(targetName = "Bluetooth disabled settings pressed") {
                                        isBluetoothEnabled.value = false
                                        val i = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                        context.startActivity(i)
                                    }) {
                                    Text(stringResource(R.string.tab_settings))
                                }
                            },
                            dismissButton = { }
                        )
                    }
                }
                else {
                    isBluetoothEnabled.value = true
                }

                item {
                    SuggestedIntakeView(
                        chViewModel,
                        ebsDeviceMonitor,
                        widthModifier,
                        updateIntakeExpanded = { isExpanded ->
                            isSuggestedIntakeExpanded = isExpanded
                        },
                        items = tabItems,
                        onItemClick = {
                            item -> navController.navigate(item.route!!) {
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
                        },
                    )
                }

                item {
                    StatsChartsView(chViewModel, ebsDeviceMonitor, widthModifier)
                }

                item {
                    WorkDaySummaryView(chViewModel, ebsDeviceMonitor, widthModifier)
                }

            }
        }
    }
}