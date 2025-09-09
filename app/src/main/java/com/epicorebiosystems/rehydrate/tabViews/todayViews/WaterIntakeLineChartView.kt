package com.epicorebiosystems.rehydrate.tabViews.todayViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.TextPaint
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.getChartDateTime
import com.epicorebiosystems.rehydrate.modelData.getChartSessionHour
import com.epicorebiosystems.rehydrate.modelData.getChartSessionSessionStart
import com.epicorebiosystems.rehydrate.nordicsemi.uart.data.HistoricalSweatDataPacket
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.DateTimeException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WaterIntakeLineChartView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, widthModifier: Dp) {

    val context = LocalContext.current
    var timeLeft by remember { mutableIntStateOf(5) }
    val lollipopClose = chViewModel.isSweatTimerLollipopClose.collectAsState().value

    LaunchedEffect(key1 = lollipopClose) {
        while (lollipopClose) {
            delay(1000L)
            timeLeft--
        }
    }

    Column {

        val historicalSweatData = ebsDeviceMonitor.getHistoricalSweatDataForPlot()
        val epochTime: UInt = ebsDeviceMonitor.getSweatDataLogStartEpochTime()
        var showHourChartXCount = 1
        if (historicalSweatData.isNotEmpty()) {
            val lastEpochTime = epochTime + historicalSweatData.last().timeStamp.toUInt()
            val startSessionHour = getChartSessionHour(epochTime)
            var currSessionHour = getChartSessionHour(lastEpochTime)

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
                .padding(start = 10.dp, end = 5.dp, bottom = 10.dp)
                .height(220.dp),
            factory = { context ->
                WaterIntakeLineChart(context)
            },
            update = { lineChart ->
                // Defining chart line arrays
                val factorsChartData: LineData

                val lineChartEntrySweatSamples: MutableList<Entry> =
                    historicalSweatData.map { sweatSampleData ->
                        Entry(((epochTime + sweatSampleData.timeStamp).toFloat()), chViewModel.userPrefsData.handleUserSweatConversion(((sweatSampleData.sweatVolumeDeficitInOz * -1.0))).toFloat(), sweatSampleData)
                    }.toMutableList()

                val lineX: LineDataSet = LineDataSet(lineChartEntrySweatSamples, chViewModel.userPrefsData.getUserSweatUnitString()).apply {
                    color = Color.Blue.toArgb()
                    setDrawCircles(false)
                    lineWidth = 2F
//                  mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                //Log.d("CHART_END", "${lineChartEntrySweatSamples.size}")

                lineX.setDrawHorizontalHighlightIndicator(false)
                lineX.setDrawVerticalHighlightIndicator(true)
                lineX.highLightColor = Color(0xFF476788).toArgb()
                lineX.enableDashedHighlightLine(4f, 3f, 0f)

                factorsChartData = LineData(lineX).apply { }
                factorsChartData.setDrawValues(false)

                lineChart.notifyDataSetChanged()
                lineChart.invalidate()

                val mMarker = WaterIntakeLollipopMarker(context, chViewModel, epochTime)
                mMarker.chartView = lineChart
                lineChart.marker = mMarker

                // After a timer run this
                if (timeLeft <= 0) {
                    lineChart.highlightValue(null)
                    chViewModel._isSweatTimerLollipopClose.value = false
                    timeLeft = 5
                }

                lineChart.apply {
                    lineData.apply {
                        setNoDataText(context.resources.getString(R.string.sensorinfo_loading))
                        setNoDataTextColor(Color.Blue.toArgb())
                        lineChart.getPaint(Chart.PAINT_INFO).textSize = 20f
                        lineChart.getPaint(Chart.PAINT_INFO).color = Color.Blue.toArgb()
                    }
                    xAxis.apply {
                        setDrawGridLines(true)
                        position = XAxis.XAxisPosition.BOTTOM
                        // Expand x-axis with more data.
                        axisMaximum = ((factorsChartData.xMax.roundToInt() + 1800).toFloat())
                        valueFormatter = WaterIntakeXAxisValueFormatter()
                        setLabelCount(showHourChartXCount, false)
                    }
                    axisLeft.apply {
                        setDrawLabels(true)
                        setDrawGridLines(true)
                        clearSweatIntakeZones()
                        axisMinimum = if (chViewModel.userPrefsData.getUnits().value == 1) -40.0f else -1000.0f
                        axisMaximum = if (chViewModel.userPrefsData.getUnits().value == 1) 40.0f else 1000.0f

                        if (chViewModel.userPrefsData.getUnits().value == 1) {
                            if(factorsChartData.yMax > 35.0) {
                                axisMaximum = (((factorsChartData.yMax / 10.0).roundToInt() + 2.0) * 10.0).toFloat()
                                axisMinimum = -1.0F * axisMaximum
                            }
                            if(factorsChartData.yMin < -35.0) {
                                axisMinimum = (((factorsChartData.yMin / 10.0).roundToInt() - 2.0) * 10.0).toFloat()
                                axisMaximum = -1.0F * axisMinimum
                            }
                        }
                        else {
                            if(factorsChartData.yMax > 875.0) {
                                axisMaximum = (((factorsChartData.yMax / 250.0).roundToInt() + 2.0) * 250.0).toFloat()
                                axisMinimum = -1.0F * axisMaximum

                            }
                            if(factorsChartData.yMin < -875.0) {
                                axisMinimum = (((factorsChartData.yMin / 250.0).roundToInt() - 2.0) * 250.0).toFloat()
                                axisMaximum = -1.0F * axisMinimum
                            }
                        }
                        setLabelCount(9, true)

                        val rangeHigh = axisMaximum
                        val rangeLow = 0f

                        lineChart.addSweatIntakeZone(
                            WaterIntakeLineChart.SweatIntakeZone(
                                Color(0xFFEBF5FC).toArgb(),
                                rangeLow,
                                rangeHigh,
                                context.getString(R.string.hydrated_chart)
                            )
                        )

                        updateUnitSelection(chViewModel.currentUnits.value)
                    }
                    axisRight.apply {
                        setDrawLabels(false)
                        setDrawGridLines(false)
                    }
                    //axisRight.apply {
                    //}
                    description.text = ""
                    data = factorsChartData
                    isDragEnabled = true
                    setScaleEnabled(false)
//                        lineChartEntrySweatSamples.clear()
//                        notifyDataSetChanged()
//                        invalidate()
                    if (!chViewModel._isSensorConnected.value) {
                        clearValues()
                        ebsDeviceMonitor.clearHistoricalDataSet()
                    }
                }
            }
        )

        if (historicalSweatData.isNotEmpty()) {
            Text(stringResource(R.string.session_started) + " ${getChartSessionSessionStart(epochTime)}",
                modifier = Modifier
                    .padding(start = 20.dp)
                    .offset(y = -(5).dp),
                fontFamily = RobotoRegularFonts,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText)
            )
        }
        else {
            Text(stringResource(R.string.session_started),
                modifier = Modifier
                    .padding(start = 20.dp)
                    .offset(y = -(5).dp),
                fontFamily = RobotoRegularFonts,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText)
            )
        }
    }
}

