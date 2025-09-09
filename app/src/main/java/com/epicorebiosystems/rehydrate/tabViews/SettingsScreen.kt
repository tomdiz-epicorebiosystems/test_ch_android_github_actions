package com.epicorebiosystems.rehydrate.tabViews

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.BuildConfig
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.SettingsSubScreens
import com.epicorebiosystems.rehydrate.SharedScreens
import com.epicorebiosystems.rehydrate.SplashActivity
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.DisconnectEvent
import com.epicorebiosystems.rehydrate.sharedViews.BgStatusView
import com.epicorebiosystems.rehydrate.sharedViews.PhysiologyInformationView
import com.epicorebiosystems.rehydrate.sharedViews.SegmentedControl
import com.epicorebiosystems.rehydrate.topBarViews.NotificationView
import com.epicorebiosystems.rehydrate.topBarViews.TopBarView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoCondensedFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoFonts
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(chViewModel: ModelData,  ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController, updateHideBottomBar: (Boolean) -> Unit) {
    val version = "Version Name : " + BuildConfig.VERSION_NAME + "\n" + "Version Code : " + BuildConfig.VERSION_CODE
    val settingsScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopBarView(chViewModel, ebsDeviceMonitor, navController, updateHideBottomBar = { viewState ->
                updateHideBottomBar(viewState)
            })

            NotificationView(chViewModel)
        }
    ) {
        BgStatusView(chViewModel, ebsDeviceMonitor)

        if (chViewModel.scrollEnableShareSettingsView) {
            coroutineScope.launch {
                settingsScrollState.scrollTo(0)
                chViewModel.scrollEnableShareSettingsView = false
            }
        }

        if (chViewModel.scrollSettingsView) {
            coroutineScope.launch {
                settingsScrollState.scrollTo(settingsScrollState.maxValue)
                chViewModel.scrollSettingsView = false
            }
        }

        BoxWithConstraints {
            val widthModifier = maxWidth - 20.dp
            Column(
                modifier = Modifier
                    .verticalScroll(settingsScrollState)
                    .height(if (chViewModel.usersEmailAddress.value.contains("epicorebiosystems.com")) 1900.dp else 1840.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    Modifier
                        .height(if (chViewModel.usersEmailAddress.value.contains("epicorebiosystems.com")) 1580.dp else 1520.dp)
                        .width(widthModifier)
                        .offset(x = 10.dp, y = 120.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                ) {
                    Column {
                        Text(
                            stringResource(R.string.account_settings),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp),
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        AccountSettingsView(navController = navController, width = widthModifier, chViewModel = chViewModel, updateHideBottomBar = { viewState ->
                            updateHideBottomBar(viewState)
                        })

                        DataSharingSettingsView(chViewModel = chViewModel, width = widthModifier, showHeading = true)

                        MeasurementUnitSettingsView(chViewModel = chViewModel, width = widthModifier)

                        PhysiologyInformationView(navController = navController, chViewModel = chViewModel, ebsMonitor = ebsDeviceMonitor, showHeading = true, onBoarding = false, isEditing = false, updateHideBottomBar = { viewState ->
                            updateHideBottomBar(viewState)
                        })

                        ModuleSettingsView(chViewModel = chViewModel, ebsDeviceMonitor = ebsDeviceMonitor, navController = navController, width = widthModifier)

                        LegalRegulatoryView(chViewModel = chViewModel, ebsDeviceMonitor = ebsDeviceMonitor, navController = navController, width = widthModifier)
                    }
                }

                Box(
                    Modifier
                        .width(widthModifier)
                        .offset(x = 10.dp, y = 150.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("\u00a92024 Epicore Biosystems Inc.",
                            fontFamily = OswaldFonts,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray)

                        Text("$version",
                            fontFamily = OswaldFonts,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun AccountSettingsView(navController: NavController, width: Dp, chViewModel: ModelData, updateHideBottomBar: (Boolean) -> Unit) {
    Box(modifier = Modifier
        .height(40.dp)
        .width(width)
        .background(Color.LightGray)
    )
    {
        Row {
            Text(
                stringResource(R.string.account),
                Modifier.padding(start = 10.dp, top = 5.dp),
                fontFamily = OswaldFonts,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White)

            Spacer(modifier = Modifier.weight(1f))

            Text(stringResource(R.string.edit),
                Modifier
                    .padding(end = 10.dp, top = 5.dp)
                    .clickable {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            updateHideBottomBar(true)
                            navController.navigate(SharedScreens.EditEnterprise.route!!)
                        }
                    },
                fontFamily = OswaldFonts,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                color = colorResource(R.color.settingsColorHydroDarkText))
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.email),
                Modifier
                    .padding(start = 20.dp, top = 10.dp)
                    .alignByBaseline(),
                fontFamily = OswaldFonts,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.settingsColorCoalText))

            Spacer(modifier = Modifier.weight(1f))

            Text(if (chViewModel.isDemoOnboardingFlow.value) "demo@demo.com" else chViewModel.usersEmailAddress.value,
                Modifier
                    .padding(end = 20.dp, top = 10.dp)
                    .alignByBaseline(),
                fontFamily = RobotoCondensedFonts,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.enterprise),
                Modifier
                    .padding(start = 20.dp, top = 10.dp)
                    .alignByBaseline(),
                fontFamily = OswaldFonts,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.settingsColorCoalText))

            Spacer(modifier = Modifier.weight(1f))

            Text(if (chViewModel.isTestAccount()) "EBS" else ( if (chViewModel.isDemoOnboardingFlow.value) "DEMO" else chViewModel.jwtEnterpriseID.value),
                Modifier
                    .padding(end = 20.dp, top = 10.dp)
                    .alignByBaseline(),
                fontFamily = RobotoCondensedFonts,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.site),
                Modifier
                    .padding(start = 20.dp, top = 10.dp, bottom = 10.dp)
                    .alignByBaseline(),
                fontFamily = OswaldFonts,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.settingsColorCoalText))

            Spacer(modifier = Modifier.weight(1f))

            Text(if (chViewModel.isTestAccount()) "TES1" else ( if (chViewModel.isDemoOnboardingFlow.value) "DEMO" else chViewModel.jwtSiteID.value),
                Modifier
                    .padding(end = 20.dp, top = 10.dp, bottom = 10.dp)
                    .alignByBaseline(),
                fontFamily = RobotoCondensedFonts,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText))
        }
    }
}

