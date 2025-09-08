package com.epicorebiosystems.rehydrate.tabViews.insightViews

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserSweatProfileView(chViewModel: ModelData) {

    BoxWithConstraints(
        modifier = Modifier.padding(bottom = 5.dp)
    ) {
        val widthModifier = maxWidth - 20.dp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .height(400.dp)
                    .width(widthModifier)
                    .offset(x = 10.dp, y = 120.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
            ) {

                Text(
                    stringResource(R.string.your_sweat_profile),
                    Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp),
                    fontFamily = OswaldFonts,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.grayStandardText)
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val sweatConcentration = chViewModel.userAvgSweatSodiumConcentration?.data?.sweat_volume_ml ?: 0.0
                    // sweat
                    // low - x<=21oz
                    // Moderate - 21oz < x <48oz
                    // Heavy - 48oz<=x
                    val mlConversionToOz =  sweatConcentration / 29.574
                    if (mlConversionToOz <= 21.0) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_1),
                            contentDescription = "image_profile_gauge_1",
                            modifier = Modifier.padding(top = 50.dp).testTag("image_profile_gauge_1")
                        )
                    }
                    else if (mlConversionToOz <= 25.5) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_2),
                            contentDescription = "image_profile_gauge_2",
                            modifier = Modifier.padding(top = 50.dp).testTag("image_profile_gauge_2")
                        )
                    }
                    else if (mlConversionToOz <= 30.0) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_3),
                            contentDescription = "image_profile_gauge_3",
                            modifier = Modifier.padding(top = 50.dp).testTag("image_profile_gauge_3")
                        )
                    }
                    else if (mlConversionToOz <= 34.5) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_4),
                            contentDescription = "image_profile_gauge_4",
                            modifier = Modifier.padding(top = 50.dp).testTag("image_profile_gauge_4")
                        )
                    }
                    else if (mlConversionToOz <= 39.0) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_5),
                            contentDescription = "image_profile_gauge_5",
                            modifier = Modifier.padding(top = 50.dp).testTag("image_profile_gauge_5")
                        )
                    }
                    else if (mlConversionToOz <= 43.5) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_6),
                            contentDescription = "image_profile_gauge_6",
                            modifier = Modifier.padding(top = 50.dp).testTag("image_profile_gauge_6")
                        )
                    }
                    else if (mlConversionToOz <= 48.0) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_7),
                            contentDescription = "image_profile_gauge_7",
                            modifier = Modifier.padding(top = 50.dp).testTag("image_profile_gauge_7")
                        )
                    }
                    else if (mlConversionToOz <= 52.5) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_8),
                            contentDescription = "image_profile_gauge_8",
                            modifier = Modifier.padding(top = 50.dp).testTag("image_profile_gauge_8")
                        )
                    }
                    else if (mlConversionToOz <= 57.0) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_9),
                            contentDescription = "guage 9",
                            modifier = Modifier.padding(top = 50.dp)
                        )
                    }
                    else if (mlConversionToOz <= 61.5) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_10),
                            contentDescription = "guage 10",
                            modifier = Modifier.padding(top = 50.dp)
                        )
                    }
                    else if (mlConversionToOz <= 66.0) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_11),
                            contentDescription = "guage 11",
                            modifier = Modifier.padding(top = 50.dp)
                        )
                    }
                    else if (mlConversionToOz <= 70.5) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_12),
                            contentDescription = "guage 12",
                            modifier = Modifier.padding(top = 50.dp)
                        )
                    }
                    else {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_guage_13),
                            contentDescription = "guage 13",
                            modifier = Modifier.padding(top = 50.dp)
                        )
                    }

                    // chloride
                    // low - x<=15mM
                    // medium -  15mM < x <40mM
                    // high - 40mM<=x
                    val sodiumConcentration = chViewModel.userAvgSweatSodiumConcentration?.data?.sodium_concentration_mm ?: 0.0
                    if (sodiumConcentration <= 15.0000) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_drop_empty),
                            contentDescription = "drop empty",
                            modifier = Modifier.padding(top = 50.dp)
                        )
                    }
                    else  if (sodiumConcentration <= 40.0000) {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_drop_half),
                            contentDescription = "drop half",
                            modifier = Modifier.padding(top = 50.dp))
                    }
                    else {
                        Image(
                            painterResource(id = R.drawable.sweat_profile_drop_full),
                            contentDescription = "drop full",
                            modifier = Modifier.padding(top = 50.dp))
                    }

                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (mlConversionToOz <= 21.0) {
                            Text(
                                stringResource(R.string.insights_light),
                                Modifier.offset(y = -(10).dp),
                                fontFamily = OswaldFonts,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.insightMediumColor)
                            )
                        } else if (mlConversionToOz <= 48) {
                            Text(
                                stringResource(R.string.insights_moderate),
                                Modifier.offset(y = -(10).dp),
                                fontFamily = OswaldFonts,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.insightMediumColor)
                            )
                        }
                        else {
                            Text(
                                stringResource(R.string.insights_heavy),
                                Modifier.offset(y = -(10).dp),
                                fontFamily = OswaldFonts,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.insightMediumColor)
                            )
                        }

                        Text(
                            stringResource(R.string.sweat_volume),
                            Modifier.offset(y = -(10).dp),
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.insightLightColor)
                        )

                    }
                }
            }

            // chloride
            // low - x<=15mM
            // medium -  15mM < x <40mM
            // high - 40mM<=x
            val sodiumConcentration = chViewModel.userAvgSweatSodiumConcentration?.data?.sodium_concentration_mm ?: 0.0
            Column(
                Modifier.padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (sodiumConcentration <= 15.0000) {
                    Text(
                        stringResource(R.string.insights_low),
                        Modifier.offset(x = 10.dp, y = -(60).dp),
                        fontFamily = OswaldFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = colorResource(R.color.insightMediumColor)
                    )
                }
                else  if (sodiumConcentration <= 40.0000) {
                    Text(
                        stringResource(R.string.insights_medium),
                        Modifier.offset(x = 10.dp, y = -(60).dp),
                        fontFamily = OswaldFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = colorResource(R.color.insightMediumColor)
                    )
                }
                else {
                    Text(
                        stringResource(R.string.insights_high),
                        Modifier.offset(x = 10.dp, y = -(60).dp),
                        fontFamily = OswaldFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = colorResource(R.color.insightMediumColor)
                    )
                }

                Text(
                    stringResource(R.string.sodium_concentration),
                    Modifier.offset(x = 10.dp, y = -(55).dp),
                    fontFamily = OswaldFonts,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.insightLightColor)
                )
            }

        }
    }
}