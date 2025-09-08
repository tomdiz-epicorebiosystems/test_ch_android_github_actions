package com.epicorebiosystems.rehydrate.topBarViews

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.epicorebiosystems.rehydrate.InfoPopupScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.getBatteryLifeLeftInDays
import com.epicorebiosystems.rehydrate.modelData.getCHDeviceBatteryLevel
import com.epicorebiosystems.rehydrate.modelData.getCurrentDeviceNetworkImage
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnRunInput
import com.epicorebiosystems.rehydrate.ui.theme.JostVariableFonts
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScanningState
import java.time.format.DateTimeFormatter
import java.util.Date

@Composable
fun TopAppBarConnectivityDropdownMenu(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController, updateHideBottomBar: (Boolean) -> Unit) {
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"

    var connExpanded by rememberSaveable { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var isModuleView by rememberSaveable { mutableStateOf(true) }
    val uartStateMgr = ebsDeviceMonitor.uartState.collectAsState().value
    val fileUploading = chViewModel.csvFileIsUploading.collectAsState().value
    val deviceConnected = chViewModel.isSensorConnected.collectAsState().value
    val scopeConnectivity  = rememberCoroutineScope()

    Box(
        Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = {
            connExpanded = true
        }) {

            scopeConnectivity.launch {

                when (uartStateMgr.uartManagerState.connectionState?.state) {
                    null,
                    GattConnectionState.STATE_CONNECTING -> {
                        //Log.d("getCurrentDeviceImage", "GattConnectionState.STATE_CONNECTING")
                        //chViewModel._isSensorConnected.value = false
                    }

                    GattConnectionState.STATE_DISCONNECTED -> {
                        //Log.d("getCurrentDeviceImage", "GattConnectionState.STATE_DISCONNECTED")
                        chViewModel._isSensorConnected.value = false
                        chViewModel.isCHDeviceConnected = false

                        chViewModel.initDeviceOnce.value = false

//                        ebsDeviceMonitor.scanBluetoothDevice()

                        // Clear all the flags for downloading and uploading upon disconnection.
                        chViewModel.sweatDataCurrentDayDownloadingCompleted = true
                        chViewModel.sweatDataMultiDaySyncWithSensorCompleted = true
                        chViewModel.historicalSweatDataDownloadCompleted = true
                        chViewModel.setCsvFileIsUploading(false)

                        ebsDeviceMonitor.clearDuplicateHash()

                        ebsDeviceMonitor.setFileReadyUploadFlag(false)
                        ebsDeviceMonitor.resetSweatDataLogCSVText()

                    }

                    GattConnectionState.STATE_DISCONNECTING -> {
                        Log.d("getCurrentDeviceImage", "GattConnectionState.STATE_DISCONNECTING")
                        chViewModel._isSensorConnected.value = false
                        chViewModel.isCHDeviceConnected = false

                        chViewModel.initDeviceOnce.value = false

                        if (chViewModel.isCurrentUserSession && chViewModel.isUserSessionToDisplay) {
//                            ebsDeviceMonitor.scanBluetoothDevice()
                        }

                        // Clear all the flags for downloading and uploading upon disconnection.
                        chViewModel.sweatDataCurrentDayDownloadingCompleted = true
                        chViewModel.sweatDataMultiDaySyncWithSensorCompleted = true
                        chViewModel.historicalSweatDataDownloadCompleted = true
                        chViewModel.setCsvFileIsUploading(false)

                        ebsDeviceMonitor.clearDuplicateHash()

                        ebsDeviceMonitor.setFileReadyUploadFlag(false)
                        ebsDeviceMonitor.resetSweatDataLogCSVText()
                    }

                    GattConnectionState.STATE_CONNECTED -> {

                        // After device is connected, set timestamp and check system status and info
                        if(!chViewModel.isCHDeviceConnected) {

                            //Log.d("getCurrentDeviceImage", "GattConnectionState.STATE_CONNECTED")
                            chViewModel._isSensorConnected.value = true
                            chViewModel.isCHDeviceConnected = true

                            // Stop BLE scanning after connection
                            ebsDeviceMonitor.stopScanningJob()
                            ScanningState.DevicesDiscovered(emptyList())

                            // Get system and user information command
                            val getSystemAndUserInfoCommandBytes: ByteArray = byteArrayOf(0x50)
                            ebsDeviceMonitor.onEvent(OnRunInput(getSystemAndUserInfoCommandBytes))

                            delay(100L)

                            ebsDeviceMonitor.setSweatSensingStartTimestamp()

                            delay(100L)

                            ebsDeviceMonitor.setButtonPressWaterIntakeVolumeInMl()

                            delay(100L)

                            // Start device status
                            val getDeviceStatusCommandBytes: ByteArray = byteArrayOf(0x51, 0xA5.toByte())
                            ebsDeviceMonitor.onEvent(OnRunInput(getDeviceStatusCommandBytes))

                            delay(100L)

//                            // Get system and user information command
//                            val getSystemAndUserInfoCommandBytes: ByteArray = byteArrayOf(0x50)
//                            ebsDeviceMonitor.onEvent(OnRunInput(getSystemAndUserInfoCommandBytes))
                        }

                    }
                }

            }

            val resId = getCurrentDeviceNetworkImage(chViewModel.isNetworkConnected, deviceConnected, fileUploading)
            Image(painterResource(resId), contentDescription = "TopBar Connectivity")
        }
    }

    DropdownMenu(
        expanded = connExpanded,
        onDismissRequest = { connExpanded = false },
    ) {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val screenWidth = configuration.screenWidthDp.dp

        Box(
            Modifier
                .width(screenWidth - 20.dp)
                .height(if (isExpanded) screenHeight - 180.dp else 130.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val resId = getCurrentDeviceNetworkImage(chViewModel.isNetworkConnected, chViewModel.isCHDeviceConnected, false)

                    Image(painterResource(resId), contentDescription = "")

                    Spacer(modifier = Modifier.width(if (isJapanese) 5.dp else 10.dp))

                    Column(
                        modifier = Modifier.offset(y = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(painterResource(R.drawable.icon_connex_divider_ok), contentDescription = "")
                        Row {
                            Text(
                                stringResource(R.string.conn_synced),
                                fontFamily = RobotoRegularFonts,
                                fontSize = if (isJapanese) 8.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black)

                            Spacer(modifier = Modifier.width(10.dp))

                            var currentDateAndTime = "--:--"
                            if (chViewModel.syncDate != null) {
                                val syncDate = chViewModel.syncDate ?: Date()
                                val instant = Instant.fromEpochMilliseconds(syncDate.time)
                                val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                                val formatter = DateTimeFormatter.ofPattern("hh:mm")
                                currentDateAndTime = localDateTime.toJavaLocalDateTime().format(formatter)
                            }

                            Text("$currentDateAndTime",
                                fontFamily = RobotoRegularFonts,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black)

                        }
                    }

                    Spacer(modifier = Modifier.width(if (isJapanese) 5.dp else 10.dp))

                    Image(painterResource(R.drawable.icon_connex_phone), contentDescription = "")

                    Spacer(modifier = Modifier.width(if (isJapanese) 5.dp else 10.dp))

                    Column(
                        modifier = Modifier.offset(y = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(painterResource(R.drawable.icon_connex_divider_ok), contentDescription = "")
                        Row {
                            Text(
                                stringResource(R.string.conn_updated),
                                fontFamily = RobotoRegularFonts,
                                fontSize = if (isJapanese) 8.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black)

                            Spacer(modifier = Modifier.width(10.dp))

                            var currentDateAndTime = "--:--"
                            if (chViewModel.updateDate != null) {
                                val syncDate = chViewModel.syncDate ?: Date()
                                val instant = Instant.fromEpochMilliseconds(syncDate.time)
                                val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                                val formatter = DateTimeFormatter.ofPattern("hh:mm")
                                currentDateAndTime = localDateTime.toJavaLocalDateTime().format(formatter)
                            }

                            Text("$currentDateAndTime",
                                fontFamily = RobotoRegularFonts,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black)

                        }
                    }

                    Spacer(modifier = Modifier.width(if (isJapanese) 5.dp else 10.dp))

                    if (chViewModel.isNetworkConnected) {
                        Image(painterResource(R.drawable.icon_connex_cloud_up), contentDescription = "")
                    }
                    else {
                        Image(painterResource(R.drawable.icon_connex_cloud_down), contentDescription = "")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val batteryVolt = ebsDeviceMonitor.getSweatStatusPacket()?.batteryVoltageInV
                    val batteryDays = batteryVolt?.let { getBatteryLifeLeftInDays(it) }
                    val batteryLevel = getCHDeviceBatteryLevel(batteryDays ?: 70)
                    Image(painterResource(batteryLevel), contentDescription = "")

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        batteryDays?.let { stringResource(R.string.conn_days, it) } ?: "70 days",
                        fontFamily = RobotoRegularFonts,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray)

                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            isExpanded = !isExpanded
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        elevation =  ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp)
                    ) {
                        Text(
                            stringResource(R.string.conn_troubleshoot),
                            modifier = Modifier.padding(end = 10.dp),
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.LightGray)

                        if (isExpanded) {
                            Image(
                                painterResource(R.drawable.connect_expand_arrow_down),
                                contentDescription = ""
                            )
                        } else {
                            Image(
                                painterResource(R.drawable.connect_expand_arrow_up),
                                contentDescription = ""
                            )
                        }
                    }
                }

                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Divider(modifier = Modifier
                            .padding(top = 5.dp, start = 10.dp, end = 10.dp)
                            .height(3.dp))

                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                onClick = {
                                    isModuleView = true
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                Text(
                                    stringResource(R.string.conn_module_sync),
                                    modifier = Modifier.padding(end = if (isJapanese) 5.dp else 10.dp),
                                    fontFamily = JostVariableFonts,
                                    fontSize = if (isJapanese) 13.sp else 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isModuleView) colorResource(R.color.connexSelectionColor) else colorResource(
                                        R.color.grayStandardText
                                    )
                                )
                            }

                            Divider(
                                color = Color.Gray, modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp)
                            )

                            Button(
                                onClick = {
                                    isModuleView = false
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                Text(
                                    stringResource(R.string.conn_cloud_update),
                                    modifier = Modifier.padding(start = if (isJapanese) 5.dp else 10.dp),
                                    fontFamily = JostVariableFonts,
                                    fontSize = if (isJapanese) 13.sp else 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isModuleView) colorResource(R.color.grayStandardText) else colorResource(
                                        R.color.connexSelectionColor
                                    )
                                )

                            }
                        }

                        if (isModuleView) {
                            if (chViewModel.isNetworkConnected && chViewModel.isCHDeviceConnected) {
                                Text(
                                    stringResource(R.string.your_module_is_successfully_communicating),
                                    modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black)
                            }
                            else if (chViewModel.isNetworkConnected && !chViewModel.isCHDeviceConnected) {
                                ModuleSharedText(chViewModel, navController, hideView = { viewState ->
                                    connExpanded = viewState
                                })
                            }
                            else if (!chViewModel.isNetworkConnected && chViewModel.isCHDeviceConnected) {
                                Text(
                                    stringResource(R.string.your_module_is_successfully_communicating),
                                    modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black)
                            }
                            else {
                                ModuleSharedText(chViewModel, navController, hideView = { viewState ->
                                    connExpanded = viewState
                                })
                            }
                        }
                        else {
                            if (chViewModel.isNetworkConnected && chViewModel.isCHDeviceConnected) {
                                Text(
                                    stringResource(R.string.your_phone_is_connected_to_the_internet),
                                    modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black)
                            }
                            else if (chViewModel.isNetworkConnected && !chViewModel.isCHDeviceConnected) {
                                Text(
                                    stringResource(R.string.your_phone_is_connected_to_the_internet_you),
                                    modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black)
                            }
                            else if (!chViewModel.isNetworkConnected && chViewModel.isCHDeviceConnected) {
                                CloudSharedText()
                            }
                            else {
                                CloudSharedText()
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 10.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Button(
                                    onClick = {
                                        connExpanded = false
                                        chViewModel.infoPopupScreen = InfoScreens.SUPPORT
                                        navController.navigate(InfoPopupScreens.InformationViews.route!!)
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                                    elevation = ButtonDefaults.elevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 0.dp,
                                        disabledElevation = 0.dp
                                    )
                                ) {
                                    Text(
                                        stringResource(R.string.conn_get_help),
                                        modifier = Modifier.padding(end = 20.dp),
                                        fontFamily = OswaldFonts,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.connexSelectionColor)
                                    )
                                }
                            }
                        }

                    }   // isExpanded column

                }

            }   // Column

        }   // Box
    }
}

