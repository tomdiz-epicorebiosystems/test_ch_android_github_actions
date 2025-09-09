package com.epicorebiosystems.rehydrate.onboarding

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import java.util.regex.Pattern

@Composable
fun LogInEnterEmailAddressScreen(chViewModel: ModelData, navController: NavController) {

    val scope  = rememberCoroutineScope()
    val connection by connectivityState()
    var showNetworkProgress by rememberSaveable { mutableStateOf(false) }
    var showServerErrorMsg by rememberSaveable { mutableStateOf(false) }
    var showNoAccountFound by rememberSaveable { mutableStateOf(false) }
    var serverErrorMsg = remember { mutableStateOf("") }
    val signInEmailPlaceholder = "example@mycompany.com"

    val invalidEmailAddressErrorMsg = stringResource(R.string.invalid_email_address)

    Box(Modifier.fillMaxSize()) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.onboardingVeryDarkBackground))
                .padding(horizontal = 0.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.account_login),
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .testTag("text_loginenteremailaddressview_account_login"),
                textAlign = TextAlign.Center
            )

            // Header image
            Image(
                painter = painterResource(R.drawable.onboarding_email_photo_hardhat),
                contentDescription = "image_loginenteremailaddressview_hard_hat",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .testTag("image_loginenteremailaddressview_hard_hat"),
                contentScale = ContentScale.Fit
            )

            // Email entry form
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, end = 40.dp, top = 10.dp)
            ) {
                Text(stringResource(R.string.welcome_back),
                    modifier = Modifier.testTag("text_loginenteremailaddressview_welcome"),
                    fontFamily = OswaldFonts,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(stringResource(R.string.enter_email),
                    fontFamily = RobotoMediumFonts,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp).testTag("text_loginenteremailaddressview_enter_email")
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 5.dp)
                        .height(60.dp)
                        .testTag("textfield_loginenteremailaddressview_email"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontFamily = RobotoRegularFonts, fontSize = 20.sp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Email,
                        autoCorrect = false
                    ),
                    shape = RoundedCornerShape(10.dp),
                    value = chViewModel.usersEmailAddress.value,
                    onValueChange = {
                        chViewModel.usersEmailAddress.value = it
                        chViewModel.updateEmailAddress(chViewModel.usersEmailAddress.value) },
                    placeholder = {
                        Text(signInEmailPlaceholder,
                            modifier = Modifier.fillMaxWidth(),
                            color = colorResource(R.color.placeHolderTextColor),
                            textAlign = TextAlign.Center) },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colorResource(R.color.settingsColorCoalText),
                        disabledTextColor = Color.Transparent,
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        placeholderColor = colorResource(R.color.placeHolderTextColor)))
            }

            if (showServerErrorMsg) {
                Text(
                    text = serverErrorMsg.value ?: "Unknown server API issue",
                    color = Color.Red,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                        .testTag("text_loginenteremailaddressview_servererror")
                )
            }

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = trackClick(targetName = "loginContext") {
                        if (validateEmail(chViewModel.usersEmailAddress.value) && chViewModel.usersEmailAddress.value.isNotBlank()) {

                            // QA automation testing
                            if (BuildConfig.QA_TESTING) {
                                navController.navigate(OnboardingScreens.LogInMainView.route)
                                return@trackClick
                            }

                            chViewModel.updateIsCreateAccountFlow(false)
                            showNetworkProgress = true
                            showServerErrorMsg = false
                            if (connection === ConnectionState.Available) {

                                val accessToken = chViewModel.encryptedPreferences.getString("access_token", "")
                                    ?.replace("\"", "")
                                    ?: ""
                                val refreshToken = chViewModel.encryptedPreferences.getString("refresh_token", "")
                                    ?.replace("\"", "")
                                    ?: ""

                                if (chViewModel.jwtEnterpriseID.value.isNotEmpty() && chViewModel.jwtSiteID.value.isNotEmpty()) {
                                    chViewModel.onboardingEnterpriseId.value = chViewModel.jwtEnterpriseID.value + "-" + chViewModel.jwtSiteID.value

                                    if(!chViewModel.onboardingComplete.value) {
                                        scope.launch {
                                            val enterpriseInfo =
                                                chViewModel.networkManager.getEnterpriseName(
                                                    chViewModel.onboardingEnterpriseId.value
                                                )

                                            if (enterpriseInfo.error != null) {
                                                showServerErrorMsg = true
                                                serverErrorMsg.value = enterpriseInfo.error
                                            } else {
                                                chViewModel.onboardingEnterpriseName.value =
                                                    enterpriseInfo.enterpriseName.toString()
                                                chViewModel.onboardingSiteName.value =
                                                    enterpriseInfo.siteName.toString()
                                            }
                                        }
                                    }
                                }

                                if (chViewModel.currentAuthAPIServer.value == chViewModel.serverSettings.value && chViewModel.currentAuthUserEmail.value.isNotEmpty()
                                    && accessToken.isNotEmpty() && refreshToken.isNotEmpty() && chViewModel.CH_EnterpriseName.value.isNotEmpty() && chViewModel.jwtEnterpriseID.value.isNotEmpty() &&
                                    chViewModel.jwtSiteID.value.isNotEmpty() && chViewModel.currentAuthUserEmail.value == chViewModel.usersEmailAddress.value &&
                                    chViewModel.currentAuthUserId.value.isNotEmpty() && chViewModel.currentAuthUserRole.value.isNotEmpty() && chViewModel.networkManager.isTokenValid()) {

                                    chViewModel.enterpriseId.value = chViewModel.jwtEnterpriseID.value + "-" +  chViewModel.jwtSiteID.value

                                    chViewModel.userExists = true
                                    chViewModel.userExistsKeystore = true
                                    chViewModel.continueWithOnboarding = false

                                    scope.launch {
                                        chViewModel.networkManager.getNewRefreshToken()
                                        navController.navigate(OnboardingScreens.LogInUserExistsScreen.route)
                                    }
                                }
                                else {
                                    val email = chViewModel.usersEmailAddress.value
                                    chViewModel.clearUserDataStore(false)
                                    chViewModel.updateEmailAddress(email)
                                    scope.launch {
                                        val loginContext =
                                            chViewModel.networkManager.getUserLoginContext(email = chViewModel.usersEmailAddress.value)
                                        Log.d("loginContext", loginContext.toString())
                                        if (loginContext.error != null) {
                                            showServerErrorMsg = true
                                            serverErrorMsg.value = loginContext.error
                                        } else {
                                            chViewModel.userExists =
                                                loginContext.userStatus == "exists"
                                            showNoAccountFound = chViewModel.userExists == false
                                            if (chViewModel.userExists) {
                                                chViewModel.continueWithOnboarding = false
                                                navController.navigate(OnboardingScreens.LogInMainView.route)
                                            }
                                            else {
                                                showServerErrorMsg = true
                                                serverErrorMsg.value = "Email address not recognized"
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
                        }
                        else {
                            serverErrorMsg.value = invalidEmailAddressErrorMsg
                            showServerErrorMsg = true
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    enabled = chViewModel.usersEmailAddress.value.isNotEmpty(),
                    modifier = Modifier
                        .size(width = 180.dp, height = 50.dp)
                        .testTag("button_loginenteremailaddressview_continue"),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White
                    )
                ) {
                    Text(text = if (showNoAccountFound) stringResource(R.string.try_again) else stringResource(R.string.continue_button),
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .testTag(if (showNoAccountFound) "text_loginenteremailaddressview_button_tryagain" else "text_loginenteremailaddressview_button_continue"),
                        textAlign = TextAlign.Center,
                        fontFamily = OswaldFonts,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (chViewModel.usersEmailAddress.value.isBlank())
                            Color.Gray
                        else
                            colorResource(R.color.waterFull)
                    )
                }
            }

            // Create account links
            if (showNoAccountFound) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(stringResource(R.string.create_new_acct),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 14.sp,
                        color = Color.White,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            navController.navigateUp()
                        }.testTag("text_loginenteremailaddressview_create")
                    )
                    Text(stringResource(R.string.requires_onboarding),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.clickable {
                            navController.navigateUp()
                        }.testTag("button_loginenteremailaddressview_requires")
                    )
                }
            }
        }

        // Network progress overlay
        if (showNetworkProgress) {
            FullScreenProgressView(R.string.verifying, true)
        }

    }
}

