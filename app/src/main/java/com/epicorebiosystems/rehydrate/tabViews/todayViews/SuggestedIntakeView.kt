package com.epicorebiosystems.rehydrate.tabViews.todayViews

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.TabScreen
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import com.epicorebiosystems.rehydrate.ui.theme.TenByEightRegularFont
import java.math.RoundingMode

@Composable
fun SuggestedIntakeView(chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor, widthModifier: Dp, updateIntakeExpanded: (Boolean) -> Unit,
                        items: List<TabScreen>, onItemClick: (TabScreen) -> Unit) {

    var isExpanded by remember { mutableStateOf(false) }
    var isWaterView by remember { mutableStateOf(true) }
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"
    val fontSizeCountImperial = 4
    val fontSizeCountMetric = 3

    Box(
        Modifier
            .height(if (isExpanded) if (isWaterView) 680.dp else 550.dp else 250.dp)
            .width(widthModifier)
            .offset(x = 10.dp, y = 120.dp)
            .background(Color.White, RoundedCornerShape(10.dp)),
    ) {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Title Row
            Text(
                text = stringResource(R.string.suggested_view_sweat_metrics),
                fontSize = 20.sp,
                fontFamily = OswaldFonts,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 10.dp, start = 20.dp),
                color = colorResource(R.color.grayStandardText)
            )

            Column(verticalArrangement = Arrangement.spacedBy((-10).dp)) {
                // Top Row: Water and Sodium Titles with Underline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.suggested_view_water) + "(${chViewModel.userPrefsData.getUserSweatUnitString()})",
                                fontSize = if (isJapanese) 12.sp else 16.sp,
                                fontFamily = OswaldFonts,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.grayStandardText)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.indicator_water),
                                contentDescription = "image_water_icon"
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(if (isJapanese) 110.dp else 150.dp)
                                .height(1.dp)
                                .background(colorResource(R.color.grayStandardText))
                        )
                    }
                    //Spacer(modifier = Modifier.weight(1f))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.suggested_view_sodium) + "(${chViewModel.userPrefsData.getUserSodiumUnitString()})",
                                fontSize = if (isJapanese) 12.sp else 16.sp,
                                fontFamily = OswaldFonts,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.grayStandardText)
                            )
                            Image(
                                modifier = Modifier.padding(start = 3.dp),
                                painter = painterResource(id = R.drawable.indicator_sodium),
                                contentDescription = "image_sodium_icon"
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 160.dp else 170.dp)
                                .height(1.dp)
                                .background(colorResource(R.color.grayStandardText))
                        )
                    }
                }

                // Middle Row: Intake Values and Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        modifier = Modifier.padding(end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = " ",
                            fontSize = 9.sp,
                            fontFamily = RobotoRegularFonts,
                            fontWeight = FontWeight.Light,
                            color = colorResource(R.color.grayStandardText),
                            modifier = Modifier.padding(top = 15.dp)
                        )

                        if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
                            if (chViewModel.fluidDeficitInOz >= 338.14 || chViewModel.sweatVolumeDeficitInMl >= 10000) {
                                Text(
                                    text = chViewModel.userPrefsData.getFluidDeficitString(),
                                    fontSize = if (chViewModel.currentUnits.value == 1) 40.sp else 30.sp,
                                    fontFamily = TenByEightRegularFont,
                                    fontWeight = FontWeight.Normal,
                                    color = colorResource(R.color.waterFull)
                                )
                            } else {
                                if ((chViewModel.sweatVolumeTotalLossInMl > 0u || chViewModel.fluidTotalIntakeInMl > 0u)
                                    && ((chViewModel.sweatVolumeTotalLossInMl - chViewModel.fluidTotalIntakeInMl).toInt() < 0)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.suggested_intake_drop),
                                        contentDescription = "image_drop_icon"
                                    )
                                } else {
                                    val displayText = if (chViewModel.currentUnits.value == 1)
                                        (chViewModel.sweatVolumeDeficitInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.HALF_DOWN).toDouble().toString() else chViewModel.sweatVolumeDeficitInMl.toString()
                                    val fontSize =
                                        if (chViewModel.currentUnits.value == 1) (if (displayText.length > fontSizeCountImperial) 40.sp else 48.sp) else ( if (displayText.length > fontSizeCountMetric) 40.sp else 48.sp)
                                    Text(
                                        text = displayText,
                                        fontSize = fontSize,
                                        fontFamily = TenByEightRegularFont,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.waterFull)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "0.0",
                                fontSize = 48.sp,
                                fontFamily = TenByEightRegularFont,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.waterFull)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        modifier = Modifier.padding(
                            end = if ((chViewModel.sweatVolumeTotalLossInMl.toDouble() != 0.0 || chViewModel.fluidTotalIntakeInMl.toDouble() != 0.0)
                                && (chViewModel.sweatVolumeTotalLossInMl - chViewModel.fluidTotalIntakeInMl < 0u)
                            ) (-30).dp else 0.dp,
                            start = if (chViewModel.currentUnits.value == 0 && chViewModel.sweatVolumeDeficitInMl.toString().length < fontSizeCountMetric) 50.dp else 20.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.suggested_view_per_diet),
                            fontSize = 9.sp,
                            fontFamily = RobotoRegularFonts,
                            fontWeight = FontWeight.Light,
                            color = colorResource(R.color.grayStandardText),
                            modifier = Modifier.padding(top = 15.dp)
                        )
                        if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
