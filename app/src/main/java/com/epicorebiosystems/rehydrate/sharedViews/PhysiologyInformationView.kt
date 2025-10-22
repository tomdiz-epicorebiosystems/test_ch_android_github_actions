package com.epicorebiosystems.rehydrate.sharedViews

import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chargemap.compose.numberpicker.ListItemPicker
import com.chargemap.compose.numberpicker.NumberPicker
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.SharedScreens
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnRunInput
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.toByteArray
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoMediumFonts
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PhysiologyInformationView(navController: NavController, chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor, showHeading: Boolean, onBoarding: Boolean, isEditing: Boolean, updateHideBottomBar: (Boolean) -> Unit) {
    val genderValues = listOf(stringResource(R.string.male), stringResource(R.string.female))
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"
    val scopeUpdateUser  = rememberCoroutineScope()
    var showNetworkProgress by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    var userHeightCm = remember { mutableStateOf(chViewModel.userHeightCm.value) }
    var userWeightLb = remember { mutableStateOf(chViewModel.userWeightLb.value) }

    BackHandler {
        navController.navigateUp()
        updateHideBottomBar(false)
    }

    if (showHeading) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .background(Color.LightGray)
        )
        {
            Row {
                Text(
                    stringResource(R.string.physiology_information),
                    Modifier.padding(start = 10.dp, top = 5.dp)
                        .testTag("text_physiologyinformationview_title"),
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    stringResource(R.string.edit),
                    Modifier
                        .padding(end = 10.dp, top = 5.dp)
                        .clickable {
                            updateHideBottomBar(true)
                            navController.navigate(SharedScreens.EditPhysiology.route!!)
                        }
                        .testTag("text_physiologyinformationview_edit"),
                    fontFamily = OswaldFonts,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    color = colorResource(R.color.settingsColorHydroDarkText)
                )
            }
        }
    }

    var viewHeight = 250.dp
    if (onBoarding) {
        viewHeight = 420.dp
    }
    else if (isEditing) {
        viewHeight = 560.dp
    }

    var viewHeightOffset = 0.dp
    if (!isEditing) {
        viewHeightOffset = if (chViewModel.currentUnits.value == 0) {
            -(55).dp
        } else {
            0.dp
        }
    }
    else {
        if (chViewModel.currentUnits.value == 0) {
            viewHeightOffset = -(20).dp
        }
    }

    Card {
        Column(
            Modifier
                .fillMaxWidth()
                .height(viewHeight)
                .background(if (!onBoarding) Color.White else colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier
                    .padding(
                        start = (if (isJapanese) (if (chViewModel.currentUnits.value == 0) 18.dp else (if (isEditing) 30.dp else 13.dp)) else (if (isEditing && (chViewModel.currentUnits.value == 1)) 30.dp else (if (!isEditing && chViewModel.currentUnits.value == 1) 15.dp else 18.dp))),
                        top = (if (isEditing && !onBoarding) 40.dp else 20.dp)
                    )
                    //.offset(x = if(!isEditing && chViewModel.currentUnits.value == 0) -(55).dp else -(5).dp),
                    .offset(x = viewHeightOffset),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.height),
                    modifier = Modifier.testTag("text_physiologyinformationview_height"),
                    fontFamily = RobotoFonts,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Left,
                    color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                )

                Spacer(modifier = Modifier.width(if (chViewModel.currentUnits.value == 0) (if (isJapanese) 16.dp else 18.dp) else 18.dp))

                if (isEditing) {
                    if (chViewModel.currentUnits.value == 0) {
                        TextField(
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .height(60.dp)
                                .width(100.dp)
                                .testTag("textfield_physiologyinformationview_heightcm"),
                            singleLine = true,
                            enabled = !showHeading,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontFamily = RobotoMediumFonts,
                                fontSize = 22.sp
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            shape = RoundedCornerShape(10.dp),
                            value = chViewModel.userHeightCm.value, //userHeightCm.value,
                            onValueChange = {
                                if (it.length <= 3) {

                                    userHeightCm.value = it

                                    chViewModel.userHeightCm.value = it

                                    chViewModel.onboardingHeightCm.value = it

                                    // Convert to imperial units as well
                                    chViewModel.userHeightFt.value = (((if (it == "") {
                                    //userHeightFt.value = (((if (it == "") {
                                        5.0
                                    } else {
                                        userHeightCm.value.toDouble()
                                    }) / 2.54) / 12.0).toInt().toString()
                                    chViewModel.userHeightIn.value = (((if (it == "") {
                                    //userHeightIn.value = (((if (it == "") {
                                        9.0
                                    } else {
                                        userHeightCm.value.toDouble()
                                    }) / 2.54) % 12.0).roundToInt().toString()

                                    //chViewModel.userHeightFt.value = userHeightFt.value
                                    //chViewModel.userHeightIn.value = userHeightIn.value
                                    chViewModel.onboardingHeightFt.value = chViewModel.userHeightFt.value//userHeightFt.value
                                    chViewModel.onboardingHeightIn.value = chViewModel.userHeightIn.value//userHeightIn.value
                                }
                            },
                            placeholder = {
                                Text(
                                    "175",
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(start = 20.dp)
                                )
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = colorResource(R.color.settingsColorCoalText),
                                disabledTextColor = if (!showHeading) Color.Transparent else colorResource(
                                    R.color.settingsColorCoalText
                                ),
                                backgroundColor = colorResource(R.color.enterManuallyLightBackground),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            chViewModel.userPrefsData.getUserHeightMinorUnitString(),
                            modifier = Modifier.testTag("text_physiologyinformationview_heightminor_metric"),
                            fontFamily = RobotoFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Left,
                            color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                        )
                    } else {

                        Spacer(modifier = Modifier.width(20.dp))
                        NumberPicker(
                            modifier = Modifier.testTag("picker_physiologyinformationview_heightft_" + chViewModel.userHeightFt),
                            dividersColor = Color.Gray,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontFamily = RobotoMediumFonts,
                                fontSize = 16.sp,
                                color = if (onBoarding) Color.White else colorResource(R.color.settingsColorCoalText)
                            ),
                            value = chViewModel.userHeightFt.value.toInt(),
                            range = 4..6,
                            onValueChange = {
                                //userHeightFt.value = it.toString()
                                chViewModel.userHeightFt.value =  it.toString()

                                chViewModel.onboardingHeightFt.value = it.toString()

                                // Convert to metric unit here as well
                                userHeightCm.value =
                                    ((chViewModel.userHeightIn.value.toInt() + it * 12).toDouble() * 2.54).roundToInt()
                                    //((userHeightIn.value.toInt() + it * 12).toDouble() * 2.54).roundToInt()
                                        .toString()

                                chViewModel.userHeightIn.value = chViewModel.userHeightIn.value//userHeightIn.value
                                chViewModel.userHeightCm.value = userHeightCm.value
                                chViewModel.onboardingHeightCm.value = userHeightCm.value
                            })

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            chViewModel.userPrefsData.getUserHeightMajorUnitString(),
                            modifier = Modifier.testTag("text_physiologyinformationview_heightmajor_imperial"),
                            fontFamily = RobotoFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Left,
                            color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        // Part of modifier -> .border(color = colorResource(R.color.settingsColorHydroDarkText), width = 1.dp, shape = RoundedCornerShape(30)),

                        NumberPicker(
                            modifier = Modifier.testTag("picker_physiologyinformationview_heightin_" + chViewModel.userHeightIn),
                            dividersColor = Color.Gray,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontFamily = RobotoMediumFonts,
                                fontSize = 16.sp,
                                color = if (onBoarding) Color.White else colorResource(R.color.settingsColorCoalText)
                            ),
                            value = chViewModel.userHeightIn.value.toInt(),//userHeightIn.value.toInt(),
                            range = 0..11,
                            onValueChange = {
                                //userHeightIn.value = it.toString()
                                chViewModel.userHeightIn.value = it.toString()

                                chViewModel.onboardingHeightIn.value = it.toString()

                                // Convert to metric unit here as well
                                userHeightCm.value =
                                    ((chViewModel.userHeightFt.value.toInt() * 12 + it).toDouble() * 2.54).roundToInt()
                                    //((userHeightFt.value.toInt() * 12 + it).toDouble() * 2.54).roundToInt()
                                        .toString()

                                chViewModel.userHeightFt.value = chViewModel.userHeightFt.value//userHeightFt.value
                                chViewModel.userHeightCm.value = userHeightCm.value
                                chViewModel.onboardingHeightCm.value = userHeightCm.value
                            })

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            chViewModel.userPrefsData.getUserHeightMinorUnitString(),
                            modifier = Modifier.testTag("text_physiologyinformationview_minor_imperial"),
                            fontFamily = RobotoFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Left,
                            color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chViewModel.currentUnits.value == 1) {
                            TextField(
                                modifier = Modifier
                                    .padding(start = 20.dp)
                                    .height(60.dp)
                                    .width(80.dp)
                                    .testTag("textfield_physiologyinformationview_heightft"),
                                singleLine = true,
                                enabled = false,
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    fontFamily = RobotoMediumFonts,
                                    fontSize = 16.sp
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword
                                ),
                                shape = RoundedCornerShape(10.dp),
                                value = chViewModel.userHeightFt.value,//userHeightFt.value,
                                onValueChange = {},
                                placeholder = {},
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = colorResource(R.color.settingsColorCoalText),
                                    disabledTextColor = if (!showHeading) Color.Transparent else colorResource(
                                        R.color.settingsColorCoalText
                                    ),
                                    backgroundColor = colorResource(R.color.enterManuallyLightBackground),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                )
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                chViewModel.userPrefsData.getUserHeightMajorUnitString(),
                                modifier = Modifier.testTag("text_physiologyinformationview_major_imperial"),
                                fontFamily = RobotoFonts,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Left,
                                color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                            )

                            Spacer(modifier = Modifier.width(0.dp))

                            TextField(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .height(60.dp)
                                    .width(80.dp)
                                    .testTag("textfield_physiologyinformationview_heightin"),
                                singleLine = true,
                                enabled = false,
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    fontFamily = RobotoMediumFonts,
                                    fontSize = 16.sp
                                ),
                                keyboardOptions = KeyboardOptions(
                                ),
                                shape = RoundedCornerShape(10.dp),
                                value = chViewModel.userHeightIn.value,
                                onValueChange = {},
                                placeholder = {
                                },
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = colorResource(R.color.settingsColorCoalText),
                                    disabledTextColor = if (!showHeading) Color.Transparent else colorResource(
                                        R.color.settingsColorCoalText
                                    ),
                                    backgroundColor = colorResource(R.color.enterManuallyLightBackground),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                )
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                chViewModel.userPrefsData.getUserHeightMinorUnitString(),
                                modifier = Modifier.testTag("text_physiologyinformationview_minor_imperial"),
                                fontFamily = RobotoFonts,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Left,
                                color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                            )

                        } else {
                            TextField(
                                modifier = Modifier
                                    .padding(start = 20.dp)
                                    .height(60.dp)
                                    .width(80.dp)
                                    .testTag("textfield_physiologyinformationview_heightcm"),
                                singleLine = true,
                                enabled = false,
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    fontFamily = RobotoMediumFonts,
                                    fontSize = 16.sp
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword
                                ),
                                shape = RoundedCornerShape(10.dp),
                                value = chViewModel.userHeightCm.value,
                                onValueChange = {},
                                placeholder = {},
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = colorResource(R.color.settingsColorCoalText),
                                    disabledTextColor = if (!showHeading) Color.Transparent else colorResource(
                                        R.color.settingsColorCoalText
                                    ),
                                    backgroundColor = colorResource(R.color.enterManuallyLightBackground),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                )
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                chViewModel.userPrefsData.getUserHeightMinorUnitString(),
                                modifier = Modifier.testTag("text_physiologyinformationview_minor_imperial_2"),
                                fontFamily = RobotoFonts,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Left,
                                color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                            )
                        }
                    }

                }
            }   // Row -height

            //Spacer(modifier = Modifier.height(if (onBoarding || isEditing) 10.dp else 0.dp))

            Row(
                Modifier
                    .padding(top = if (isEditing && (chViewModel.currentUnits.value == 1)) 0.dp else 10.dp)
                    .offset(x = if (isEditing) -(15).dp else -(50).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.weight),
                    modifier = Modifier.testTag("text_physiologyinformationview_weight"),
                    fontFamily = RobotoFonts,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Left,
                    color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                )

                Spacer(modifier = Modifier.width(37.dp))

                if (chViewModel.currentUnits.value == 0) {
                    TextField(
                        modifier = Modifier
                            .height(60.dp)
                            .width(if (isEditing) 100.dp else 80.dp)
                            .testTag("textfield_physiologyinformationview_weightkg"),
                        singleLine = true,
                        enabled = !showHeading,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontFamily = RobotoMediumFonts,
                            fontSize = (if (isEditing) 22.sp else 16.sp)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        shape = RoundedCornerShape(10.dp),
                        value = chViewModel.userWeightKg.value,//userWeightKg.value,
                        onValueChange = {
                            if (it.length <= 3) {
                                //userWeightKg.value = it
                                chViewModel.userWeightKg.value = it

                                chViewModel.onboardingWeightKg.value = chViewModel.userWeightKg.value//userWeightKg.value

                                // Convert to imperial unit as well
                                userWeightLb.value = ((if ((it == "") || (it == "0")) {
                                    165.0
                                } else {
                                    chViewModel.userWeightKg.value.toDouble()//userWeightKg.value.toDouble()
                                }) / 0.453592).roundToInt().toString()
                            }

                            //chViewModel.userWeightKg.value = userWeightKg.value
                            chViewModel.userWeightLb.value = userWeightLb.value
                            chViewModel.onboardingWeightLb.value = userWeightLb.value
                        },
                        placeholder = {
                            Text(
                                "75",
                                color = Color.LightGray,
                                modifier = Modifier.padding(start = 20.dp)
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = colorResource(R.color.settingsColorCoalText),
                            disabledTextColor = if (!showHeading) Color.Transparent else colorResource(
                                R.color.settingsColorCoalText
                            ),
                            backgroundColor = colorResource(R.color.enterManuallyLightBackground),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                } else {
                    TextField(
                        modifier = Modifier
                            .height(60.dp)
                            .width(if (isEditing) 100.dp else 80.dp)
                            .testTag("textfield_physiologyinformationview_weightlb"),
                        singleLine = true,
                        enabled = !showHeading,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontFamily = RobotoMediumFonts,
                            fontSize = (if (isEditing) 22.sp else 16.sp)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        shape = RoundedCornerShape(10.dp),
                        value = userWeightLb.value,
                        onValueChange = {
                            if (it.length <= 3) {

                                userWeightLb.value = it

                                chViewModel.onboardingWeightLb.value = userWeightLb.value

                                // Convert to metric unit as well
                                chViewModel.userWeightKg.value = ((if ((it == "") || (it == "0")) {
                                //userWeightKg.value = ((if (it == "") {
                                    75.0
                                } else {
                                    userWeightLb.value.toDouble()
                                }) * 0.453592).roundToInt().toString()
                            }

                            chViewModel.userWeightLb.value = userWeightLb.value
                            chViewModel.userWeightKg.value = chViewModel.userWeightKg.value//userWeightKg.value
                            chViewModel.onboardingWeightKg.value = chViewModel.userWeightKg.value//userWeightKg.value
                        },
                        placeholder = {
                            Text(
                                "165",
                                color = Color.LightGray,
                                modifier = Modifier.padding(start = 20.dp)
                            )
                        },

                        colors = TextFieldDefaults.textFieldColors(
                            textColor = colorResource(R.color.settingsColorCoalText),
                            disabledTextColor = if (!showHeading) Color.Transparent else colorResource(
                                R.color.settingsColorCoalText
                            ),
                            backgroundColor = colorResource(R.color.enterManuallyLightBackground),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.width(if (isEditing) 20.dp else 10.dp))

                Text(
                    chViewModel.userPrefsData.getUserWeightString(),
                    modifier = Modifier.testTag("text_physiologyinformationview_weight"),
                    fontFamily = RobotoFonts,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Left,
                    color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                )

            }   // Row - weight

            Row(
                Modifier
                    .padding(start = 20.dp, top = if (isEditing) 0.dp else 10.dp)
                    .offset(x = if (isEditing) if (isJapanese) -(45).dp else -(40).dp else if (isJapanese) -(60).dp else -(55).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    stringResource(R.string.sex),
                    modifier = Modifier.testTag("text_physiologyinformationview_sex"),
                    fontFamily = RobotoFonts,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Left,
                    color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                )

                Spacer(modifier = Modifier.width(if (isEditing) if (isJapanese) 50.dp else 60.dp else if (isJapanese) 35.dp else 43.dp))

                Column {
                    if (!isEditing) {
                        TextField(
                            modifier = Modifier
                                .height(60.dp)
                                .width(100.dp)
                                .testTag("textfield_physiologyinformationview_gender"),
                            singleLine = true,
                            enabled = false,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontFamily = RobotoMediumFonts,
                                fontSize = 16.sp
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            shape = RoundedCornerShape(10.dp),
                            value = if (chViewModel.userGender.value == "Male") stringResource(R.string.male) else stringResource(R.string.female),
                            onValueChange = {},
                            placeholder = {},
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = colorResource(R.color.settingsColorCoalText),
                                disabledTextColor = if (!showHeading) Color.Transparent else colorResource(
                                    R.color.settingsColorCoalText
                                ),
                                backgroundColor = colorResource(R.color.enterManuallyLightBackground),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        ListItemPicker(
                            modifier = Modifier.testTag("picker_physiologyinformationview_gender_" + chViewModel.userGender),
                            dividersColor = Color.Gray,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontFamily = RobotoMediumFonts,
                                fontSize = 16.sp,
                                color = if (onBoarding) Color.White else colorResource(R.color.settingsColorCoalText)
                            ),
                            value = if (chViewModel.userGender.value == "Male") {//if (userGender.value == "Male") {
                                genderValues[0]
                            } else {
                                genderValues[1]
                            },
                            list = genderValues,
                            onValueChange = {
                                chViewModel.userGender.value = if (it == chViewModel.applicationContext!!.getString(R.string.male))  "Male" else "Female"
                                //userGender.value = if (it == chViewModel.applicationContext!!.getString(R.string.male))  "Male" else "Female"
                                chViewModel.onboardingGender.value = chViewModel.userGender.value
                            })
                    }

                    Text(
                        stringResource(R.string.assigned_at_birth),
                        modifier = Modifier.padding(start = 5.dp).testTag("text_physiologyinformationview_birth"),
                        fontFamily = RobotoFonts,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left,
                        color = if (!onBoarding) colorResource(R.color.settingsColorCoalText) else Color.White
                    )
                }

            }   // Row - sex

            if (!showHeading) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val units = listOf(stringResource(R.string.metric),
                        stringResource(R.string.imperial))
                    SegmentedControl(
                        items = units,
                        fontSize = if (isJapanese) 8.sp else 12.sp,
                        defaultSelectedItemIndex = chViewModel.currentUnits.value,
                    ) {
                        Log.d("MetricToggle", "Selected item : ${units[it]}")
                        chViewModel.updateUnits(it)
                    }
                }
            }

            if (isEditing && !onBoarding) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        modifier = Modifier.width(140.dp).testTag("button_physiologyinformationview_ok"),
                        onClick = {
                            if (chViewModel.oldUserHeightFt != chViewModel.userHeightFt.value || chViewModel.oldUserHeightIn != chViewModel.userHeightIn.value ||
                                chViewModel.oldUserHeightCm != chViewModel.userHeightCm.value || chViewModel.oldUserWeightLb != chViewModel.userWeightLb.value ||
                                chViewModel.oldUserWeightKg != chViewModel.userWeightKg.value || chViewModel.oldUserGender != chViewModel.userGender.value
                            ) {

                                val builder = AlertDialog.Builder(context)

                                if ((chViewModel.userHeightCm.value.toInt() > 212) || (chViewModel.userHeightCm.value.toInt() < 125) ||
                                    (chViewModel.userWeightKg.value.toInt() > 300) || (chViewModel.userWeightKg.value.toInt() < 23)) {

                                    builder.setTitle(chViewModel.applicationContext!!.getString(R.string.warning))
                                    builder.setMessage(chViewModel.applicationContext!!.getString(R.string.out_of_range_physiology_input))
                                    builder.setNegativeButton(chViewModel.applicationContext!!.getString(
                                        R.string.hydration_ok
                                    ),
                                        DialogInterface.OnClickListener { _, _ ->
                                            chViewModel.userHeightFt.value =
                                                chViewModel.oldUserHeightFt
                                            chViewModel.userHeightIn.value =
                                                chViewModel.oldUserHeightIn
                                            chViewModel.userHeightCm.value =
                                                chViewModel.oldUserHeightCm
                                            chViewModel.userWeightLb.value =
                                                chViewModel.oldUserWeightLb
                                            chViewModel.userWeightKg.value =
                                                chViewModel.oldUserWeightKg
                                            chViewModel.userGender.value = chViewModel.oldUserGender

//                                            updateHideBottomBar(false)
//                                            navController.navigateUp()
                                        })
                                }

                                else {
                                    builder.setTitle(chViewModel.applicationContext!!.getString(R.string.confirm_alert))
                                    builder.setMessage(chViewModel.applicationContext!!.getString(R.string.are_you_sure))
                                    builder.setNegativeButton(chViewModel.applicationContext!!.getString(
                                        R.string.cancel_alert
                                    ),
                                        DialogInterface.OnClickListener { _, _ ->
                                            chViewModel.userHeightFt.value =
                                                chViewModel.oldUserHeightFt
                                            chViewModel.userHeightIn.value =
                                                chViewModel.oldUserHeightIn
                                            chViewModel.userHeightCm.value =
                                                chViewModel.oldUserHeightCm
                                            chViewModel.userWeightLb.value =
                                                chViewModel.oldUserWeightLb
                                            chViewModel.userWeightKg.value =
                                                chViewModel.oldUserWeightKg
                                            chViewModel.userGender.value = chViewModel.oldUserGender

                                            updateHideBottomBar(false)
                                            navController.navigateUp()
                                        })
                                    builder.setPositiveButton(chViewModel.applicationContext!!.getString(
                                        R.string.hydration_ok
                                    ),
                                        DialogInterface.OnClickListener { _, _ ->
                                            // Update the stored user info
                                            //chViewModel.userHeightFt.value = userHeightFt.value
                                            //chViewModel.userHeightIn.value = userHeightIn.value
                                            //chViewModel.userHeightCm.value = userHeightCm.value
                                            //chViewModel.userWeightKg.value = userWeightKg.value
                                            //chViewModel.userWeightLb.value = userWeightLb.value
                                            //chViewModel.userGender.value = userGender.value

                                            chViewModel.savePhysiologyChangedValues(
                                                chViewModel.userWeightLb.value,
                                                chViewModel.userWeightKg.value,
                                                chViewModel.userHeightFt.value,
                                                chViewModel.userHeightIn.value,
                                                chViewModel.userHeightCm.value,
                                                chViewModel.userGender.value
                                            )

                                            scopeUpdateUser.launch {
                                                showNetworkProgress = true

                                                // Update user information here
                                                val paddedSize = 16
                                                val paddedHexZeros =
                                                    ByteArray(paddedSize) { 0xFF.toByte() }   // Create the padded array of trailing 0x00's
                                                val userHeightInCms: ByteArray = byteArrayOf(
                                                    (if ((chViewModel.userHeightCm.value == "") || (chViewModel.userHeightCm.value == "0")) {
                                                        "175"
                                                    } else {
                                                        chViewModel.userHeightCm.value
                                                    }).toInt().toByte()
                                                )

                                                val userWeightInKg: ByteArray =
                                                    (if ((chViewModel.userWeightKg.value == "") || (chViewModel.userWeightKg.value == "0")) {
                                                        "75"
                                                    } else {
                                                        chViewModel.userWeightKg.value
                                                    }).toUShort().toByteArray()

                                                val userGender: ByteArray = byteArrayOf(
                                                    if (chViewModel.userGender.value == "Male") {
                                                        0x00
                                                    } else {
                                                        0x01
                                                    }
                                                )

                                                val userAge: ByteArray = byteArrayOf(0)

                                                val userClothTypeCode: ByteArray = byteArrayOf(0)

                                                val setUserInfoCommand: ByteArray =
                                                    byteArrayOf(0x55).plus(paddedHexZeros)
                                                        .plus(userGender).plus(userHeightInCms)
                                                        .plus(userWeightInKg).plus(userAge)
                                                        .plus(userClothTypeCode)

                                                ebsMonitor.onEvent(OnRunInput(setUserInfoCommand))

                                                val userInfo: Map<String, Any> = mapOf(
                                                    "height" to chViewModel.userHeightCm.value,
                                                    "weight" to chViewModel.userWeightKg.value,
                                                    "biologicalSex" to if (chViewModel.userGender.value == "Male") "male" else "female"
                                                )

                                                ebsMonitor.setUserInfoForCSVFile(
                                                    chViewModel.userGender.value,
                                                    chViewModel.userHeightCm.value.toInt(),
                                                    chViewModel.userWeightKg.value.toInt()
                                                )

                                                chViewModel.networkManager.updateUser(
                                                    enterpriseId = chViewModel.jwtEnterpriseID.value,
                                                    siteId = chViewModel.jwtSiteID.value,
                                                    userInfo = userInfo
                                                )

                                                showNetworkProgress = false
                                                updateHideBottomBar(false)
                                                navController.navigateUp()
                                            }

                                            //chViewModel._updateUserInfoFromDevice.value = true
                                        })

                                }

                                val alertDialog = builder.show()
                                alertDialog.setCanceledOnTouchOutside(false)
                                alertDialog.setCancelable(false)
                            } else {
                                updateHideBottomBar(false)
                                navController.navigateUp()
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
                        Text(
                            modifier = Modifier.testTag("text_physiologyinformationview_ok"),
                            text = stringResource(R.string.edit_enterprise_ok),
                            fontFamily = OswaldFonts,
                            fontSize = if (isJapanese) 14.sp else 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(R.color.settingsColorCoalText)
                        )
                    }

                    Spacer(modifier = Modifier.width(50.dp))

                    OutlinedButton(
                        modifier = Modifier.width(140.dp).testTag("button_physiologyinformationview_cancel"),
                        onClick = {
                            chViewModel.userHeightFt.value = chViewModel.oldUserHeightFt
                            chViewModel.userHeightIn.value = chViewModel.oldUserHeightIn
                            chViewModel.userHeightCm.value = chViewModel.oldUserHeightCm
                            chViewModel.userWeightLb.value = chViewModel.oldUserWeightLb
                            chViewModel.userWeightKg.value = chViewModel.oldUserWeightKg
                            chViewModel.userGender.value = chViewModel.oldUserGender

                            updateHideBottomBar(false)
                            navController.navigateUp()
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
                        Text(
                            modifier = Modifier.testTag("text_physiologyinformationview_cancel"),
                            text = stringResource(R.string.edit_enterprise_cancel),
                            fontFamily = OswaldFonts,
                            fontSize = if (isJapanese) 14.sp else 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(R.color.settingsColorCoalText)
                        )

                    }
                }
            }

        }   // Column

        if (showNetworkProgress) {
            FullScreenProgressView(R.string.updating_user_info, true)
        }

    }
}