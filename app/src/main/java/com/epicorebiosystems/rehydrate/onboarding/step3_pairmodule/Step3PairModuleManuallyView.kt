package com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.OnboardingScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.SettingsSubScreens
import com.epicorebiosystems.rehydrate.SharedScreens
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.PairState
import com.epicorebiosystems.rehydrate.modelData.isValidDeviceSerialNumber
import com.epicorebiosystems.rehydrate.sharedViews.FullScreenProgressView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoMediumFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import kotlinx.coroutines.delay
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScanningState

@Composable
fun Step3PairModuleManuallyView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, isNewPair: Boolean, isOnboarding: Boolean, navController: NavController) {
    var deviceScanningErrors = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val pairingState = chViewModel.qrPairingState.collectAsState().value
    var deviceSN = remember { mutableStateOf("") }
    var scanErrorMsg = remember { mutableStateOf("") }
    var scanDeviceState by remember { mutableStateOf(false) }

    val notAbleToPairErrorMsg = stringResource(R.string.not_able_to_pair_err_msg)

    if (chViewModel._qrPairingState.value.pairedMoveView) {
        chViewModel._qrPairingState.value = chViewModel._qrPairingState.value.copy(showLoading = false)
        isLoading.value = false

        if (isOnboarding) {
            navController.navigate(OnboardingScreens.LogInModuleIdentifyView.route)
        }
        else if (!isNewPair) {
            navController.navigate(OnboardingScreens.Step3PairModuleIdentify.route)
        }
        else {
            navController.navigate(SettingsSubScreens.PairNewModuleIdentify.route!!)
        }
    }

    if (scanDeviceState) {
        LaunchedEffect(Unit) {
            delay(90000L)
            if (ebsDeviceMonitor.chDevicesFound == -1) {
                chViewModel._qrPairingState.value =
                    chViewModel._qrPairingState.value.copy(showErrorMsg = true)
                scanErrorMsg.value = ebsDeviceMonitor.scanErrString
            } else if (ebsDeviceMonitor.chDevicesFound >= 0) {
                chViewModel._qrPairingState.value =
                    chViewModel._qrPairingState.value.copy(showErrorMsg = true)
                scanErrorMsg.value = notAbleToPairErrorMsg

                // Reset the scanned sensor name and connection state
                chViewModel.deviceSN.value = ""
                chViewModel.isCHDeviceConnected = false
                chViewModel._isSensorConnected.value = false

                // Clear device scan array and start scan again
                ScanningState.DevicesDiscovered(emptyList())
                ebsDeviceMonitor.scanBluetoothDevice()

            }

            scanDeviceState = false
            chViewModel._qrPairingState.value =
                chViewModel._qrPairingState.value.copy(showLoading = false)
        }
    }

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                stringResource(R.string.pair_module),
                modifier = Modifier.testTag("text_pairmanuallytopviews_pairmodule"),
                textAlign = TextAlign.Center,
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = colorResource(R.color.onboardingLtGrayColor), thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painterResource(R.drawable.progress_bar_4_3),
                    contentDescription = "image_pairmanuallytopviews_progress_3",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(bottom = 20.dp)
                        .testTag("image_pairmanuallytopviews_progress_3"))

                Image(
                    painterResource(R.drawable.pair_module_code),
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(bottom = 20.dp)
                        .testTag("image_step3pairmodulemanuallyview_device"))

                Text(
                    stringResource(R.string.locate_the_serial_number),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                        .testTag("text_step3pairmodulemanuallyview_locate"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = Color.White)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp/*, top = 10.dp*/),
                    horizontalAlignment = Alignment.Start,
                ) {

                    Text(
                        stringResource(R.string.serial_number),
                        modifier = Modifier.padding(start = 5.dp)
                            .testTag("text_step3pairmodulemanuallyview_sn"),
                        fontFamily = OswaldFonts,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    BoxWithConstraints() {
                        TextField(
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .height(60.dp)
                                .width(maxWidth - 20.dp)
                                .testTag("textfield_step3pairmodulemanuallyview_devicesn"),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontFamily = RobotoMediumFonts, fontSize = 22.sp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                keyboardType = KeyboardType.Text
                            ),
                            shape = RoundedCornerShape(10.dp),
                            value = deviceSN.value,
                            onValueChange = {
                                deviceSN.value = it
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = colorResource(R.color.settingsColorCoalText),
                                disabledTextColor = Color.Transparent,
                                backgroundColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                if (chViewModel._qrPairingState.value.showErrorMsg) {
                    Text(scanErrorMsg.value,
                        modifier = Modifier
                            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                            .testTag("textfield_step3pairmodulemanuallyview_servererror"),
                        textAlign = TextAlign.Center,
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Red)
                }

                if (chViewModel._qrPairingState.value.showErrorMsg) {
                    Text(deviceScanningErrors.value,
                        textAlign = TextAlign.Center,
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Red)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = trackClick(targetName = "Step3PairModuleManuallyView - device SN - $deviceSN.value") {
                            chViewModel.deviceSN.value = ""
                            deviceSN.value = deviceSN.value.trimEnd(' ')
                            if (isValidDeviceSerialNumber(deviceSN.value)) {
                                chViewModel._qrPairingState.value = PairState(showErrorMsg = false, showLoading = true)
                                isLoading.value = !isLoading.value
                                chViewModel.deviceSN.value = deviceSN.value

                                // Check if is continue onboarding device
                                if (chViewModel.deviceSN.value == "CHTEST00" || chViewModel.deviceSN.value == "CHTEST01") {
                                    chViewModel._qrPairingState.value =
                                        chViewModel._qrPairingState.value.copy(pairedMoveView = true)
                                }

                                deviceScanningErrors.value = ""

                            }
                            else {
                                chViewModel._qrPairingState.value = chViewModel._qrPairingState.value.copy(showErrorMsg = true)
                                deviceScanningErrors.value = "Serial number should be 8 characters long\nDo not include dashes."
                            }
                        },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp)
                            .testTag("button_step3pairmodulemanuallyview_submit"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                            if (pairingState.showLoading) {
                                val infiniteTransition = rememberInfiniteTransition(label = "")
                                val angle by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(5000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    ), label = ""
                                )
                                Image(painter = painterResource(id = R.drawable.icon_loading_on_bttn), modifier = Modifier.rotate(angle).testTag("image_step3pairmodulemanuallyview_spinner"), contentDescription = null)
                            }
                            else {
                                Text(
                                    stringResource(R.string.submit_button),
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                        .testTag("text_step3pairmodulemanuallyview_submit"),
                                    textAlign = TextAlign.Center,
                                    fontFamily = OswaldFonts,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = colorResource(R.color.onboardingLtBlueColor))
                            }
                    }

                    Button(
                        onClick = trackClick(targetName = if (!isNewPair) "SharedScreens.ScanDeviceQRCode" else "SettingsSubScreens.PairNewModuleQrScanView") {
                            if (!isNewPair) {
                                navController.navigate(SharedScreens.ScanDeviceQRCode.route!!)
                            }
                            else {
                                navController.navigate(SettingsSubScreens.PairNewModuleQrScanView.route!!)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                        modifier = Modifier.testTag("button_step3pairmodulemanuallyview_scan"),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(stringResource(R.string.scan_qr_code_instead),
                            modifier = Modifier.testTag("text_step3pairmodulemanuallyview_scan"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            style = TextStyle(textDecoration = TextDecoration.Underline),
                            color = colorResource(R.color.linkStandardText))
                    }

                }
            }
        }

        if (pairingState.showLoading) {
            scanDeviceState = true
            FullScreenProgressView(R.string.pairing, true)
        }
    }
}