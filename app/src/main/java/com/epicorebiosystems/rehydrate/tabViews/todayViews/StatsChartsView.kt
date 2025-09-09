package com.epicorebiosystems.rehydrate.tabViews.todayViews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun StatsChartsView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, widthModifier: Dp) {

    val imageList = listOf(R.drawable.indicator_water, R.drawable.indicator_sodium, R.drawable.indicator_activity, R.drawable.indicator_temperature)
    var selectedIndex by remember { mutableIntStateOf(Random.nextInt(0, 4)) }

    LaunchedEffect(key1 = true) {
        while (true) {
            selectedIndex = Random.nextInt(0, 4)
            delay(5000)
        }
    }

    Box(
        Modifier
            .height(310.dp)
            .width(widthModifier)
            .offset(x = 10.dp, y = 150.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
    ) {

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val chartString = when (selectedIndex) {
                    0 -> stringResource(R.string.today_stats_chart_water)
                    1 -> stringResource(R.string.today_stats_chart_sodium)
                    2 -> stringResource(R.string.today_stats_chart_activity)
                    else -> {
                        stringResource(R.string.today_stats_chart_temp)
                    }
                }

                Text(
                    stringResource(R.string.today_stats_card_title) + " " + chartString,
                    Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                    fontFamily = OswaldFonts,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.grayStandardText)
                )

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        imageList.forEachIndexed { index, imageResId ->
                            SegmentedControlItem(
                                imageResId = imageResId,
                                isSelected = index == selectedIndex,
                                itemWidth = 40.dp,
                                onClick = {
                                    selectedIndex = index
                                }
                            )
                        }
                    }
                }
            }

            when (selectedIndex) {
                0 -> WaterIntakeLineChartView(chViewModel, ebsDeviceMonitor, widthModifier)
                1 -> SodiumIntakeLineChartView(chViewModel, ebsDeviceMonitor, widthModifier)
                2 -> PhysiologyActivityView(chViewModel, ebsDeviceMonitor, widthModifier)
                else -> {
                    PhysiologySkinTempView(chViewModel, ebsDeviceMonitor, widthModifier)
                }
            }

        }

    }   // Box
}

@Composable
private fun SegmentedControlItem(
    imageResId: Int,
    isSelected: Boolean,
    itemWidth: Dp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(itemWidth)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color.LightGray else Color.Transparent // Change color as needed
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = "image_segment",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .height(itemWidth)
        )
    }
}