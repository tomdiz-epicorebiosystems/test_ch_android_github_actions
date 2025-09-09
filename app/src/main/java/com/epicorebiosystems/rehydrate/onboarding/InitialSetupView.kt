package com.epicorebiosystems.rehydrate.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
import com.epicorebiosystems.rehydrate.ui.theme.OrbitronRegularFont
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun InitialSetupView(chViewModel: ModelData, navController: NavController) {
    var currentStep by rememberSaveable { mutableStateOf(1) }
    val stepSpacing = 30.dp
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
                stringResource(R.string.initial_set_up),
                modifier = Modifier.testTag("text_toptitleinitialsetupview_setup"),
                textAlign = TextAlign.Center,
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White)

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = colorResource(R.color.onboardingLtGrayColor), thickness = 1.dp)

            Spacer(modifier = Modifier.height(stepSpacing))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 40.dp),
                 horizontalAlignment = Alignment.Start
            ) {
                Row {

                    StepNumberView(chViewModel, "1")

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(stringResource(R.string.account_setup),
                            modifier = Modifier.testTag("text_stepmenuinitialsetupview_setup"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White)

                        Text(stringResource(R.string.setup_secure_login),
                            modifier = Modifier.testTag("text_stepmenuinitialsetupview_secure"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtGrayColor))
                    }
                }

                Spacer(modifier = Modifier.height(stepSpacing))

                Row {

                    StepNumberView(chViewModel, "2")

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            stringResource(R.string.personalize),
                            modifier = Modifier.testTag("text_stepmenuinitialsetupview_personalize"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White)

                        Text(
                            stringResource(R.string.tailor_your_hydration_recommendations),
                            modifier = Modifier.padding(end = 20.dp).testTag("text_stepmenuinitialsetupview_tailor"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtGrayColor))
                    }
                }

                Spacer(modifier = Modifier.height(stepSpacing))

                Row {

                    StepNumberView(chViewModel, "3")

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            stringResource(R.string.pair_module),
                            modifier = Modifier.testTag("text_stepmenuinitialsetupview_pair"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White)

                        Text(stringResource(R.string.enable_data_transfer),
                            modifier = Modifier.padding(end = 20.dp).testTag("text_stepmenuinitialsetupview_enable"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtGrayColor))
                    }
                }

                Spacer(modifier = Modifier.height(stepSpacing))

                Row {

                    StepNumberView(chViewModel, "4")

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(stringResource(R.string.attach_module),
                            modifier = Modifier.testTag("text_stepmenuinitialsetupview_attach"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White)

                        Text(stringResource(R.string.ensure_attachment),
                            modifier = Modifier.padding(end = 20.dp).testTag("text_stepmenuinitialsetupview_ensure"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtGrayColor))
                    }
                }

                Spacer(modifier = Modifier.height(stepSpacing))

                Row {

                    StepNumberView(chViewModel, "5")

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            stringResource(R.string.overview),
                            modifier = Modifier.testTag("text_stepmenuinitialsetupview_overview"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White)

                        Text(
                            stringResource(R.string.a_quick_orientation_of_the_basics),
                            modifier = Modifier.padding(end = 20.dp).testTag("text_stepmenuinitialsetupview_quick"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtGrayColor))
                    }
                }

            }   // Column - Steps

        }   // Column - Main view

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {

            StepButtonView(chViewModel, navController, updateStep = { newStep ->
                currentStep = newStep
            })

        }

    }
}

@Composable
fun StepNumberView(chViewModel: ModelData, viewNumber: String) {

    val currStep = viewNumber.toInt()

    if (currStep < chViewModel.onboardingStep) {
        Image(
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .background(colorResource(id = R.color.onboardingVeryDarkBackground), CircleShape)
                .clip(CircleShape)
                .testTag("image_stepnumberview_checkmark"),
            colorFilter = ColorFilter.tint(Color.White),
            contentScale = ContentScale.Crop,
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_check_24),
            contentDescription = "image_stepnumberview_checkmark")
    }
    else if (currStep == chViewModel.onboardingStep) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color = colorResource(id = R.color.onboardingLtBlueColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(viewNumber,
                modifier = Modifier.testTag("text_stepnumberview_viewnumber_1"),
                fontFamily = OrbitronRegularFont,
                fontSize = 36.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal)
        }
    }
    else {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color = colorResource(id = R.color.onboardingLtGrayColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(viewNumber,
                modifier = Modifier.testTag("text_stepnumberview_viewnumber_2"),
                fontFamily = OrbitronRegularFont,
                fontSize = 36.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal)
        }
    }
}

