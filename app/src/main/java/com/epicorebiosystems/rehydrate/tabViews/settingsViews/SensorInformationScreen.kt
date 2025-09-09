package com.epicorebiosystems.rehydrate.tabViews.settingsViews

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.SettingsSubScreens
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.getBatteryLifeLeftInDays
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.DisconnectEvent
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnRunInput
import com.epicorebiosystems.rehydrate.sharedViews.BgStatusView
import com.epicorebiosystems.rehydrate.sharedViews.SegmentedControl
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScanningState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SensorInformationScreen(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController, updateHideBottomBar: (Boolean) -> Unit) {

    val uartStateMgr = ebsDeviceMonitor.uartState.collectAsState().value
    var allowUnpair by rememberSaveable { mutableStateOf(false) }
    var showSensorWaveform by rememberSaveable { mutableStateOf(false) }
    var sweatWaveformChannelSelect = 6   // Show fluidic encoder channel (channel 6) waveform first after load
    val isInternetConnectivityAlertShowing = remember { mutableStateOf(false)  }
    val showUploadingAlert = remember { mutableStateOf(false)  }
    val delayScanScope  = rememberCoroutineScope()
    val clearFileUploadErrorFlag = remember { mutableStateOf(true) }

    var imuData = LineData()

    LaunchedEffect(clearFileUploadErrorFlag) {
        chViewModel.networkUploadFailed.value = false
        clearFileUploadErrorFlag.value = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings),
                    modifier = Modifier
                        .offset(x = -(35).dp)
                        .clickable {
                            navController.navigateUp()
                        },
                    fontFamily = OswaldFonts,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.linkStandardText))
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.offset(x = -(15).dp, y = 2.dp),
                        onClick = trackClick(targetName = "SensorInformationScreen back pressed") { navController.navigateUp() }
                    ) {
                        Image(
                            painterResource(R.drawable.baseline_chevron_left_24),
                            contentDescription = "image_back",
                            colorFilter = ColorFilter.tint(colorResource(R.color.linkStandardText))
                        )
                    }
                }
            )
        }
    ) {
        BgStatusView(chViewModel, ebsDeviceMonitor)

        BoxWithConstraints {
            val widthModifier = maxWidth - 20.dp
            //val heightModifier = maxHeight - 200.dp
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .height(if (showSensorWaveform) 1220.dp else 1040.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        //.height(heightModifier)
                        .width(widthModifier)
                        .offset(x = 10.dp, y = 120.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                ) {

                    if (showUploadingAlert.value) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = {
                                showUploadingAlert.value = false
                            },
                            title = {
                                Text(stringResource(R.string.data_upload_in_progress))
                            },
                            text = {
                                Text(stringResource(R.string.please_wait_until_it_s_completed))
                            },
                            confirmButton = { },
                            dismissButton = {
                                Button(
                                    onClick = trackClick(targetName = "SensorInformationScreen ok pressed") {
                                        showUploadingAlert.value = false
                                    }) {
                                    Text(stringResource(R.string.hydration_ok))
                                }
                            }
                        )
                    }

                    if (isInternetConnectivityAlertShowing.value) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = {
                                isInternetConnectivityAlertShowing.value = false
                            },
                            title = {
                                Text(stringResource(R.string.no_internet))
                            },
                            text = {
                                Text(stringResource(R.string.please_check_your_network_and_try_again))
                            },
                            confirmButton = { },
                            dismissButton = {
                                Button(
                                    onClick = trackClick(targetName = "SensorInformationScreen ok pressed - no internet") {
                                        isInternetConnectivityAlertShowing.value = false
                                    }) {
                                    Text(stringResource(R.string.hydration_ok))
                                }
                            }
                        )
                    }

                    Column {
                        Text(stringResource(R.string.sensor_information),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp),
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.settingsColorCoalText))

                        Divider(color = Color.Gray, thickness = 1.dp)

                        Row(
                            modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp, end = 20.dp)
                        ) {
                            Text(
                                stringResource(R.string.sensorinfo_serial_number),
                                //Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp),
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.settingsColorCoalText))

                            Spacer(modifier = Modifier.weight(1f))

                            ebsDeviceMonitor.getDeviceName()?.let { it ->
                                Text(it,
                                    fontFamily = OswaldFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = colorResource(R.color.settingsColorCoalText))
                            }
                        }

                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(70.dp)
                                    .padding(top = 10.dp, bottom = 10.dp),
                                onClick = trackClick(targetName = "SensorInformationScreen ebsDeviceMonitor.onEvent(0x5A)") {
                                    val identifyCommandBytes: ByteArray = byteArrayOf(0x5A)
                                    ebsDeviceMonitor.onEvent(OnRunInput(identifyCommandBytes))
                                },
                                border = BorderStroke(
                                    1.dp,
                                    colorResource(R.color.settingsColorHydroDarkText)
                                ),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorResource(
                                        R.color.settingsColorHydroDarkText
                                    )
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stringResource(R.string.find_my),
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        textAlign = TextAlign.Center,
                                        fontFamily = OswaldFonts,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.settingsColorCoalText)
                                    )

                                    Image(
                                        painterResource(R.drawable.device_buzzer_2),
                                        contentDescription = "image_device_buzzer",
                                        colorFilter = ColorFilter.tint(colorResource(R.color.onboardingLtBlueColor))
                                    )
                                }
                            }
                        }

                        Divider(color = Color.Gray, thickness = 1.dp)

                        Row(
                            modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp, end = 20.dp)
                        ) {
                            Text(
                                stringResource(R.string.sensorinfo_sensor_status),
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.settingsColorCoalText))

                            Spacer(modifier = Modifier.weight(1f))

                            when (uartStateMgr.uartManagerState.connectionState?.state) {
                                null,
                                GattConnectionState.STATE_CONNECTING -> {
                                    Text(
                                        stringResource(R.string.sensorinfo_paired),
                                        fontFamily = OswaldFonts,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.settingsColorCoalText))
                                    allowUnpair = false
                                }
                                GattConnectionState.STATE_DISCONNECTED -> {
                                    Text(
                                        if (chViewModel.isTestAccount()) stringResource(R.string.sensorinfo_paired) else stringResource(R.string.sensorinfo_unpaired),
                                        fontFamily = OswaldFonts,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.settingsColorCoalText))
                                    allowUnpair = false
                                }
                                GattConnectionState.STATE_DISCONNECTING -> {
                                    Text(
                                        if (chViewModel.isTestAccount()) stringResource(R.string.sensorinfo_paired) else stringResource(R.string.sensorinfo_unpaired),
                                        fontFamily = OswaldFonts,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.settingsColorCoalText))
                                    allowUnpair = false
                                }
                                GattConnectionState.STATE_CONNECTED -> {
                                    Text(
                                        stringResource(R.string.sensorinfo_paired),
                                        fontFamily = OswaldFonts,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.settingsColorCoalText))
                                    allowUnpair = true
                                }
                            }
                        }

                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(70.dp)
                                    .padding(top = 10.dp, bottom = 10.dp),
                                onClick = trackClick(targetName = "Open SettingsSubScreens.PairNewModuleMain") {
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        updateHideBottomBar(true)
                                        navController.navigate(SettingsSubScreens.PairNewModuleMain.route!!)
                                    }
                                },
                                border = BorderStroke(
                                    1.dp,
                                    colorResource(R.color.settingsColorHydroDarkText)
                                ),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorResource(
                                        R.color.settingsColorHydroDarkText
                                    )
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stringResource(R.string.pair_to_new_module),
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        textAlign = TextAlign.Center,
                                        fontFamily = OswaldFonts,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.settingsColorCoalText)
                                    )
                                }
                            }
                        }

                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(70.dp)
                                    .padding(top = 10.dp, bottom = 10.dp),
                                onClick = trackClick(targetName = "Open SettingsSubScreens.UnPairModuleMain") {

                                    // disconnect
                                    ebsDeviceMonitor.onEvent(DisconnectEvent)
                                    ebsDeviceMonitor.disconnect()

                                    // Reset the scanned sensor name and connection state
                                    chViewModel.deviceSN.value = ""
                                    chViewModel.isCHDeviceConnected = false
                                    chViewModel._isSensorConnected.value = false

                                    // Clear device scan array and start scan again
                                    ScanningState.DevicesDiscovered(emptyList())
                                    ebsDeviceMonitor.scanBluetoothDevice()

                                },
                                enabled = allowUnpair,
                                border = BorderStroke(
                                    1.dp,
                                    colorResource(R.color.settingsColorHydroDarkText)
                                ),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorResource(
                                        R.color.settingsColorHydroDarkText
                                    )
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stringResource(R.string.sensorinfo_unpair),
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        textAlign = TextAlign.Center,
                                        fontFamily = OswaldFonts,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.settingsColorCoalText)
                                    )
                                }
                            }
                        }

                        Divider(color = Color.Gray, thickness = 1.dp)

                        Row(
                            modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp, end = 20.dp)
                        ) {
                            Text(
                                stringResource(R.string.sensorinfo_firmware_rev),
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.settingsColorCoalText))

                            Spacer(modifier = Modifier.weight(1f))

                            val fwRevMajor = ebsDeviceMonitor.getSysInfo()?.fwRevisonString ?: "0.0"
                            Text( if (chViewModel.isTestAccount()) "v3.22" else fwRevMajor,
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.settingsColorCoalText))
                        }

                        Divider(color = Color.Gray, thickness = 1.dp)

                        Row(
                            modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp, end = 20.dp)
                        ) {
                            Text(
                                stringResource(R.string.sensorinfo_battery_left),
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.settingsColorCoalText))

                            Spacer(modifier = Modifier.weight(1f))

                            val batteryVolt = ebsDeviceMonitor.getSweatStatusPacket()?.batteryVoltageInV ?: 3.0
                            val batteryDays = batteryVolt?.let { getBatteryLifeLeftInDays(it.toDouble()) } ?: 70

                            Text(
                                stringResource(R.string.sensorinfo_days, batteryDays),
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.settingsColorCoalText))
                        }

                        Divider(color = Color.Gray, thickness = 1.dp)

                        Row(
                            modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp, end = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.sensorinfo_rf_signal_strength),
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.settingsColorCoalText))

                            Spacer(modifier = Modifier.weight(1f))

                            Image(painterResource(getSignalBarImageFromRSSI(chViewModel.deviceRSSI)), "image_device_rss1", Modifier.size(50.dp))
                        }

                        Divider(color = Color.Gray, thickness = 1.dp)

                        Row(
                            modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp, end = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.sensorinfo_device_status),
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.settingsColorCoalText))

                            Spacer(modifier = Modifier.weight(1f))

                            val deviceStatus = ebsDeviceMonitor.getSysInfo()?.brownOutResetCounter ?: 0
                            var deviceStatusStr = stringResource(R.string.sensorinfo_ok)
                            when (deviceStatus) {
                                0.toUByte() -> deviceStatusStr = stringResource(R.string.sensorinfo_ok)
                                1 -> deviceStatusStr = stringResource(R.string.sensorinfo_ppg_fail)
                                2 -> deviceStatusStr = stringResource(R.string.sensorinfo_imu_fail)
                                3 -> deviceStatusStr =
                                    stringResource(R.string.sensorinfo_ppg_imu_fail)
                                4 -> deviceStatusStr = stringResource(R.string.sensorinfo_mem_fail)
                                5 -> deviceStatusStr =
                                    stringResource(R.string.sensorinfo_ppg_mem_fail)
                                6 -> deviceStatusStr =
                                    stringResource(R.string.sensorinfo_imu_mem_fail)
                                7 -> deviceStatusStr = stringResource(R.string.sensorinfo_fail)
                                else -> {
                                    if (chViewModel.isTestAccount()) {
                                        deviceStatusStr = "OK"
                                    }
                                    else {
                                        deviceStatusStr = " "
                                    }
                                }
                            }
                            Text("${deviceStatusStr}",
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.settingsColorCoalText))
                        }

                        Divider(color = Color.Gray, thickness = 1.dp)

                        Row(
                            modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 15.dp, end = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.sensor_info_sensor_waveform),
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.settingsColorCoalText)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Switch(
                                checked = showSensorWaveform,
                                onCheckedChange = { showSensorWaveform = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colorResource(R.color.waterFull),
                                    checkedBorderColor = colorResource(R.color.waterFull),
                                    uncheckedThumbColor = colorResource(R.color.switch_off_thumb),
                                    uncheckedTrackColor = colorResource(R.color.switch_off_track),
                                    uncheckedBorderColor = colorResource(R.color.switch_off_boarder),
                                )
                            )
                        }

                        Divider(color = Color.Gray, thickness = 1.dp)

                        if (showSensorWaveform) {
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val units = listOf("CL", "R1", "R2", "R3", "R4", "ENC")
                                SegmentedControl(
                                    items = units,
                                    defaultSelectedItemIndex = 0
                                ) {
                                    Log.d("SensorToggle", "Selected item : ${units[it]}")
                                    // Sweat waveform channel on sensor side is 1-based.
                                    sweatWaveformChannelSelect = it + 1

                                    // Send command to change waveform selection channel on the sensor side
                                    imuData.clearValues()
                                    val sweatWaveFormSelectionCommandBytes : ByteArray = byteArrayOf(0x58, sweatWaveformChannelSelect.toByte())
                                    ebsDeviceMonitor.onEvent(OnRunInput(sweatWaveFormSelectionCommandBytes))
                                }
                            }

                            // sweatDataWaveformSamplesInMv
                            val sweatWaveformSampleData = uartStateMgr.uartManagerState.sweatDataWaveformSamplesInMv

                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 30.dp, top = 10.dp)
                                    .height(160.dp),
                                factory = { context ->
                                    LineChart(context)
                                          },
                                update = { lineChart ->
                                    //val sweatDataWaveformSampleCount = 80

                                    var sensorWaveformChartData: List<Entry> =
                                        sweatWaveformSampleData.mapIndexed { index, waveFormData ->
/*
                                                var waveformSamplesAvg : Double = 0.0
                                                var waveformSampleMin: Int = 3000
                                                var waveformSampleMax: Int = 0

                                                waveformSamplesAvg += (waveFormData.toDouble() / sweatDataWaveformSampleCount.toDouble())

                                                if (waveFormData > waveformSampleMax)  {
                                                    waveformSampleMax = waveFormData
                                                }

                                                if (waveFormData < waveformSampleMin) {
                                                    waveformSampleMin = waveFormData
                                                }
*/
                                            Entry(index.toFloat(), (waveFormData.toFloat() / 1000.0f))
                                        }

                                    var lineX = LineDataSet(sensorWaveformChartData, "V").apply {
                                        color = Color(0xFF0000FF).toArgb()
                                        setDrawCircles(false)
                                    }

                                    imuData = LineData(lineX).apply { }
                                    imuData.setDrawValues(false)

                                    lineChart.apply {
                                        lineData.apply {
                                            setNoDataText(context.resources.getString(R.string.sensorinfo_loading))
                                            setNoDataTextColor(Color.Blue.toArgb())
                                            //setNoDataTextTypeface("Helvetica", size: 20.0)
                                            //lineChart.setNoDataTextTypeface()
                                            lineChart.getPaint(Chart.PAINT_INFO).textSize = 20f
                                            lineChart.getPaint(Chart.PAINT_INFO).color = Color.Blue.toArgb()
                                            lineChart.data?.clearValues()
                                        }
                                        xAxis.apply {
                                            setDrawGridLines(true)
                                            //position = XAxis.XAxisPosition.BOTTOM
                                            //axisMaximum = sweatWaveformSampleData.size + 1.0f
                                        }
                                        axisLeft.apply {
                                            setDrawLabels(true)
                                            setDrawGridLines(true)
                                            axisMaximum = 3.0f
                                            axisMinimum = 0.0f
                                            setLabelCount(6, false)
                                        }
                                        axisRight.apply {
                                            setDrawLabels(false)
                                            setDrawGridLines(false)
                                        }
                                        //legend.apply {
                                        //    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                                        //    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                                        //}
                                        description.text = ""
                                        data = imuData
                                        isDragEnabled = false
                                        setScaleEnabled(false)
                                        notifyDataSetChanged()
                                        invalidate()
                                    }
                                }
                            )
                        }

                        if (chViewModel.networkUploadSuccess.value) {
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    stringResource(R.string.data_csv_file_upload_success),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Green)
                            }
                        }

                        if (chViewModel.networkUploadFailed.value) {
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    stringResource(R.string.upload_failed) + chViewModel.networkUploadFailedMsg,
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Red)
                            }
                        }

                        Row (
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                modifier = Modifier
                                    .width(150.dp)
                                    .padding(top = 20.dp),
                                onClick = trackClick(targetName = "SensorInformationScreen file uploading") {
                                    if (chViewModel.isSweatDataDownloadProgressAlertShowing) {
                                        showUploadingAlert.value = true
                                    }
                                    else {
                                        // File is already uploading in background on Today Vieew
                                        if (!chViewModel.csvFileIsUploading.value) {
                                            // Check if the internet connectivity is available before uploading starts.
                                            // If yes, go ahead and start the uploading, if not remind user to get internet
                                            // connectivity before they can upload.
                                            if (chViewModel.isNetworkConnected) {

                                                // This would start the multi-day data sync with sensor and cloud.
                                                if (chViewModel.sweatDataMultiDaySyncWithSensorCompleted && chViewModel.historicalSweatDataDownloadCompleted) {
                                                    chViewModel.networkUploadSuccess.value = false
                                                    chViewModel.networkUploadFailed.value = false
                                                    chViewModel.isSweatDataDownloadProgressAlertShowing = true
                                                    delayScanScope.launch {
                                                        chViewModel.networkManager.getNewRefreshToken()
                                                        ebsDeviceMonitor.scanDeviceCurrentDayData(uartStateMgr) //, chViewModel)

                                                        // Add a timeout here for the data downloading/uploading so that it won't hang the system.
                                                        delay(20000)
                                                        Log.d("scanDeviceData", "Delay complete!")
                                                        if (!chViewModel.sweatDataCurrentDayDownloadingCompleted) {
                                                            chViewModel.sweatDataCurrentDayDownloadingCompleted= true
                                                            ebsDeviceMonitor.setCurrentDayDownloadingCompletedFlagForHistoricalData(true)
                                                        }

                                                        if (!chViewModel.sweatDataMultiDaySyncWithSensorCompleted) {
                                                            chViewModel.sweatDataMultiDaySyncWithSensorCompleted = true
                                                        }

                                                        if (chViewModel.csvFileIsUploading.value) {
                                                            chViewModel.setCsvFileIsUploading(false)
                                                        }

                                                        if (!uartStateMgr.uartManagerState.sweatDataLogDownloadCompleted) {
                                                            ebsDeviceMonitor.setSweatDataLogDownloadCompletedFlag(true)
                                                        }

                                                    }
                                                }
                                            } else {
                                                isInternetConnectivityAlertShowing.value = true
                                            }
                                        }
                                    }
                                },
                                border = BorderStroke(
                                    1.dp,
                                    colorResource(R.color.settingsColorHydroDarkText)
                                ),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorResource(
                                        R.color.settingsColorHydroDarkText
                                    )
                                )
                            ) {
                                Text(
                                    stringResource(R.string.sensorinfo_sync_now),
                                    fontFamily = OswaldFonts,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(R.color.settingsColorCoalText)
                                )
                            }
                        }

                   }
                }
            }
        }
    }

}

fun getSignalBarImageFromRSSI(deviceRSSI: Int): Int {
    return if (deviceRSSI < -90) {
        R.drawable.signal_bar_0
    }
    else if (deviceRSSI < -80) {
        R.drawable.signal_bar_25
    }
    else if (deviceRSSI < -70) {
        R.drawable.signal_bar_50
    }
    else if (deviceRSSI < -50) {
        R.drawable.signal_bar_75
    }
    else {
        R.drawable.signal_bar_100
    }
}