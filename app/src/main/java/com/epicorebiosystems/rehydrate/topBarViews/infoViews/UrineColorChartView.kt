package com.epicorebiosystems.rehydrate.topBarViews.infoViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.topBarViews.InfoScreens
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun UrineColorChartView(chViewModel: ModelData, showNextInfoPopup: (InfoScreens) -> Unit) {
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .height(1240.dp)
            .background(colorResource(R.color.legalScreensBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            stringResource(R.string.urine_color_chart),
            modifier = Modifier.padding(bottom = 10.dp),
            textAlign = TextAlign.Center,
            fontFamily = OswaldFonts,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.info_view_title_color))

        Text(
            stringResource(R.string.you_can_check_your_hydration_level_before),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            fontFamily = RobotoRegularFonts,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White)

        Text(
            stringResource(R.string.observe_the_color_of_your_urine),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
            fontFamily = RobotoRegularFonts,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White)

        Text(
            stringResource(R.string.match_your_urine_color),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
            fontFamily = RobotoRegularFonts,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White)

        // HYDRATED
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .offset(x = -(10).dp)
        ) {
            Text(
                stringResource(R.string.hydrated_urine),
                modifier = Modifier
                    .rotate(-90f)
                    .offset(x = -(140).dp),
                fontFamily = OswaldFonts,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White)

            Column {
                Column(
                    modifier = Modifier
                        .padding(end = 10.dp, top = 5.dp)
                        .clip(RoundedCornerShape((10.dp)))
                        .height(80.dp)
                        .width(300.dp)
                        .background(colorResource(R.color.urineChartOptimal1Bkgrd)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.optimal),
                        fontFamily = OswaldFonts,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.urineChartTextColor))
                }

                Text("",
                    modifier = Modifier
                        .padding(end = 10.dp, top = 5.dp)
                        .height(80.dp)
                        .width(300.dp)
                        .clip(RoundedCornerShape((10.dp)))
                        .background(colorResource(R.color.urineChartOptimal2Bkgrd)),
                    fontFamily = OswaldFonts,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.urineChartTextColor))

                Column(
                    modifier = Modifier
                        .padding(end = 10.dp, top = 5.dp)
                        .clip(RoundedCornerShape((10.dp)))
                        .height(80.dp)
                        .width(300.dp)
                        .background(colorResource(R.color.urineChartOptimal3Bkgrd)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.well_hydrated),
                        textAlign = TextAlign.Center,
                        fontFamily = OswaldFonts,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.urineChartTextColor))
                }

                Text("",
                    modifier = Modifier
                        .padding(end = 10.dp, top = 5.dp)
                        .height(80.dp)
                        .width(300.dp)
                        .clip(RoundedCornerShape((10.dp)))
                        .background(colorResource(R.color.urineChartOptimal4Bkgrd)),
                    fontFamily = OswaldFonts,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.urineChartTextColor))

            }
        }

        // DEHYDRATED
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .offset(x = -(10).dp)
        ) {
            Text(
                stringResource(R.string.hydration_dehydrated),
                modifier = Modifier
                    .rotate(-90f)
                    .offset(x = -(140).dp),
                fontFamily = OswaldFonts,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White)

            Column(
                modifier = Modifier
                    .padding(start = if (isJapanese) 20.dp else 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = 10.dp, top = 5.dp)
                        .clip(RoundedCornerShape((10.dp)))
                        .height(80.dp)
                        .width(300.dp)
                        .background(colorResource(R.color.urineChartOptimal5Bkgrd)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.hydration_dehydrated),
                        fontFamily = OswaldFonts,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.urineChartTextColor))
                }

                Text("",
                    modifier = Modifier
                        .padding(end = 10.dp, top = 5.dp)
                        .height(80.dp)
                        .width(300.dp)
                        .clip(RoundedCornerShape((10.dp)))
                        .background(colorResource(R.color.urineChartOptimal6Bkgrd)),
                    fontFamily = OswaldFonts,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.urineChartTextColor))

                Text("",
                    modifier = Modifier
                        .padding(end = 10.dp, top = 5.dp)
                        .height(80.dp)
                        .width(300.dp)
                        .clip(RoundedCornerShape((10.dp)))
                        .background(colorResource(R.color.urineChartOptimal7Bkgrd)),
                    fontFamily = OswaldFonts,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.urineChartTextColor))

                Column(
                    modifier = Modifier
                        .padding(end = 10.dp, top = 5.dp)
                        .clip(RoundedCornerShape((10.dp)))
                        .height(80.dp)
                        .width(300.dp)
                        .background(colorResource(R.color.urineChartOptimal8Bkgrd)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.seek_medical_aid),
                        textAlign = TextAlign.Center,
                        fontFamily = OswaldFonts,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White)
                }

            }
        }

        Column(
            modifier = Modifier
                .padding(top = 20.dp, start = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                "Reference:",
                textAlign = TextAlign.Left,
                fontFamily = RobotoRegularFonts,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            Text(
                "U.S. Army Public Health Command (provisional)",
                textAlign = TextAlign.Left,
                fontFamily = RobotoRegularFonts,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            Text(
                "http://phc.amedd.army.mil Cp-070-0510",
                textAlign = TextAlign.Left,
                fontFamily = RobotoRegularFonts,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = trackClick(targetName = "Open InfoScreens.SUPPORT") {
                    showNextInfoPopup(InfoScreens.SUPPORT)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                elevation =  ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp)
            ) {
                Text(
                    stringResource(R.string.support),
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