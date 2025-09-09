package com.epicorebiosystems.rehydrate.tabViews.historyViews

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.TabScreen
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.getChartSessionHour
import com.epicorebiosystems.rehydrate.networkManager.DayIntakeLossData
import com.epicorebiosystems.rehydrate.sharedViews.BgStatusView
import com.epicorebiosystems.rehydrate.sharedViews.SegmentedControl
import com.epicorebiosystems.rehydrate.tabViews.insightViews.LegacyBlurImage
import com.epicorebiosystems.rehydrate.topBarViews.NotificationView
import com.epicorebiosystems.rehydrate.topBarViews.TopBarView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.DateTimeException
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HistoryScreen(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController, updateHideBottomBar: (Boolean) -> Unit, items: List<TabScreen>, onItemClick: (TabScreen) -> Unit) {
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"
    val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.history_blur)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        Scaffold(
            modifier = Modifier.fillMaxWidth()
                .blur(if (!chViewModel.switchShareAnonymousDataEpicore) 10.dp else 0.dp),
            topBar = {
                TopBarView(
                    chViewModel,
                    ebsDeviceMonitor,
                    navController,
                    updateHideBottomBar = { viewState ->
                        updateHideBottomBar(viewState)
                    })

                NotificationView(chViewModel)

            }
        ) {
            if (!chViewModel.switchShareAnonymousDataEpicore) {
                LegacyBlurImage(bitmap, 25f)
            }
            else {
                val sweatElectrolyteDisplaySelection = remember { mutableIntStateOf(0) }
                val timeChange = remember { mutableIntStateOf(0) }
                val sweatList =
                    listOf(stringResource(R.string.title_water), stringResource(R.string.title_sodium))
                val timeList = listOf(
                    stringResource(R.string.history_day),
                    stringResource(R.string.week),
                    stringResource(R.string.history_month)
                )
                var viewTitle by remember { mutableStateOf(timeList[0]) }

                BgStatusView(chViewModel, ebsDeviceMonitor)

                BoxWithConstraints {
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

                            Column {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            viewTitle,
                                            Modifier.padding(
                                                start = 20.dp,
                                                top = 10.dp,
                                                bottom = 20.dp
                                            ),
                                            fontFamily = OswaldFonts,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorResource(R.color.grayStandardText)
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        if (chViewModel.switchShareAnonymousDataEpicore) {
                                            SegmentedControl(
                                                items = sweatList,
                                                cornerRadius = 20,
                                                defaultSelectedItemIndex = 0,
                                                color = R.color.insightMediumColor
                                            ) {
                                                //Log.d("HistorySweatSodiumToggle", "Selected item : ${sweatList[it]}")
                                                sweatElectrolyteDisplaySelection.value = it
                                            }
                                        }

                                    }
                                }   // Column - Sweat-Segment control

                                Column {
                                    if (timeChange.value == 0) {
                                        // LineGraph
                                        val sweatHistoricalData =
                                            ebsDeviceMonitor.getHistoricalSweatDataForPlot()
                                        val epochTime = ebsDeviceMonitor.getSweatDataLogStartEpochTime()
                                        var showHourChartXCount = 1
                                        if (sweatHistoricalData.isNotEmpty()) {
                                            val lastEpochTime =
                                                epochTime + sweatHistoricalData.last().timeStamp.toUInt()
                                            val startSessionHour = getChartSessionHour(epochTime)
                                            var currSessionHour = getChartSessionHour(lastEpochTime)

                                            //Log.d("startSessionHour", "$startSessionHour , $currSessionHour")

                                            if (currSessionHour < startSessionHour) {
                                                currSessionHour += 24
                                            }

                                            val diffTime = currSessionHour - startSessionHour
                                            showHourChartXCount = if (diffTime <= 1) {
                                                3
                                            } else if (currSessionHour - startSessionHour <= 4) {
                                                3
                                            } else if (currSessionHour - startSessionHour <= 8) {
                                                5
                                            } else if (currSessionHour - startSessionHour <= 12) {
                                                6
                                            } else {
                                                7
                                            }

                                        }

                                        AndroidView(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(end = 5.dp)
                                                .height(230.dp),
                                            factory = { context ->
                                                LineChart(context)
                                            },
                                            update = { lineChart ->
                                                var lineChartEntrySweatSamples: List<Entry>
                                                var lossDataSet: LineDataSet

                                                if (sweatElectrolyteDisplaySelection.value == 0) {

                                                    lineChartEntrySweatSamples =
                                                        sweatHistoricalData.map { historicalSweatData ->
                                                            Entry(
                                                                ((epochTime + historicalSweatData.timeStamp).toFloat()/* / 3600.0f*/),
                                                                chViewModel.userPrefsData.handleUserSweatConversionOz(
                                                                    historicalSweatData.sweatVolumeDeficitInOz * -1.0
                                                                ).toFloat()
                                                            )
                                                        }

                                                    lossDataSet = LineDataSet(
                                                        lineChartEntrySweatSamples,
                                                        chViewModel.userPrefsData.getUserSweatUnitString()
                                                    ).apply {
                                                        color = Color(0xFF2A9AD6).toArgb()
                                                        setDrawCircles(false)
                                                        lineWidth = 2F
                                                        setDrawHorizontalHighlightIndicator(false)
                                                        setDrawVerticalHighlightIndicator(false)
                                                    }
                                                } else {

                                                    lineChartEntrySweatSamples =
                                                        sweatHistoricalData.map { historicalSweatData ->
                                                            Entry(
                                                                ((epochTime + historicalSweatData.timeStamp).toFloat()/* / 3600.0f*/),
                                                                ((historicalSweatData.sweatSodiumDeficitInMg * -1.0).toFloat())
                                                            )
                                                        }

                                                    lossDataSet = LineDataSet(
                                                        lineChartEntrySweatSamples,
                                                        chViewModel.userPrefsData.getUserSodiumUnitString()
                                                    ).apply {
                                                        color = Color(0xFF684B92).toArgb()
                                                        setDrawCircles(false)
                                                        lineWidth = 2F
                                                        setDrawHorizontalHighlightIndicator(false)
                                                        setDrawVerticalHighlightIndicator(false)
                                                    }
                                                }

                                                var lineX = LineData(lossDataSet).apply { }
                                                lineX.setDrawValues(false)

                                                lineChart.apply {
                                                    lineData.apply {
                                                        setNoDataText(context.resources.getString(R.string.sensorinfo_loading))
                                                        setNoDataTextColor(Color.Blue.toArgb())
                                                        //setNoDataTextTypeface("Helvetica", size: 20.0)
                                                        //lineChart.setNoDataTextTypeface()
                                                        lineChart.getPaint(Chart.PAINT_INFO).textSize =
                                                            20f
                                                        lineChart.getPaint(Chart.PAINT_INFO).color =
                                                            Color.Blue.toArgb()
                                                        lineChart.data?.clearValues()
                                                    }
                                                    xAxis.apply {
                                                        position = XAxis.XAxisPosition.BOTTOM
                                                        setDrawGridLines(true)
                                                        setDrawLabels(timeChange.value != 2)
                                                        //setLabelCount(10, false)

                                                        axisMaximum =
                                                            if (sweatHistoricalData.size > 0) ((epochTime + sweatHistoricalData.last().timeStamp).toFloat()/* / 3600.0f*/ + 1800.0f) else 1.0f
                                                        valueFormatter = HistoryXAxisValueFormatter()
                                                        setLabelCount(showHourChartXCount, false)
                                                    }
                                                    axisLeft.apply {
                                                        setDrawLabels(true)
                                                        setDrawGridLines(true)
                                                        if (sweatElectrolyteDisplaySelection.value == 0) {
                                                            axisMinimum =
                                                                if (chViewModel.userPrefsData.getUnits().value == 1) -40.0f else -1000.0f
                                                            axisMaximum =
                                                                if (chViewModel.userPrefsData.getUnits().value == 1) 40.0f else 1000.0f

                                                            if (chViewModel.userPrefsData.getUnits().value == 1) {
                                                                if (lineX.yMax > 35.0) {
                                                                    axisMaximum =
                                                                        (((lineX.yMax / 10.0).roundToInt() + 2.0) * 10.0).toFloat()
                                                                    axisMinimum = -1.0F * axisMaximum
                                                                }
                                                                if (lineX.yMin < -35.0) {
                                                                    axisMinimum =
                                                                        (((lineX.yMin / 10.0).roundToInt() - 2.0) * 10.0).toFloat()
                                                                    axisMaximum = -1.0F * axisMinimum
                                                                }
                                                            } else {
                                                                if (lineX.yMax > 875.0) {
                                                                    axisMaximum =
                                                                        (((lineX.yMax / 250.0).roundToInt() + 2.0) * 250.0).toFloat()
                                                                    axisMinimum = -1.0F * axisMaximum

                                                                }
                                                                if (lineX.yMin < -875.0) {
                                                                    axisMinimum =
                                                                        (((lineX.yMin / 250.0).roundToInt() - 2.0) * 250.0).toFloat()
                                                                    axisMaximum = -1.0F * axisMinimum
                                                                }
                                                            }

                                                            setLabelCount(9, true)
                                                        } else {
                                                            axisMinimum = -1000.0f
                                                            axisMaximum = 1000.0f
                                                            if (lineX.yMax > 800.0) {
                                                                axisMaximum =
                                                                    (((lineX.yMax / 100.0).roundToInt() + 2.0) * 100.0).toFloat()
                                                                axisMinimum = -1.0F * axisMaximum
                                                            }
                                                            if (lineX.yMin < -800.0) {
                                                                axisMinimum =
                                                                    (((lineX.yMin / 100.0).roundToInt() - 2.0) * 100.0).toFloat()
                                                                axisMaximum = -1.0F * axisMinimum
                                                            }
                                                            setLabelCount(11, true)
                                                        }
                                                    }
                                                    axisRight.apply {
                                                        setDrawLabels(false)
                                                        setDrawGridLines(false)
                                                    }
                                                    legend.apply {
                                                        horizontalAlignment =
                                                            Legend.LegendHorizontalAlignment.CENTER
                                                    }
                                                    description.text = ""
                                                    //animation.duration = 2
                                                    data = lineX
                                                    notifyDataSetChanged()
                                                    invalidate()
                                                    if (!chViewModel._isSensorConnected.value) clearValues()
                                                }
                                            }
                                        )
                                    } else {
                                        HistoricalBarChart(
                                            chViewModel = chViewModel,
                                            ebsDeviceMonitor,
                                            sweatDisplayChange = sweatElectrolyteDisplaySelection.value,
                                            timeChange = timeChange.value
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

                                    if (timeChange.value == 0) {
                                        Text(
                                            stringResource(R.string.work_time_hr),
                                            Modifier.padding(
                                                start = 20.dp,
                                                top = 10.dp,
                                                bottom = 20.dp
                                            ),
                                            fontFamily = OswaldFonts,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = colorResource(R.color.grayStandardText)
                                        )
                                    }

                                    if (chViewModel.switchShareAnonymousDataEpicore) {
                                        SegmentedControl(
                                            items = timeList,
                                            cornerRadius = 20,
                                            defaultSelectedItemIndex = 0,
                                            color = R.color.insightMediumColor
                                        ) {
                                            //Log.d("HistorySweatSodiumToggle", "Selected item : ${timeList[it]}")
                                            timeChange.value = it
                                            viewTitle = timeList[it]

                                            if (it == 0) {
                                                plotHistoricalSweatData(chViewModel)
                                            } else {
                                                plotHistoricalSweatDataWeeklyOrMonthly(
                                                    chViewModel,
                                                    it
                                                )
                                            }
                                        }
                                    }

                                }   // Column - Week-Segment control
                            }

                        }
                    }   // Column
                }
            }

            if (!chViewModel.switchShareAnonymousDataEpicore) {

                if (chViewModel.isDemoOnboardingFlow.value) {
                    Text(
                        text = "No analysis available in demo mode.",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 10.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        maxLines = 2,
                        lineHeight = 50.sp,
                        fontFamily = OswaldFonts,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val placeholder = stringResource(R.string.epicore_share_settings)
                    val globalText = stringResource(R.string.epicore_share_disabled, placeholder)
                    val start = globalText.indexOf(placeholder)
                    val spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(textDecoration = TextDecoration.Underline),
                            start = start,
                            end = start + placeholder.length
                        )
                    )

                    if (isJapanese) {
                        Text(
                            stringResource(R.string.epicore_share_disabled),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 10.dp, end = 10.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .clickable {
                                    chViewModel.scrollEnableShareSettingsView = true
                                    onItemClick(items[3])
                                },
                            maxLines = 4,
                            lineHeight = 50.sp,
                            fontFamily = OswaldFonts,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = AnnotatedString(
                                text = globalText,
                                spanStyles = spanStyles
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 10.dp, end = 10.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .clickable {
                                    chViewModel.scrollEnableShareSettingsView = true
                                    onItemClick(items[3])
                                },
                            maxLines = 4,
                            lineHeight = 50.sp,
                            fontFamily = OswaldFonts,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    else {
        Scaffold(
            modifier = Modifier.fillMaxWidth()
                .blur(if (!chViewModel.switchShareAnonymousDataEpicore) 10.dp else 0.dp),
            topBar = {
                TopBarView(
                    chViewModel,
                    ebsDeviceMonitor,
                    navController,
                    updateHideBottomBar = { viewState ->
                        updateHideBottomBar(viewState)
                    })

                NotificationView(chViewModel)

            }
        ) {
            val sweatElectrolyteDisplaySelection = remember { mutableIntStateOf(0) }
            val timeChange = remember { mutableIntStateOf(0) }
            val sweatList =
                listOf(stringResource(R.string.title_water), stringResource(R.string.title_sodium))
            val timeList = listOf(
                stringResource(R.string.history_day),
                stringResource(R.string.week),
                stringResource(R.string.history_month)
            )
            var viewTitle by remember { mutableStateOf(timeList[0]) }

            BgStatusView(chViewModel, ebsDeviceMonitor)

            BoxWithConstraints {
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

                        Column {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        viewTitle,
                                        Modifier.padding(
                                            start = 20.dp,
                                            top = 10.dp,
                                            bottom = 20.dp
                                        ),
                                        fontFamily = OswaldFonts,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorResource(R.color.grayStandardText)
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    if (chViewModel.switchShareAnonymousDataEpicore) {
                                        SegmentedControl(
                                            items = sweatList,
                                            cornerRadius = 20,
                                            defaultSelectedItemIndex = 0,
                                            color = R.color.insightMediumColor
                                        ) {
                                            //Log.d("HistorySweatSodiumToggle", "Selected item : ${sweatList[it]}")
                                            sweatElectrolyteDisplaySelection.value = it
                                        }
                                    }
                                }
                            }   // Column - Sweat-Segment control

                            Column {
                                if (timeChange.value == 0) {
                                    // LineGraph
                                    val sweatHistoricalData =
                                        ebsDeviceMonitor.getHistoricalSweatDataForPlot()
                                    val epochTime = ebsDeviceMonitor.getSweatDataLogStartEpochTime()
                                    var showHourChartXCount = 1
                                    if (sweatHistoricalData.isNotEmpty()) {
                                        val lastEpochTime =
                                            epochTime + sweatHistoricalData.last().timeStamp.toUInt()
                                        val startSessionHour = getChartSessionHour(epochTime)
                                        var currSessionHour = getChartSessionHour(lastEpochTime)

                                        //Log.d("startSessionHour", "$startSessionHour , $currSessionHour")

                                        if (currSessionHour < startSessionHour) {
                                            currSessionHour += 24
                                        }

                                        val diffTime = currSessionHour - startSessionHour
                                        showHourChartXCount = if (diffTime <= 1) {
                                            3
                                        } else if (currSessionHour - startSessionHour <= 4) {
                                            3
                                        } else if (currSessionHour - startSessionHour <= 8) {
                                            5
                                        } else if (currSessionHour - startSessionHour <= 12) {
                                            6
                                        } else {
                                            7
                                        }

                                    }

                                    AndroidView(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 5.dp)
                                            .height(230.dp),
                                        factory = { context ->
                                            LineChart(context)
                                        },
                                        update = { lineChart ->
                                            var lineChartEntrySweatSamples: List<Entry>
                                            var lossDataSet: LineDataSet

                                            if (sweatElectrolyteDisplaySelection.value == 0) {

                                                lineChartEntrySweatSamples =
                                                    sweatHistoricalData.map { historicalSweatData ->
                                                        Entry(
                                                            ((epochTime + historicalSweatData.timeStamp).toFloat()/* / 3600.0f*/),
                                                            chViewModel.userPrefsData.handleUserSweatConversionOz(
                                                                historicalSweatData.sweatVolumeDeficitInOz * -1.0
                                                            ).toFloat()
                                                        )
                                                    }

                                                lossDataSet = LineDataSet(
                                                    lineChartEntrySweatSamples,
                                                    chViewModel.userPrefsData.getUserSweatUnitString()
                                                ).apply {
                                                    color = Color(0xFF2A9AD6).toArgb()
                                                    setDrawCircles(false)
                                                    lineWidth = 2F
                                                    setDrawHorizontalHighlightIndicator(false)
                                                    setDrawVerticalHighlightIndicator(false)
                                                }
                                            } else {

                                                lineChartEntrySweatSamples =
                                                    sweatHistoricalData.map { historicalSweatData ->
                                                        Entry(
                                                            ((epochTime + historicalSweatData.timeStamp).toFloat()/* / 3600.0f*/),
                                                            ((historicalSweatData.sweatSodiumDeficitInMg * -1.0).toFloat())
                                                        )
                                                    }

                                                lossDataSet = LineDataSet(
                                                    lineChartEntrySweatSamples,
                                                    chViewModel.userPrefsData.getUserSodiumUnitString()
                                                ).apply {
                                                    color = Color(0xFF684B92).toArgb()
                                                    setDrawCircles(false)
                                                    lineWidth = 2F
                                                    setDrawHorizontalHighlightIndicator(false)
                                                    setDrawVerticalHighlightIndicator(false)
                                                }
                                            }

                                            var lineX = LineData(lossDataSet).apply { }
                                            lineX.setDrawValues(false)

                                            lineChart.apply {
                                                lineData.apply {
                                                    setNoDataText(context.resources.getString(R.string.sensorinfo_loading))
                                                    setNoDataTextColor(Color.Blue.toArgb())
                                                    //setNoDataTextTypeface("Helvetica", size: 20.0)
                                                    //lineChart.setNoDataTextTypeface()
                                                    lineChart.getPaint(Chart.PAINT_INFO).textSize =
                                                        20f
                                                    lineChart.getPaint(Chart.PAINT_INFO).color =
                                                        Color.Blue.toArgb()
                                                    lineChart.data?.clearValues()
                                                }
                                                xAxis.apply {
                                                    position = XAxis.XAxisPosition.BOTTOM
                                                    setDrawGridLines(true)
                                                    setDrawLabels(timeChange.value != 2)
                                                    //setLabelCount(10, false)

                                                    axisMaximum =
                                                        if (sweatHistoricalData.size > 0) ((epochTime + sweatHistoricalData.last().timeStamp).toFloat()/* / 3600.0f*/ + 1800.0f) else 1.0f
                                                    valueFormatter = HistoryXAxisValueFormatter()
                                                    setLabelCount(showHourChartXCount, false)
                                                }
                                                axisLeft.apply {
                                                    setDrawLabels(true)
                                                    setDrawGridLines(true)
                                                    if (sweatElectrolyteDisplaySelection.value == 0) {
                                                        axisMinimum =
                                                            if (chViewModel.userPrefsData.getUnits().value == 1) -40.0f else -1000.0f
                                                        axisMaximum =
                                                            if (chViewModel.userPrefsData.getUnits().value == 1) 40.0f else 1000.0f

                                                        if (chViewModel.userPrefsData.getUnits().value == 1) {
                                                            if (lineX.yMax > 35.0) {
                                                                axisMaximum =
                                                                    (((lineX.yMax / 10.0).roundToInt() + 2.0) * 10.0).toFloat()
                                                                axisMinimum = -1.0F * axisMaximum
                                                            }
                                                            if (lineX.yMin < -35.0) {
                                                                axisMinimum =
                                                                    (((lineX.yMin / 10.0).roundToInt() - 2.0) * 10.0).toFloat()
                                                                axisMaximum = -1.0F * axisMinimum
                                                            }
                                                        } else {
                                                            if (lineX.yMax > 875.0) {
                                                                axisMaximum =
                                                                    (((lineX.yMax / 250.0).roundToInt() + 2.0) * 250.0).toFloat()
                                                                axisMinimum = -1.0F * axisMaximum

                                                            }
                                                            if (lineX.yMin < -875.0) {
                                                                axisMinimum =
                                                                    (((lineX.yMin / 250.0).roundToInt() - 2.0) * 250.0).toFloat()
                                                                axisMaximum = -1.0F * axisMinimum
                                                            }
                                                        }

                                                        setLabelCount(9, true)
                                                    } else {
                                                        axisMinimum = -1000.0f
                                                        axisMaximum = 1000.0f
                                                        if (lineX.yMax > 800.0) {
                                                            axisMaximum =
                                                                (((lineX.yMax / 100.0).roundToInt() + 2.0) * 100.0).toFloat()
                                                            axisMinimum = -1.0F * axisMaximum
                                                        }
                                                        if (lineX.yMin < -800.0) {
                                                            axisMinimum =
                                                                (((lineX.yMin / 100.0).roundToInt() - 2.0) * 100.0).toFloat()
                                                            axisMaximum = -1.0F * axisMinimum
                                                        }
                                                        setLabelCount(11, true)
                                                    }
                                                }
                                                axisRight.apply {
                                                    setDrawLabels(false)
                                                    setDrawGridLines(false)
                                                }
                                                legend.apply {
                                                    horizontalAlignment =
                                                        Legend.LegendHorizontalAlignment.CENTER
                                                }
                                                description.text = ""
                                                //animation.duration = 2
                                                data = lineX
                                                notifyDataSetChanged()
                                                invalidate()
                                                if (!chViewModel._isSensorConnected.value) clearValues()
                                            }
                                        }
                                    )
                                } else {
                                    HistoricalBarChart(
                                        chViewModel = chViewModel,
                                        ebsDeviceMonitor,
                                        sweatDisplayChange = sweatElectrolyteDisplaySelection.value,
                                        timeChange = timeChange.value
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

                                if (timeChange.value == 0) {
                                    Text(
                                        stringResource(R.string.work_time_hr),
                                        Modifier.padding(
                                            start = 20.dp,
                                            top = 10.dp,
                                            bottom = 20.dp
                                        ),
                                        fontFamily = OswaldFonts,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorResource(R.color.grayStandardText)
                                    )
                                }

                                if (chViewModel.switchShareAnonymousDataEpicore) {
                                    SegmentedControl(
                                        items = timeList,
                                        cornerRadius = 20,
                                        defaultSelectedItemIndex = 0,
                                        color = R.color.insightMediumColor
                                    ) {
                                        //Log.d("HistorySweatSodiumToggle", "Selected item : ${timeList[it]}")
                                        timeChange.value = it
                                        viewTitle = timeList[it]

                                        if (it == 0) {
                                            plotHistoricalSweatData(chViewModel)
                                        } else {
                                            plotHistoricalSweatDataWeeklyOrMonthly(chViewModel, it)
                                        }
                                    }
                                }

                            }   // Column - Week-Segment control
                        }

                    }
                }   // Column
            }
        }

        if (!chViewModel.switchShareAnonymousDataEpicore) {

            if (chViewModel.isDemoOnboardingFlow.value) {
                Text(
                    text = "No analysis available in demo mode.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    maxLines = 2,
                    lineHeight = 50.sp,
                    fontFamily = OswaldFonts,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            } else {
                val placeholder = stringResource(R.string.epicore_share_settings)
                val globalText = stringResource(R.string.epicore_share_disabled, placeholder)
                val start = globalText.indexOf(placeholder)
                val spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(textDecoration = TextDecoration.Underline),
                        start = start,
                        end = start + placeholder.length
                    )
                )

                if (isJapanese) {
                    Text(
                        stringResource(R.string.epicore_share_disabled),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 10.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .clickable {
                                chViewModel.scrollEnableShareSettingsView = true
                                onItemClick(items[3])
                            },
                        maxLines = 4,
                        lineHeight = 50.sp,
                        fontFamily = OswaldFonts,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = AnnotatedString(
                            text = globalText,
                            spanStyles = spanStyles
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 10.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .clickable {
                                chViewModel.scrollEnableShareSettingsView = true
                                onItemClick(items[3])
                            },
                        maxLines = 4,
                        lineHeight = 50.sp,
                        fontFamily = OswaldFonts,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Brief: Method to display historical sweat data on the appropriate LineChart object
fun plotHistoricalSweatData(chViewModel: ModelData) {
}

fun plotHistoricalSweatDataWeeklyOrMonthly(chViewModel: ModelData, selected: Int) {
    var numDays = 7

    if (selected == 2) {
        numDays = 30
    }

    chViewModel.weeklyOrMonthlyDataArray.clear()

    // Get the past 7 days of data
    val weeklyStatsData: List<DayIntakeLossData> = chViewModel.userHistoryStats?.data?.takeLast(numDays) ?: return
    weeklyStatsData.forEach { chViewModel.weeklyOrMonthlyDataArray.add(it) }
}

class HistoryXAxisValueFormatter() : IndexAxisValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return try {
            val convert = value.toLong()
            if (convert == 0L) {
                return ""
            }

            // Convert to Instant
            val instant = Instant.fromEpochSeconds(convert)

            // Convert to LocalDateTime in the system's default time zone
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            // Check the year to avoid "1969" or "1970" dates
            if (localDateTime.year == 1969 || localDateTime.year == 1970) {
                return ""
            }

            // Format the time as "hh:mm a" (e.g., "03:45 PM")
            val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            localDateTime.toJavaLocalDateTime().format(formatter)
        } catch (e: DateTimeException) {
            ""
        }
    }
}