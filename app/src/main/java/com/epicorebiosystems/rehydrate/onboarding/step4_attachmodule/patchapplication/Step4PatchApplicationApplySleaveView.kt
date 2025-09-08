package com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.patchapplication

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.epicorebiosystems.rehydrate.OnboardingScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun Step4PatchApplicationApplySleeveView(chViewModel: ModelData, navController: NavController) {
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
                stringResource(R.string.patch_application),
                modifier = Modifier.testTag("text_patchapplicationapplysleaveview_patch"),
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
                    contentDescription = "",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(bottom = 20.dp)
                        .testTag("text_patchapplicationapplysleaveview_dots"))

                Text(stringResource(R.string.optional),
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                        .testTag("text_patchapplicationshareinfo4view_optional"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    color = Color.White)

                Text(stringResource(R.string.armband_cover),
                    modifier = Modifier.padding(start = 20.dp, bottom = 20.dp, end = 20.dp)
                        .testTag("text_patchapplicationshareinfo4view_module"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    color = Color.White)

                Image(
                    painterResource(R.drawable.patchapplication_sleave),
                    contentDescription = "",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(bottom = 20.dp)
                        .testTag("image_patchapplicationshareinfo4view_sleave"))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                chViewModel.onboardingStep = 5
                                navController.navigate(OnboardingScreens.InitialSetupView.route)
                            }
                        },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp)
                            .testTag("button_step4patchapplicationapplysleeveview_ontinue"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(
                            stringResource(R.string.continue_button),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_step4patchapplicationapplysleeveview_continue"),
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