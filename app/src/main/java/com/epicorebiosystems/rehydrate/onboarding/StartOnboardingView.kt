package com.epicorebiosystems.rehydrate.onboarding

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.epicorebiosystems.rehydrate.BuildConfig
import com.epicorebiosystems.rehydrate.OnboardingScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun StartOnboardingView(chViewModel: ModelData, navController: NavController) {

    val version = "Version : " + BuildConfig.VERSION_NAME + " " + BuildConfig.VERSION_CODE

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            contentAlignment = Alignment.TopCenter,
        ) {

            SignInScreen()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {

                Spacer(modifier = Modifier.weight(1f))

                Button(onClick = trackClick(targetName = "Open OnboardingScreens.CreateAccountGetStartedView") {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            navController.navigate(OnboardingScreens.CreateAccountGetStartedView.route)
                        }
                    },
                    modifier = Modifier
                        .size(width = 260.dp, height = 60.dp)
                        .padding(bottom = 10.dp)
                        .testTag("button_startonboardingview_create_new_account"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White
                    )) {
                    Text(stringResource(R.string.create_new_account),
                        modifier = Modifier.align(Alignment.CenterVertically).testTag("text_startonboardingview_create_new_account"),
                        textAlign = TextAlign.Center,
                        fontFamily = OswaldFonts,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorResource(R.color.waterFull))
                }

                Row(
                    modifier = Modifier.padding(bottom = 40.dp, top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.already_have_acct),
                        modifier = Modifier.testTag("text_startonboardingview_already"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Button(
                        onClick = trackClick(targetName = "Open - SharedScreens.EditEnterprise") {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.navigate(OnboardingScreens.LogInEnterEmailAddressScreen.route)
                            }
                        },
                        modifier = Modifier.testTag("button_startonboardingview_login"),
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(stringResource(R.string.log_in),
                            modifier = Modifier.padding(start = 5.dp).testTag("text_startonboardingview_login"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            style = TextStyle(textDecoration = TextDecoration.Underline),
                            color = Color.White
                        )
                    }
                }

                Row {
                    Text("\u00a92025 Epicore Biosystems Inc.",
                        modifier = Modifier.testTag("text_startonboardingview_copyright"),
                        fontFamily = OswaldFonts,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    Text("$version",
                        modifier = Modifier.testTag("text_startonboardingview_version"),
                        fontFamily = OswaldFonts,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }

            }
        }
    }
}

@Composable
fun SignInScreen() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.signin_background),
                contentDescription = "image_sign_in_background",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.signin_epicore_logo),
                    contentDescription = "image_epicore_logo",
                    modifier = Modifier
                        .size(300.dp, 200.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.signin_worker),
                    contentDescription = "image_worker",
                    modifier = Modifier
                        .size(300.dp, 300.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}