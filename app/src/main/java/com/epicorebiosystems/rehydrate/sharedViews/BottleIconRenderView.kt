package com.epicorebiosystems.rehydrate.sharedViews

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.BottleData
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.deleteUserBottleMenuItem
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import kotlinx.coroutines.delay
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottleIconRenderView(chViewModel: ModelData, bottle: BottleData, showName: Boolean, isIntake: Boolean, isClickable: Boolean) {
    var color: Int
    val isGlowing = remember { mutableStateOf(false) }
    var showBottleDropDownPopover by rememberSaveable { mutableStateOf(false) }

    if (chViewModel.newBottlesItemsAdded.size > 0 && !isIntake && isClickable) {
        chViewModel.newBottlesItemsAdded.forEach { newBottle ->
            if (newBottle == bottle.id) {
                LaunchedEffect(Unit) {
                    isGlowing.value = true
                    while (isGlowing.value) {
                        delay(5000)
                        isGlowing.value = false
                    }
                }
            }
        }
    }

    if (showBottleDropDownPopover) {
        LaunchedEffect(Unit) {
            while (showBottleDropDownPopover) {
                delay(5000)
                showBottleDropDownPopover = false
            }
        }
    }

    if (bottle.sodiumAmount <= 0 && bottle.waterAmount > 0) {
        color = R.color.waterFull
    }
    else if (bottle.sodiumAmount > 0 && bottle.waterAmount <= 0) {
        color = R.color.sodiumFull
    }
    color = if (bottle.sodiumAmount > bottle.waterAmount) {
        if (bottle.sodiumAmount > 500) {
            R.color.sodiumHalf
        } else {
            R.color.sodiumQuarter
        }
    }
    else {
        if (bottle.waterAmount > 500) {
            R.color.waterHalf
        } else {
            R.color.waterQuarter
        }
    }

    var width = 100.dp
    var height = 100.dp
    if (isIntake) {
        width = 90.dp
    }

    val intakeBottleUpdated by chViewModel.updateCurrentUserIntakeItems.collectAsState()

    Column {
        GlowingCard(
            glowingColor = if (isGlowing.value) colorResource(R.color.waterFull) else if (showBottleDropDownPopover) Color.Black else Color.Transparent,
            cardRadius = 7.dp,
        ) {
            Box(
                Modifier
                    .height(height)
                    .width(width)
                    .padding(if (isGlowing.value) 0.dp else 5.dp)
                    .background(colorResource(color), RoundedCornerShape(7.dp))
                    .combinedClickable(
                        onClick = {
                            if (isClickable) {
                                if (isIntake) {
                                    val waterAmount = bottle.waterAmount
                                    val sodiumAmount = bottle.sodiumAmount
                                    chViewModel.removeIntakeBottle(bottle = bottle)
                                    chViewModel.totalWaterAmount -= waterAmount.toDouble()
                                    if (chViewModel.totalWaterAmount < 0) {
                                        chViewModel.totalWaterAmount = 0.0
                                    }
                                    chViewModel.totalSodiumAmount -= sodiumAmount.toDouble()
                                    if (chViewModel.totalSodiumAmount < 0) {
                                        chViewModel.totalSodiumAmount = 0.0
                                    }
                                } else {
                                    chViewModel.addIntakeBottle(bottle)
                                    chViewModel.totalWaterAmount += bottle.waterAmount.toDouble()
                                    chViewModel.totalSodiumAmount += bottle.sodiumAmount.toDouble()
                                }
                            }
                        },
                        onLongClick = {
                            if (isClickable) {
                                showBottleDropDownPopover = true
                                Log.d("LONG_PRESS", "LONG_PRESS")
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painterResource(id = convertImageNameToResourceId(bottle.image_name)),
                    contentDescription = "bottle icon",
                    alpha = 0.4F
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (bottle.sodiumAmount > 0.0) {
                        if (isIntake) {
                            Text(
                                String.format("%.1f", bottle.sodiumAmount) + " mg",
                                fontFamily = RobotoRegularFonts,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                String.format("%.1f", bottle.sodiumAmount) + " mg",
                                fontFamily = RobotoRegularFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        if (isIntake) {
                            Text(
                                "",
                                fontFamily = RobotoRegularFonts,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                "",
                                fontFamily = RobotoRegularFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (bottle.waterAmount > 0.0) {
                        if (isIntake) {
                            Text(
                                String.format(
                                    "%.1f",
                                    chViewModel.userPrefsData.handleUserSweatConversionMl(ml = (bottle.waterAmount).toDouble())
                                ) + " " + chViewModel.userPrefsData.getUserSweatUnitString(),
                                fontFamily = RobotoRegularFonts,
                                fontSize = if(bottle.waterAmount >= 1000.0)  15.sp else 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                String.format(
                                    "%.1f",
                                    chViewModel.userPrefsData.handleUserSweatConversionMl(ml = (bottle.waterAmount).toDouble())
                                ) + " " + chViewModel.userPrefsData.getUserSweatUnitString(),
                                fontFamily = RobotoRegularFonts,
                                fontSize = if(bottle.waterAmount >= 1000.0) 18.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        if (isIntake) {
                            Text(
                                "",
                                fontFamily = RobotoRegularFonts,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                "",
                                fontFamily = RobotoRegularFonts,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                if (intakeBottleUpdated || !intakeBottleUpdated) {
                    if (isIntake) {
                        Column {
                            // Show count if intake view and item already there
                            val backColor = colorResource(R.color.intakeBottleIconStandardText)
                            val keyExists = chViewModel.currentBottleCounts[bottle.id] != null
                            if (keyExists) {
                                val count = (chViewModel.currentBottleCounts[bottle.id])
                                if (count != "1") {
                                    Text(count.toString(),
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(alignment = CenterHorizontally)
                                            .width(16.dp)
                                            .height(16.dp)
                                            .offset(x = 15.dp, y = -(25).dp)
                                            .drawBehind {
                                                drawCircle(
                                                    color = backColor,
                                                    radius = this.size.maxDimension
                                                )
                                            })
                                }
                            }
                        }

                    }
                }
            }   // Box
        }

        if (showName) {
            Text(bottle.name,
                fontFamily = RobotoRegularFonts,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 3,
                modifier = Modifier
                    .height(40.dp)
                    .width(if (isIntake) 80.dp else 100.dp)
            )
        }

        DropdownMenu(
            expanded = showBottleDropDownPopover,
            onDismissRequest = { showBottleDropDownPopover = false },
        ) {
            Box(
                Modifier
                    .width(260.dp)
                    .height(40.dp)
                    .background(Color.White)
            ) {
                Row {
                    Button(
                        onClick = {
                            val fractionalBottleWaterAmount = if (bottle.waterAmount == 0F) 0 else bottle.waterAmount * 0.25
                            val fractionalBottleSodiumAmount = if (bottle.sodiumAmount == 0F) 0 else bottle.sodiumAmount * 0.25
                            val newUserFractionalBottle = BottleData(id = UUID.randomUUID().hashCode(), name = bottle.name, image_name = bottle.image_name, barcode = "", sodiumAmount = fractionalBottleSodiumAmount.toFloat(), sodiumSize = "mg", waterAmount = fractionalBottleWaterAmount.toFloat(), waterSize = "oz")
                            chViewModel.addIntakeBottle(newUserFractionalBottle)
                            chViewModel.totalWaterAmount += fractionalBottleWaterAmount.toDouble()
                            chViewModel.totalSodiumAmount += fractionalBottleSodiumAmount.toDouble()
                            showBottleDropDownPopover = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            "+1/4",
                            fontFamily = OswaldFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(R.color.intakeFractionalStandardText)
                        )
                    }

                    Divider(
                        color = Color.LightGray,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                    )

                    Button(
                        onClick = {
                            val fractionalBottleWaterAmount = if (bottle.waterAmount == 0F) 0 else bottle.waterAmount * 0.50
                            val fractionalBottleSodiumAmount = if (bottle.sodiumAmount == 0F) 0 else bottle.sodiumAmount * 0.50
                            val newUserFractionalBottle = BottleData(id = UUID.randomUUID().hashCode(), name = bottle.name, image_name = bottle.image_name, barcode = "", sodiumAmount = fractionalBottleSodiumAmount.toFloat(), sodiumSize = "mg", waterAmount = fractionalBottleWaterAmount.toFloat(), waterSize = "oz")
                            chViewModel.addIntakeBottle(newUserFractionalBottle)
                            chViewModel.totalWaterAmount += fractionalBottleWaterAmount.toDouble()
                            chViewModel.totalSodiumAmount += fractionalBottleSodiumAmount.toDouble()
                            showBottleDropDownPopover = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            "+1/2",
                            fontFamily = OswaldFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(R.color.intakeFractionalStandardText)
                        )
                    }

                    Divider(
                        color = Color.LightGray,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                    )

                    Button(
                        onClick = {
                            val fractionalBottleWaterAmount = if (bottle.waterAmount == 0F) 0 else bottle.waterAmount * 0.75
                            val fractionalBottleSodiumAmount = if (bottle.sodiumAmount == 0F) 0 else bottle.sodiumAmount * 0.75
                            val newUserFractionalBottle = BottleData(id = UUID.randomUUID().hashCode(), name = bottle.name, image_name = bottle.image_name, barcode = "", sodiumAmount = fractionalBottleSodiumAmount.toFloat(), sodiumSize = "mg", waterAmount = fractionalBottleWaterAmount.toFloat(), waterSize = "oz")
                            chViewModel.addIntakeBottle(newUserFractionalBottle)
                            chViewModel.totalWaterAmount += fractionalBottleWaterAmount.toDouble()
                            chViewModel.totalSodiumAmount += fractionalBottleSodiumAmount.toDouble()
                            showBottleDropDownPopover = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            "+3/4",
                            fontFamily = OswaldFonts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(R.color.intakeFractionalStandardText)
                        )
                    }

                    Divider(
                        color = Color.LightGray,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                    )

                    Button(
                        onClick = {
                            deleteUserBottleMenuItem(chViewModel, bottle.id)
                            showBottleDropDownPopover = false
                            chViewModel._updateCurrentUserIntakeItems.value = chViewModel._updateCurrentUserIntakeItems.value == false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Image(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_restore_from_trash_24),
                            modifier = Modifier.scale(1.2F),
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(colorResource(R.color.intakeFractionalStandardText)))
                    }
                }

            }
        }

    }   // Column
}

fun convertImageNameToResourceId(imageName: String): Int {
    if (imageName == "Preview Icon - Can Water - 9") {
        return R.drawable.icon_intake_drink_9
    }
    else if (imageName == "Preview Icon - Electro - Pickle") {
        return R.drawable.icon_intake_electro_pickle
    }
    else if (imageName == "Preview Icon - Electro - Popscicle") {
        return R.drawable.icon_intake_electro_popscicle
    }
    else if (imageName == "Preview Icon - Glass Ice - 6") {
        return R.drawable.icon_intake_drink_6
    }
    else if (imageName == "Preview Icon - Glass Water - 10") {
        return R.drawable.icon_intake_drink_10
    }
    else if (imageName == "Preview Icon - Electro - 1") {
        return R.drawable.icon_intake_electro_1
    }
    else if (imageName == "Preview Icon - Electro - 2") {
        return R.drawable.icon_intake_electro_2
    }
    else if (imageName == "Preview Icon - Electro - 3") {
        return R.drawable.icon_intake_electro_3
    }
    else if (imageName == "Preview Icon - Electro - 4") {
        return R.drawable.icon_intake_electro_4
    }
    else if (imageName == "Preview Icon - Electro - 5") {
        return R.drawable.icon_intake_electro_5
    }
    else if (imageName == "Preview Icon - Electro - 6") {
        return R.drawable.icon_intake_electro_6
    }
    else if (imageName == "Preview Icon - Electro - 7") {
        return R.drawable.icon_intake_electro_7
    }
    else if (imageName == "Preview Icon - Electro - 8") {
        return R.drawable.icon_intake_electro_8
    }
    else if (imageName == "Preview Icon - Electro - 9") {
        return R.drawable.icon_intake_electro_9
    }
    else if (imageName == "Preview Icon - Water - 1") {
        return R.drawable.icon_intake_water_1
    }
    else if (imageName == "Preview Icon - Water - 2") {
        return R.drawable.icon_intake_water_2
    }
    else if (imageName == "Preview Icon - Water - 3") {
        return R.drawable.icon_intake_water_3
    }
    else if (imageName == "Preview Icon - Water - 4") {
        return R.drawable.icon_intake_water_4
    }
    else if (imageName == "Preview Icon - Water - 5") {
        return R.drawable.icon_intake_water_5
    }
    else if (imageName == "Preview Icon - Water - 6") {
        return R.drawable.icon_intake_water_6
    }
    else if (imageName == "Preview Icon - Water - 7") {
        return R.drawable.icon_intake_water_7
    }
    else if (imageName == "Preview Icon - Water - 8") {
        return R.drawable.icon_intake_water_8
    }
    else if (imageName == "Preview Icon - Water - 9") {
        return R.drawable.icon_intake_water_9
    }
    else if (imageName == "Preview Icon - Water - 10") {
        return R.drawable.icon_intake_water_10
    }
    else if (imageName == "Preview Icon - Water - 11") {
        return R.drawable.icon_intake_water_11
    }
    else if (imageName == "Preview Icon - Water - 12") {
        return R.drawable.icon_intake_water_12
    }
    else if (imageName == "Preview Icon - Water - 13") {
        return R.drawable.icon_intake_water_13
    }
    else if (imageName == "Preview Icon - Water - 14") {
        return R.drawable.icon_intake_water_14
    }
    else if (imageName == "Preview Icon - Water - 16") {
        return R.drawable.icon_intake_water_16
    }
    else if (imageName == "Preview Icon - Water - 17") {
        return R.drawable.icon_intake_water_17
    }
    else if (imageName == "Preview Icon - Water - 18") {
        return R.drawable.icon_intake_water_18
    }

    throw Exception("COULD NOT FIND BOTTLE IMAGE NAME - THIS SHOULD NOT HAPPEN")
}