class WaterIntakeXAxisValueFormatter() : IndexAxisValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return try {
            val convert = value.toLong()
            if (convert == 0L) {
                return ""
            }

            // Convert to Instant using epoch seconds
            val instant = Instant.fromEpochSeconds(convert)

            // Convert to LocalDateTime in the system's default time zone
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            // Check the year to avoid "1969" or "1970" dates
            if (localDateTime.year == 1969 || localDateTime.year == 1970) {
                return ""
            }

            // Format the time as "hh:mm a" (e.g., "03:45 PM") in the system's time zone
            val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            return localDateTime.toJavaLocalDateTime().format(formatter)
        } catch (e: DateTimeException) {
            ""
        }
    }
}

class WaterIntakeLineChart : LineChart {
    private var mYAxisSafeZonePaint = Paint()
    private var textPaint = TextPaint()
    private var mSweatIntakeZones: MutableList<SweatIntakeZone> = ArrayList()
    private var selection = 0
    private var unitSelection = 0
    private val tfJost = ResourcesCompat.getFont(context, R.font.jost_variablefont_wght)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onDraw(canvas: Canvas) {
        if (mSweatIntakeZones == null) {
            super.onDraw(canvas)
        }
        else {
            textPaint.isAntiAlias = true
            textPaint.textSize = 20 * resources.displayMetrics.density
            mYAxisSafeZonePaint.style = Paint.Style.FILL

            for (zone in mSweatIntakeZones!!) {
                // prepare coordinates
                val pts = FloatArray(4)
                pts[1] = zone.lowerLimit
                pts[3] = zone.upperLimit
                mLeftAxisTransformer.pointValuesToPixel(pts)

                // draw Sweat Intake background and text string
                mYAxisSafeZonePaint?.color = zone.color
                canvas.drawRect(
                    mViewPortHandler.contentLeft(), pts[1], mViewPortHandler.contentRight(),
                    pts[3], mYAxisSafeZonePaint
                )

                textPaint.color = Color(0xFFACC8E0).toArgb()
                textPaint.textSize = 40f
                textPaint.typeface = tfJost

                //Log.d("height", "$height")
                //Log.d("width", "$width")

                var textXLocation = pts[1] + 70
                if (height > 500) {
                    textXLocation = pts[1] + 120
                } else if (height > 400) {
                    textXLocation = pts[1] + 90
                }

                var textYLocation = -115f
                if (width > 800) {
                    textYLocation = -115f
                } else if (width > 500) {
                    textYLocation = -80f
                }

                val path = Path()
                path.moveTo(pts[1], height.toFloat() + 45)
                path.lineTo(pts[1], 0f)
                //canvas.drawTextOnPath(zone.text, path, pts[1] + 50, if (selection == 0) -100f else -80f, textPaint!!)
                //            canvas.drawTextOnPath(mSweatIntakeZones!!.text, path, textXLocation, if (selection == 0) textYLocation else -90f, textPaint!!)
                canvas.drawTextOnPath(
                    zone.text,
                    path,
                    textXLocation,
                    if ((unitSelection == 1) && (selection == 0)) (textYLocation - 30f) else textYLocation,
                    textPaint
                )
            }
            super.onDraw(canvas)
        }
    }

