package com.epicorebiosystems.rehydrate.tabViews.IntakeViews

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.IntakeButtonState
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.BottleData
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.sharedViews.convertImageNameToResourceId
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoMediumFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterBottleManuallyScreen(chViewModel: ModelData, navController: NavController, updateIntakeState: (IntakeButtonState) -> Unit) {
    val waterAmountEnterManual = remember { mutableStateOf("") }
    val sodiumAmountEnterManual = remember { mutableStateOf("") }
    val manualUserBottleName = remember { mutableStateOf("") }
    val previewBottleScrollState = rememberScrollState()
    var previewSelectedIconState by rememberSaveable { mutableStateOf(1000) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.enter_menu_item),
                        fontFamily = OswaldFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.settingsColorCoalText)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = trackClick(targetName = "EnterBottleManuallyScreen back pressed") {
                            updateIntakeState(IntakeButtonState.INTAKE_CANCEL)
                            navController.navigateUp()
                        }
                    ) {
                        Image(
                            painterResource(R.drawable.baseline_chevron_left_24),
                            modifier = Modifier.testTag("image_back"),
                            contentDescription = "image_back",
                            colorFilter = ColorFilter.tint(colorResource(R.color.settingsColorCoalText))
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = trackClick(targetName = "EnterBottleManuallyScreen cancel pressed") {
                            updateIntakeState(IntakeButtonState.INTAKE_CANCEL)
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(end = 20.dp),
                            imageVector = Icons.Filled.Close,
                            contentDescription = "add item close")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .height(760.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoxWithConstraints {
                val widthModifier = maxWidth - 20.dp

                LaunchedEffect(updateIntakeState) {
                    // change intake button to cancel
                    updateIntakeState(IntakeButtonState.INTAKE_ADD)
                }

                Box(
                    Modifier
                        .width(widthModifier)
                        .offset(x = 10.dp, y = 50.dp)
                        .background(Color.White)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Row {
                            Column {
                                Text(
                                    stringResource(R.string.water_content),
                                    Modifier.padding(start = 10.dp, top = 10.dp, bottom = 20.dp),
                                    fontFamily = OswaldFonts,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.settingsColorCoalText))

                                Row {
                                    TextField(
                                        modifier = Modifier
                                            .padding(start = 10.dp)
                                            .height(55.dp)
                                            .width(100.dp),
                                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, fontFamily = RobotoMediumFonts, fontSize = 18.sp),                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        value = waterAmountEnterManual.value,
                                        onValueChange = {
                                            if(it.length <= 4) waterAmountEnterManual.value = it },
                                        placeholder = {
                                            Text(
                                                "0",
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Right,
                                                color = Color.LightGray
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = TextFieldDefaults.textFieldColors(
                                            textColor = colorResource(R.color.settingsColorCoalText),
                                            disabledTextColor = Color.Transparent,
                                            backgroundColor = colorResource(R.color.enterManuallyLightBackground),
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent
                                        )
                                    )

                                    Text(chViewModel.userPrefsData.getUserSweatUnitString(),
                                        Modifier.padding(start = 10.dp, top = 10.dp, bottom = 20.dp),
                                        fontFamily = OswaldFonts,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.settingsColorCoalText))

                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Column(
                                Modifier.padding(end = 20.dp)
                            ) {
                                Text(
                                    stringResource(R.string.sodium_content),
                                    Modifier.padding(start = 10.dp, top = 10.dp, bottom = 20.dp),
                                    fontFamily = OswaldFonts,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.settingsColorCoalText))

                                Row {
                                    TextField(
                                        modifier = Modifier
                                            .padding(start = 10.dp)
                                            .height(55.dp)
                                            .width(100.dp),
                                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, fontFamily = RobotoMediumFonts, fontSize = 18.sp),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        value = sodiumAmountEnterManual.value,
                                        onValueChange = {
                                            if (it.length <= 4) sodiumAmountEnterManual.value = it
                                                        },
                                        placeholder = {
                                            Text(
                                                "0",
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Right,
                                                color = Color.LightGray,
                                                )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        colors = TextFieldDefaults.textFieldColors(
                                            textColor = colorResource(R.color.settingsColorCoalText),
                                            disabledTextColor = Color.Transparent,
                                            backgroundColor = colorResource(R.color.enterManuallyLightBackground),
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent
                                        )
                                    )

                                    Text("mg",
                                        Modifier.padding(start = 10.dp, top = 10.dp, bottom = 20.dp),
                                        fontFamily = OswaldFonts,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.settingsColorCoalText))

                                }
                            }
                        }

                        Row {
                            Text(
                                stringResource(R.string.name_optional),
                                Modifier.padding(start = 10.dp, top = 10.dp, bottom = 20.dp),
                                fontFamily = OswaldFonts,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.settingsColorCoalText))

                            Spacer(modifier = Modifier.weight(1f))
                        }

                        TextField(
                            modifier = Modifier
                                .padding(start = 10.dp, end = 10.dp)
                                .height(55.dp)
                                .width(widthModifier),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Left, fontFamily = RobotoMediumFonts, fontSize = 18.sp),
                            value = manualUserBottleName.value,
                            singleLine = true,
                            onValueChange = {
                                if (it.length <= 25) manualUserBottleName.value = it },
                            placeholder = {
                                Text(stringResource(R.string.e_g_spring_water),
                                color = Color.LightGray) },
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = colorResource(R.color.settingsColorCoalText),
                                disabledTextColor = Color.Transparent,
                                backgroundColor = colorResource(R.color.enterManuallyLightBackground),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )

                        Text(
                            stringResource(R.string.preview),
                            Modifier.padding(start = 10.dp, top = 10.dp, bottom = 20.dp),
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.settingsColorCoalText))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement  =  Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Top
                        ) {
                            PreviewBottleEmptyView()

                            SingleBottleView(chViewModel = chViewModel, name = manualUserBottleName.value, waterAmount = waterAmountEnterManual, sodiumAmount = sodiumAmountEnterManual,
                                imageName = chViewModel.bottlePreviewIcons[previewSelectedIconState - 1000].image_name)

                            PreviewBottleEmptyView()
                        }

                        Row {
                            Text(
                                stringResource(R.string.select_an_icon),
                                Modifier.padding(start = 10.dp, top = 10.dp, bottom = 20.dp),
                                fontFamily = OswaldFonts,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.settingsColorCoalText))

                            Spacer(modifier = Modifier.weight(1f))
                        }

                        // User preview bottle list view
                        Column(
                            modifier = Modifier.scrollable(
                                state = previewBottleScrollState,
                                orientation = Orientation.Horizontal
                            )
                        ) {
                            //BoxWithConstraints {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    items(chViewModel.bottlePreviewIcons) { bottle ->
                                        PreviewBottleView(bottle, previewSelectedIconState, updateSelectedPreviewIcon = { newSelectedPreviewIcon ->
                                            previewSelectedIconState = newSelectedPreviewIcon
                                        })
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                }
                            //}
                        }

                    }   // Column
                }
            }
        }
    }
}