//                            if (chViewModel.capSodiumValue.value != 0 && chViewModel.sweatSodiumDeficitInMg >= chViewModel.capSodiumValue.value) {
                            if (chViewModel.sweatSodiumDeficitInMg >= 8000) {
                                Text(
//                                    text = "$chViewModel.capSodiumValue.value+",
                                    text = "8000+",
//                                    fontSize = if (chViewModel.capSodiumValue.value.toString().length > fontSizeCountMetric) 40.sp else 48.sp,
                                    fontSize = 40.sp,
                                    fontFamily = TenByEightRegularFont,
                                    fontWeight = FontWeight.Normal,
                                    color = colorResource(R.color.sodiumFull)
                                )
                            } else {
                                Text(
                                    text = chViewModel.sweatSodiumDeficitInMg.toString(),
                                    fontSize = if (chViewModel.sweatSodiumDeficitInMg.toString().length > fontSizeCountMetric) 40.sp else 48.sp,
                                    fontFamily = TenByEightRegularFont,
                                    fontWeight = FontWeight.Normal,
                                    color = colorResource(R.color.sodiumFull)
                                )
                            }
                        } else {
                            Text(
                                text = "0.0",
                                fontSize = 48.sp,
                                fontFamily = TenByEightRegularFont,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.sodiumFull)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Bottom Row: LOST / CONSUMED Labels and Values
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        modifier = Modifier.padding(end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.suggested_view_per_lost),
                            fontSize = if (isJapanese) 12.sp else 14.sp,
                            fontFamily = OswaldFonts,
                            fontWeight = FontWeight.Light,
                            color = colorResource(R.color.grayStandardText)
                        )
                        if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
                            Text(
                                text = ( if (chViewModel.currentUnits.value == 1)
                                    (chViewModel.sweatVolumeTotalLossInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.HALF_DOWN).toDouble().toString() else chViewModel.sweatVolumeTotalLossInMl.toString()
                                        ),
                                fontSize = if (chViewModel.sweatVolumeTotalLossInMl.toString().length >= 4) 12.sp else 16.sp,
                                fontFamily = TenByEightRegularFont,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.waterFull)
                            )
                        } else {
                            Text(
                                text = "0.0",
                                fontSize = 16.sp,
                                fontFamily = TenByEightRegularFont,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.waterFull)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.suggested_view_per_consumed),
                            fontSize = if (isJapanese) 12.sp else 14.sp,
                            fontFamily = OswaldFonts,
                            fontWeight = FontWeight.Light,
                            color = colorResource(R.color.grayStandardText)
                        )
                        if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
                            val displayIntake = if (chViewModel.currentUnits.value == 1)
                                (chViewModel.fluidTotalIntakeInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.HALF_DOWN).toDouble() else chViewModel.fluidTotalIntakeInMl.toDouble()
                            Text(
                                text = String.format(
                                    if (chViewModel.currentUnits.value == 1) "%.1f" else "%.0f", displayIntake.toFloat()
                                ),
                                fontSize = if (chViewModel.sweatVolumeTotalLossInMl.toString().length >= 4) 12.sp else 16.sp,
                                fontFamily = TenByEightRegularFont,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.waterFull)
                            )
                        } else {
                            Text(
                                text = "0.0",
                                fontSize = 16.sp,
                                fontFamily = TenByEightRegularFont,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.waterFull)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.suggested_view_per_lost),
                            fontSize = if (isJapanese) 12.sp else 14.sp,
                            fontFamily = OswaldFonts,
                            fontWeight = FontWeight.Light,
                            color = colorResource(R.color.grayStandardText)
                        )
                        if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
                            Text(
                                text = chViewModel.sweatSodiumTotalLossInMg.toString(),
                                fontSize = if (chViewModel.sweatSodiumTotalLossInMg.toString().length >= 4) 12.sp else 16.sp,
                                fontFamily = TenByEightRegularFont,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.sodiumFull)
                            )
                        } else {
                            Text(
                                text = "0",
                                fontSize = 16.sp,
                                fontFamily = TenByEightRegularFont,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.sodiumFull)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.suggested_view_per_consumed),
                            fontSize = if (isJapanese) 12.sp else 14.sp,
                            fontFamily = OswaldFonts,
                            fontWeight = FontWeight.Light,
                            color = colorResource(R.color.grayStandardText)
                        )
                        if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
                            Text(
                                text = chViewModel.sodiumTotalIntakeInMg.toString(),
                                fontSize = if (chViewModel.sweatSodiumTotalLossInMg.toString().length >= 4) 12.sp else 16.sp,
                                fontFamily = TenByEightRegularFont,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.sodiumFull)
                            )
                        } else {
                            Text(
                                text = "0",
                                fontSize = 16.sp,
                                fontFamily = TenByEightRegularFont,
                                fontWeight = FontWeight.Normal,
                                color = colorResource(R.color.sodiumFull)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Expand/Collapse Button within main column
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = trackClick(targetName = "SuggestedIntakeView expanded pressed") {
                            isExpanded = !isExpanded
                            updateIntakeExpanded(isExpanded)
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Image(painterResource(if (ebsMonitor.getIsCHArmband()) R.drawable.icon_info_light_blue else R.drawable.icon_info_grey), contentDescription = "image_info_light_blue")
                    }
                }
            } // End of main intake Column

            // Expanded details (if isExpanded is true)
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (!isWaterView)
                                colorResource(R.color.suggestedIntakeExpandedSodiumBackground)
                            else
                                colorResource(R.color.suggestedIntakeExpandedWaterBackground)
                        )
                        .padding(top = 10.dp)
                ) {
                    // Toggle Water / Sodium view buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isWaterView = true },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(R.string.today_stats_chart_water) + "(${chViewModel.userPrefsData.getUserSweatUnitString()})",
                                        fontSize = 16.sp,
                                        fontFamily = OswaldFonts,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isWaterView)
                                            colorResource(R.color.waterFull)
                                        else colorResource(R.color.grayStandardText)
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.indicator_water),
                                        contentDescription = "Water Icon",
                                        tint = if (isWaterView)
                                            colorResource(R.color.waterFull)
                                        else colorResource(R.color.grayStandardText)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(1.dp)
                                        .background(
                                            if (isWaterView)
                                                colorResource(R.color.waterFull)
                                            else colorResource(R.color.grayStandardText)
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { isWaterView = false },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(R.string.today_stats_chart_sodium) + "(${chViewModel.userPrefsData.getUserSodiumUnitString()})",
                                        fontSize = 16.sp,
                                        fontFamily = OswaldFonts,
                                        fontWeight = FontWeight.Normal,
                                        color = if (!isWaterView)
                                            colorResource(R.color.sodiumFull)
                                        else colorResource(R.color.grayStandardText)
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.indicator_sodium),
                                        contentDescription = "Sodium Icon",
                                        tint = if (isWaterView)
                                            colorResource(R.color.grayStandardText)
                                        else colorResource(R.color.sodiumFull)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(1.dp)
                                        .background(
                                            if (!isWaterView)
                                                colorResource(R.color.sodiumFull)
                                            else colorResource(R.color.grayStandardText)
                                        )
                                )
                            }
                        }
                    }

                    if (isWaterView) {
                        Column(modifier = Modifier.padding(start = 20.dp)) {
                            Row (verticalAlignment = Alignment.CenterVertically) {
                                Text("\u2022",
                                    fontSize = 20.sp,
                                    fontFamily = RobotoRegularFonts,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,)
                                Text(
                                    text = stringResource(R.string.suggested_view_expanded_1),
                                    fontSize = 10.sp,
                                    fontFamily = RobotoRegularFonts,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Row (verticalAlignment = Alignment.Top, modifier = Modifier.padding(end = 20.dp)) {
                                Text("\u2022", fontSize = 20.sp)
                                Text(
                                    text = stringResource(R.string.suggested_view_expanded_2),
                                    fontSize = 10.sp,
                                    fontFamily = RobotoRegularFonts,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .offset(x = if (chViewModel.userPassiveLossState.value) 0.dp else 4.dp)
                                        .padding(top = 5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = stringResource(R.string.suggested_view_water_loss) + if (chViewModel.userPassiveLossState.value) "*" else "",
                                        fontSize = 12.sp,
                                        fontFamily = OswaldFonts,
                                        fontWeight = FontWeight.Light,
                                        color = colorResource(R.color.grayStandardText),
                                        modifier = Modifier.padding(start = 13.dp)
                                    )
                                    if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
                                        Text(
                                            text = ( if (chViewModel.currentUnits.value == 1)
                                                (chViewModel.sweatVolumeTotalLossInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.HALF_DOWN).toDouble().toString() else chViewModel.sweatVolumeTotalLossInMl.toString()
                                                    ),
                                            fontSize = 18.sp,
                                            fontFamily = TenByEightRegularFont,
                                            fontWeight = FontWeight.Normal,
                                            color = colorResource(R.color.waterFull)
                                        )
                                    } else {
                                        Text(
                                            text = "0.0",
                                            fontSize = 18.sp,
                                            fontFamily = TenByEightRegularFont,
                                            fontWeight = FontWeight.Normal,
                                            color = colorResource(R.color.waterFull)
                                        )
                                    }
                                    Text(
                                        text = chViewModel.userPrefsData.getUserSweatUnitString(),
                                        fontSize = 12.sp,
                                        fontFamily = TenByEightRegularFont,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.waterFull),
                                        modifier = Modifier.padding(top = 20.dp)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .offset(x = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) (if (isJapanese) (-20).dp else 0.dp) else (if (isJapanese) (-24).dp else (-3).dp))
                                        .padding(top = 0.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = stringResource(R.string.suggested_view_water_tracked),
                                        fontSize = 12.sp,
                                        fontFamily = OswaldFonts,
                                        fontWeight = FontWeight.Light,
                                        color = colorResource(R.color.grayStandardText),
                                        modifier = Modifier.padding(start = if (isJapanese) 2.dp else 0.dp)
                                    )
                                    if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
                                        val displayIntake = if (chViewModel.currentUnits.value == 1)
                                            (chViewModel.fluidTotalIntakeInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.HALF_DOWN).toDouble() else chViewModel.fluidTotalIntakeInMl.toDouble()
                                        Text(
                                            text = String.format(
                                                if (chViewModel.currentUnits.value == 1) "%.1f" else "%.0f", displayIntake.toFloat()
                                            ),
                                            fontSize = 18.sp,
                                            fontFamily = TenByEightRegularFont,
                                            fontWeight = FontWeight.Normal,
                                            color = colorResource(R.color.waterFull)
                                        )
                                    } else {
                                        Text(
                                            text = "0.0",
                                            fontSize = 18.sp,
                                            fontFamily = TenByEightRegularFont,
                                            fontWeight = FontWeight.Normal,
                                            color = colorResource(R.color.waterFull)
                                        )
                                    }
                                    Text(
                                        text = chViewModel.userPrefsData.getUserSweatUnitString(),
                                        fontSize = 12.sp,
                                        fontFamily = TenByEightRegularFont,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.waterFull),
                                        modifier = Modifier.padding(top = 3.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(190.dp)
                                        .height(1.dp)
                                        .offset(x = (-25).dp)
                                        .background(
                                            if (isWaterView)
                                                colorResource(R.color.waterFull)
                                            else
                                                colorResource(R.color.sodiumFull)
                                        )
                                )
                                Row(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .offset(x = if (isJapanese) 4.dp else 0.dp)
                                        .padding(top = 0.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = stringResource(R.string.suggested_view_water_shift),
                                        fontSize = 12.sp,
                                        fontFamily = OswaldFonts,
                                        fontWeight = FontWeight.Light,
                                        color = colorResource(R.color.grayStandardText),
                                        modifier = Modifier.padding(start = 13.dp)
                                    )
                                    if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
                                        Text(
                                            text = (if (chViewModel.currentUnits.value == 1)
                                                (chViewModel.sweatVolumeDeficitInMl.toDouble() * 0.033814).toBigDecimal().setScale(1, RoundingMode.HALF_DOWN).toDouble().toString() else chViewModel.sweatVolumeDeficitInMl.toString()
                                                    ),
                                            fontSize = 18.sp,
                                            fontFamily = TenByEightRegularFont,
                                            fontWeight = FontWeight.Normal,
                                            color = colorResource(R.color.waterFull)
                                        )
                                    } else {
                                        Text(
                                            text = "0.0",
                                            fontSize = 18.sp,
                                            fontFamily = TenByEightRegularFont,
                                            fontWeight = FontWeight.Normal,
                                            color = colorResource(R.color.waterFull)
                                        )
                                    }
                                    Text(
                                        text = chViewModel.userPrefsData.getUserSweatUnitString(),
                                        fontSize = 12.sp,
                                        fontFamily = TenByEightRegularFont,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.waterFull),
                                        modifier = Modifier.padding(top = 3.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.padding(
                                    top = 10.dp,
                                    end = 20.dp
                                )
                            ) {
                                if (chViewModel.userPassiveLossState.value) {
                                    if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession) {
                                        if (chViewModel.sweatVolumeTotalLossInMl > 0u) {
                                            Text(
                                                text = stringResource(R.string.suggested_view_expanded_3) +
                                                        String.format(
                                                            if (chViewModel.currentUnits.value == 1) " %.1f " else " %.0f ",
                                                            chViewModel.userPrefsData.handleUserSweatConversionMl(
                                                                (chViewModel.sweatVolumeTotalLossInMl.toDouble() - chViewModel.currentTEWLInMl.toDouble())
                                                            )
                                                        ) +
                                                        "${chViewModel.userPrefsData.getUserSweatUnitString()}" + stringResource(R.string.suggested_view_expanded_4) +
                                                        String.format(
                                                            if (chViewModel.currentUnits.value == 1) " %.1f " else " %.0f ",
                                                            chViewModel.userPrefsData.handleUserSweatConversionMl(
                                                                chViewModel.currentTEWLInMl.toDouble()
                                                            )
                                                        ) +
                                                        "${chViewModel.userPrefsData.getUserSweatUnitString()}" + ". " + stringResource(R.string.suggested_view_expanded_5),
                                                fontSize = 10.sp,
                                                fontFamily = RobotoRegularFonts,
                                                fontWeight = FontWeight.Normal,
                                                color = colorResource(R.color.grayStandardText),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            Text(
                                                text = stringResource(R.string.suggested_view_expanded_3) +
                                                        String.format(
                                                            if (chViewModel.currentUnits.value == 1) "%.1f " else "%.0f ",
                                                            chViewModel.userPrefsData.handleUserSweatConversion(
                                                                0.0
                                                            )
                                                        ) +
                                                        "${chViewModel.userPrefsData.getUserSweatUnitString()}" + stringResource(R.string.suggested_view_expanded_4) +
                                                        String.format(
                                                            if (chViewModel.currentUnits.value == 1) "%.1f " else "%.0f ",
                                                            chViewModel.userPrefsData.handleUserSweatConversion(
                                                                chViewModel.currentTEWLInMl.toDouble()
                                                            )
                                                        ) +
                                                        "${chViewModel.userPrefsData.getUserSweatUnitString()}" + ". " + stringResource(R.string.suggested_view_expanded_5),
                                                fontSize = 10.sp,
                                                fontFamily = RobotoRegularFonts,
                                                fontWeight = FontWeight.Normal,
                                                color = colorResource(R.color.grayStandardText),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = stringResource(R.string.suggested_view_expanded_6) + " ${chViewModel.userPrefsData.getUserSweatUnitString()}" + stringResource(R.string.suggested_view_expanded_7) + " ${chViewModel.userPrefsData.getUserSweatUnitString()}" + stringResource(R.string.suggested_view_expanded_8),
                                            fontSize = 10.sp,
                                            fontFamily = RobotoRegularFonts,
                                            fontWeight = FontWeight.Normal,
                                            color = colorResource(R.color.grayStandardText),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Text(
                                    text = stringResource(R.string.suggested_view_expanded_9),
                                    fontSize = 12.sp,
                                    fontFamily = RobotoRegularFonts,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.grayStandardText),
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(bottom = 10.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            onItemClick(items[1])
                                        },
                                        modifier = Modifier
                                            .width(160.dp)
                                            .height(30.dp)
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = true,
                                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.suggestedIntakeButtonBackground))
                                    ) {
                                        Text(
                                            text = stringResource(R.string.suggested_view_expanded_10),
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontFamily = RobotoRegularFonts,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(
                                        onClick = {
                                            chViewModel.scrollSettingsView = true
                                            onItemClick(items[3])
                                        },
                                        modifier = Modifier
                                            .width(160.dp)
                                            .height(30.dp)
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = true,
                                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.suggestedIntakeButtonBackground))
                                    ) {
                                        Text(
                                            text = stringResource(R.string.suggested_view_expanded_11),
                                            fontSize = 9.sp,
                                            fontFamily = RobotoRegularFonts,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                        )
                                    }
                                }

                                Text(
                                    text = stringResource(R.string.suggested_view_expanded_12),
                                    fontSize = 10.sp,
                                    fontFamily = RobotoRegularFonts,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.suggestedIntakeDisclaimerRed),
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .padding(bottom = 8.dp)
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.padding(start = 20.dp)) {
                            Row (modifier = Modifier.padding(end = 20.dp)) {
                                Text("\u2022", fontSize = 20.sp)
                                Text(
                                    text = " " + stringResource(R.string.suggested_view_expanded_13) + "${chViewModel.getSweatSodiumString()}",
                                    fontSize = 10.sp,
                                    fontFamily = RobotoRegularFonts,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Row (modifier = Modifier.padding(end = 20.dp)) {
                                Text("\u2022", fontSize = 20.sp)
                                Text(
                                    text = " " + stringResource(R.string.suggested_view_expanded_14),
                                    fontSize = 10.sp,
                                    fontFamily = RobotoRegularFonts,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = stringResource(R.string.suggested_view_expanded_15),
                                fontSize = 12.sp,
                                fontFamily = RobotoRegularFonts,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.grayStandardText),
                                modifier = Modifier.fillMaxWidth()
                                    .padding(bottom = 10.dp, end = 20.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = {
                                        onItemClick(items[2])
                                    },
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(30.dp)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = true,
                                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.suggestedIntakeButtonBackground))
                                ) {
                                    Text(
                                        text = stringResource(R.string.suggested_view_expanded_16),
                                        fontSize = 9.sp,
                                        fontFamily = RobotoRegularFonts,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.suggested_view_expanded_17),
                                fontSize = 10.sp,
                                fontFamily = RobotoRegularFonts,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.suggestedIntakeDisclaimerRed),
                                modifier = Modifier.fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .padding(bottom = 6.dp)
                                    .padding(end = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Expand/Collapse Button (overlayed at the bottom)
        Button(
            onClick = trackClick(targetName = "SuggestedIntakeView expanded pressed") {
                isExpanded = !isExpanded
                updateIntakeExpanded(isExpanded)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 20.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp
            )
        ) {
            Image(painterResource(if (isExpanded) R.drawable.icon_tile_expand_arrorw_up else R.drawable.icon_tile_expand_arrorw_dwn), contentDescription = "image_expand_down")
        }
    }
}