package com.epicorebiosystems.rehydrate.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
import com.epicorebiosystems.rehydrate.SettingsSubScreens
import com.epicorebiosystems.rehydrate.SharedScreens
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.dial
import com.epicorebiosystems.rehydrate.modelData.isValidEnterpriseCode
import com.epicorebiosystems.rehydrate.modelData.validateEmail
import com.epicorebiosystems.rehydrate.networkManager.ConnectionState
import com.epicorebiosystems.rehydrate.networkManager.connectivityState
import com.epicorebiosystems.rehydrate.sharedViews.FullScreenProgressView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoMediumFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import kotlinx.coroutines.launch

@Composable
fun CreateAccountGetStartedView(chViewModel: ModelData, navController: NavController) {

    var isTermsChecked by rememberSaveable { mutableStateOf(false) }
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.onboardingVeryDarkBackground))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(stringResource(R.string.welcome_connected),
            modifier = Modifier.testTag("text_createaccountgetstartedview_welcome"),
            fontSize = 18.sp,
            fontFamily = RobotoMediumFonts,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(stringResource(R.string.get_started_gather),
            modifier = Modifier.testTag("text_createaccountgetstartedview_started_gather").semantics {
                this.contentDescription = "text_createaccountgetstartedview_started_gather"
            },
            fontSize = 16.sp,
            fontFamily = OswaldFonts,
            color = Color.White,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(stringResource(R.string.also_need),
            modifier = Modifier.testTag("text_createaccountgetstartedview_need"),
            fontSize = 16.sp,
            fontFamily = OswaldFonts,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconGroupDisplay(chViewModel.getCurrentLocale(), R.drawable.getstarted_phoneicon, stringResource(R.string.smart_phone))
            IconGroupDisplay(chViewModel.getCurrentLocale(), R.drawable.getstarted_internet, stringResource(R.string.internet_conn))
            IconGroupDisplay(chViewModel.getCurrentLocale(), R.drawable.getstarted_enterprise, stringResource(R.string.site_id_enterprise))
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = isTermsChecked,
                colors = CheckboxDefaults.colors(
                    checkedColor = colorResource(R.color.linkStandardText),
                    uncheckedColor = Color.White,
                ),
                onCheckedChange = { checked ->
                    isTermsChecked = checked
                }
            )

            Column {
                Text(
                    stringResource(R.string.i_agree_to_epicore_biosystem_s),
                    modifier = Modifier.testTag("text_createaccountgetstartedview_agree"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                val fontSize = 12.sp
                //if (isJapanese) {
                //    if (heightModifier < 400.dp) {
                //        fontSize = 12.sp
                //    }
                //}

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.onboarding_terms_conditions),
                        modifier = Modifier.clickable {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.navigate(SettingsSubScreens.TermsConditions.route!!)
                            }
                        }.testTag("text_createaccountgetstartedview_terms"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Normal,
                        style = TextStyle(textDecoration = TextDecoration.Underline),
                        color = colorResource(R.color.linkStandardText)
                    )

                    Text(
                        stringResource(R.string.and_acknowledge),
                        modifier = Modifier.testTag("text_createaccountgetstartedview_ack"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                }

                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isJapanese) {
                        Text(
                            stringResource(R.string.that_the),
                            modifier = Modifier.testTag("text_createaccountgetstartedview_that"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    }

                    Text(
                        stringResource(R.string.privacy_policy),
                        Modifier.clickable {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.navigate(SettingsSubScreens.PrivacyPolicy.route!!)
                            }
                        }.testTag("text_createaccountgetstartedview_privacy"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Normal,
                        style = TextStyle(textDecoration = TextDecoration.Underline),
                        color = colorResource(R.color.linkStandardText)
                    )

                    Text(
                        stringResource(R.string.applies),
                        modifier = Modifier.testTag("text_createaccountgetstartedview_applies"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                }

            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = trackClick(targetName = "Open - OnboardingScreens.InitialSetupView") {
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(OnboardingScreens.InitialSetupView.route)
                    }
                }
            },
            modifier = Modifier
                .padding(top = 10.dp, bottom = 30.dp)
                .size(width = 180.dp, height = 50.dp)
                .testTag("button_createaccountgetstartedview_started"),
            shape = RoundedCornerShape(10.dp),
            enabled = isTermsChecked,
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = Color.White
            )
        ) {
            Text(stringResource(R.string.get_started),
                modifier = Modifier.testTag("text_createaccountgetstartedview_started").semantics {
                    this.contentDescription = "text_createaccountgetstartedview_started"
                },
                fontSize = 18.sp,
                fontFamily = OswaldFonts,
                fontWeight = FontWeight.Normal,
                color = if (isTermsChecked)
                    colorResource(R.color.waterFull)
                else
                    Color.Gray
                )
        }

    }

}

@Composable
fun IconGroupDisplay(language: String, imageRes: Int, label: String) {
    val removeSpaces = label.replace("\\s".toRegex(), "")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "image_createaccountgetstartedview_$removeSpaces",
            modifier = Modifier
                .padding(bottom = 10.dp)
                .size(100.dp)
                .testTag("image_createaccountgetstartedview_$removeSpaces")
        )
        Text(
            text = label,
            fontSize = if (language == "ja_JP") 12.sp else 14.sp,
            fontFamily = RobotoRegularFonts,
            color = Color(0xFFB0B0B0),
            textAlign = TextAlign.Center,
            modifier = Modifier.size(width = 110.dp, height = 50.dp).testTag("text_createaccountgetstartedview_$removeSpaces")
        )
    }
}

