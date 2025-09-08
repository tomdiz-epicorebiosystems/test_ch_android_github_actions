package com.epicorebiosystems.rehydrate.tabViews.historyViews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun HistoricalBarChart(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, sweatDisplayChange: Int, timeChange: Int) {

    val context = LocalContext.current

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp),
        factory = { context ->
            BarChart(context)
        },
        update = { barChart ->
            var lossEntries: List<BarEntry>
            var intakeEntries: List<BarEntry>
            var lossDataSet: BarDataSet
            var intakeDataSet: BarDataSet

            if (sweatDisplayChange == 0) {
                lossEntries = chViewModel.weeklyOrMonthlyDataArray.mapIndexed { index, dataIntakeLoss ->
                    BarEntry(index.toFloat(), if (dataIntakeLoss.water_loss_ml != null) (chViewModel.userPrefsData.handleUserSweatConversionMl(dataIntakeLoss.water_loss_ml).toFloat()) else 0f)
                }
                intakeEntries = chViewModel.weeklyOrMonthlyDataArray.mapIndexed { index, dataIntakeLoss ->
                    BarEntry(index.toFloat(), if (dataIntakeLoss.water_intake_ml != null) (chViewModel.userPrefsData.handleUserSweatConversionMl(dataIntakeLoss.water_intake_ml).toFloat()) else 0f)
                }

                lossDataSet = BarDataSet(lossEntries, context.getString(R.string.history_chart_loss) + "(" + chViewModel.userPrefsData.getUserSweatUnitString() + ")").apply {
                    color = Color(0xFFADC9E0).toArgb()
                }
                intakeDataSet = BarDataSet(intakeEntries, context.getString(R.string.history_chart_intake) + "(" + chViewModel.userPrefsData.getUserSweatUnitString() + ")").apply {
                    color = Color(0xFF239BDA).toArgb()
                }
            }
            else {
                lossEntries = chViewModel.weeklyOrMonthlyDataArray.mapIndexed { index, dataIntakeLoss ->
                    BarEntry(index.toFloat(), if (dataIntakeLoss.sodium_loss_ml != null) dataIntakeLoss.sodium_loss_ml.toFloat() else 0f)
                }
                intakeEntries = chViewModel.weeklyOrMonthlyDataArray.mapIndexed { index, dataIntakeLoss ->
                    BarEntry(index.toFloat(), if (dataIntakeLoss.sodium_intake_ml != null) dataIntakeLoss.sodium_intake_ml.toFloat() else 0f)
                }

                val sweatHistoricalData = ebsDeviceMonitor.getHistoricalSweatDataForPlot()
                if (sweatHistoricalData.isNotEmpty()) {
                    lossEntries += BarEntry(6f, chViewModel.userPrefsData.handleUserSodiumConversion(sweatHistoricalData[0].sweatSodiumLossWholeBodyInMg).toFloat())
                    intakeEntries += BarEntry(6f, chViewModel.userPrefsData.handleUserSodiumConversion(sweatHistoricalData[0].sodiumTotalIntakeInMg).toFloat())
                }

                lossDataSet = BarDataSet(lossEntries, context.getString(R.string.history_chart_loss) + "(" + chViewModel.userPrefsData.getUserSodiumUnitString() + ")").apply {
                    color = Color(0xFFB3A2CE).toArgb()
                }
                intakeDataSet = BarDataSet(intakeEntries, context.getString(R.string.history_chart_intake) + "(" + chViewModel.userPrefsData.getUserSodiumUnitString() + ")").apply {
                    color = Color(0xFF6C4A97).toArgb()
                }
            }

            var barData = BarData(intakeDataSet, lossDataSet).apply {  }

            barData.setDrawValues(false)

            val xAxisFormatter = IndexAxisValueFormatter(
                chViewModel.weeklyOrMonthlyDataArray.map {
                    val monthDay = MonthDay.parse(it.date.takeLast(5), DateTimeFormatter.ofPattern("MM-dd"))
                    val formatter = DateTimeFormatter.ofPattern("MMM-dd")
                    monthDay.format(formatter)
                }
            )

            barChart.apply {
                barChart.apply {
                    setNoDataText(context.resources.getString(R.string.sensorinfo_loading))
                    setNoDataTextColor(Color.Blue.toArgb())
                    //setNoDataTextTypeface("Helvetica", size: 20.0)
                    //lineChart.setNoDataTextTypeface()
                    barChart.getPaint(Chart.PAINT_INFO).textSize = 20f
                    barChart.getPaint(Chart.PAINT_INFO).color = Color.Blue.toArgb()
                    barChart.data?.clearValues()
                }
                xAxis.apply {
                    var groupSpace = 0.10f
                    var barSpace = 0.01f
                    var barWidth = 0.44f
                    val startDate = 0f

                    valueFormatter = xAxisFormatter
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    setDrawLabels(true)
                    setCenterAxisLabels(true)
                    if (timeChange == 1) {
                        barData.barWidth = barWidth
                        barData.groupBars(startDate, groupSpace, barSpace)
                        val gg = barData.getGroupWidth(groupSpace, barSpace)
                        axisMinimum = startDate
                        axisMaximum = startDate + gg + 6

                        labelRotationAngle = 0f
                        setLabelCount(7, false)
                    }
                    else {
                        barData.barWidth = barWidth
                        barData.groupBars(startDate, groupSpace, barSpace)
                        val gg = barData.getGroupWidth(groupSpace, barSpace)
                        axisMinimum = startDate
                        axisMaximum = startDate + gg + 30

                        labelRotationAngle = -60.0f
                        setLabelCount(15, false)
                    }
                }
                axisLeft.apply {
                    setDrawLabels(true)
                    setDrawGridLines(true)
                    axisMinimum = 0.0f

                    // Set axisMaximum based on Water/Sodium selection, current unit.
                    if (sweatDisplayChange == 0) {

                        if (chViewModel.userPrefsData.getUnits().value == 0) {
                            axisMaximum = 1000.0f

                            if(barData.yMax > 800.0) {
                                axisMaximum = (((barData.yMax / 100.0).roundToInt() + 2.0) * 100.0).toFloat()
                            }

                            setLabelCount(11, true)

                        }

                        else {
                            axisMaximum = 40.0f

                            if (barData.yMax > 35.0) {
                                axisMaximum =
                                    (((barData.yMax / 10.0).roundToInt() + 2.0) * 10.0).toFloat()
                            }

                            setLabelCount(9, true)

                        }

                    }

                    else {
                        axisMaximum = 1000.0f

                        if(barData.yMax > 800.0) {
                            axisMaximum = (((barData.yMax / 100.0).roundToInt() + 2.0) * 100.0).toFloat()
                        }

                        setLabelCount(11, true)
                    }

                }
                axisRight.apply {
                    setDrawLabels(false)
                    setDrawGridLines(false)
                }
                legend.apply {
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    extraBottomOffset = 5.0f
                }
                description.text = ""
                //animation.duration = 1
                data = barData
                notifyDataSetChanged()
                invalidate()
            }
        }
    )
}