package com.epicorebiosystems.rehydrate.tabViews.todayViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun PhysiologySkinTempView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, widthModifier: Dp) {
    val context = LocalContext.current
    var timeLeft by remember { mutableIntStateOf(5) }
    val lollipopClose = chViewModel.isSkinTempTimerLollipopClose.collectAsState().value

    var labelText = stringResource(R.string.skin_side_temp_chart)

    LaunchedEffect(key1 = lollipopClose) {
        while (lollipopClose) {
            delay(1000L)
            timeLeft--
        }
    }

    Column {
        val sweatHistoricalData = ebsDeviceMonitor.getHistoricalSweatDataForPlot()

        val epochTime = ebsDeviceMonitor.getSweatDataLogStartEpochTime()
        var showHourChartXCount = 1
        if (sweatHistoricalData.isNotEmpty()) {
            val lastEpochTime = epochTime + sweatHistoricalData.last().timeStamp.toUInt()
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
                .padding(start = 10.dp, end = 5.dp, bottom = 10.dp)
                .height(200.dp),
            factory = { context ->
                PhysiologySkinTempLineChart(context)
            },
            update = { lineChart ->
                // ************************
                // Handle Heat/Exertion Stress Chart

                var lineChartExertion: List<Entry> =
                    sweatHistoricalData.map { historicalSweatData ->
                        Entry(((epochTime + historicalSweatData.timeStamp).toFloat()), chViewModel.userPrefsData.getUserTemperatureC(historicalSweatData.bodyTemperatureSkinInC).toFloat(), historicalSweatData)
                    }

                var lineExertionTempX = LineDataSet(lineChartExertion, labelText + "(${chViewModel.userPrefsData.getUserTempUnitString()})").apply {
                    color = Color(0xFF6E8A9F).toArgb()
                    setDrawCircles(false)
                    lineWidth = 2F
                    setDrawHorizontalHighlightIndicator(false)
                    setDrawVerticalHighlightIndicator(true)
                    highLightColor = Color(0xFF476788).toArgb()
                    enableDashedHighlightLine(4f, 3f, 0f)
                    //mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                var factorsChartData = LineData().apply {
                    addDataSet(lineExertionTempX)
                }

                factorsChartData.setDrawValues(false)

                lineChart.notifyDataSetChanged()
                lineChart.invalidate()

                val mMarker = SkinTempLollipopMarker(context, chViewModel, epochTime)
                mMarker.chartView = lineChart
                lineChart.marker = mMarker

                // After a timer run this
                if (timeLeft <= 0) {
                    lineChart.highlightValue(null)
                    chViewModel._isSkinTempTimerLollipopClose.value = false
                    timeLeft = 5
                }

                lineChart.apply {
                    lineData.apply {
                        setNoDataText(context.resources.getString(R.string.sensorinfo_loading))
                        setNoDataTextColor(Color.Blue.toArgb())
                        //setNoDataTextTypeface("Helvetica", size: 20.0)
                        //lineChart.setNoDataTextTypeface()
                        lineChart.getPaint(Chart.PAINT_INFO).textSize = 20f
                        lineChart.getPaint(Chart.PAINT_INFO).color = Color.Blue.toArgb()
                        lineChart.data?.clearValues()
                    }
                    xAxis.apply {
                        setDrawGridLines(true)
                        position = XAxis.XAxisPosition.BOTTOM
//                            axisMaximum = if (sweatHistoricalData.size > 0) ((sweatHistoricalData.last().timeStamp).toFloat() / 3600.0f + 1.0f) else 1.0f
                        axisMaximum = ((factorsChartData.xMax.roundToInt() + 1800).toFloat())
                        valueFormatter = SkinTempXAxisValueFormatter()
                        setLabelCount(showHourChartXCount, false)
                    }
                    axisLeft.apply {
                        setDrawLabels(true)
                        setDrawGridLines(false)
                        clearSkinTempZones()
                        if (chViewModel.updateSecondTime >= 1) {
                            axisMaximum =
                                if (chViewModel.userPrefsData.getUnits().value == 1) 120f else 60f
                            axisMinimum =
                                if (chViewModel.userPrefsData.getUnits().value == 1) 60f else 20f
                            granularity =
                                if (chViewModel.userPrefsData.getUnits().value == 1) 20f else 10f
                            setLabelCount(if (chViewModel.userPrefsData.getUnits().value == 1) 4 else 5, true)
                        }

                        /*
                        Mapping:
                        Normal - green - <= 90F (need to convert to C if metric is set)
                        Moderate - yellow - 90F < x < 98.6F
                        High - red - >= 98.6F
                        */
                        val lowColor = ContextCompat.getColor(context, R.color.chart_low)
                        val moderateColor = ContextCompat.getColor(context, R.color.chart_moderate)
                        val highColor = ContextCompat.getColor(context, R.color.chart_high)

                        // Imperial
                        //lineChart.addSkinTempZone(PhysiologySkinTempLineChart.SkinTempZone(lowColor, Color(0xFF90BF70).toArgb(), 15, 60F, 90F, context.getString(R.string.activity_chart_low)))
                        //lineChart.addSkinTempZone(PhysiologySkinTempLineChart.SkinTempZone(moderateColor, Color(0xFFFFC103).toArgb(), 5, 90F, 98.6F, context.getString(R.string.activity_chart_moderate)))
                        //lineChart.addSkinTempZone(PhysiologySkinTempLineChart.SkinTempZone(highColor, Color(0xFFFF2E2E).toArgb(), 15, 98.6F, 120F, context.getString(R.string.activity_chart_high)))

                        if (chViewModel.updateSecondTime >= 1) {
                            // Metric
                            var minLowValue = 20F
                            var maxLowValue = 32.2F
                            var minModValue = 32.2F
                            var maxModValue = 37F
                            var minHighValue = 37F
                            var maxHighValue = 60F
                            if (chViewModel.userPrefsData.getUnits().value == 1) {
                                minLowValue = 60F
                                maxLowValue = 90F
                                minModValue = 90F
                                maxModValue = 98.6F
                                minHighValue = 98.6F
                                maxHighValue = 120F
                            }

                            lineChart.addSkinTempZone(
                                PhysiologySkinTempLineChart.SkinTempZone(
                                    lowColor,
                                    Color(0xFF90BF70).toArgb(),
                                    15,
                                    minLowValue,
                                    maxLowValue,
                                    context.getString(R.string.skin_temp_chart_normal)
                                )
                            )
                            lineChart.addSkinTempZone(
                                PhysiologySkinTempLineChart.SkinTempZone(
                                    moderateColor,
                                    Color(0xFFFFC103).toArgb(),
                                    5,
                                    minModValue,
                                    maxModValue,
                                    context.getString(R.string.skin_temp_chart_moderate)
                                )
                            )
                            lineChart.addSkinTempZone(
                                PhysiologySkinTempLineChart.SkinTempZone(
                                    highColor,
                                    Color(0xFFFF2E2E).toArgb(),
                                    15,
                                    minHighValue,
                                    maxHighValue,
                                    context.getString(R.string.skin_temp_chart_high)
                                )
                            )
                        }

                        chViewModel.updateSecondTime += 1
                    }
                    axisRight.apply {
                        setDrawLabels(false)
                        setDrawGridLines(false)
                    }
                    //legend.apply {
                    //    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    //    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    //}
                    description.text = ""
                    data = factorsChartData
                    isDragEnabled = true
                    setScaleEnabled(false)
                    notifyDataSetChanged()
                    invalidate()
                    if (!chViewModel._isSensorConnected.value) clearValues()
                }
            }
        )

        if (sweatHistoricalData.isNotEmpty()) {
            Text(stringResource(R.string.session_started) + " ${getChartSessionSessionStart(epochTime)}",
                modifier = Modifier.padding(top = 5.dp, start = 20.dp),
                fontFamily = RobotoRegularFonts,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText)
            )
        }
        else {
            Text(
                stringResource(R.string.session_started),
                modifier = Modifier.padding(top = 5.dp, start = 20.dp),
                fontFamily = RobotoRegularFonts,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left,
                color = colorResource(R.color.settingsColorCoalText)
            )
        }

    }
}

