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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.OnboardingScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.SettingsSubScreens
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnRunInput
import com.epicorebiosystems.rehydrate.sharedViews.FullScreenProgressView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import kotlinx.coroutines.launch

@Composable
fun Step3PairModuleIdentify(chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor, isNewPair: Boolean, isOnboarding: Boolean, navController: NavController, updateHideBottomBar: (Boolean) -> Unit) {
    val scopeUpdateUser  = rememberCoroutineScope()
    var isBuzzPressed by rememberSaveable { mutableStateOf(false) }
    var showNetworkProgress by rememberSaveable { mutableStateOf(false) }

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
                modifier = Modifier.testTag("text_step3pairmoduleidentify_pairmodule"),
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
                    painterResource(R.drawable.progress_bar_4_2),
                    contentDescription = "image_step3pairmoduleidentify_progress_3",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(bottom = 20.dp)
                        .testTag("image_step3pairmoduleidentify_progress_3"))

                Text(
                    stringResource(R.string.bluetooth_connection_established),
                    modifier = Modifier.padding(start = 20.dp, bottom = 20.dp, end = 20.dp)
                        .testTag("text_step3pairmoduleidentify_established"),
                    fontFamily = RobotoFonts,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.White)

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painterResource(R.drawable.pair_module_icon_connex_device),
                        contentDescription = "image_step3pairmoduleidentify_device",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.padding(end = 10.dp, bottom = 20.dp)
                            .testTag("image_step3pairmoduleidentify_device"))

                    Image(
                        painterResource(R.drawable.icon_connex_divider_ok),
                        contentDescription = "image_step3pairmoduleidentify_connex",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.padding(end = 10.dp, bottom = 20.dp)
                            .testTag("image_step3pairmoduleidentify_connex"))

                    Image(
                        painterResource(R.drawable.icon_connex_phone),
                        contentDescription = "",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.padding(bottom = 20.dp)
                            .testTag("image_step3pairmoduleidentify_phone"))
                }

                Text(
                    stringResource(R.string.to_confirm_that_your_phone),
                    modifier = Modifier.padding(start = 20.dp, bottom = 60.dp, end = 20.dp)
                        .testTag("text_step3pairmoduleidentify_confirm"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    color = Color.White)

                Button(onClick = trackClick(targetName = "ebsMonitor.onEvent(0x5A)") {
                    isBuzzPressed = true
                    val identifyCommandBytes : ByteArray = byteArrayOf(0x5A)
                    ebsMonitor.onEvent(OnRunInput(identifyCommandBytes))
                },
                    modifier = Modifier
                        .size(width = 180.dp, height = 60.dp)
                        .padding(bottom = 10.dp)
                        .testTag("button_step3pairmoduleidentify_buzzme"),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.buzz_me),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_step3pairmoduleidentify_buzzme"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor))

                        Image(
                            painterResource(R.drawable.device_buzzer_2),
                            contentDescription = "",
                            modifier = Modifier.testTag("image_step3pairmoduleidentify_devicebuzz"),
                            colorFilter = ColorFilter.tint(colorResource(R.color.onboardingLtBlueColor)))
                    }
                }

                Text(
                    stringResource(R.string.was_your_module_responsive),
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)
                        .testTag("text_step3pairmoduleidentify_responsive"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    color = Color.White)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row {
                        Button(
                            onClick = trackClick(targetName = if (!isNewPair) "OnboardingScreens.Step3PairModuleUnresponsive" else "SettingsSubScreens.PairNewModuleUnresponsiveView") {
                                if(isBuzzPressed) {
                                    isBuzzPressed = false
                                    if (!isNewPair) {
                                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                            navController.navigate(OnboardingScreens.Step3PairModuleUnresponsive.route)
                                        }
                                    } else {
                                        if (isOnboarding) {
                                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                                navController.navigate(OnboardingScreens.Step3PairModuleUnresponsive.route)
                                            }
                                        } else {
                                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                                chViewModel.deviceSN.value = ""
                                                navController.navigate(SettingsSubScreens.PairNewModuleUnresponsiveView.route!!)
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(width = 100.dp, height = 60.dp)
                                .padding(bottom = 10.dp)
                                .testTag("button_step3pairmoduleidentify_no"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = Color.White
                            )
                        ) {
                            Text(
                                stringResource(R.string.no),
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .testTag("text_step3pairmoduleidentify_no"),
                                textAlign = TextAlign.Center,
                                fontFamily = OswaldFonts,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (!isBuzzPressed) Color.Gray else colorResource(R.color.onboardingLtBlueColor)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Button(
                            onClick = trackClick(targetName = "ebsMonitor.onEvent(setUserInfoCommand)") {
                                if(isBuzzPressed) {
                                    isBuzzPressed = false
                                    if (!isNewPair) {
                                        showNetworkProgress = true
                                        scopeUpdateUser.launch {
                                            // Save user information here
                                            val paddedSize = 16
                                            val paddedHexZeros: ByteArray =
                                                ByteArray(paddedSize) { 0xFF.toByte() }   // Create the padded array of trailing 0x00's
                                            val userHeightInCms: ByteArray = byteArrayOf(
                                                (chViewModel.userHeightCm.value).toInt().toByte()
                                            )

                                            val userWeightInKg: ByteArray =
                                                (chViewModel.userWeightKg.value).toUShort()
                                                    .toByteArray()

                                            val userGender: ByteArray = byteArrayOf(
                                                if (chViewModel.userGender.value == "Male") {
                                                    0x00
                                                } else {
                                                    0x01
                                                }
                                            )

                                            val userAge: ByteArray = byteArrayOf(0)

                                            val userClothTypeCode: ByteArray = byteArrayOf(0)

                                            val setUserInfoCommand: ByteArray =
                                                byteArrayOf(0x55).plus(paddedHexZeros)
                                                    .plus(userGender).plus(userHeightInCms)
                                                    .plus(userWeightInKg).plus(userAge)
                                                    .plus(userClothTypeCode)

                                            ebsMonitor.onEvent(OnRunInput(setUserInfoCommand))

                                            val userInfo: Map<String, Any> = mapOf(
                                                "height" to chViewModel.userHeightCm.value,
                                                "weight" to chViewModel.userWeightKg.value,
                                                "biologicalSex" to if (chViewModel.userGender.value == "Male") "male" else "female"
                                            )

                                            chViewModel.networkManager.updateUser(
                                                enterpriseId = chViewModel.jwtEnterpriseID.value,
                                                siteId = chViewModel.jwtSiteID.value,
                                                userInfo = userInfo
                                            )

                                            chViewModel.onboardingStep = 4

                                            showNetworkProgress = false
                                            navController.navigate(OnboardingScreens.InitialSetupView.route)
                                        }
                                    } else {
                                        if (isOnboarding) {
                                            if (!chViewModel.continueWithOnboarding) {
                                                navController.navigate(OnboardingScreens.LogInVerifyPhysiologyInfoView.route)
                                            } else {
                                                chViewModel.onboardingStep = 4
                                                navController.navigate(OnboardingScreens.InitialSetupView.route)
                                            }
                                        } else {
                                            navController.navigate(SettingsSubScreens.VerifyPhysioloyInfoView.route!!)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(width = 100.dp, height = 60.dp)
                                .padding(bottom = 10.dp)
                                .testTag("button_step3pairmoduleidentify_yes"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = Color.White
                            )
                        ) {
                            Text(
                                stringResource(R.string.yes),
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .testTag("text_step3pairmoduleidentify_yes"),
                                textAlign = TextAlign.Center,
                                fontFamily = OswaldFonts,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (!isBuzzPressed) Color.Gray else colorResource(R.color.onboardingLtBlueColor)
                            )
                        }
                    }

                }
            }
        }

        if (showNetworkProgress) {
            FullScreenProgressView(R.string.updating_user_info, true)
        }

    }
}

fun UShort.toByteArray(): ByteArray {
    return byteArrayOf(
        (this.toUInt() and 255u).toByte(),
        (this.toUInt() shr 8).toByte()
    )
}