@Composable
fun DataSharingSettingsView(chViewModel: ModelData, width: Dp, showHeading: Boolean) {
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"
    var switchShareAnonymousEnterpriseState by rememberSaveable { mutableStateOf(chViewModel.switchShareAnonymousDataEnterprise) }
    var switchShareAnonymousEpicoreState by rememberSaveable { mutableStateOf(chViewModel.switchShareAnonymousDataEpicore) }
    val scopeSetPrivacy  = rememberCoroutineScope()

    if (showHeading) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(width)
                .background(Color.LightGray)
        )
        {
            Text(
                stringResource(R.string.data_sharing),
                Modifier.padding(start = 10.dp, top = 5.dp)
                    .testTag("text_datasharingsettingsview_data_sharing"),
                fontFamily = OswaldFonts,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White)
        }
    }
    Column {
        Row(
            Modifier
                .padding(top = 10.dp, end = 20.dp)
        ) {
            Text(
                stringResource(
                    R.string.share_anonymous_data_with_occupational_hygienists,
                    if (chViewModel.isTestAccount()) "Epicore Biosystems" else chViewModel.CH_EnterpriseName.value
                ),
                Modifier
                    .padding(top = 5.dp, bottom = 10.dp, start = 20.dp)
                    .width(if (isJapanese) 220.dp else 250.dp)
                    .testTag("text_datasharingsettingsview_anonymous"),
                fontFamily = RobotoFonts,
                fontSize = if (isJapanese) 14.sp else 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Left,
                color = if (showHeading) colorResource(R.color.settingsColorCoalText) else Color.White)

            Spacer(modifier = Modifier.weight(1f))

            Switch(
                modifier = Modifier.testTag("toggle_datasharingsettingsview_anonymous"),
                checked = switchShareAnonymousEnterpriseState,
                onCheckedChange = {
                    switchShareAnonymousEnterpriseState = it
                    chViewModel.switchShareAnonymousDataEnterprise = it
                    scopeSetPrivacy.launch {
                        chViewModel.networkManager.setUserInfo(switchShareAnonymousEpicoreState, switchShareAnonymousEnterpriseState)
                    } },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colorResource(R.color.waterFull),
                    checkedBorderColor = colorResource(R.color.waterFull),
                    uncheckedThumbColor = if (!showHeading) colorResource(R.color.switch_off_onboard_thumb) else colorResource(R.color.switch_off_thumb),
                    uncheckedTrackColor = if (!showHeading) colorResource(R.color.switch_off_onboard_track) else colorResource(R.color.switch_off_track),
                    uncheckedBorderColor = if (!showHeading) colorResource(R.color.switch_off_onboard_boarder) else colorResource(R.color.switch_off_boarder),
                )

            )
        }

        if (showHeading) {
            Row(
                Modifier
                    .padding(end = 20.dp)
            ) {
                Text(
                    stringResource(R.string.share_anonymous_data_with_epicore_biosystems),
                    Modifier
                        .padding(top = 5.dp, bottom = 10.dp, start = 20.dp)
                    .width(if (isJapanese) 220.dp else 250.dp)
                        .testTag("text_datasharingsettingsview_epicore"),
                    fontFamily = RobotoFonts,
                    fontSize = if (isJapanese) 14.sp else 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Left,
                    color = colorResource(R.color.settingsColorCoalText)
                )

                Spacer(modifier = Modifier.weight(1f))

                if (chViewModel.isDemoOnboardingFlow.value) {
                    Switch(
                        checked = false,
                        onCheckedChange = {
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colorResource(R.color.waterFull),
                            checkedBorderColor = colorResource(R.color.waterFull),
                            uncheckedThumbColor = colorResource(R.color.switch_off_thumb),
                            uncheckedTrackColor = colorResource(R.color.switch_off_track),
                            uncheckedBorderColor = colorResource(R.color.switch_off_boarder),
                        )
                    )
                }
                else {
                    Switch(
                        checked = switchShareAnonymousEpicoreState,
                        onCheckedChange = {
                            switchShareAnonymousEpicoreState = it
                            chViewModel.switchShareAnonymousDataEpicore = it
                            scopeSetPrivacy.launch {
                                chViewModel.networkManager.setUserInfo(
                                    switchShareAnonymousEpicoreState,
                                    switchShareAnonymousEnterpriseState
                                )
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colorResource(R.color.waterFull),
                            checkedBorderColor = colorResource(R.color.waterFull),
                            uncheckedThumbColor = colorResource(R.color.switch_off_thumb),
                            uncheckedTrackColor = colorResource(R.color.switch_off_track),
                            uncheckedBorderColor = colorResource(R.color.switch_off_boarder),
                        )
                    )
                }
            }

            Text(
                stringResource(R.string.unchecking_this_will_disable_historical),
                Modifier
                    .padding(top = 5.dp, bottom = 10.dp, start = 10.dp)
                    .testTag("text_datasharingsettingsview_unchecking"),
                fontFamily = RobotoFonts,
                fontSize = if (isJapanese) 12.sp else 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText)
            )
        }
    }

}