class PhysiologySkinTempLineChart : LineChart {
    private var mYAxisSafeZonePaint: Paint = Paint()
    private var textPaint: TextPaint = TextPaint()
    private var mSkinTempZones: MutableList<SkinTempZone> = ArrayList()
    private val tfJost = ResourcesCompat.getFont(context, R.font.jost_variablefont_wght)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )
/*
    override fun init() {
        super.init()
        //textPaint.isAntiAlias = true
        //textPaint.textSize = 20 * resources.displayMetrics.density
        //mYAxisSafeZonePaint.style = Paint.Style.FILL
    }
*/
    override fun onDraw(canvas: Canvas) {
        if (mSkinTempZones == null) {
            super.onDraw(canvas)
        }
        else {
            textPaint.isAntiAlias = true
            textPaint.textSize = 20 * resources.displayMetrics.density
            mYAxisSafeZonePaint.style = Paint.Style.FILL

            for (zone in mSkinTempZones) {
                // prepare coordinates
                val pts = FloatArray(4)
                pts[1] = zone.lowerLimit
                pts[3] = zone.upperLimit
                mLeftAxisTransformer.pointValuesToPixel(pts)

                // draw Sweat Intake background and text string
                mYAxisSafeZonePaint.color = zone.color
                canvas.drawRect(
                    mViewPortHandler.contentLeft(), pts[1], mViewPortHandler.contentRight(),
                    pts[3], mYAxisSafeZonePaint
                )

                textPaint.color = zone.textColor
                textPaint.textSize = 40f
                textPaint.typeface = tfJost

                //Log.d("height", "$height")
                //Log.d("width", "$width")

                canvas.drawText(zone.text, 80F, pts[1] - zone.textAdjust, textPaint)
            }
            super.onDraw(canvas)
        }
    }

    fun addSkinTempZone(skinTempZone: SkinTempZone) {
        mSkinTempZones.add(skinTempZone)
    }

    val skinTempZones: List<SkinTempZone>? get() = mSkinTempZones

    fun clearSkinTempZones() {
        mSkinTempZones = ArrayList()
    }

    class SkinTempZone(val color: Int, val textColor: Int, val textAdjust: Int, val lowerLimit: Float, val upperLimit: Float, val text: String)
}

