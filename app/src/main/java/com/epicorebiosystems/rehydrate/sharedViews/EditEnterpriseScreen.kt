package com.epicorebiosystems.rehydrate.sharedViews

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.epicorebiosystems.rehydrate.OnboardingScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnRunInput
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.toByteArray
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoMediumFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import kotlinx.coroutines.launch

@Composable
fun EditEnterpriseScreen(navController: NavController, ebsDeviceMonitor: EBSDeviceMonitor, chViewModel: ModelData, updateHideBottomBar: (Boolean) -> Unit) {
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"

    var jwtEnterpriseID = remember { mutableStateOf(chViewModel.jwtEnterpriseID.value) }
    var jwtSiteID = remember { mutableStateOf(chViewModel.jwtSiteID.value) }
    val scopeUpdateUser  = rememberCoroutineScope()
    var showServerErrorMsg by remember { mutableStateOf(false) }
    var serverErrorMsg by remember { mutableStateOf("") }
    var showNetworkProgress by remember { mutableStateOf(false) }

    val invalidEnterpriseCodeOrSiteIDMsg = stringResource(R.string.enterprise_code_site_id_invalid)

    BackHandler {
        navController.navigateUp()
        updateHideBottomBar(false)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        BoxWithConstraints {
            val widthModifier = maxWidth - 20.dp
            val heightModifier = maxHeight - 200.dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .height(heightModifier)
                        .width(widthModifier)
                        .offset(x = 10.dp, y = 120.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row {
                            Text(
                                stringResource(R.string.enterprise),
                                Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp),
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.settingsColorCoalText))


                            TextField(
                                modifier = Modifier
                                    .padding(start = 80.dp)
                                    .height(60.dp)
                                    .width(100.dp)
                                    .border(
                                        color = colorResource(R.color.settingsColorHydroDarkText),
                                        width = 1.dp,
                                        shape = RoundedCornerShape(30)
                                    ),
                                singleLine = true,
                                value = jwtEnterpriseID.value,
                                onValueChange = {
                                    jwtEnterpriseID.value = it },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    keyboardType = KeyboardType.Text),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontFamily = RobotoMediumFonts, fontSize = 22.sp),
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = colorResource(R.color.settingsColorCoalText),
                                    disabledTextColor = Color.Transparent,
                                    backgroundColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row {
                            Text(
                                stringResource(R.string.site_id),
                                Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp),
                                fontFamily = OswaldFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.settingsColorCoalText))

                            var padding = 100.dp
                            if (chViewModel.getCurrentLocale() == "ja_JP") {
                                padding = 50.dp
                            }

                            TextField(
                                modifier = Modifier
                                    .padding(start = padding)
                                    .height(60.dp)
                                    .width(100.dp)
                                    .border(
                                        color = colorResource(R.color.settingsColorHydroDarkText),
                                        width = 1.dp,
                                        shape = RoundedCornerShape(30)
                                    ),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontFamily = RobotoMediumFonts, fontSize = 22.sp),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    keyboardType = KeyboardType.Text),
                                value = jwtSiteID.value,
                                onValueChange = {
                                    jwtSiteID.value = it },
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = colorResource(R.color.settingsColorCoalText),
                                    disabledTextColor = Color.Transparent,
                                    backgroundColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                ),
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (showServerErrorMsg) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(serverErrorMsg,
                                    modifier = Modifier
                                        .padding(top = 20.dp, start = 20.dp, end = 20.dp),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Red
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp))

                        Row (
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                modifier = Modifier.width(140.dp),
                                onClick = {
                                    if (chViewModel.jwtEnterpriseID.value == jwtEnterpriseID.value && chViewModel.jwtSiteID.value == jwtSiteID.value) {
                                        updateHideBottomBar(false)
                                        navController.navigateUp()
                                    }
                                    else {
                                        showNetworkProgress = true
                                        showServerErrorMsg = false
                                        serverErrorMsg = ""
                                        scopeUpdateUser.launch {
                                            val paddedSize = 16
                                            val paddedHexZeros : ByteArray = ByteArray(paddedSize) { 0xFF.toByte() }   // Create the padded array of trailing 0x00's
                                            val userHeightInCms : ByteArray = byteArrayOf((chViewModel.userHeightCm.value).toInt().toByte())

                                            val userWeightInKg : ByteArray = (chViewModel.userWeightKg.value).toUShort().toByteArray()

                                            val userGender : ByteArray= byteArrayOf(if(chViewModel.userGender.value == "Male") { 0x00 } else { 0x01 })

                                            val userAge : ByteArray = byteArrayOf(0)

                                            val userClothTypeCode : ByteArray = byteArrayOf(0)

                                            val setUserInfoCommand :  ByteArray =  byteArrayOf(0x55).plus(paddedHexZeros).plus(userGender).plus(userHeightInCms).plus(userWeightInKg).plus(userAge).plus(userClothTypeCode)

                                            ebsDeviceMonitor.onEvent(OnRunInput(setUserInfoCommand))

                                            val userInfo: Map<String, Any> = mapOf(
                                                "height" to chViewModel.userHeightCm.value,
                                                "weight" to chViewModel.userWeightKg.value,
                                                "biologicalSex" to if (chViewModel.userGender.value == "Male") "male" else "female"
                                            )

//                                            chViewModel.onboardingEnterpriseId.value = jwtEnterpriseID.value + "-" + jwtSiteID.value
//                                            chViewModel.enterpriseId.value = chViewModel.onboardingEnterpriseId.value

//                                            if(!chViewModel.onboardingComplete.value) {
//                                                val enterpriseInfo =
//                                                    chViewModel.networkManager.getEnterpriseName(
//                                                        chViewModel.onboardingEnterpriseId.value
//                                                    )
//
//                                                if (enterpriseInfo.error != null) {
//                                                    showServerErrorMsg = true
//                                                    serverErrorMsg = enterpriseInfo.error
//                                                } else {
//                                                    chViewModel.onboardingEnterpriseName.value =
//                                                        enterpriseInfo.enterpriseName.toString()
//                                                    chViewModel.onboardingSiteName.value =
//                                                        enterpriseInfo.siteName.toString()
//                                                }
//                                            }

                                            if(chViewModel.networkManager.isTokenValid()) {
                                                var serverError =
                                                    chViewModel.networkManager.updateUser(
                                                        enterpriseId = jwtEnterpriseID.value,
                                                        siteId = jwtSiteID.value,
                                                        userInfo = userInfo
                                                    )

                                                if (serverError != null) {
                                                    showNetworkProgress = false
                                                    showServerErrorMsg = true
                                                    serverErrorMsg = invalidEnterpriseCodeOrSiteIDMsg //serverError
                                                } else {

                                                    // Update enterprise/site for onboarding only after they are verified and added successfully
                                                    chViewModel.onboardingEnterpriseId.value = jwtEnterpriseID.value + "-" + jwtSiteID.value
                                                    chViewModel.enterpriseId.value = chViewModel.onboardingEnterpriseId.value

                                                    if(!chViewModel.onboardingComplete.value) {
                                                        val enterpriseInfo =
                                                            chViewModel.networkManager.getEnterpriseName(
                                                                chViewModel.onboardingEnterpriseId.value
                                                            )

                                                        if (enterpriseInfo.error != null) {
                                                            showServerErrorMsg = true
                                                            serverErrorMsg = enterpriseInfo.error
                                                        } else {
                                                            chViewModel.onboardingEnterpriseName.value =
                                                                enterpriseInfo.enterpriseName.toString()
                                                            chViewModel.onboardingSiteName.value =
                                                                enterpriseInfo.siteName.toString()
                                                        }
                                                    }

                                                    showNetworkProgress = false
                                                    updateHideBottomBar(false)
                                                    navController.navigateUp()
                                                }
                                            }

                                            else {
                                                navController.navigateUp()
                                            }

                                        }
                                    }
                                },
                                border = BorderStroke(1.dp, colorResource(R.color.settingsColorHydroDarkText)),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorResource(R.color.settingsColorHydroDarkText))
                            ){
                                Text( text = stringResource(R.string.edit_enterprise_ok) ,
                                    fontFamily = OswaldFonts,
                                    fontSize = if (isJapanese) 14.sp else 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(R.color.settingsColorCoalText))
                            }

                            Spacer(modifier = Modifier.width(50.dp))

                            OutlinedButton(
                                modifier = Modifier.width(140.dp),
                                onClick = {
                                    updateHideBottomBar(false)
                                    navController.navigateUp()
                                },
                                border = BorderStroke(1.dp, colorResource(R.color.settingsColorHydroDarkText)),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorResource(R.color.settingsColorHydroDarkText))
                            ){
                                Text( text = stringResource(R.string.edit_enterprise_cancel) ,
                                    fontFamily = OswaldFonts,
                                    fontSize = if (isJapanese) 14.sp else 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(R.color.settingsColorCoalText))

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
}