@Composable
fun LogInUserExistsScreen(chViewModel: ModelData, navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.onboardingVeryDarkBackground))
            .padding(20.dp)
    ) {
        LaunchedEffect(Unit) {
            chViewModel.networkManager.getUserInfo()
        }

        LogInShowAccountText()

        Spacer(Modifier.height(8.dp))

        Text(
            text = chViewModel.usersEmailAddress.value,
            modifier = Modifier.testTag("text_loginuserexistsview_emailaddress"),
            fontFamily = RobotoRegularFonts,
            fontSize = 18.sp,
            color = colorResource(R.color.onboardingEmailColor)
        )

        Spacer(Modifier.height(12.dp))

        Text(stringResource(R.string.confirm_key_info),
            modifier = Modifier.testTag("text_loginuserexistsview_pair"),
            fontFamily = RobotoRegularFonts,
            fontSize = 14.sp,
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))

        Text(stringResource(R.string.confirm_site_info),
            modifier = Modifier.testTag("text_loginuserexistsview_confirm"),
            fontFamily = RobotoRegularFonts,
            fontSize = 14.sp,
            color = Color.White
        )

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_location_pin_24),
                contentDescription = "image_loginuserexistsview_mappin",
                modifier = Modifier.testTag("image_loginuserexistsview_mappin"),
                colorFilter = ColorFilter.tint(colorResource(R.color.waterFull))
            )

            Text(
                text = chViewModel.CH_EnterpriseName.value,
                fontFamily = OswaldFonts,
                fontSize = 28.sp,
                color = colorResource(R.color.waterFull),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().testTag("text_loginuserexistsview_enterprisename")
            )

            Text(
                text = chViewModel.onboardingSiteName.value,
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                color = colorResource(R.color.waterFull),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().testTag("text_loginuserexistsview_sitename")
            )
        }

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            ) {
            Button(
                onClick = trackClick(targetName = "Open - SharedScreens.EditEnterprise") {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(SharedScreens.EditEnterprise.route!!)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                modifier = Modifier.fillMaxWidth().testTag("button_loginuserexistsview_change"),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Text(stringResource(R.string.change_enterprise),
                    modifier = Modifier.fillMaxWidth().testTag("text_loginuserexistsview_change"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    color = colorResource(R.color.linkStandardText)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = trackClick(targetName = "log_in_flow_user_exists_pair_module") {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(OnboardingScreens.LogInPairModuleView.route)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                enabled = chViewModel.usersEmailAddress.value.isNotEmpty(),
                modifier = Modifier
                    .size(width = 180.dp, height = 50.dp)
                    .testTag("button_loginuserexistsview_correct"),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color.White
                )
            ) {
                Text(stringResource(R.string.this_is_correct),
                    modifier = Modifier.fillMaxWidth().testTag("text_loginuserexistsview_correct"),
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    color = colorResource(R.color.onboardingLtBlueColor)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun LogInShowAccountText() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // “ACCOUNT LOGIN”
        Text(
            text = stringResource(R.string.account_login),
            fontFamily = OswaldFonts,
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .testTag("text_loginshowaccounttext_account_login"),
            textAlign = TextAlign.Center
        )

        // gray divider line
        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
        )

        Text( stringResource(R.string.login_successful),
            fontFamily = RobotoRegularFonts,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 10.dp)
                .testTag("text_loginshowaccounttext_success")
        )
    }

}

@Composable
fun LogInCheckEmailView(chViewModel: ModelData, navController: NavController) {
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
                Text(stringResource(R.string.account_login),
                    modifier = Modifier.testTag("text_logincheckemailview_account_login"),
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
                                .padding(top = 20.dp, start = 20.dp, bottom = 40.dp, end = 20.dp)
                                .testTag("text_logincheckemailview_account_check_inbox"),
                            fontFamily = RobotoMediumFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                        Text(stringResource(R.string.complete_login_passcode),
                            modifier = Modifier
                                .padding(start = 20.dp, bottom = 20.dp, end = 20.dp)
                                .testTag("text_logincheckemailview_complete_login"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                        Text(
                            chViewModel.usersEmailAddress.value,
                            modifier = Modifier
                                .padding(top = 3.dp, start = 20.dp, end = 20.dp, bottom = 3.dp)
                                .testTag("text_logincheckemailview_user_email"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

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
                                modifier = Modifier.testTag("button_logincheckemailview_how_navigate"),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                Text(
                                    stringResource(R.string.how_do_i_navigate_to_my_email),
                                    modifier = Modifier.testTag("text_logincheckemailview_how_navigate"),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Normal,
                                    style = TextStyle(textDecoration = TextDecoration.Underline),
                                    color = colorResource(R.color.linkStandardText)
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            Button(
                                onClick = trackClick(targetName = "Open - LogInEnterCodeView") {
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        navController.navigate(OnboardingScreens.LogInEnterCodeView.route)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = colorResource(
                                        R.color.onboardingVeryDarkBackground
                                    )
                                ),
                                modifier = Modifier.testTag("button_logincheckemailview_enter_code"),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                Text(
                                    stringResource(R.string.enter_verification_code_manually),
                                    modifier = Modifier.testTag("text_logincheckemailview_enter_code"),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Normal,
                                    style = TextStyle(textDecoration = TextDecoration.Underline),
                                    color = colorResource(R.color.linkStandardText)
                                )
                            }

                            Spacer(Modifier.weight(1f))

                        }
                    }
                }

            }
        }
    }
}

@Composable
fun LogInMainView(chViewModel: ModelData, navController: NavController) {
    val context = LocalContext.current
    val scope  = rememberCoroutineScope()
    var showNetworkProgress by rememberSaveable { mutableStateOf(false) }
    var showServerErrorMsg by rememberSaveable { mutableStateOf(false) }
    var serverErrorMsg = ""
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.account_login),
                modifier = Modifier.testTag("text_logInmainview_accountsetup"),
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

                Text(stringResource(R.string.find_enterrpise_id),
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, bottom = 20.dp)
                        .testTag("text_loginmainview_find"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                Image(
                    painterResource(R.drawable.getstarted_enterprise),
                    contentDescription = "image_loginmainview_started",
                    modifier = Modifier.size(200.dp, 200.dp).testTag("image_loginmainview_started"))

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
                        .padding(bottom = 5.dp, top = 10.dp)
                        .testTag("button_loginmainview_qrcode"),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.scan_qr_code),
                            modifier = Modifier.align(Alignment.CenterVertically).testTag("text_loginmainview_qrcode"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor))

                        Image(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_qr_code_scanner_24),
                            contentDescription = "image_loginmainview_qrcode",
                            modifier = Modifier.testTag("image_loginmainview_qrcode"),
                            colorFilter = ColorFilter.tint(colorResource(R.color.onboardingLtBlueColor)))
                    }
                }

                Text(
                    stringResource(R.string.or_enter_manually),
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 10.dp)
                        .testTag("text_loginmainview_enter_manually"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                TextField(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .height(60.dp)
                        .width(180.dp)
                        .testTag("textfield_loginmainview_enterprise"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontFamily = RobotoMediumFonts, fontSize = 22.sp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(10.dp),
                    value = chViewModel.enterpriseId.value,
                    onValueChange = {
                        chViewModel.enterpriseId.value = it },
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
                                .padding(top = 10.dp, start = 20.dp)
                                .testTag("text_loginmainview_servererror"),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 14.sp,
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
                        chViewModel.enterpriseId.value = chViewModel.enterpriseId.value.trimEnd(' ')
                        if (isValidEnterpriseCode(chViewModel.enterpriseId.value)) {

                            // QA automation testing
                            if (BuildConfig.QA_TESTING) {
                                navController.navigate(OnboardingScreens.LogInCheckEmailView.route)
                                return@trackClick
                            }

                            showNetworkProgress = true
                            scope.launch {
                                chViewModel.updateEnterpriseId(chViewModel.enterpriseId.value)

                                chViewModel.onboardingEnterpriseId.value = chViewModel.enterpriseId.value

                                val sendCode = chViewModel.networkManager.sendCode(
                                    chViewModel.usersEmailAddress.value,
                                    chViewModel.enterpriseId.value
                                )

                                if (sendCode.error != null) {
                                    showServerErrorMsg = true
                                    serverErrorMsg = sendCode.error!!
                                }
                                else {
                                    navController.navigate(OnboardingScreens.LogInCheckEmailView.route)
                                }
                                showNetworkProgress = false
                            }
                        }
                        else {
                            showServerErrorMsg = true
                            serverErrorMsg = context.resources.getString(R.string.enterpise_id_is_wrong_format)
                        }
                    },
                        modifier = Modifier
                            .size(width = 180.dp, height = 60.dp)
                            .testTag("button_loginmainview_submit"),
                        enabled = chViewModel.enterpriseId.value.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(
                            stringResource(R.string.submit),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_loginmainview_submit"),
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
fun LogInNavToEmailView(chViewModel: ModelData, navController: NavController) {
    val context = LocalContext.current
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.navigating_to_email),
                modifier = Modifier.testTag("text_loginnavtoemailview_navigating"),
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
                    painterResource(R.drawable.progress_bar_2),
                    contentDescription = "text_loginnavtoemailview_progress_2",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(bottom = 20.dp).testTag("text_loginnavtoemailview_progress_2"),
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        stringResource(R.string.if_you_use_the_android_mail),
                        modifier = Modifier
                            .padding(top = 20.dp, start = 20.dp, bottom = 20.dp)
                            .testTag("text_loginnavtoemailview_email"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Button(onClick = trackClick(targetName = "Calling sendMail - opens email app") {
                        //context.sendMail()
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                        context.startActivity(intent)
                    },
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(width = 290.dp, height = 70.dp)
                            .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
                            .testTag("button_loginnavtoemailview_emailapp"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_email_24),
                                contentDescription = "image_loginnavtoemailview_email",
                                modifier = Modifier.testTag("image_loginnavtoemailview_email"),
                                colorFilter = ColorFilter.tint(colorResource(R.color.onboardingVeryDarkBackground)))

                            Text(
                                stringResource(R.string.take_me_to_android_s_mail_app),
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .offset(y = -(5).dp)
                                    .testTag("text_loginnavtoemailview_emailapp"),
                                textAlign = TextAlign.Center,
                                fontFamily = OswaldFonts,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.onboardingVeryDarkBackground)
                            )
                        }
                    }

                    Text(
                        stringResource(R.string.if_you_check_your_email_another_way),
                        modifier = Modifier
                            .padding(start = 20.dp, bottom = 20.dp)
                            .testTag("text_loginnavtoemailview_check"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string._1_switch_to_the_app_you_use_to_check_email),
                        modifier = Modifier
                            .padding(start = 20.dp, bottom = 10.dp)
                            .testTag("text_loginnavtoemailview_step1"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string._2_look_for_an_email_from_epicore_biosystems),
                        modifier = Modifier
                            .padding(start = 20.dp, bottom = 10.dp)
                            .testTag("text_loginnavtoemailview_step2"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string._3_follow_instructions_in_the_email),
                        modifier = Modifier
                            .padding(start = 20.dp, bottom = 10.dp)
                            .testTag("text_loginnavtoemailview_step3"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Button(
                            onClick = trackClick(targetName = "Open troubleshooting") {
                                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                    navController.navigate(OnboardingScreens.LogInTroubleshootingEmailView.route)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                            modifier = Modifier.testTag("button_loginnavtoemailview_more"),
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Text(
                                stringResource(R.string.more_troubleshooting_steps),
                                modifier = Modifier.testTag("text_loginnavtoemailview_more"),
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
}

@Composable
fun LogInEnterCodeView(chViewModel: ModelData, navController: NavController, emailCode: String?) {
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
            Text(stringResource(R.string.account_login),
                modifier = Modifier.testTag("text_loginentercodeview_account_login"),
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
                        .testTag("text_loginentercodeview_enter_code"),
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
                        .testTag("textfield_loginentercodeview_code"),
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
                                .padding(top = 20.dp, start = 20.dp)
                                .testTag("textfield_loginentercodeview_servererror"),
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
                                navController.navigate(OnboardingScreens.LogInUserExistsScreen.route)
                            }
                            else {
                                navController.navigate(OnboardingScreens.LogInVerificationFailedView.route)
                            }
                        }

                        else {
                            showNetworkProgress = true
                            val authReturnCode = chViewModel.networkManager.authenticateWithCode(
                                chViewModel.usersEmailAddress.value,
                                verificationCode.value
                            )

                            if (authReturnCode != null) {
                                navController.navigate(OnboardingScreens.LogInVerificationFailedView.route)
                            } else {

                                chViewModel.userExistsKeystore = true

                                if (chViewModel.jwtEnterpriseID.value.isNotEmpty() && chViewModel.jwtSiteID.value.isNotEmpty()) {
                                    chViewModel.onboardingEnterpriseId.value = chViewModel.jwtEnterpriseID.value + "-" + chViewModel.jwtSiteID.value

                                    if(!chViewModel.onboardingComplete.value) {
                                        scope.launch {
                                            val enterpriseInfo =
                                                chViewModel.networkManager.getEnterpriseName(
                                                    chViewModel.onboardingEnterpriseId.value
                                                )

                                            if (enterpriseInfo.error != null) {
                                                showServerErrorMsg = true
                                                serverErrorMsg = enterpriseInfo.error
                                            } else {
                                                chViewModel.onboardingEnterpriseName.value =
                                                    enterpriseInfo.enterpriseName.toString()
                                                chViewModel.onboardingSiteName.value =
                                                    enterpriseInfo.siteName.toString()
                                            }
                                        }
                                    }
                                }

                                navController.navigate(OnboardingScreens.LogInUserExistsScreen.route)
                            }
                            showNetworkProgress = false
                        }
                    }
                },
                    modifier = Modifier
                        .size(width = 180.dp, height = 60.dp)
                        .padding(bottom = 10.dp)
                        .testTag("button_loginentercodeview_button_submit"),
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
                                .testTag("text_loginentercodeview_button_submit"),
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
                        modifier = Modifier.testTag("button_loginentercodeview_call"),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            stringResource(R.string.call_for_support),
                            modifier = Modifier.testTag("text_loginentercodeview_button_call"),
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
fun LogInTroubleshootingEmailView(chViewModel: ModelData, navController: NavController) {
    val context = LocalContext.current
    val scope  = rememberCoroutineScope()
    var showNetworkProgress by rememberSaveable { mutableStateOf(false) }
    var showServerErrorMsg by rememberSaveable { mutableStateOf(false) }
    var serverErrorMsg = ""

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.troubleshooting_steps),
                modifier = Modifier.testTag("text_logintroubleshootingemailview_troubleshooting"),
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
                    painterResource(R.drawable.progress_bar_2),
                    contentDescription = "text_logintroubleshootingemailview_progress_2",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(bottom = 20.dp).testTag("text_logintroubleshootingemailview_progress_2")
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        stringResource(R.string.email_not_received),
                        modifier = Modifier
                            .padding(top = 20.dp, start = 20.dp, bottom = 20.dp)
                            .testTag("text_logintroubleshootinginstructionsView_not_received"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.it_may_take_5_minutes_for_email_to_arrive),
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .testTag("text_logintroubleshootinginstructionsView_1"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.check_spam_or_junk_folders),
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .testTag("text_logintroubleshootinginstructionsView_2"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(
                            R.string.ensure_you_re_checking_inbox_for,
                            chViewModel.usersEmailAddress.value
                        ),
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .testTag("text_logintroubleshootinginstructionsView_3"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.check_network_connection),
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .testTag("text_logintroubleshootinginstructionsView_4"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.request_a_new_email_below),
                        modifier = Modifier
                            .padding(start = 20.dp, bottom = 20.dp)
                            .testTag("text_logintroubleshootinginstructionsView_5"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.link_verification_code_not_working),
                        modifier = Modifier
                            .padding(top = 20.dp, start = 20.dp, bottom = 20.dp)
                            .testTag("text_logintroubleshootinginstructionsView_link_1"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.link_verification_code_may_have_expired_request_a_new_email_below),
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .testTag("text_logintroubleshootinginstructionsView_link_2"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.if_manually_entered),
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .testTag("text_logintroubleshootinginstructionsView_2"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
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
                                        .padding(top = 20.dp, start = 20.dp)
                                        .testTag("text_logintroubleshootinginstructionsView_serrvererror"),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Red
                                )
                            }
                        }

                        Button(onClick = trackClick(targetName = "resend email button pressed") {
                            showNetworkProgress = true
                            scope.launch {
                                chViewModel.updateEnterpriseId(chViewModel.enterpriseId.value)

                                chViewModel.onboardingEnterpriseId.value = chViewModel.enterpriseId.value

                                val sendCode = chViewModel.networkManager.sendCode(
                                    chViewModel.usersEmailAddress.value,
                                    chViewModel.enterpriseId.value
                                )

                                if (sendCode.error != null) {
                                    showServerErrorMsg = true
                                    serverErrorMsg = sendCode.error!!
                                } else {
                                    navController.navigate(OnboardingScreens.LogInCheckEmailView.route)
                                }
                                showNetworkProgress = false
                            }
                        },
                            modifier = Modifier
                                .size(width = 180.dp, height = 60.dp)
                                .padding(bottom = 10.dp)
                                .testTag("button_logintroubleshootingemailview_resend"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = Color.White
                            )) {
                            Text(
                                stringResource(R.string.resend_email),
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .testTag("text_logintroubleshootingemailview_resend"),
                                textAlign = TextAlign.Center,
                                fontFamily = OswaldFonts,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.onboardingLtBlueColor)
                            )
                        }

                        Button(
                            onClick = trackClick(targetName = "user pressed dial support") {
                                context.dial(phone = "+1-617-397-3756")
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                            modifier = Modifier.testTag("button_logintroubleshootingemailview_call"),
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Text(
                                stringResource(R.string.trouble_call_for_support),
                                modifier = Modifier.testTag("text_logintroubleshootingemailview_call"),
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

        if (showNetworkProgress) {
            FullScreenProgressView(R.string.verifying, true)
        }

    }
}

@Composable
fun LogInVerificationFailedView(chViewModel: ModelData, navController: NavController) {
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
            Text(stringResource(R.string.account_login),
                modifier = Modifier.testTag("text_logincountmaintitleview_accountsetup"),
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
                        .padding(top = 40.dp, bottom = 20.dp).testTag("text_loginverificationfailedview_check"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Red
                )

                Text(
                    stringResource(R.string.carefully_check_the_verification_code),
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 20.dp, start = 20.dp).testTag("text_loginverificationfailedview_check"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                Button(onClick = trackClick(targetName = "LogInVerificationFailedView back button pressed") {
                    navController.navigateUp()
                },
                    modifier = Modifier
                        .size(width = 280.dp, height = 60.dp)
                        .padding(bottom = 10.dp)
                        .testTag("button_loginverificationfailedview_enter_code"),
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
                                .testTag("text_loginverificationfailedview_enter_code"),
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
                        .padding(top = 40.dp, bottom = 20.dp, start = 20.dp).testTag("text_loginverificationfailedview_request"),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                Button(onClick = trackClick(targetName = "Calling sendCode") {

                    chViewModel.onboardingEnterpriseId.value = chViewModel.enterpriseId.value

                    sendCodeScope.launch {
                        chViewModel.networkManager.sendCode(
                            chViewModel.usersEmailAddress.value,
                            chViewModel.enterpriseId.value
                        )
                    }
                    navController.navigate(OnboardingScreens.LogInCheckEmailView.route)
                },
                    modifier = Modifier
                        .size(width = 280.dp, height = 60.dp)
                        .padding(bottom = 10.dp)
                        .testTag("button_loginverificationfailedview_resend"),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.resend_email),
                            modifier = Modifier.align(Alignment.CenterVertically).testTag("text_loginverificationfailedview_resend"),
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
                        modifier = Modifier.testTag("button_loginverificationfailedview_call"),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            stringResource(R.string.call_for_support),
                            modifier = Modifier.testTag("text_loginverificationfailedview_call"),
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
fun LogInAccountCreatedView(chViewModel: ModelData, navController: NavController) {
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LaunchedEffect(Unit) {
                chViewModel.networkManager.getUserInfo()
            }

            Text(
                stringResource(R.string.account_login),
                modifier = Modifier.testTag("text_logincreateaccounttext_accountlogin"),
                textAlign = TextAlign.Center,
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(stringResource(R.string.login_successful),
                        modifier = Modifier
                            .offset(x = -(10).dp)
                            .padding(start = 20.dp, bottom = if (isJapanese) 10.dp else 20.dp)
                            .testTag("text_logincreateaccounttext_success"),
                        fontFamily = RobotoMediumFonts,
                        fontSize = if (isJapanese) 16.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        chViewModel.usersEmailAddress.value,
                        modifier = Modifier
                            .padding(start = 10.dp, bottom = if (isJapanese) 20.dp else 40.dp)
                            .testTag("text_loginaccountcreatedview_emailaddress"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = if (isJapanese) 16.sp else 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorResource(R.color.onboardingEmailColor)
                    )

                    Text(stringResource(R.string.confirm_key_info),
                        modifier = Modifier
                            .offset(x = -(10).dp)
                            .padding(
                                top = 10.dp,
                                start = 20.dp,
                                bottom = if (isJapanese) 10.dp else 20.dp,
                                end = 20.dp
                            )
                            .testTag("text_loginaccountcreatedview_next"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = if (isJapanese) 16.sp else 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(stringResource(R.string.confirm_site_info),
                        modifier = Modifier
                            .offset(x = -(10).dp)
                            .padding(
                                top = 20.dp,
                                start = 20.dp,
                                bottom = if (isJapanese) 10.dp else 18.dp,
                                end = 20.dp
                            )
                            .testTag("text_loginaccountcreatedview_please"),
                        fontFamily = RobotoRegularFonts,
                        fontSize = if (isJapanese) 16.sp else 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Image(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_location_pin_24),
                            contentDescription = "image_loginaccountcreatedview_mappin",
                            modifier = Modifier.testTag("image_loginaccountcreatedview_mappin"),
                            colorFilter = ColorFilter.tint(colorResource(R.color.waterFull))
                        )

                        Text(
                            if (chViewModel.isTestAccount()) "Epicore Biosystems Inc." else chViewModel.onboardingEnterpriseName.value,  //chViewModel.CH_EnterpriseName.value,
                            modifier = Modifier
                                .padding(start = 10.dp, bottom = 10.dp)
                                .testTag("text_loginaccountcreatedview_enterprise_name"),
                            fontFamily = OswaldFonts,
                            fontSize = if (isJapanese) 28.sp else 32.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.waterFull)
                        )

                        Text(
                            if (chViewModel.isTestAccount()) "EBS-TES1" else chViewModel.onboardingSiteName.value, //("${chViewModel.jwtEnterpriseID.value} - ${chViewModel.jwtSiteID.value}"),
                            modifier = Modifier
                                .padding(start = 10.dp, bottom = 10.dp)
                                .testTag("text_loginaccountcreatedview_site_name"),
                            fontFamily = OswaldFonts,
                            fontSize = if (isJapanese) 24.sp else 28.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.waterFull),
                            textAlign = TextAlign.Center,
                        )
                    }

                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (isJapanese) 10.dp else 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = trackClick(targetName = "Open - OnboardingScreens.LogInPairModuleView") {
                        navController.navigate(OnboardingScreens.LogInPairModuleView.route)
                    },
                        modifier = Modifier
                            .size(width = 200.dp, height = 60.dp)
                            .testTag("button_loginaccountcreatedview_correct"),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.this_is_correct),
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .testTag("text_loginaccountcreatedview_correct"),
                                textAlign = TextAlign.Center,
                                fontFamily = OswaldFonts,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.onboardingLtBlueColor)
                            )
                        }
                    }

                    Button(
                        onClick = trackClick(targetName = "Open - SharedScreens.EditEnterprise") {
                            navController.navigate(SharedScreens.EditEnterprise.route!!)
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.onboardingVeryDarkBackground)),
                        modifier = Modifier.testTag("button_loginaccountcreatedview_change"),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(stringResource(R.string.change_enterprise_id),
                            modifier = Modifier.testTag("text_loginaccountcreatedview_change"),
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