    fun addSweatIntakeZone(sweatIntakeZone: SweatIntakeZone) {
        mSweatIntakeZones.add(sweatIntakeZone)
    }

    val sweatIntakeZones: List<SweatIntakeZone>? get() = mSweatIntakeZones

    fun clearSweatIntakeZones() {
        mSweatIntakeZones = ArrayList()
    }

    fun updateSelection(sel: Int) {
        selection = sel
    }

    fun updateUnitSelection(sel: Int) {
        unitSelection = sel
    }

    class SweatIntakeZone(val color: Int, val lowerLimit: Float, val upperLimit: Float, val text: String)
}


class WaterIntakeLollipopMarker(context: Context, chViewModel: ModelData, epochTime: UInt) : MarkerView(context, R.layout.sweat_intake_lollipop_layout_file)
{
    private val arrowSize = 35
    private val chViewModel = chViewModel
     private val epoch = epochTime

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val lossTv: TextView = this.findViewById(R.id.loss_tv)
            val consumedTv: TextView = this.findViewById(R.id.consumed_tv)
            val dateTv: TextView = this.findViewById(R.id.time_tv)
            val lossTitleTv: TextView = this.findViewById(R.id.loss_title_tv)
            val consumedTitleTv: TextView = this.findViewById(R.id.consumed_title_tv)
            val lollipopLayout: LinearLayoutCompat = this.findViewById(R.id.lollipop_liner_layout)
            lollipopLayout.setBackgroundResource(R.drawable.sweat_intake_lollipop_shape)
            lossTitleTv.setTextColor(ContextCompat.getColor(context, R.color.intakeChartLollipopColor))
            consumedTitleTv.setTextColor(ContextCompat.getColor(context, R.color.intakeChartLollipopColor))
            lossTv.setTextColor(ContextCompat.getColor(context, R.color.intakeChartLollipopColor))
            consumedTitleTv.setTextColor(ContextCompat.getColor(context, R.color.intakeChartLollipopColor))
            lossTitleTv.text = context.resources.getString(R.string.water_loss_intake)
            consumedTitleTv.text = context.getString(R.string.water_consumed)
            (e.data as? HistoricalSweatDataPacket)?.let {
                lossTv.text = String.format(if (chViewModel.userPrefsData.getUnits().value == 1) "%.1f" else "%.0f", chViewModel.userPrefsData.handleUserSweatConversionOz(it.sweatVolumeLossWholeBodyInOz))
                consumedTv.text = String.format(if (chViewModel.userPrefsData.getUnits().value == 1) "%.1f" else "%.0f", chViewModel.userPrefsData.handleUserSweatConversionOz(it.fluidTotalIntakeInOz))
                it.timeStamp?.let { time ->
                    val instant = Instant.fromEpochMilliseconds(getChartDateTime(epoch, time).time)
                    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                    dateTv.text = localDateTime.toJavaLocalDateTime().atZone(ZoneId.systemDefault()).format(formatter)
                    dateTv.setBackgroundColor(ContextCompat.getColor(context, R.color.intakeChartLollipopColor))
                }
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        val offset = offset
        val chart = chartView
        val width = width.toFloat()
        val height = height.toFloat()

        if (posY <= height + arrowSize) {
            offset.y = arrowSize.toFloat()
        } else {
            offset.y = -height - arrowSize
        }

        val chartThird = chart.width / 3
        if (posX < chartThird) {
            offset.x = -width / 6
        } else if (posX < chartThird * 2) {
            offset.x = -width / 2
        } else {
            offset.x = -width
        }

        return offset
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {

        if (!chViewModel.isSweatTimerLollipopClose.value) {
            chViewModel._isSweatTimerLollipopClose.value = true
        }

        val offset = getOffsetForDrawingAtPoint(posX, posY)
        val saveId: Int = canvas.save()
        canvas.translate(posX + offset.x, posY + offset.y)
        draw(canvas)
        canvas.restoreToCount(saveId)
    }

}