@Composable
fun ModuleSharedText(chViewModel: ModelData, navController: NavController, hideView: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .height(460.dp)
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val placeholder = stringResource(R.string.you_can_check_the_following)
        val globalText = stringResource(id = R.string.your_module_can_hold_16_hours, placeholder)
        val start = globalText.indexOf(placeholder)
        val spanStyles = listOf(
            AnnotatedString.Range(
                SpanStyle(fontWeight = FontWeight.Bold),
                start = start,
                end = start + placeholder.length
            )
        )
        Text(
            text = AnnotatedString(
                text = globalText,
                spanStyles = spanStyles
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp),
            fontFamily = RobotoRegularFonts,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(
                onClick = {
                    hideView(false)
                    chViewModel.infoPopupScreen = InfoScreens.SUPPORT
                    navController.navigate(InfoPopupScreens.InformationViews.route!!)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Text(
                    stringResource(R.string.conn_get_help),
                    modifier = Modifier.padding(end = 20.dp),
                    fontFamily = OswaldFonts,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.connexSelectionColor)
                )
            }
        }
    }
}

@Composable
fun CloudSharedText() {
    val placeholder = stringResource(R.string.in_the_meantime_you_can_try_the_following)
    val globalText = stringResource(
        id = R.string.your_latest_data_is_being_stored,
        placeholder
    )
    val start = globalText.indexOf(placeholder)
    val spanStyles = listOf(
        AnnotatedString.Range(
            SpanStyle(fontWeight = FontWeight.Bold),
            start = start,
            end = start + placeholder.length
        )
    )
    Text(
        text = AnnotatedString(
            text = globalText,
            spanStyles = spanStyles
        ),
        modifier = Modifier.padding(start = 20.dp, end = 20.dp),
        fontFamily = RobotoRegularFonts,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black
    )
    Text(
        stringResource(R.string.your_latest_data_is_being_stored),
        modifier = Modifier.padding(start = 20.dp, end = 20.dp),
        fontFamily = RobotoRegularFonts,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black)
}