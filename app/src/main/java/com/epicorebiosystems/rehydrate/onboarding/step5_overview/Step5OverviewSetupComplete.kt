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
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun Step5OverviewSetupComplete(chViewModel: ModelData, navController: NavController) {
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
                stringResource(R.string.setup_completed),
                modifier = Modifier.testTag("text_step5overviewsetupcomplete_setupcompleted"),
                textAlign = TextAlign.Center,
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            Image(
                painterResource(R.drawable.onboarding_icon_way_to_go),
                contentDescription = "image_step5overviewsetupcomplete_go",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.padding(bottom = 10.dp)
                    .testTag("image_step5overviewsetupcomplete_go"))

            Text(
                stringResource(R.string.you_re_good_to_go),
                modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
                    .testTag("text_step5overviewsetupcomplete_good"),
                fontFamily = RobotoRegularFonts,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                color = Color.White)

            Text(
                stringResource(R.string.you_can_revisit_instruction),
                modifier = Modifier.padding(start = 20.dp, bottom = 20.dp, end = 20.dp)
                    .testTag("text_step5overviewsetupcomplete_revisit"),
                fontFamily = RobotoRegularFonts,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                color = Color.White)

            Image(
                painterResource(R.drawable.overview_3),
                contentDescription = "",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.padding(bottom = 10.dp)
                    .testTag("image_step5overviewsetupcomplete_congrates"))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Button(onClick = trackClick(targetName = "User completed onboarding - updateOnBoardingComplete(true)") {
                    chViewModel.updateOnBoardingComplete(done = true)
                },
                    modifier = Modifier
                        .size(width = 180.dp, height = 60.dp)
                        .testTag("button_step5overviewsetupcomplete_enterapp"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White
                    )) {
                    Text(
                        stringResource(R.string.enter_the_app),
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .testTag("text_step5overviewsetupcomplete_enterapp"),
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