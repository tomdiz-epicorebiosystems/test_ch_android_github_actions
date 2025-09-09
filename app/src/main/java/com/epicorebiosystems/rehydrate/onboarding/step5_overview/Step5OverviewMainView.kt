package com.epicorebiosystems.rehydrate.onboarding.step5_overview

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun Step5OverviewMainView(chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor, navController: NavController) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                stringResource(R.string.overview),
                modifier = Modifier.testTag("text_overviewtopview_overview"),
                textAlign = TextAlign.Center,
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = colorResource(R.color.onboardingLtGrayColor), thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            Image(
                painterResource(R.drawable.overview_progress_bar_1),
                contentDescription = "image_overviewtopview_progress",
                modifier = Modifier.testTag("image_overviewtopview_progress"),
                contentScale = ContentScale.FillBounds)

            Spacer(modifier = Modifier.height(20.dp))

            Step5OverviewMainShareInfoView(ebsMonitor)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Image(
                    painterResource(R.drawable.patch_icon_onboarding_i),
                    contentDescription = "image_step5overviewmainview_info",
                    modifier = Modifier.testTag("image_step5overviewmainview_info"),
                    contentScale = ContentScale.FillBounds
                )

                Text(stringResource(R.string.instructions_avail),
                    modifier = Modifier.padding(start = 20.dp, bottom = 20.dp, end = 20.dp)
                        .testTag("text_step5overviewmainview_instructions"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize().padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Button(onClick = {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            navController.navigate(OnboardingScreens.Step5OverviewNotificationsView.route)
                        }
                    },
                    modifier = Modifier
                        .size(width = 180.dp, height = 60.dp)
                        .testTag("button_step5overviewmainview_continue"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White
                    )) {
                    Text(
                        stringResource(R.string.continue_button),
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .testTag("text_step5overviewmainview_continue"),
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
                    modifier = Modifier.testTag("button_step5overviewmainview_skip"),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Text(stringResource(R.string.skip_overview),
                        modifier = Modifier.testTag("text_step5overviewmainview_skip"),
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

@Composable
fun Step5OverviewMainShareInfoView(ebsMonitor: EBSDeviceMonitor) {
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.power_on),
            modifier = Modifier.padding(bottom = 10.dp)
                .testTag("text_overviewshareinfo1view_power"),
            textAlign = TextAlign.Center,
            fontFamily = RobotoRegularFonts,
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )

        Text(stringResource(R.string.too_turn_on_module),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                .testTag("text_overviewshareinfo1view_turnon"),
            fontFamily = RobotoRegularFonts,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (ebsMonitor.getIsCHArmband()) {
            Image(
                painterResource(R.drawable.patch_overview_arm_1),
                contentDescription = "image_overviewshareinfo1view_patch",
                modifier = Modifier.size(200.dp, 200.dp).testTag("image_overviewshareinfo1view_patch")
            )
        } else {
            Image(
                painterResource(R.drawable.patch_overview_1),
                contentDescription = "image_overviewshareinfo1view_band",
                modifier = Modifier.size(200.dp, 200.dp).testTag("image_overviewshareinfo1view_band")
            )
        }

    }
}