@Composable
fun StepButtonView(chViewModel: ModelData, navController: NavController, updateStep: (Int) -> Unit) {

    when (chViewModel.onboardingStep) {
        1 -> {
            Button(onClick = trackClick(targetName = "Open - OnboardingScreens.CreateAccountMainView") {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        updateStep(1)
                        navController.navigate(OnboardingScreens.CreateAccountMainView.route)
                    }
                },
                modifier = Modifier
                    .size(width = 280.dp, height = 60.dp)
                    .padding(bottom = 10.dp)
                    .testTag("button_stepmenuinitialsetupview_begin"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color.White
                )) {

                Text(stringResource(R.string.begin_account_setup),
                    modifier = Modifier.align(Alignment.CenterVertically).testTag("text_stepmenuinitialsetupview_begin"),
                    textAlign = TextAlign.Center,
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.waterFull))
            }
        }
        2 -> {
            Button(onClick = trackClick(targetName = "Open - OnboardingScreens.Step2PersonalizeMainView") {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        updateStep(2)
                        navController.navigate(OnboardingScreens.Step2PersonalizeMainView.route)
                    }
                },
                modifier = Modifier
                    .size(width = 280.dp, height = 60.dp)
                    .padding(bottom = 10.dp)
                    .testTag("button_stepmenuinitialsetupview_personalize"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color.White
                )) {
                Text(
                    stringResource(R.string.next_personalize),
                    modifier = Modifier.align(Alignment.CenterVertically).testTag("text_stepmenuinitialsetupview_personalize"),
                    textAlign = TextAlign.Center,
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.waterFull))
            }
        }
        3 -> {
            Button(onClick = trackClick(targetName = "Open - OnboardingScreens.Step3PairModuleMainView") {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        updateStep(3)
                        navController.navigate(OnboardingScreens.Step3PairModuleMainView.route)
                    }
                },
                modifier = Modifier
                    .size(width = 280.dp, height = 60.dp)
                    .padding(bottom = 10.dp)
                    .testTag("button_stepmenuinitialsetupview_pair"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color.White
                )) {
                Text(
                    stringResource(R.string.next_pair_module),
                    modifier = Modifier.align(Alignment.CenterVertically).testTag("text_stepmenuinitialsetupview_pair"),
                    textAlign = TextAlign.Center,
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.waterFull))
            }
        }
        4 -> {
            Button(onClick = trackClick(targetName = "Open - Step4AttachModule") {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        updateStep(4)
                        navController.navigate(OnboardingScreens.Step4AttachModule.route)
                    }
                },
                modifier = Modifier
                    .size(width = 280.dp, height = 60.dp)
                    .padding(bottom = 10.dp)
                    .testTag("button_stepmenuinitialsetupview_attach"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color.White
                )) {

                Text(stringResource(R.string.next_attach_module),
                    modifier = Modifier.align(Alignment.CenterVertically).testTag("text_stepmenuinitialsetupview_attach"),
                    textAlign = TextAlign.Center,
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.waterFull))
            }
        }
        else -> {
            Button(onClick = trackClick(targetName = "Open - OnboardingScreens.Step5OverviewMainView") {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        updateStep(5)
                        navController.navigate(OnboardingScreens.Step5OverviewMainView.route)
                    }
                },
                modifier = Modifier
                    .size(width = 280.dp, height = 60.dp)
                    .padding(bottom = 10.dp)
                    .testTag("button_stepmenuinitialsetupview_overview"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color.White
                )) {
                Text(
                    stringResource(R.string.next_overview),
                    modifier = Modifier.align(Alignment.CenterVertically).testTag("text_stepmenuinitialsetupview_overview"),
                    textAlign = TextAlign.Center,
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.waterFull))
            }
        }
    }

}