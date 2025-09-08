package com.epicorebiosystems.rehydrate.onboarding.step2_personalize

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.toByteArray
import com.epicorebiosystems.rehydrate.sharedViews.FullScreenProgressView
import com.epicorebiosystems.rehydrate.sharedViews.PhysiologyInformationView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import kotlinx.coroutines.launch

@Composable
fun Step2PersonalizeMainView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController) {

    val scopeUpdateUser  = rememberCoroutineScope()
    var showNetworkProgress by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // If Japanese make metrics the default - do here before show physiology view
    //if (chViewModel.getCurrentLocale() == "ja_JP") {
    val isMetric = chViewModel.isMetricRegion()
    if (isMetric) {
        chViewModel.updateUnits(0)
    }

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                stringResource(R.string.personalize),
                modifier = Modifier.testTag("text_step2personalizemainview_personalize"),
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
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painterResource(R.drawable.progress_bar_2_1),
                    contentDescription = "image_step2personalizemainview_progress_1",
                    modifier = Modifier.testTag("image_step2personalizemainview_progress_1"),
                    contentScale = ContentScale.FillBounds)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(R.string.this_information_helps_tailor),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal,
                    color = Color.White, 
                    modifier = Modifier.padding(10.dp)
                        .testTag("text_step2personalizemainview_information"))

                PhysiologyInformationView(navController, chViewModel = chViewModel, ebsMonitor = ebsDeviceMonitor, showHeading = false, onBoarding = true, isEditing = true, updateHideBottomBar = { })

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = (if (chViewModel.getCurrentLocale() == "ja_JP") 20.dp else 30.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = trackClick(targetName = "savePhysiologyChangedValues") {
                            if (chViewModel.oldUserHeightFt != chViewModel.userHeightFt.value || chViewModel.oldUserHeightIn != chViewModel.userHeightIn.value ||
                                chViewModel.oldUserHeightCm != chViewModel.userHeightCm.value || chViewModel.oldUserWeightLb != chViewModel.userWeightLb.value ||
                                chViewModel.oldUserWeightKg != chViewModel.userWeightKg.value || chViewModel.oldUserGender != chViewModel.userGender.value
                            ) {

                                val builder = AlertDialog.Builder(context)


                                if ((chViewModel.userHeightCm.value.toInt() > 212) || (chViewModel.userHeightCm.value.toInt() < 125) ||
                                    (chViewModel.userWeightKg.value.toInt() > 300) || (chViewModel.userWeightKg.value.toInt() < 23)) {

                                    builder.setTitle(chViewModel.applicationContext!!.getString(R.string.warning))
                                    builder.setMessage(chViewModel.applicationContext!!.getString(R.string.out_of_range_physiology_input))
                                    builder.setNegativeButton(chViewModel.applicationContext!!.getString(
                                        R.string.hydration_ok
                                    ),
                                        DialogInterface.OnClickListener { _, _ ->
                                            chViewModel.userHeightFt.value =
                                                chViewModel.oldUserHeightFt
                                            chViewModel.userHeightIn.value =
                                                chViewModel.oldUserHeightIn
                                            chViewModel.userHeightCm.value =
                                                chViewModel.oldUserHeightCm
                                            chViewModel.userWeightLb.value =
                                                chViewModel.oldUserWeightLb
                                            chViewModel.userWeightKg.value =
                                                chViewModel.oldUserWeightKg
                                            chViewModel.userGender.value = chViewModel.oldUserGender

//                                            updateHideBottomBar(false)
//                                            navController.navigateUp()
                                        })
                                }

                                else {
                                    builder.setTitle(chViewModel.applicationContext!!.getString(R.string.confirm_alert))
                                    builder.setMessage(chViewModel.applicationContext!!.getString(R.string.are_you_sure))
                                    builder.setNegativeButton(chViewModel.applicationContext!!.getString(
                                        R.string.cancel_alert
                                    ),
                                        DialogInterface.OnClickListener { _, _ ->
                                            chViewModel.userHeightFt.value =
                                                chViewModel.oldUserHeightFt
                                            chViewModel.userHeightIn.value =
                                                chViewModel.oldUserHeightIn
                                            chViewModel.userHeightCm.value =
                                                chViewModel.oldUserHeightCm
                                            chViewModel.userWeightLb.value =
                                                chViewModel.oldUserWeightLb
                                            chViewModel.userWeightKg.value =
                                                chViewModel.oldUserWeightKg
                                            chViewModel.userGender.value = chViewModel.oldUserGender
                                        })
                                    builder.setPositiveButton(chViewModel.applicationContext!!.getString(
                                        R.string.hydration_ok
                                    ),
                                        DialogInterface.OnClickListener { _, _ ->
                                            // Update the stored user info
                                            //chViewModel.userHeightFt.value = userHeightFt.value
                                            //chViewModel.userHeightIn.value = userHeightIn.value
                                            //chViewModel.userHeightCm.value = userHeightCm.value
                                            //chViewModel.userWeightKg.value = userWeightKg.value
                                            //chViewModel.userWeightLb.value = userWeightLb.value
                                            //chViewModel.userGender.value = userGender.value

                                            chViewModel.savePhysiologyChangedValues(
                                                chViewModel.userWeightLb.value,
                                                chViewModel.userWeightKg.value,
                                                chViewModel.userHeightFt.value,
                                                chViewModel.userHeightIn.value,
                                                chViewModel.userHeightCm.value,
                                                chViewModel.userGender.value
                                            )

                                            scopeUpdateUser.launch {
                                                showNetworkProgress = true

                                                // Update user information here
                                                val paddedSize = 16
                                                val paddedHexZeros =
                                                    ByteArray(paddedSize) { 0xFF.toByte() }   // Create the padded array of trailing 0x00's
                                                val userHeightInCms: ByteArray = byteArrayOf(
                                                    (if ((chViewModel.userHeightCm.value == "") || (chViewModel.userHeightCm.value == "0")) {
                                                        "175"
                                                    } else {
                                                        chViewModel.userHeightCm.value
                                                    }).toInt().toByte()
                                                )

                                                val userWeightInKg: ByteArray =
                                                    (if ((chViewModel.userWeightKg.value == "") || (chViewModel.userWeightKg.value == "0")) {
                                                        "75"
                                                    } else {
                                                        chViewModel.userWeightKg.value
                                                    }).toUShort().toByteArray()

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

                                                ebsDeviceMonitor.onEvent(
                                                    OnRunInput(
                                                        setUserInfoCommand
                                                    )
                                                )

                                                val userInfo: Map<String, Any> = mapOf(
                                                    "height" to chViewModel.userHeightCm.value,
                                                    "weight" to chViewModel.userWeightKg.value,
                                                    "biologicalSex" to if (chViewModel.userGender.value == "Male") "male" else "female"
                                                )

                                                ebsDeviceMonitor.setUserInfoForCSVFile(
                                                    chViewModel.userGender.value,
                                                    chViewModel.userHeightCm.value.toInt(),
                                                    chViewModel.userWeightKg.value.toInt()
                                                )

                                                chViewModel.networkManager.updateUser(
                                                    enterpriseId = chViewModel.jwtEnterpriseID.value,
                                                    siteId = chViewModel.jwtSiteID.value,
                                                    userInfo = userInfo
                                                )

                                                chViewModel.savePhysiologyChangedValues(
                                                    chViewModel.onboardingWeightLb.value,
                                                    chViewModel.onboardingWeightKg.value,
                                                    chViewModel.onboardingHeightFt.value,
                                                    chViewModel.onboardingHeightIn.value,
                                                    chViewModel.onboardingHeightCm.value,
                                                    chViewModel.onboardingGender.value
                                                )

                                                showNetworkProgress = false

                                                navController.navigate(OnboardingScreens.Step2SharingMainView.route)
                                            }

                                        })
                                }

                                val alertDialog = builder.show()
                                alertDialog.setCanceledOnTouchOutside(false)
                                alertDialog.setCancelable(false)
                            } else {
                                showNetworkProgress = false
                                navController.navigate(OnboardingScreens.Step2SharingMainView.route)
                            }
                        },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp)
                            .testTag("button_step2personalizemainview_continue"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(stringResource(R.string.continue_button),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_step2personalizemainview_continue"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor))
                    }
                }

            }
        }

        if (showNetworkProgress) {
            FullScreenProgressView(R.string.updating_user_info, true)
        }
    }
}