@Composable
fun MeasurementUnitSettingsView(chViewModel: ModelData, width: Dp) {
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"

    Box(modifier = Modifier
        .height(40.dp)
        .width(width)
        .background(Color.LightGray)
    )
    {
        Text(
            stringResource(R.string.measurement_unit),
            Modifier.padding(start = 10.dp, top = 5.dp),
            fontFamily = OswaldFonts,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White)
    }

    Column {
        if (isJapanese) {
           Text(
                stringResource(R.string.unit),
                Modifier
                    .padding(top = 5.dp, bottom = 10.dp, start = 20.dp),
                fontFamily = RobotoFonts,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText)
            )
            
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val units = listOf(
                    stringResource(R.string.metric),
                    stringResource(R.string.imperial)
                )
                SegmentedControl(
                    items = units,
                    fontSize = 8.sp,
                    defaultSelectedItemIndex = chViewModel.currentUnits.value,
                ) {
                    Log.d("CustomToggle", "Selected item : ${units[it]}")
                    chViewModel.updateUnits(it)
                }
            }
        }
        else {
            Row(
                Modifier
                    .padding(top = 10.dp, end = 20.dp)
            ) {
                Text(
                    stringResource(R.string.unit),
                    Modifier
                        .padding(top = 5.dp, bottom = 10.dp, start = 20.dp),
                    fontFamily = RobotoFonts,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Left,
                    color = colorResource(R.color.settingsColorCoalText)
                )

                Spacer(modifier = Modifier.weight(1f))

                val units = listOf(stringResource(R.string.metric),
                    stringResource(R.string.imperial))
                SegmentedControl(
                    items = units,
                    fontSize = 12.sp,
                    defaultSelectedItemIndex = chViewModel.currentUnits.value
                ) {
                    Log.d("CustomToggle", "Selected item : ${units[it]}")
                    chViewModel.updateUnits(it)
                }
            }
        }
    }
}

