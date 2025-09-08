package com.epicorebiosystems.rehydrate.onboarding.step2_personalize

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.OnboardingScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.tabViews.DataSharingSettingsView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun Step2SharingMainView(chViewModel: ModelData, navController: NavController) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(stringResource(R.string.personalize),
                modifier = Modifier.testTag("text_step2sharingmainview_personalize"),
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
                    painterResource(R.drawable.progress_bar_2_2),
                    contentDescription = "image_step2sharingmainview_progress_2",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(bottom = 20.dp)
                        .testTag("image_step2sharingmainview_progress_2"))

                Text(
                    stringResource(R.string.choose_whether_to_share_safety),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                        .testTag("text_step2sharingmainview_choose"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal,
                    color = Color.White)

                BoxWithConstraints {
                    DataSharingSettingsView(chViewModel = chViewModel, width = maxWidth - 20.dp, showHeading = false)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        stringResource(R.string.you_can_change_your_preference),
                        modifier = Modifier.padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
                            .testTag("text_step2sharingmainview_references"),
                        textAlign = TextAlign.Center,
                        fontFamily = OswaldFonts,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White)

                    Button(onClick = trackClick(targetName = "Step2SharingMainView back button pressed") {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                chViewModel.onboardingStep = 3
                                navController.navigate(OnboardingScreens.InitialSetupView.route)
                            }
                        },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp)
                            .padding(bottom = 10.dp)
                            .testTag("button_step2sharingmainview_continue"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(
                            stringResource(R.string.continue_button),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_step2sharingmainview_continue"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor))
                    }
                }

            }
        }
    }
}