class SkinTempXAxisValueFormatter() : IndexAxisValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return try {
            val convert = value.toLong()
            if (convert == 0L) {
                return ""
            }

            // Convert the value to Instant (epoch seconds)
            val instant = Instant.fromEpochSeconds(convert)

            // Convert Instant to LocalDateTime in the system's default time zone
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            // Check the year and return an empty string if it's 1969 or 1970
            if (localDateTime.year == 1969 || localDateTime.year == 1970) {
                return ""
            }

            // Format the time as "hh:mm a" (e.g., "03:45 PM") in the system's time zone
            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
            localDateTime.toJavaLocalDateTime().format(formatter)
        } catch (e: DateTimeException) {
            ""
        }
    }
}

class SkinTempLollipopMarker(context: Context, chViewModel: ModelData, epochTime: UInt) : MarkerView(context, R.layout.physiology_lollipop_layout_file)
{
    private val arrowSize = 35
    private val chViewModel = chViewModel
    private val epoch = epochTime

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val lossTv: TextView = this.findViewById(R.id.loss_tv)
            val dateTv: TextView = this.findViewById(R.id.time_tv)
            val lossTitleTv: TextView = this.findViewById(R.id.loss_title_tv)
            val lollipopLayout: LinearLayoutCompat = this.findViewById(R.id.lollipop_liner_layout)
            lollipopLayout.setBackgroundResource(R.drawable.sweat_intake_lollipop_shape)
            lossTitleTv.setTextColor(ContextCompat.getColor(context, R.color.skinTemp))
            lossTv.setTextColor(ContextCompat.getColor(context, R.color.skinTemp))
            lossTitleTv.text = context.getString(R.string.skin_side_temp_chart)
            (e.data as? HistoricalSweatDataPacket)?.let {
                lossTv.text = String.format("%.1f", chViewModel.userPrefsData.getUserTemperatureC(it.bodyTemperatureSkinInC))
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
        val height = height.toFloat()

        if (!chViewModel.isSkinTempTimerLollipopClose.value) {
            chViewModel._isSkinTempTimerLollipopClose.value = true
        }
        val offset = getOffsetForDrawingAtPoint(posX, posY)
        val saveId: Int = canvas.save()
        //canvas.translate(posX + offset.x, posY + offset.y)
        canvas.translate(posX + offset.x, height / 2)
        draw(canvas)
        canvas.restoreToCount(saveId)
    }

}