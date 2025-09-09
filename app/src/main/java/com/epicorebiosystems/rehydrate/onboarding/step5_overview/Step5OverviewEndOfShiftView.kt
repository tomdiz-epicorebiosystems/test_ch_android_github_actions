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
fun Step5OverviewEndOfShiftView(ebsMonitor: EBSDeviceMonitor, navController: NavController) {
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
                modifier = Modifier.testTag("text_step5overviewendOfshiftview_overview"),
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
                    painterResource(R.drawable.overview_progress_bar_5),
                    contentDescription = "text_step5overviewendOfshiftview_progress_5",
                    modifier = Modifier.testTag("text_step5overviewendOfshiftview_progress_5"),
                    contentScale = ContentScale.FillBounds,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Step5OverviewEndOfShift(ebsMonitor)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = trackClick(targetName = "Open OnboardingScreens.Step5OverviewSetupComplete") {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.navigate(OnboardingScreens.Step5OverviewSetupComplete.route)
                            }
                        },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp)
                            .padding(bottom = 10.dp)
                            .testTag("button_step5overviewendOfshiftview_done"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(stringResource(R.string.done),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_step5overviewendOfshiftview_done"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor)
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun Step5OverviewEndOfShift(ebsMonitor: EBSDeviceMonitor) {
    Text(stringResource(R.string.end_shift),
        modifier = Modifier.testTag("text_overviewshareinfo4view_eos"),
        textAlign = TextAlign.Center,
        fontFamily = RobotoRegularFonts,
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White)

    Spacer(modifier = Modifier.height(10.dp))

    Text(stringResource(R.string.enter_unlogged),
        modifier = Modifier.testTag("text_overviewshareinfo4view_unlogged"),
        textAlign = TextAlign.Center,
        fontFamily = RobotoRegularFonts,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
    ) {

        Text(stringResource(R.string.data_syncd),
            modifier = Modifier.padding(start = 10.dp, end = 20.dp)
                .testTag("text_overviewshareinfo4view_synced"),
            fontFamily = RobotoRegularFonts,
            fontSize = if (getCurrentLocale() == "ja_JP") 14.sp else 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )

        Image(
            painterResource(R.drawable.overview_sync),
            contentDescription = "image_overviewshareinfo4view_synced",
            modifier = Modifier.testTag("image_overviewshareinfo4view_synced"),
            contentScale = ContentScale.Fit,
        )

    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
    ) {
        if (ebsMonitor.getIsCHArmband()) {
            Image(
                painterResource(R.drawable.overview_2_arm),
                contentDescription = "image_overviewshareinfo4view_armband",
                modifier = Modifier.testTag("image_overviewshareinfo4view_armband"),
                contentScale = ContentScale.FillBounds,
            )
        }
        else {
            Image(
                painterResource(R.drawable.overview_2),
                contentDescription = "image_overviewshareinfo4view_patch",
                modifier = Modifier.testTag("image_overviewshareinfo4view_patch"),
                contentScale = ContentScale.Fit,
            )
        }

        Text(stringResource(R.string.long_press_power),
            modifier = Modifier.padding(start = 20.dp, end = 10.dp)
                .testTag("text_overviewshareinfo4view_long"),
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
        if (ebsMonitor.getIsCHArmband()) {
            Text(stringResource(R.string.wash_armband),
                modifier = Modifier.padding(start = 10.dp, end = 20.dp)
                    .testTag("text_overviewshareinfo4view_detach"),
                fontFamily = RobotoRegularFonts,
                fontSize = if (getCurrentLocale() == "ja_JP") 14.sp else 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            Image(
                painterResource(R.drawable.overview_detach_arm),
                contentDescription = "image_overviewshareinfo4view_armband_remove",
                modifier = Modifier.testTag("image_overviewshareinfo4view_armband_remove"),
                contentScale = ContentScale.Fit,
            )
        }
        else {
            Text(stringResource(R.string.grab_patch),
                modifier = Modifier.padding(start = 10.dp, end = 20.dp)
                    .testTag("text_overviewshareinfo4view_peel"),
                fontFamily = RobotoRegularFonts,
                fontSize = if (getCurrentLocale() == "ja_JP") 14.sp else 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            Image(
                painterResource(R.drawable.overview_4),
                contentDescription = "image_overviewshareinfo4view_patch_peel",
                modifier = Modifier.testTag("image_overviewshareinfo4view_patch_peel"),
                contentScale = ContentScale.Fit,
            )
        }

    }
}