@Composable
fun PreviewBottleEmptyView() {
    Column {
        Box(
            modifier = Modifier
                .height(100.dp)
                .width(100.dp)
                .background(
                    colorResource(R.color.enterManuallyLightBackground),
                    RoundedCornerShape(10.dp))
        )

        Text("")
    }
}

@Composable
fun PreviewBottleView(bottle: BottleData, selectedPreviewIcon: Int, updateSelectedPreviewIcon: (Int) -> Unit) {
    var selectColor = Color.Transparent
    if (selectedPreviewIcon == bottle.id) {
        selectColor = colorResource(R.color.insightMediumColor)
    }
    Box(modifier = Modifier
        .height(100.dp)
        .width(100.dp)
        .background(Color.LightGray, RoundedCornerShape(10.dp))
        .border(width = 4.dp, color = selectColor, shape = RoundedCornerShape(10.dp))
        .clickable {
            updateSelectedPreviewIcon(bottle.id)
        }
    ) {
        Image(
            painterResource(id = convertImageNameToResourceId(bottle.image_name)),
            modifier = Modifier.align(Alignment.Center).testTag("image_bottle_preview_icon"),
            contentDescription = "image_bottle_preview_icon",
        )

        Text("")
    }
}

@Composable
fun SingleBottleView(chViewModel: ModelData, name: String, waterAmount: MutableState<String>, sodiumAmount: MutableState<String>, imageName: String) {
    var color: Int

    chViewModel.sodiumAmountEnterManual = sodiumAmount.value
    chViewModel.waterAmountEnterManual = waterAmount.value
    chViewModel.manualUserBottle.name = name
    chViewModel.manualUserBottle.image_name = imageName

    var sodiumAmountValue = 0
    if (sodiumAmount.value != "") {
        sodiumAmountValue = sodiumAmount.value.toInt()
    }
    var waterAmountValue = 0.0F
    if (waterAmount.value != "") {
        waterAmountValue = if(waterAmount.value.toFloatOrNull() == null) 0.0F else waterAmount.value.toFloat()
    }

    if (sodiumAmountValue <= 0 && waterAmountValue > 0) {
        color = R.color.waterFull
    }
    else if (sodiumAmountValue > 0 && waterAmountValue <= 0) {
        color = R.color.sodiumFull
    }
    color = if (sodiumAmountValue > waterAmountValue) {
        if (sodiumAmountValue > 500) {
            R.color.sodiumHalf
        }
        else {
            R.color.sodiumQuarter
        }
    }
    else {
        if (waterAmountValue > 500) {
            R.color.waterHalf
        } else {
            R.color.waterQuarter
        }
    }

    Column {
        Box(
            modifier = Modifier
                .height(100.dp)
                .width(100.dp)
                .background(colorResource(color), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        )
        {
            Image(
                painterResource(id = convertImageNameToResourceId(imageName)),
                modifier = Modifier.testTag("image_bottle_bottle_icon"),
                contentDescription = "image_bottle_bottle_icon",
                alpha = 0.4F
            )

            Column {
                if (sodiumAmountValue > 0.0) {
                    Text("$sodiumAmountValue mg",
                        fontFamily = RobotoRegularFonts,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                else {
                    Text("",
                        fontFamily = RobotoRegularFonts,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (waterAmountValue > 0.0) {
                    Text(String.format("%.1f", (waterAmountValue).toDouble()) + " " + chViewModel.userPrefsData.getUserSweatUnitString(),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                else {
                    Text("",
                        fontFamily = RobotoRegularFonts,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

            }   // Column - Amounts text

        }

        Text(name,
            modifier = Modifier.width(100.dp),
            maxLines = 2,
            textAlign = TextAlign.Center)

    }
}