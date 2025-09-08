package com.epicorebiosystems.rehydrate.topBarViews.infoViews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.topBarViews.InfoScreens
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun SodiumEqView(chViewModel: ModelData, showNextInfoPopup: (InfoScreens) -> Unit) {
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .height(1040.dp)
            .background(colorResource(R.color.legalScreensBackground)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(stringResource(R.string.title_sodium_equivalents),
            modifier = Modifier.padding(top = 20.dp),
            fontFamily = OswaldFonts,
            fontSize = if (isJapanese) 22.sp else 24.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.info_view_title_color)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {

            // Salt
            Row(
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 20.dp),
                ) {
                Image(painterResource(id = R.drawable.icon_intake_salt),
                    contentDescription = "Sodium Salt")

                Column {
                    Row(
                        modifier = Modifier.padding(start = 20.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {

                        Text(
                            stringResource(R.string.table_salt),
                            fontFamily = OswaldFonts,
                            fontSize = if (isJapanese) 22.sp else 24.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Normal,
                            color = Color.White)

                        Spacer(modifier = Modifier.width(5.dp))

                        Text(
                            stringResource(R.string.per_mayo_clinic),
                            modifier = Modifier.offset(y = -(5).dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = if (isJapanese) 12.sp else 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White)
                    }

                    Text(
                        stringResource(R.string._1_teaspoon_2_325mg_sodium),
                        modifier = Modifier.padding(start = 20.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = if (isJapanese) 12.sp else 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White)

                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.when_you_deplete_1_000mg_sodium_it_roughly_equals),
                    modifier = Modifier.padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                    fontFamily = OswaldFonts,
                    fontSize = if (isJapanese) 16.sp else 18.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }

            // Burger
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                ) {
                    Image(
                        painterResource(id = R.drawable.icon_intake_burger),
                        contentDescription = "Sodium Salt"
                    )

                    Column {
                        Row(
                            modifier = Modifier.padding(start = 20.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {

                            Text(
                                stringResource(R.string.one_big_mac),
                                fontFamily = OswaldFonts,
                                fontSize = if (isJapanese) 22.sp else 24.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Normal,
                                color = Color.White
                            )
                        }

                        Text(
                            "1,010mg",
                            modifier = Modifier.padding(start = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = if (isJapanese) 12.sp else 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                    }
                }
            }

            // Pizza
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                ) {
                    Image(
                        painterResource(id = R.drawable.icon_intake_pizza),
                        contentDescription = "Sodium Salt"
                    )

                    Column {
                        Row(
                            modifier = Modifier.padding(start = 20.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {

                            Text(
                                stringResource(R.string.one_medium_domino_s_pepperoni_pizza),
                                fontFamily = OswaldFonts,
                                fontSize = if (isJapanese) 22.sp else 24.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.Normal,
                                color = Color.White
                            )
                        }

                        Text(
                            stringResource(R.string.one_mg),
                            modifier = Modifier.padding(start = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = if (isJapanese) 12.sp else 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                    }
                }
            }

            // Fries
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                ) {
                    Image(
                        painterResource(id = R.drawable.icon_intake_fries),
                        contentDescription = stringResource(R.string.sodium_salt)
                    )

                    Column {
                        Row(
                            modifier = Modifier.padding(start = 20.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {

                            Text(
                                stringResource(R.string._2_1_2_large_mcdonald_s_fries),
                                fontFamily = OswaldFonts,
                                fontSize = if (isJapanese) 22.sp else 24.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.Normal,
                                color = Color.White
                            )
                        }

                        Text(
                            stringResource(R.string.four_mg_each),
                            modifier = Modifier.padding(start = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = if (isJapanese) 12.sp else 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                    }
                }
            }

            if (!isJapanese) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp, bottom = 40.dp),
                ) {
                    Column {
                        Text(
                            stringResource(R.string.we_need_sodium_for),
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.info_view_title_color)
                        )

                        Text(
                            stringResource(R.string.balancing_the_body_s_fluid_levels),
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                        Text(
                            stringResource(R.string.digestion),
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )


                        Text(
                            stringResource(R.string.nerve_function),
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )


                        Text(
                            stringResource(R.string.muscle_control),
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column {
                        Text(
                            stringResource(R.string.but_too_much_can_lead_to),
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.info_view_title_color)
                        )

                        Text(
                            stringResource(R.string.high_blood_pressure),
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                        Text(
                            stringResource(R.string.stroke),
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                        Text(
                            stringResource(R.string.heart_disease),
                            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    }
                }
            }
            else {
                Column {
                    Text(
                        stringResource(R.string.we_need_sodium_for),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorResource(R.color.info_view_title_color)
                    )

                    Text(
                        stringResource(R.string.balancing_the_body_s_fluid_levels),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.digestion),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )


                    Text(
                        stringResource(R.string.nerve_function),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )


                    Text(
                        stringResource(R.string.muscle_control),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column {
                    Text(
                        stringResource(R.string.but_too_much_can_lead_to),
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorResource(R.color.info_view_title_color)
                    )

                    Text(
                        stringResource(R.string.high_blood_pressure),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.stroke),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    Text(
                        stringResource(R.string.heart_disease),
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }

            // Move to next info screen
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {

                Button(
                    onClick = {
                        showNextInfoPopup(InfoScreens.URINE_COLOR_CHART)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                    elevation =  ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp)
                ) {
                    Text(
                        stringResource(R.string.urine_color_chart_next),
                        modifier = Modifier
                            .padding(end = 10.dp),
                        fontFamily = OswaldFonts,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }

        }
    }
}