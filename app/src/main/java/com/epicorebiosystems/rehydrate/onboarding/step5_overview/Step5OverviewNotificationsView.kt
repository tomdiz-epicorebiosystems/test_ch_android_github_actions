package com.epicorebiosystems.rehydrate.onboarding.step5_overview

import android.content.res.Resources
import android.os.Build
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
import androidx.compose.ui.modifier.modifierLocalConsumer
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
fun Step5OverviewNotificationsView(chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor, navController: NavController) {
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
                stringResource(R.string.overview),
                modifier = Modifier.testTag("text_step5overviewnotificationsview_overview"),
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
                    painterResource(R.drawable.overview_progress_bar_2),
                    contentDescription = "",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.testTag("text_step5overviewnotificationsview_progress_2"),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Step5OverviewShareInfoView(ebsMonitor)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = trackClick(targetName = "Open OnboardingScreens.Step5OverviewTrackIntakeView") {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.navigate(OnboardingScreens.Step5OverviewTrackIntakeView.route)
                            }
                        },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp)
                            .testTag("button_step5overviewnotificationsview_continue"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(
                            stringResource(R.string.continue_button),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_step5overviewnotificationsview_continue"),
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
                        modifier = Modifier.testTag("button_step5overviewnotificationsview_skip"),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            stringResource(R.string.skip_overview),
                            modifier = Modifier.testTag("text_step5overviewnotificationsview_skip"),
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

@Suppress("DEPRECATION")
fun getCurrentLocale(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Resources.getSystem().configuration.locales.get(0).toString()
    } else {
        Resources.getSystem().configuration.locale.toString()
    }
}

@Composable
fun Step5OverviewShareInfoView(ebsMonitor: EBSDeviceMonitor) {
    Text(stringResource(R.string.module_notification),
        modifier = Modifier.testTag("text_overviewshareinfo2view_types"),
        textAlign = TextAlign.Center,
        fontFamily = RobotoRegularFonts,
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
    ) {
        Image(
            painterResource(R.drawable.overview_2_a),
            contentDescription = "",
            modifier = Modifier.testTag("image_overviewshareinfo2view_short"),
            contentScale = ContentScale.FillBounds,
        )

        Text(
            stringResource(R.string.short_vibration_alerts_you_to_drink),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                .testTag("text_overviewshareinfo2view_short"),
            fontFamily = RobotoRegularFonts,
            fontSize = if (getCurrentLocale() == "ja_JP") 14.sp else 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
    ) {

        Text(
            stringResource(R.string.continuous_vibration_alarm),
            modifier = Modifier.padding(end = 10.dp)
                .testTag("text_overviewshareinfo2view_alarm"),
            fontFamily = RobotoRegularFonts,
            fontSize = if (getCurrentLocale() == "ja_JP") 14.sp else 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )

        Image(
            painterResource(R.drawable.overview_2_b),
            contentDescription = "",
            modifier = Modifier.testTag("image_overviewshareinfo2view_alarm"),
            contentScale = ContentScale.FillBounds,
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
    ) {
        if (ebsMonitor.getIsCHArmband()) {
            Image(
                painterResource(R.drawable.overview_2_arm),
                contentDescription = "",
                modifier = Modifier.testTag("image_overviewshareinfo2view_armband"),
                contentScale = ContentScale.FillBounds,
            )
        }
        else {
            Image(
                painterResource(R.drawable.overview_2),
                contentDescription = "",
                modifier = Modifier.testTag("image_overviewshareinfo2view_patch"),
                contentScale = ContentScale.FillBounds,
            )
        }

        Text(
            stringResource(R.string.to_stop_the_alarm_press_the_large_button),
            modifier = Modifier.padding(start = 10.dp)
                .testTag("text_overviewshareinfo2view_continuous"),
            fontFamily = RobotoRegularFonts,
            fontSize = if (getCurrentLocale() == "ja_JP") 14.sp else 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )
    }
}