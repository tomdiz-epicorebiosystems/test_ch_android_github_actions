package com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.OnboardingScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.SettingsSubScreens
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.dial
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun Step3PairModuleUnresponsive(chViewModel: ModelData, navController: NavController, isNewPair: Boolean, isOnboarding: Boolean, updateHideBottomBar: (Boolean) -> Unit) {
    val context = LocalContext.current

    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.unresponsive_module),
                modifier = Modifier.testTag("text_pairunresponsivetopview_unresponive"),
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

                Text(
                    stringResource(R.string.if_your_module_is_unresponsive_when_tested),
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
                        .testTag("text_pairunresponsivetopview_tested"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal,
                    color = Color.White)

                Text(
                    stringResource(R.string.step_1_app_may_be_paired),
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
                        .testTag("text_pairstepsview_step1"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = if (isJapanese) 16.sp else 18.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.White)

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        stringResource(R.string.try_pairing_again_by_scanning_the_qr_code),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
                            .testTag("text_pairstepsview_pairing"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = if (isJapanese) 14.sp else 16.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.if_manually_entering_the_serial_number),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
                            .testTag("text_pairstepsview_manually"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = if (isJapanese) 14.sp else 16.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }

                Text(
                    stringResource(R.string.step_2_app_may),
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
                        .testTag("text_pairstepsview_step2"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = if (isJapanese) 16.sp else 18.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.White)

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        stringResource(R.string.if_the_test_fails_after_step_1),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
                            .testTag("text_pairstepsview_restart"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = if (isJapanese) 14.sp else 16.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }

                Text(
                    stringResource(R.string.step_3_depleted_battery),
                    modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
                        .testTag("text_pairstepsview_step3"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = if (isJapanese) 16.sp else 18.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.White)

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        stringResource(R.string.if_step_2_is_unsuccessful),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
                            .testTag("text_pairstepsview_unsuccessful"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = if (isJapanese) 14.sp else 16.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = trackClick(targetName = if (!isNewPair) "OnboardingScreens.Step3PairModuleMainView" else "SettingsSubScreens.SensorInformation") {
                            if (isOnboarding) {
                                if (chViewModel.isCreateAccountFlow.value) {
                                    if(chViewModel.continueWithOnboarding) {
                                        navController.popBackStack(
                                            OnboardingScreens.Step3PairModuleMainView.route,
                                            inclusive = true
                                        )
                                    }
                                    else {
                                        navController.navigate(OnboardingScreens.Step3PairModuleMainView.route)
                                    }
                                }
                                else {
                                    navController.popBackStack(OnboardingScreens.LogInPairModuleView.route, inclusive = false)
                                }
                            }
                            else if (!isNewPair) {
                                navController.popBackStack(OnboardingScreens.Step3PairModuleMainView.route, inclusive = true)
                            }
                            else {
                                updateHideBottomBar(false)
                                navController.popBackStack(SettingsSubScreens.SensorInformation.route!!, inclusive = true)
                            }
                        },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp)
                            .padding(bottom = 10.dp)
                            .testTag("button_step3pairmoduleunresponsive_tryagain"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(
                            stringResource(R.string.try_again),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_step3pairmoduleunresponsive_tryagain"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor)
                        )
                    }

                    Button(
                        onClick = trackClick(targetName = "User dialing support") {
                            context.dial(phone = "+1-617-397-3756")
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                        modifier = Modifier.testTag("button_step3pairmoduleunresponsive_call"),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            stringResource(R.string.call_for_support),
                            modifier = Modifier.testTag("text_step3pairmoduleunresponsive_call"),
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
    }
}