@Composable
fun CreateAccountMainView(chViewModel: ModelData, navController: NavController) {
    val context = LocalContext.current
    val scope  = rememberCoroutineScope()
    var showNetworkProgress by rememberSaveable { mutableStateOf(false) }
    var showServerErrorMsg by rememberSaveable { mutableStateOf(false) }

    var serverErrorMsg = ""

    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(stringResource(R.string.account_setup),
                modifier = Modifier.testTag("text_createaccountmaintitleview_accountsetup"),
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
                painterResource(R.drawable.progress_bar_4_5),
                contentDescription = "image_createaccountmainview_progress_1",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.padding(bottom = 20.dp).testTag("image_createaccountmainview_progress_1"))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(stringResource(R.string.find_enterrpise_id),
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, bottom = 20.dp).testTag("text_createaccountmainview_find"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Image(
                    painterResource(R.drawable.getstarted_enterprise),
                    contentDescription = "image_createaccountmainview_getstarted",
                    modifier = Modifier.size(200.dp, 200.dp).testTag("image_createaccountmainview_getstarted"))

                var buttonWidth = 180.dp
                var fontSize = 18.sp
                if (chViewModel.getCurrentLocale() == "ja_JP") {
                    buttonWidth = 280.dp
                    fontSize = 14.sp
                }

                Button(onClick = trackClick(targetName = "Open SharedScreens.ScanEnterpriseQRCode") {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            navController.navigate(SharedScreens.ScanEnterpriseQRCode.route!!)
                        }
                    },
                    modifier = Modifier
                        .size(width = buttonWidth, height = 60.dp)
                        .padding(bottom = 5.dp, top = 5.dp)
                        .testTag("button_createaccountmainview_qrcode"),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.scan_qr_code),
                            modifier = Modifier.align(Alignment.CenterVertically).testTag("text_createaccountmainview_qrcode"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor))

                        Image(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_qr_code_scanner_24),
                            contentDescription = "image_createaccountmainview_qrcode",
                            modifier = Modifier.testTag("image_createaccountmainview_qrcode"),
                            colorFilter = ColorFilter.tint(colorResource(R.color.onboardingLtBlueColor)))
                    }
                }

                Text(
                    stringResource(R.string.or_enter_manually),
                    modifier = Modifier
                        .padding(top = 5.dp, bottom = 5.dp)
                        .testTag("text_createaccountmainview_enter_manually"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                TextField(
                    modifier = Modifier
                        .height(60.dp)
                        .width(180.dp)
                        .testTag("textfield_createaccountmainview_enterprise"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontFamily = RobotoMediumFonts, fontSize = 22.sp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(10.dp),
                    value = chViewModel.onboardingEnterpriseId.value,
                    onValueChange = {
                        chViewModel.onboardingEnterpriseId.value = it },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colorResource(R.color.settingsColorCoalText),
                        disabledTextColor = Color.Transparent,
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                if (showServerErrorMsg) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            serverErrorMsg,
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .testTag("text_createaccountmainview_servererror"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = if (isJapanese) 12.sp else 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Normal,
                            color = Color.Red
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = trackClick(targetName = "Calling updateEnterpriseId") {
                            showServerErrorMsg = false
                            chViewModel.onboardingEnterpriseId.value = chViewModel.onboardingEnterpriseId.value.trimEnd(' ')

                            if (chViewModel.enterpriseId.value.isEmpty()) {
                                chViewModel.enterpriseId.value = chViewModel.onboardingEnterpriseId.value
                            }

                        if (chViewModel.onboardingEnterpriseId.value == "DEMO-DEMO") {
                            chViewModel.usersEmailAddress.value = "demo@demo.com"
                            chViewModel.onboardingEnterpriseName.value = "Demo"
                            chViewModel.jwtEnterpriseID.value = "DEMO"
                            chViewModel.jwtSiteID.value = "DEMO"
                            chViewModel.enterpriseId.value = "DEMO-DEMO"
                            chViewModel.currentAuthUserId.value = "9911ff32-957c-4f39-a754-e381ad7c3a2c"
                            chViewModel.CH_UserRole.value = "CH_USER"

                            chViewModel.isDemoOnboardingFlow.value = true
                            chViewModel.updateDemoDemoMode(true)

                            chViewModel.switchShareAnonymousDataEpicore = false

                            chViewModel.onboardingStep = 2
                            navController.navigate(OnboardingScreens.InitialSetupView.route)
                            return@trackClick
                        }

                        if (isValidEnterpriseCode(chViewModel.onboardingEnterpriseId.value)) {

                            showNetworkProgress = true

                            chViewModel.isDemoOnboardingFlow.value = false
                            chViewModel.updateDemoDemoMode(false)

                            //chViewModel.CH_EnterpriseName.value = ""
                            //chViewModel.CH_SiteName.value = ""
                            //chViewModel.onboardingEnterpriseName.value = ""
                            //chViewModel.onboardingSiteName.value = ""

                            scope.launch {

                                val enterpriseInfo = chViewModel.networkManager.getEnterpriseName(chViewModel.onboardingEnterpriseId.value)

                                if (enterpriseInfo.error != null) {
                                    showServerErrorMsg = true
                                    serverErrorMsg = enterpriseInfo.error
                                }
                                else {
                                    chViewModel.onboardingEnterpriseName.value = enterpriseInfo.enterpriseName.toString()
                                    chViewModel.onboardingSiteName.value = enterpriseInfo.siteName.toString()
                                    chViewModel.enterpriseId.value = chViewModel.onboardingEnterpriseId.value
                                    navController.navigate(OnboardingScreens.CreateAccountConfirmEnterprise.route)
                                }
                                showNetworkProgress = false
                            }
                        }
                        else {
                            showServerErrorMsg = true
                            serverErrorMsg = context.resources.getString(R.string.enterpise_id_is_wrong_format)
                        } },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp).testTag("button_createaccountmainview_submit"),
                        enabled = chViewModel.onboardingEnterpriseId.value.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(
                            stringResource(R.string.submit),
                            modifier = Modifier.align(Alignment.CenterVertically).testTag("text_createaccountmainview_submit"),
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

        if (showNetworkProgress) {
            FullScreenProgressView(R.string.verifying, false)
        }

    }

}

@Composable
fun CreateAccountConfirmEnterprise(chViewModel: ModelData, navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.onboardingVeryDarkBackground))
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = stringResource(R.string.account_setup),
            fontFamily = OswaldFonts,
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("text_createaccountmaintitleview_accountsetup"),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // gray divider line
        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painterResource(R.drawable.progress_bar_4_4),
                contentDescription = "image_createaccountconfirmenterprise_progress_2",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.padding(bottom = 20.dp).testTag("image_createaccountconfirmenterprise_progress_2")
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.confirm_job_site),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp).testTag("text_createaccountconfirmenterprise_confirm"),
            fontFamily = RobotoRegularFonts,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(Modifier.height(60.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_location_pin_24),
                contentDescription = "image_createaccountconfirmenterprise_mappin",
                modifier = Modifier.testTag("image_createaccountconfirmenterprise_mappin"),
                colorFilter = ColorFilter.tint(colorResource(R.color.waterFull))
            )

            Text(
                text = chViewModel.onboardingEnterpriseName.value,
                fontFamily = OswaldFonts,
                fontSize = 32.sp,
                color = colorResource(R.color.waterFull),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().testTag("text_createaccountconfirmenterprise_enterprisename")
            )

            if (chViewModel.onboardingSiteName.value.isEmpty()) {
                Text("${chViewModel.jwtEnterpriseID.value}-${chViewModel.jwtSiteID.value}",
                    fontFamily = OswaldFonts,
                    fontSize = 28.sp,
                    color = colorResource(R.color.waterFull),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().testTag("text_createaccountconfirmenterprise_enterprisesite")
                )
            }
            else {
                Text(
                    text = chViewModel.onboardingSiteName.value,
                    fontFamily = OswaldFonts,
                    fontSize = 28.sp,
                    color = colorResource(R.color.waterFull),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().testTag("text_createaccountconfirmenterprise_sitename")
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = trackClick(targetName = "CreateAccountEnterEmailAddress") {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(OnboardingScreens.CreateAccountEnterEmailAddress.route)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .size(width = 180.dp, height = 50.dp).testTag("button_createaccountconfirmenterprise_confirm"),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color.White
                )
            ) {
                Text(stringResource(R.string.confirm),
                    modifier = Modifier.testTag("text_createaccountconfirmenterprise_confirm"),
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    color = colorResource(R.color.onboardingLtBlueColor)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = trackClick(targetName = "Open - SharedScreens.EditEnterprise") {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {

                        val splitCode = chViewModel.onboardingEnterpriseId.value.split("-")
                        if (splitCode.isNotEmpty()) {
                            chViewModel.jwtEnterpriseID.value = splitCode[0]
                            chViewModel.jwtSiteID.value = splitCode[1]
                        }

                        navController.navigate(SharedScreens.EditEnterprise.route!!)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                modifier = Modifier.testTag("button_createaccountconfirmenterprise_incorrect"),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Text(stringResource(R.string.site_incorrect),
                    modifier = Modifier.testTag("text_createaccountconfirmenterprise_incorrect"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    color = colorResource(R.color.linkStandardText)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun CreateAccountEnterEmailAddress(chViewModel: ModelData, navController: NavController) {
    val signInEmailPlaceholder = "example@mycompany.com"
    val scope  = rememberCoroutineScope()
    val connection by connectivityState()
    var showNetworkProgress by rememberSaveable { mutableStateOf(false) }
    var showServerErrorMsg by rememberSaveable { mutableStateOf(false) }
    var serverErrorMsg = remember { mutableStateOf("") }
    var onboardingUsersEmailAddress = remember { mutableStateOf("") }

    val invalidEmailAddressErrorMsg = stringResource(R.string.invalid_email_address)
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"

    Card {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.onboardingVeryDarkBackground))
                .padding(top = 20.dp, bottom = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.account_setup),
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .testTag("text_createaccountmaintitleview_accountsetup"),
                textAlign = TextAlign.Center
            )

            // gray divider line
            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painterResource(R.drawable.progress_bar_4_3),
                    contentDescription = "image_createaccountenteremailaddress_progress_3",
                    modifier = Modifier.testTag("image_createaccountenteremailaddress_progress_3"),
                    contentScale = ContentScale.FillBounds,
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.enterprise_code_confirmed),
                    modifier = Modifier
                        .padding(top = 20.dp, start = 20.dp)
                        .testTag("text_createaccountenteremailaddress_confirm"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_location_pin_24),
                    contentDescription = "image_createaccountenteremailaddress_mappin",
                    modifier = Modifier.testTag("image_createaccountenteremailaddress_mappin"),
                    colorFilter = ColorFilter.tint(colorResource(R.color.waterFull))
                )

                Text(
                    text = chViewModel.onboardingEnterpriseName.value,
                    fontFamily = OswaldFonts,
                    fontSize = 32.sp,
                    color = colorResource(R.color.waterFull),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().testTag("text_createaccountenteremailaddress_enterprisename")
                )

                if (chViewModel.onboardingSiteName.value.isEmpty()) {
                    Text("${chViewModel.jwtEnterpriseID.value}-${chViewModel.jwtSiteID.value}",
                        fontFamily = OswaldFonts,
                        fontSize = 28.sp,
                        color = colorResource(R.color.waterFull),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().testTag("text_createaccountenteremailaddress_sitecode")
                    )
                } else {
                    Text(
                        text = chViewModel.onboardingSiteName.value,
                        fontFamily = OswaldFonts,
                        fontSize = 28.sp,
                        color = colorResource(R.color.waterFull),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().testTag("text_createaccountenteremailaddress_sitename")
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Column {

                Text(
                    stringResource(R.string.enter_email_next),
                    modifier = Modifier
                        .padding(top = 20.dp, start = 20.dp)
                        .testTag("text_createaccountenteremailaddress_enter"),
                    fontFamily = OswaldFonts,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 5.dp)
                        .height(60.dp)
                        .testTag("textfield_createaccountenteremailaddress_emailaddress"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontFamily = RobotoRegularFonts,
                        fontSize = 20.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Email,
                        autoCorrect = false
                    ),
                    shape = RoundedCornerShape(10.dp),
                    value = onboardingUsersEmailAddress.value,
                    onValueChange = {
                        onboardingUsersEmailAddress.value = it
                    },
                    placeholder = {
                        Text(
                            signInEmailPlaceholder,
                            modifier = Modifier.fillMaxWidth(),
                            color = colorResource(R.color.placeHolderTextColor),
                            textAlign = TextAlign.Center,
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colorResource(R.color.settingsColorCoalText),
                        disabledTextColor = Color.Transparent,
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        placeholderColor = colorResource(R.color.placeHolderTextColor)
                    )
                )

                if (showServerErrorMsg) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            serverErrorMsg.value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .testTag("text_createaccountenteremailaddress_servererror"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = if (isJapanese) 12.sp else 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Normal,
                            color = Color.Red
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(
                        onClick = trackClick(targetName = "CreateAccountContext") {
                            if (validateEmail(onboardingUsersEmailAddress.value) && onboardingUsersEmailAddress.value.isNotBlank()) {

                                // QA automation testing
                                if (BuildConfig.QA_TESTING) {
                                    chViewModel.usersEmailAddress.value = onboardingUsersEmailAddress.value
                                    navController.navigate(OnboardingScreens.CreateAccountCheckEmailView.route)
                                    return@trackClick
                                }
                                chViewModel.usersEmailAddress.value = onboardingUsersEmailAddress.value
                                chViewModel.updateIsCreateAccountFlow(true)
                                showNetworkProgress = true
                                showServerErrorMsg = false
                                if (connection === ConnectionState.Available) {

                                    val accessToken = chViewModel.encryptedPreferences.getString(
                                        "access_token",
                                        ""
                                    )
                                        ?.replace("\"", "")
                                        ?: ""
                                    val refreshToken = chViewModel.encryptedPreferences.getString(
                                        "refresh_token",
                                        ""
                                    )
                                        ?.replace("\"", "")
                                        ?: ""

                                    if (chViewModel.currentAuthAPIServer.value == chViewModel.serverSettings.value && chViewModel.currentAuthUserEmail.value.isNotEmpty()
                                        && accessToken.isNotEmpty() && refreshToken.isNotEmpty() && chViewModel.CH_EnterpriseName.value.isNotEmpty() && chViewModel.jwtEnterpriseID.value.isNotEmpty() &&
                                        chViewModel.jwtSiteID.value.isNotEmpty() && chViewModel.currentAuthUserEmail.value == chViewModel.usersEmailAddress.value &&
                                        chViewModel.currentAuthUserId.value.isNotEmpty() && chViewModel.currentAuthUserRole.value.isNotEmpty() && chViewModel.networkManager.isTokenValid()
                                    ) {

                                        chViewModel.enterpriseId.value = chViewModel.jwtEnterpriseID.value + "-" +  chViewModel.jwtSiteID.value

                                        chViewModel.userExists = true
                                        chViewModel.userExistsKeystore = true
                                        onboardingUsersEmailAddress.value = ""

                                        scope.launch {
                                            chViewModel.networkManager.getNewRefreshToken()
                                            if (chViewModel.onboardingEnterpriseId.value != chViewModel.enterpriseId.value) {
                                                navController.navigate(OnboardingScreens.CreateAccountChooseCurrentEnterprise.route)
                                            }
                                            else {
                                                navController.navigate(OnboardingScreens.CreateAccountUserExistsScreen.route)
                                            }
                                        }
                                    } else {
                                        onboardingUsersEmailAddress.value = ""
                                        scope.launch {
                                            val loginContext =
                                                chViewModel.networkManager.getUserLoginContext(email = chViewModel.usersEmailAddress.value)
                                            //Log.d("loginContext", loginContext.toString())
                                            if (loginContext.error != null) {
                                                showServerErrorMsg = true
                                                serverErrorMsg.value = loginContext.error
                                            } else {
                                                chViewModel.userExists = loginContext.userStatus == "exists"

                                                val sendCode = chViewModel.networkManager.sendCode(
                                                    chViewModel.usersEmailAddress.value,
                                                    chViewModel.enterpriseId.value
                                                )

                                                if (sendCode.error != null) {
                                                    showServerErrorMsg = true
                                                    serverErrorMsg.value = sendCode.error!!
                                                } else {
                                                    navController.navigate(OnboardingScreens.CreateAccountCheckEmailView.route)
                                                }

                                            }
                                            showNetworkProgress = false
                                        }
                                    }
                                } else {
                                    showNetworkProgress = false
                                    showServerErrorMsg = true
                                    serverErrorMsg.value = "There is no internet connection."
                                }
                            } else {
                                serverErrorMsg.value = invalidEmailAddressErrorMsg
                                showServerErrorMsg = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        enabled = onboardingUsersEmailAddress.value.isNotEmpty(),
                        modifier = Modifier
                            .size(width = 180.dp, height = 50.dp)
                            .testTag("button_createaccountenteremailaddress_continue"),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )
                    ) {
                        Text(
                            stringResource(R.string.continue_button),
                            modifier = Modifier.align(Alignment.CenterVertically).testTag("text_createaccountenteremailaddress_continue"),
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

        if (showNetworkProgress) {
            FullScreenProgressView(R.string.verifying, true)
        }
    }
}

@Composable
fun CreateAccountCheckEmailView(chViewModel: ModelData, navController: NavController) {
    Card {
        BoxWithConstraints {
            val heightModifier = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(colorResource(R.color.onboardingVeryDarkBackground)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(30.dp))

                Text(stringResource(R.string.account_setup),
                    modifier = Modifier.testTag("text_createaccountmaintitleview_accountsetup"),
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

                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            stringResource(R.string.check_your_email_inbox),
                            modifier = Modifier
                                .padding(top = 20.dp, start = 20.dp, bottom = 40.dp, end = 20.dp).testTag("text_createaccountcheckemailview_inbox"),
                            fontFamily = RobotoMediumFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                        Text(stringResource(R.string.complete_login_passcode),
                            modifier = Modifier
                                .padding(start = 20.dp, bottom = 20.dp, end = 20.dp).testTag("text_createaccountcheckemailview_complete"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                        Text(
                            chViewModel.usersEmailAddress.value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 3.dp, start = 20.dp, end = 20.dp, bottom = 3.dp).testTag("text_createaccountcheckemailview_emailaddress"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                        Spacer(Modifier.weight(1f))

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = if ((chViewModel.getCurrentLocale() == "ja_JP") || (heightModifier < 700.dp)) 0.dp else 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(if ((chViewModel.getCurrentLocale() == "ja_JP") || (heightModifier < 700.dp)) (-10).dp else 0.dp, Alignment.Bottom)
                        ) {

                            var fontSize = 16.sp
                            if (chViewModel.getCurrentLocale() == "ja_JP") {
                                if (heightModifier < 700.dp) {
                                    fontSize = 14.sp
                                }
                            }

                            Button(
                                onClick = trackClick(targetName = "Open - LogInNavToEmailView") {
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        navController.navigate(OnboardingScreens.LogInNavToEmailView.route)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = colorResource(
                                        R.color.onboardingVeryDarkBackground
                                    )
                                ),
                                modifier = Modifier.testTag("button_createaccountcheckemailview_navigate"),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                Text(
                                    stringResource(R.string.how_do_i_navigate_to_my_email),
                                    modifier = Modifier.testTag("text_createaccountcheckemailview_navigate"),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Normal,
                                    style = TextStyle(textDecoration = TextDecoration.Underline),
                                    color = colorResource(R.color.linkStandardText)
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = trackClick(targetName = "Open - CreateAccountEnterCodeView") {
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        navController.navigate(OnboardingScreens.CreateAccountEnterCodeView.route)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = colorResource(
                                        R.color.onboardingVeryDarkBackground
                                    )
                                ),
                                modifier = Modifier.testTag("button_createaccountcheckemailview_entercode"),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                Text(
                                    stringResource(R.string.enter_verification_code_manually),
                                    modifier = Modifier.testTag("text_createaccountcheckemailview_entercode"),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Normal,
                                    style = TextStyle(textDecoration = TextDecoration.Underline),
                                    color = colorResource(R.color.linkStandardText)
                                )
                            }

                            Spacer(Modifier.height(30.dp))

                        }
                    }
                }

            }
        }
    }
}

@Composable
fun CreateAccountEnterCodeView(chViewModel: ModelData, navController: NavController, emailCode: String?) {
    val scope  = rememberCoroutineScope()
    var showNetworkProgress by rememberSaveable { mutableStateOf(false) }
    val verificationCode = remember { mutableStateOf("") }
    val context = LocalContext.current
    var showServerErrorMsg by rememberSaveable { mutableStateOf(false) }
    var serverErrorMsg = ""
    //Log.d("DEEPLINK", "emailCode = ${emailCode}")
    if (emailCode != null) {
        verificationCode.value = emailCode
    }
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(stringResource(R.string.account_setup),
                modifier = Modifier.testTag("text_createaccountmaintitleview_accountsetup"),
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

                Text(
                    stringResource(R.string.enter_verification_code_received_by_email),
                    modifier = Modifier
                        .offset(x = -(10).dp)
                        .padding(top = 20.dp, start = 20.dp, bottom = 20.dp)
                        .testTag("text_createaccountentercodeview_verification"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                TextField(
                    modifier = Modifier
                        .padding(top = 5.dp, bottom = 40.dp)
                        .height(60.dp)
                        .width(180.dp)
                        .testTag("textfield_createaccountentercodeview_code"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontFamily = RobotoMediumFonts, fontSize = 22.sp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(10.dp),
                    value = verificationCode.value,
                    onValueChange = {
                        verificationCode.value = it },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colorResource(R.color.settingsColorCoalText),
                        disabledTextColor = Color.Transparent,
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                if (showServerErrorMsg) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            serverErrorMsg,
                            modifier = Modifier
                                .padding(top = 20.dp, start = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Red
                        )
                    }
                }

                Button(onClick = trackClick(targetName = "calling authenticateWithCode") {
                    scope.launch {

                        // QA automation testing
                        if (BuildConfig.QA_TESTING) {
                            if (verificationCode.value == "1234") {
                                chViewModel.onboardingStep = 2
                                navController.navigate(OnboardingScreens.InitialSetupView.route)
                                return@launch
                           }
                        }

                        // Handle test account here, bypass authentication with code.
                        if (chViewModel.isTestAccount()) {
                            if (verificationCode.value == "123456") {
                                navController.navigate(OnboardingScreens.CreateAccountUserExistsScreen.route)
                            }
                            else {
                                navController.navigate(OnboardingScreens.CreateAccountVerificationFailedView.route)
                            }
                        }

                        else {
                            showNetworkProgress = true

                            if (chViewModel.usersEmailAddress.value.isEmpty()) {
                                chViewModel.updateCreateAccountUserInfo()
                                if (chViewModel.usersEmailAddress.value.isEmpty()) {
                                    showNetworkProgress = false
                                    serverErrorMsg = "Email address is empty"
                                    return@launch
                                }
                            }

                            if (chViewModel.enterpriseId.value.isEmpty()) {
                                chViewModel.enterpriseId.value = chViewModel.onboardingEnterpriseId.value
                            }

                            val authReturnCode = chViewModel.networkManager.authenticateWithCode(
                                chViewModel.usersEmailAddress.value,
                                verificationCode.value
                            )

                            if (authReturnCode != null) {
                                navController.navigate(OnboardingScreens.CreateAccountVerificationFailedView.route)
                            } else {

                                chViewModel.networkManager.getUserInfo()

                                //if (chViewModel.onboardingEnterpriseId.value != chViewModel.enterpriseId.value && chViewModel.userExists) {
                                //    navController.navigate(OnboardingScreens.CreateAccountChooseCurrentEnterprise.route)
                                //}
                                //else {
                                    if (chViewModel.userExistsKeystore && chViewModel.userExists) {
                                        navController.navigate(OnboardingScreens.LogInPairModuleView.route)
                                    }
                                    else {

                                        chViewModel.userExistsKeystore = true

                                        chViewModel.onboardingStep = 2
                                        navController.navigate(OnboardingScreens.InitialSetupView.route)
                                    }
                                //}
                            }

                            showNetworkProgress = false
                        }
                    }
                },
                    modifier = Modifier
                        .size(width = 180.dp, height = 60.dp)
                        .padding(bottom = 10.dp)
                        .testTag("button_createaccountentercodeview_submit"),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.submit_button),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_createaccountentercodeview_submit"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(
                        onClick = trackClick(targetName = "user pressed dial support") {
                            context.dial(phone = "+1-617-397-3756")
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                        modifier = Modifier.testTag("button_createaccountentercodeview_call"),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            stringResource(R.string.call_for_support),
                            modifier = Modifier.testTag("text_createaccountentercodeview_call"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            style = TextStyle(textDecoration = TextDecoration.Underline),
                            color = colorResource(R.color.linkStandardText)
                        )
                    }
                }

            }
        }

        if (showNetworkProgress) {
            FullScreenProgressView(R.string.verifying, true)
        }

    }
}

@Composable
fun CreateAccountVerificationFailedView(chViewModel: ModelData, navController: NavController) {
    val context = LocalContext.current
    val sendCodeScope  = rememberCoroutineScope()

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.account_setup),
                modifier = Modifier.testTag("text_createaccountmaintitleview_accountsetup"),
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

                Text(
                    stringResource(R.string.verification_did_not_succeed),
                    modifier = Modifier
                        .padding(top = 40.dp, bottom = 20.dp).testTag("text_createaccountverificationfailedview_check"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Red
                )

                Text(
                    stringResource(R.string.carefully_check_the_verification_code),
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 20.dp, start = 20.dp).testTag("text_createaccountverificationfailedview_check"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                Button(onClick = trackClick(targetName = "CreateAccountVerificationFailedView back button pressed") {
                    navController.navigateUp()
                },
                    modifier = Modifier
                        .size(width = 280.dp, height = 60.dp)
                        .padding(bottom = 10.dp)
                        .testTag("button_accountcreateverificationfailedview_enter_code"),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.enter_code_manually),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_accountcreateverificationfailedview_enter_code"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor)
                        )
                    }
                }

                Text(
                    stringResource(R.string.request_a_new_email_containing),
                    modifier = Modifier
                        .padding(top = 40.dp, bottom = 20.dp, start = 20.dp)
                        .testTag("text_createaccountverificationfailedview_request"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                Button(onClick = trackClick(targetName = "Calling sendCode") {
                    sendCodeScope.launch {
                        chViewModel.networkManager.sendCode(
                            chViewModel.usersEmailAddress.value,
                            chViewModel.enterpriseId.value
                        )
                    }
                    navController.navigate(OnboardingScreens.CreateAccountCheckEmailView.route)
                },
                    modifier = Modifier
                        .size(width = 280.dp, height = 60.dp)
                        .padding(bottom = 10.dp)
                        .testTag("button_createaccountverificationfailedview_resend"),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.resend_email),
                            modifier = Modifier.align(Alignment.CenterVertically).testTag("text_createaccountverificationfailedview_resend"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(
                        onClick = trackClick(targetName = "user pressed dial support") {
                            context.dial(phone = "+1-617-397-3756")
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                        modifier = Modifier.testTag("button_createaccountverificationfailedview_call"),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            stringResource(R.string.call_for_support),
                            modifier = Modifier.testTag("text_createaccountverificationfailedview_call"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
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
fun CreateAccountUserExistsScreen(chViewModel: ModelData, navController: NavController) {

    val scope  = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.onboardingVeryDarkBackground))
            .padding(top = 20.dp, bottom = 20.dp)
    ) {
        CreateShowAccountText(true)

        Spacer(Modifier.height(10.dp))

        Text(
            text = chViewModel.usersEmailAddress.value,
            fontFamily = RobotoRegularFonts,
            fontSize = 18.sp,
            color = colorResource(R.color.onboardingEmailColor),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp)
                .testTag("button_createaccountUserexistsscreen_emailaddress")
        )

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_location_pin_24),
                contentDescription = "button_createaccountUserexistsscreen_mappin",
                modifier = Modifier.testTag("button_createaccountUserexistsscreen_mappin"),
                colorFilter = ColorFilter.tint(colorResource(R.color.waterFull))
            )

            Text(
                text = chViewModel.onboardingEnterpriseName.value,
                fontFamily = OswaldFonts,
                fontSize = 28.sp,
                color = colorResource(R.color.waterFull),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().testTag("button_createaccountUserexistsscreen_enterprisename")
            )

            if (chViewModel.onboardingSiteName.value.isEmpty()) {
                Text("${chViewModel.jwtEnterpriseID.value}-${chViewModel.jwtSiteID.value}",
                    fontFamily = OswaldFonts,
                    fontSize = 24.sp,
                    color = colorResource(R.color.waterFull),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().testTag("button_createaccountUserexistsscreen_jwt")
                )
            } else {
                Text(
                    text = chViewModel.onboardingSiteName.value,
                    fontFamily = OswaldFonts,
                    fontSize = 24.sp,
                    color = colorResource(R.color.waterFull),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().testTag("button_createaccountUserexistsscreen_sitenname")
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(stringResource(R.string.choose_skip_onboarding),
            fontFamily = OswaldFonts,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp).testTag("button_createaccountUserexistsscreen_choose")
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = trackClick(targetName = "create_account_flow_user_exists_pair_module") {
                    /*if (chViewModel.userExists) {
                        scope.launch {
                            chViewModel.networkManager.getUserInfo()
                            navController.navigate(OnboardingScreens.LogInPairModuleView.route)
                        }
                    }
                    else {*/
                    chViewModel.continueWithOnboarding = true
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        scope.launch {
                            chViewModel.networkManager.getUserInfo()
                            chViewModel.onboardingStep = 2
                            navController.navigate(OnboardingScreens.InitialSetupView.route)
                        }
                    }
                    //}
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .size(width = 280.dp, height = 50.dp)
                    .testTag("button_createaccountUserexistsscreen_continue"),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color.White
                )
            ) {
                Text(stringResource(R.string.continue_with_onboard),
                    modifier = Modifier.testTag("text_createaccountUserexistsscreen_continue"),
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    color = colorResource(R.color.onboardingLtBlueColor)
                )
            }

            if (chViewModel.userExists) {
                Button(
                    onClick = trackClick(targetName = "Open - CreateAccount-SkipOnboarding") {
                        chViewModel.continueWithOnboarding = false
                        scope.launch {
                            chViewModel.networkManager.getUserInfo()
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.navigate(OnboardingScreens.LogInPairModuleView.route)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                    modifier = Modifier.testTag("button_createaccountUserexistsscreen_skip"),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Text(stringResource(R.string.skip_onboarding),
                        modifier = Modifier.testTag("text_createaccountUserexistsscreen_skip"),
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
fun CreateAccountChooseCurrentEnterprise(chViewModel: ModelData, navController: NavController) {

    val scopeUpdateUser  = rememberCoroutineScope()
    var isOnboardingEnterpriseChecked by remember { mutableStateOf(true) }
    var isEnterpriseChecked by remember { mutableStateOf(false) }
    var showNetworkProgress by remember { mutableStateOf(false) }

    Card {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.onboardingVeryDarkBackground))
                .padding(20.dp)
        ) {
            CreateShowAccountText(showSuccessText = false)

            Text(stringResource(R.string.enterprise_id_change),
                fontFamily = OswaldFonts,
                fontSize = 18.sp,
                color = colorResource(R.color.onboardingEmailColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                    .testTag("text_createaccountchoosecurrententerprise_changed"),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // --- Left option: Onboarding enterprise ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = chViewModel.onboardingEnterpriseName.value,
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        color = colorResource(R.color.waterFull),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .testTag("text_createaccountchoosecurrententerprise_enterprisename_1")
                    )

                    Text(
                        text = chViewModel.onboardingEnterpriseId.value,
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        color = colorResource(R.color.waterFull),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .testTag("text_createaccountchoosecurrententerprise_enterpriseid_1")
                    )

                    Checkbox(
                        checked = isOnboardingEnterpriseChecked,
                        modifier = Modifier.testTag("checkbox_createaccountchoosecurrententerprise_checkbox_1"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorResource(R.color.linkStandardText),
                            uncheckedColor = Color.White,
                        ),
                        onCheckedChange = { _ ->
                            isEnterpriseChecked = false
                            isOnboardingEnterpriseChecked = true
                        }
                    )
                }

                // --- Right option: Current CH enterprise ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = chViewModel.CH_EnterpriseName.value,
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        color = colorResource(R.color.waterFull),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .testTag("text_createaccountchoosecurrententerprise_enterprisename_2")
                    )

                    Text(
                        text = chViewModel.enterpriseId.value,
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        color = colorResource(R.color.waterFull),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .testTag("text_createaccountchoosecurrententerprise_enterpriseid_2")
                    )

                    Checkbox(
                        checked = isEnterpriseChecked,
                        modifier = Modifier.testTag("checkbox_createaccountchoosecurrententerprise_checkbox_2"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorResource(R.color.linkStandardText),
                            uncheckedColor = Color.White,
                        ),
                        onCheckedChange = { _ ->
                            isOnboardingEnterpriseChecked = false
                            isEnterpriseChecked = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = {
                        scopeUpdateUser.launch {
                            if (isOnboardingEnterpriseChecked) {
                                showNetworkProgress = true
                                // Use onboarding enterprise values
                                chViewModel.enterpriseId.value =
                                    chViewModel.onboardingEnterpriseId.value
                                chViewModel.CH_EnterpriseName.value =
                                    chViewModel.onboardingEnterpriseId.value

                                // Parse enterprise code and site ID
                                val parts = chViewModel.enterpriseId.value.split("-")
                                if (parts.size >= 2) {
                                    val enterpriseCode = parts[0]
                                    val siteId = parts[1]

                                    val userInfo: Map<String, Any> = mapOf(
                                        "height" to chViewModel.userHeightCm.value,
                                        "weight" to chViewModel.userWeightKg.value,
                                        "biologicalSex" to if (chViewModel.userGender.value == "Male") "male" else "female"
                                    )

                                    chViewModel.networkManager.updateUser(
                                        enterpriseId = enterpriseCode,
                                        siteId = siteId,
                                        userInfo = userInfo
                                    )

                                    showNetworkProgress = false
                                }

                                navController.navigate(OnboardingScreens.CreateAccountUserExistsScreen.route)
                            }
                            else {

                                if(!chViewModel.onboardingComplete.value) {
                                    val enterpriseInfo =
                                        chViewModel.networkManager.getEnterpriseName(
                                            chViewModel.enterpriseId.value
                                        )

                                    if (enterpriseInfo.error != null) {

                                    } else {
                                        chViewModel.onboardingEnterpriseName.value =
                                            enterpriseInfo.enterpriseName.toString()
                                        chViewModel.onboardingSiteName.value =
                                            enterpriseInfo.siteName.toString()
                                    }
                                }

                                navController.navigate(OnboardingScreens.CreateAccountUserExistsScreen.route)
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .size(width = 280.dp, height = 50.dp)
                        .testTag("button_createaccountchoosecurrententerprise_ok"),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.hydration_ok),
                        modifier = Modifier.testTag("text_createaccountchoosecurrententerprise_ok"),
                        fontFamily = OswaldFonts,
                        fontSize = 18.sp,
                        color = colorResource(R.color.onboardingLtBlueColor)
                    )
                }

            }
        }

        if (showNetworkProgress) {
            FullScreenProgressView(R.string.updating_user_info, true)
        }
    }

}

@Composable
fun CreateShowAccountText(showSuccessText: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.account_setup),
            fontFamily = OswaldFonts,
            fontSize = 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp)
                .testTag("text_createshowaccounttext_accountsetup")
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colorResource(R.color.onboardingLtGrayColor))
        )

        if (showSuccessText) {
            Text(stringResource(R.string.login_successful),
                fontFamily = RobotoRegularFonts,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 5.dp, start = 20.dp)
                    .testTag("text_createshowaccounttext_success")
            )
        }
    }
}