@Composable
fun ModuleSettingsView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController, width: Dp) {
    var switchAlertMeState by rememberSaveable { mutableStateOf(true) }
    var switchPassiveLossState by rememberSaveable { mutableStateOf(chViewModel.userPassiveLossState) }
    var switchButtonWaterIntakeState by rememberSaveable { mutableStateOf(chViewModel.buttonPressWaterIntakeState) }
    val scopePassiveWaterLossState  = rememberCoroutineScope()

    Box(modifier = Modifier
        .height(40.dp)
        .width(width)
        .background(Color.LightGray)
    )
    {
        Text(
            stringResource(R.string.module),
            Modifier.padding(start = 10.dp, top = 5.dp),
            fontFamily = OswaldFonts,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White)

    }

    Column {
        Row(
            Modifier
                .padding(top = 10.dp, end = 20.dp)
        ) {
            Text((if(chViewModel.userPrefsData.getUnits().value == 1) stringResource(R.string.alert_me_for_every_16_9Oz_of_sweat_loss) else stringResource(R.string.alert_me_for_every_500ml_of_sweat_loss)) + stringResource(R.string.recommended),
                Modifier
                    .padding(top = 5.dp, bottom = 10.dp, start = 20.dp)
                    .width(250.dp),
                fontFamily = RobotoFonts,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText)
            )

            Spacer(modifier = Modifier.weight(1f))

            Switch(
                checked = switchAlertMeState,
                onCheckedChange = { switchAlertMeState = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colorResource(R.color.waterFull),
                    checkedBorderColor = colorResource(R.color.waterFull),
                    uncheckedThumbColor = colorResource(R.color.switch_off_thumb),
                    uncheckedTrackColor = colorResource(R.color.switch_off_track),
                    uncheckedBorderColor = colorResource(R.color.switch_off_boarder),
                )
            )
        }

        Row(
            Modifier
                .padding(top = 10.dp, end = 20.dp)
        ) {
            Text(stringResource(R.string.include_passive_water_loss),
                Modifier
                    .padding(top = 5.dp, bottom = 10.dp, start = 20.dp)
                    .width(250.dp),
                fontFamily = RobotoFonts,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText)
            )

            Spacer(modifier = Modifier.weight(1f))

            Switch(
                checked = switchPassiveLossState.value,
                onCheckedChange = {
                    switchPassiveLossState.value = it
                    scopePassiveWaterLossState.launch {
                        chViewModel.updatePassiveWaterLossState(switchPassiveLossState.value)
                        ebsDeviceMonitor.setPassiveWaterLoss(switchPassiveLossState.value)
                        ebsDeviceMonitor.clearDuplicateHash()
                        ebsDeviceMonitor.clearHistoricalDataSet()
                    } },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colorResource(R.color.waterFull),
                    checkedBorderColor = colorResource(R.color.waterFull),
                    uncheckedThumbColor = colorResource(R.color.switch_off_thumb),
                    uncheckedTrackColor = colorResource(R.color.switch_off_track),
                    uncheckedBorderColor = colorResource(R.color.switch_off_boarder),
                )
            )
        }

        Column {
            Row(
                Modifier
                    .padding(top = 10.dp, end = 20.dp)
            ) {
                Text(
                    stringResource(R.string.enable_button_press_water_intake),
                    Modifier
                        .padding(top = 5.dp, bottom = 10.dp, start = 20.dp)
                        .width(250.dp),
                    fontFamily = RobotoFonts,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Left,
                    color = colorResource(R.color.settingsColorCoalText)
                )

                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = switchButtonWaterIntakeState.value,
                    onCheckedChange = {
                        switchButtonWaterIntakeState.value = it
                        scopePassiveWaterLossState.launch {
                            chViewModel.updateButtonPressWaterIntakeState(
                                switchButtonWaterIntakeState.value
                            )
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colorResource(R.color.waterFull),
                        checkedBorderColor = colorResource(R.color.waterFull),
                        uncheckedThumbColor = colorResource(R.color.switch_off_thumb),
                        uncheckedTrackColor = colorResource(R.color.switch_off_track),
                        uncheckedBorderColor = colorResource(R.color.switch_off_boarder),
                    )
                )
            }

            if (switchButtonWaterIntakeState.value) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val units = listOf(
                        if (chViewModel.currentUnits.value == 0) "500 ml" else "16.9 oz",
                        if (chViewModel.currentUnits.value == 0) "330 ml" else "11.2 oz",
                    )
                    SegmentedControl(
                        items = units,
                        fontSize = 14.sp,
                        defaultSelectedItemIndex = if (chViewModel.buttonPressWaterIntakeVolumeInMl.value == 500) 0 else 1,
                    ) {
                        if (it == 0) {
                            chViewModel.buttonPressWaterIntakeVolumeInMl.value = 500
                        }
                        else {
                            chViewModel.buttonPressWaterIntakeVolumeInMl.value = 330
                        }

                        chViewModel.updateButtonPressWaterIntakeValue(chViewModel.buttonPressWaterIntakeVolumeInMl.value)
                        ebsDeviceMonitor.setButtonPressWaterIntakeVolumeInMl()
                    }
                }
            }
        }

        Divider(
            Modifier.padding(start = 15.dp),
            color = Color.LightGray,
            thickness = 2.dp)

        Row(
            Modifier
                .padding(start = 15.dp, top = 10.dp, bottom = 15.dp)
                .background(Color.White)
                .clickable {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(SettingsSubScreens.SensorInformation.route!!)
                    }
                }) {
            Text(
                stringResource(R.string.settings_sensor_information),
                fontFamily = RobotoFonts,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorHydroDarkText))

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painterResource(R.drawable.baseline_chevron_right_24),
                modifier = Modifier.testTag("image_chevron_right"),
                contentDescription = "image_chevron_right",
                colorFilter = ColorFilter.tint(colorResource(R.color.settingsColorHydroDarkText)))
        }
    }

}

