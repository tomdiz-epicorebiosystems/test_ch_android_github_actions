package com.epicorebiosystems.rehydrate.onboarding.step5_overview

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.OnboardingScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.boldWordInString
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun Step5ModuleButtonTrackIntakeView(chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor, navController: NavController) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(stringResource(R.string.overview),
                modifier = Modifier.testTag("text_step5modulebuttontrackIntakeview_overview"),
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
                    painterResource(R.drawable.overview_progress_bar_4),
                    contentDescription = "text_step5modulebuttontrackIntakeview_progress_4",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(bottom = 20.dp)
                        .testTag("text_step5modulebuttontrackIntakeview_progress_4"))

                Step5TButtonTrackIntakeView()

                if (ebsMonitor.getIsCHArmband()) {
                    Image(
                        painterResource(R.drawable.overview_intake_button_arm),
                        contentDescription = "image_step5modulebuttontrackintakeview_armband",
                        modifier = Modifier.size(200.dp, 200.dp)
                            .testTag("image_step5modulebuttontrackintakeview_armband")
                    )
                }
                else {
                    Image(
                        painterResource(R.drawable.overview_intake_button),
                        contentDescription = "image_step5modulebuttontrackintakeview_patch",
                        modifier = Modifier.size(200.dp, 200.dp)
                            .testTag("image_step5modulebuttontrackintakeview_patch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = trackClick(targetName = "Open OnboardingScreens.Step5OverviewEndOfShiftView") {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.navigate(OnboardingScreens.Step5OverviewEndOfShiftView.route)
                            }
                        },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp)
                            .testTag("button_step5modulebuttontrackintakeview_continue"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(
                            stringResource(R.string.continue_button),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_step5modulebuttontrackintakeview_continue"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor)
                        )
                    }

                    Button(
                        onClick = trackClick(targetName = "Open - CreateAccount-SkipOverview") {
                            chViewModel.updateOnBoardingComplete(done = true)
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                        modifier = Modifier.testTag("button_step5modulebuttontrackintakeview_skip"),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            stringResource(R.string.skip_overview),
                            modifier = Modifier.testTag("text_step5modulebuttontrackintakeview_skip"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            style = TextStyle(textDecoration = TextDecoration.Underline),
                            color = colorResource(R.color.linkStandardText)
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun Step5TButtonTrackIntakeView() {

    Text(stringResource(R.string.track_intake),
        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
            .testTag("text_moduleintakeshareinfoview_intake"),
        textAlign = TextAlign.Center,
        fontFamily = OswaldFonts,
        fontSize = if (getCurrentLocale() == "ja_JP") 20.sp else 24.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White)

    Text(boldWordInString(stringResource(R.string.quick_track), stringResource(R.string.quick_bold)),
        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
            .testTag("text_moduleintakeshareinfoview_tracking"),
        fontFamily = OswaldFonts,
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Normal,
        color = Color.White)

    Text(stringResource(R.string.one_bottle),
        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
            .testTag("text_moduleintakeshareinfoview_bottle"),
        fontFamily = RobotoRegularFonts,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Normal,
        color = Color.White)
}