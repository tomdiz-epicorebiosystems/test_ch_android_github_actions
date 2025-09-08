package com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
import com.epicorebiosystems.rehydrate.sharedViews.FullScreenProgressView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import kotlinx.coroutines.delay
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScanningState

@Composable
fun Step3PairModuleScanView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, isNewPair: Boolean, isOnboarding: Boolean, navController: NavController) {
    val pairingState = chViewModel.qrPairingState.collectAsState().value
    var scanErrorMsg = remember { mutableStateOf("") }
    var scanDeviceState by remember { mutableStateOf(false) }

    val notAbleToPairErrorMsg = stringResource(R.string.not_able_to_pair_err_msg)

    if (chViewModel._qrPairingState.value.pairedMoveView) {
        chViewModel._qrPairingState.value = chViewModel._qrPairingState.value.copy(showLoading = false)

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
                modifier = Modifier.testTag("text_step3pairmodulescanview_pair"),
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

                Image(painterResource(R.drawable.progress_bar_4_4),
                    contentDescription = "image_step3pairmodulescanview_progress_2",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(bottom = 20.dp)
                        .testTag("image_step3pairmodulescanview_progress_2"))

                Text(boldWordInString(stringResource(R.string.locate_label), stringResource(R.string.scan_bold)),
                    modifier = Modifier.padding(start = 20.dp, bottom = 20.dp, end = 20.dp)
                        .testTag("text_step3pairmodulescanview_locate"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = Color.White)

                Step3ModuleScanShareInfoView()

                if (chViewModel._qrPairingState.value.showErrorMsg) {
                    Text(scanErrorMsg.value,
                        modifier = Modifier
                            .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                            .testTag("text_step3pairmodulescanview_servererror"),
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
                    Button(
                        onClick = trackClick(targetName = if (!isNewPair) "SharedScreens.ScanDeviceQRCode" else "SettingsSubScreens.PairNewModuleQrScanView") {
                            chViewModel.deviceSN.value = ""
                            chViewModel._qrPairingState.value = chViewModel._qrPairingState.value.copy(showErrorMsg = false)
                            if (isOnboarding) {
                                navController.navigate(SharedScreens.ScanDeviceQRCode.route!!)
                            }
                            else if (!isNewPair) {
                                navController.navigate(SharedScreens.ScanDeviceQRCode.route!!)
                            }
                            else {
                                navController.navigate(SettingsSubScreens.PairNewModuleQrScanView.route!!)
                            }
                        },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp)
                            .padding(bottom = 10.dp)
                            .testTag("button_step3pairmodulescanview_qrcode"),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.scan_qr_code),
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .testTag("text_step3pairmodulescanview_qrcode"),
                                textAlign = TextAlign.Center,
                                fontFamily = OswaldFonts,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.onboardingLtBlueColor))

                            Image(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_qr_code_scanner_24),
                                contentDescription = "",
                                modifier = Modifier.testTag("image_step3pairmodulescanview_qrcode"),
                                colorFilter = ColorFilter.tint(colorResource(R.color.onboardingLtBlueColor)))
                        }
                    }

                    Button(
                        onClick = trackClick(targetName = if (!isNewPair) "OnboardingScreens.Step3PairModuleManuallyView" else "SettingsSubScreens.PairNewModuleManuallyView") {
                            chViewModel.deviceSN.value = ""
                            chViewModel._qrPairingState.value = chViewModel._qrPairingState.value.copy(showErrorMsg = false)
                            if (isOnboarding) {
                                navController.navigate(OnboardingScreens.LogInModuleManuallyView.route)
                            }
                            else if (!isNewPair) {
                                navController.navigate(OnboardingScreens.Step3PairModuleManuallyView.route)
                            }
                            else {
                                navController.navigate(SettingsSubScreens.PairNewModuleManuallyView.route!!)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                        modifier = Modifier.testTag("button_step3pairmodulescanview_manually"),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(stringResource(R.string.enter_manually_instead),
                            modifier = Modifier.testTag("text_step3pairmodulescanview_manually"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            style = TextStyle(textDecoration = TextDecoration.Underline),
                            color = colorResource(R.color.linkStandardText)
                        )
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

fun boldWordInString(text: String, wordToBold: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val startIndex = text.indexOf(wordToBold)
        if(startIndex != -1){
            val endIndex = startIndex + wordToBold.length
            append(text.substring(0, startIndex))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(text.substring(startIndex, endIndex))
            }
            append(text.substring(endIndex))
        } else {
            append(text)
        }
    }
}

@Composable
fun Step3ModuleScanShareInfoView() {
    Image(
        painterResource(R.drawable.pair_module_scan),
        contentDescription = "",
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.padding(bottom = 20.dp)
            .testTag("image_pairmoduleshareinfo2view_scan"))
 }