@Composable
fun LegalRegulatoryView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController, width: Dp) {
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"
    val agreements: MutableList<String> = mutableListOf(stringResource(R.string.settings_legal_regulatory),
        stringResource(R.string.settings_terms_conditions),
        stringResource(R.string.privacy_policy),
        "DEBUG")
    val context = LocalContext.current
    val usersEmail = chViewModel.usersEmailAddress.value
    val scopeLogOutUser  = rememberCoroutineScope()
    if (!usersEmail.contains("epicorebiosystems.com")) {
//    if (!usersEmail.contains("gmail.com")) {
        agreements.remove("DEBUG")
    }

    Box(
        modifier = Modifier
            .height(40.dp)
            .width(width)
            .background(Color.LightGray)
    )
    {
        Text(
            stringResource(R.string.settings_legal_regulatory),
            Modifier.padding(start = 10.dp, top = 5.dp),
            fontFamily = OswaldFonts,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    Column(
        Modifier.padding(start = 15.dp, top = 10.dp)
    ) {
        for (item in agreements) {
            Row(
                Modifier
                    .padding(top = 10.dp, bottom = 15.dp)
                    .background(Color.White)
                    .clickable {
                        when (item) {
                            agreements[0] -> {
                                navController.navigate(SettingsSubScreens.LegalRegulatory.route!!)
                            }

                            agreements[1] -> {
                                navController.navigate(SettingsSubScreens.TermsConditions.route!!)
                            }

                            agreements[2] -> {
                                navController.navigate(SettingsSubScreens.PrivacyPolicy.route!!)
                            }

                            "DEBUG" -> {
                                navController.navigate(SettingsSubScreens.Debug.route!!)
                            }
                        }
                    }) {
                Text(item,
                    fontFamily = RobotoFonts,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left,
                    color = colorResource(R.color.settingsColorHydroDarkText))

                Spacer(modifier = Modifier.weight(1f))

                Image(painterResource(R.drawable.baseline_chevron_right_24),
                    modifier = Modifier.testTag("image_chevron_right_1"),
                    contentDescription = "image_chevron_right_1",
                    colorFilter = ColorFilter.tint(colorResource(R.color.settingsColorHydroDarkText)))
            }

            Divider(color = Color.LightGray, thickness = 2.dp)
        }

        // logout button
        Row (
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                modifier = Modifier.width(if (isJapanese) 150.dp else 120.dp).testTag("button_mainsettingsview_logout"),
                onClick = trackClick(targetName = "SettingsScreen logout pressed") {
                    // Add networking logout code here
                    // And jump to onboarding screens
                    scopeLogOutUser.launch {
                        ebsDeviceMonitor.onEvent(DisconnectEvent)
                        ebsDeviceMonitor.disconnect()
                        ebsDeviceMonitor.stopScanningJob()
                        chViewModel.clearUserDataStore(false)
                        chViewModel.resetModelDataMutables()
                        ebsDeviceMonitor.clearHistoricalDataSet()

                        chViewModel.networkManager.logOutUser()
                        chViewModel.onboardingStep = 1
                        chViewModel.updateOnBoardingComplete(false)
                        chViewModel.updateDemoDemoMode(false)
                        ebsDeviceMonitor.scanBluetoothDevice()

                        // Restart the application in case user used email button for code
                        (context as Activity).finish()
                        val restartIntent = Intent(context, SplashActivity::class.java)
                        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(restartIntent)
                   }
                },
                border = BorderStroke(
                    1.dp,
                    colorResource(R.color.settingsColorHydroDarkText)
                ),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorResource(
                        R.color.settingsColorHydroDarkText
                    )
                )
            ) {
                Text(text = stringResource(R.string.logout),
                    modifier = Modifier.testTag("text_mainsettingsview_logout"),
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.settingsColorCoalText))
            }
        }
    }
}