package com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.topBarViews.InfoScreens
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun Step4AttachModule(ebsMonitor: EBSDeviceMonitor, navController: NavController) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(stringResource(R.string.patch_module_attach),
                modifier = Modifier.testTag("text_step4attachmodule_attachment"),
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

                AttachModuleTopView()

                Column(
                    modifier = Modifier
                        .fillMaxSize().padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {

                    Row(
                        modifier = Modifier
                            .padding(start = 40.dp, end = 40.dp)
                    ) {
                        Image(painterResource(R.drawable.icon_info_light_blue),
                            modifier = Modifier.testTag("image_step4attachmodule_info"),
                            contentDescription = "image_step4attachmodule_info")

                        Text(stringResource(R.string.instructions_will_always_be_available),
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
                                .testTag("text_step4attachmodule_instructions"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.Normal,
                            color = Color.White)
                    }

                    Button(onClick = {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                if (ebsMonitor.getIsCHArmband()) {
                                    navController.navigate(OnboardingScreens.Step4ModuleApplicationStrapTighten.route)
                                } else {
                                    navController.navigate(OnboardingScreens.Step4PatchApplicationMainView.route)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(width = 300.dp, height = 60.dp)
                            .padding(bottom = 10.dp)
                            .testTag("button_step4attachmodule_attached"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(stringResource(R.string.module_is_attached_now),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_step4attachmodule_attached"),
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
fun AttachModuleTopView() {
    Text(stringResource(R.string.module_snapped),
        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
            .testTag("text_moduleapplicationshareinfoview_turnedon"),
        fontFamily = RobotoRegularFonts,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Normal,
        color = Color.White)

    Spacer(modifier = Modifier.height(10.dp))

    Text(stringResource(R.string.accessory_patch),
        modifier = Modifier.testTag("text_moduleapplicationshareinfoview_patch"),
        fontFamily = OswaldFonts,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        color = Color.White)

    Spacer(modifier = Modifier.height(5.dp))

    Image(
        painterResource(R.drawable.armband_step_1),
        contentDescription = "",
        modifier = Modifier.size(300.dp, 100.dp)
            .testTag("text_step5overviewendOfshiftview_overview"))

    Spacer(modifier = Modifier.height(10.dp))

    Text(stringResource(R.string.accessory_armband),
        modifier = Modifier.testTag("text_moduleapplicationshareinfoview_patch"),
        fontFamily = OswaldFonts,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        color = Color.White)

    Spacer(modifier = Modifier.height(5.dp))

    Image(
        painterResource(R.drawable.armband_step_2),
        contentDescription = "",
        modifier = Modifier.size(300.dp, 100.dp)
            .testTag("image_moduleapplicationshareinfoview_armband_1"))
}
