package com.epicorebiosystems.rehydrate.tabViews.todayViews

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.epicorebiosystems.rehydrate.ui.theme.TenByEightRegularFont
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

// Helper to convert hex strings to Compose Color
fun hexToColor(hex: String): Color {
    return Color(AndroidColor.parseColor(hex))
}

// Custom color definitions similar to your SummaryChartColors enum.
object SummaryChartColors {
    val inactiveGray = hexToColor("#B7B7B7")
    val lightGreen = hexToColor("#466888")
    val moderateYellow = hexToColor("#D5BB59")
    val highRed = hexToColor("#A4302B")
}

// Define a default text color (adjust as needed)
val grayStandardText = hexToColor("#333333")

// Data class equivalent to Swift's Entry
data class Entry(
    //val id: String = java.util.UUID.randomUUID().toString(),
    val label: String,
    val percentage: Int
)

// The main Composable equivalent to WorkDaySummaryView
@Composable
fun WorkDaySummaryView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, widthModifier: Dp) {

    val context = LocalContext.current
    var alarmCount by remember { mutableStateOf(ebsDeviceMonitor.getAlertStatus()) }

    val minMaxActivityCounts: List<Pair<Float, Float>> = listOf(
        0.0f to 16.9f,
        17.0f to 29.9f,
        30.0f to 34.9f,
        35.0f to 45.0f
    )

    // Values are only in F skinTempSChart array
    val minMaxTemps: List<Pair<Float, Float>> = listOf(
        60.0f to 89.9f,
        90.0f to 98.6f,
        98.7f to 120.0f
    )

    var activityData: List<Entry> = emptyList()
    var skinTempData: List<Entry> = emptyList()

    val sweatHistoricalData = ebsDeviceMonitor.getHistoricalSweatDataForPlot()
    if (sweatHistoricalData.isNotEmpty()) {

        val activityCounts: List<Float> = sweatHistoricalData.map { it.activityCounts.toFloat() }
        val skinTemps: List<Float> = sweatHistoricalData.map { (it.bodyTemperatureSkinInC.toFloat() * 1.8f + 32.0f) }

        val activityPercentages: List<Float> = calculateBucketPercentages(data = activityCounts, minMaxBuckets = minMaxActivityCounts)
        val skinTempPercentages: List<Float> = calculateBucketPercentages(data = skinTemps, minMaxBuckets = minMaxTemps)

        activityData = listOf(
            Entry(label = context.getString(R.string.summary_view_very_low), percentage = activityPercentages[0].toInt()),
            Entry(label = context.getString(R.string.summary_view_very_light), percentage = activityPercentages[1].toInt()),
            Entry(label = context.getString(R.string.summary_view_very_moderate), percentage = activityPercentages[2].toInt()),
            Entry(label = context.getString(R.string.summary_view_very_intense), percentage = activityPercentages[3].toInt())
        )

        skinTempData = listOf(
            Entry(label = context.getString(R.string.summary_view_temp_normal), percentage = skinTempPercentages[0].toInt()),
            Entry(label = context.getString(R.string.summary_view_temp_moderate), percentage = skinTempPercentages[1].toInt()),
            Entry(label = context.getString(R.string.summary_view_temp_high), percentage = skinTempPercentages[2].toInt())
        )
    }

    Box(
        Modifier
            .height(340.dp)
            .width(widthModifier)
            .offset(x = 10.dp, y = 180.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
    ) {
        // Outer container with background and rounded corners
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Title
            Text(
                text = stringResource(R.string.summary_view_title),
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 10.dp, start = 20.dp),
                color = grayStandardText
            )

            // Row for Duration and Alarms
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Duration Column
                Column(
                    modifier = Modifier.padding(end = 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.summary_view_duration_hr_min),
                        fontSize = 16.sp,
                        fontFamily = OswaldFonts,
                        fontWeight = FontWeight.Normal,
                        color = grayStandardText
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_access_time_filled_24),
                            contentDescription = "image_clock",
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession)
                                generateTimeStringFromTimeStamp(ebsDeviceMonitor.getCurrentRecordingDuration().toInt())
                            else "00:00",
                            fontSize = 36.sp,
                            fontFamily = TenByEightRegularFont,
                            fontWeight = FontWeight.Normal,
                            color = grayStandardText
                        )
                    }
                }
                // Alarms Column
                Column(
                    modifier = Modifier.padding(start = 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.summary_view_alarms),
                        fontSize = 16.sp,
                        fontFamily = OswaldFonts,
                        fontWeight = FontWeight.Normal,
                        color = grayStandardText
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_alert_dehydrated),
                            contentDescription = "image_alarm",
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(Color.Black)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (chViewModel.isUserSessionToDisplay && chViewModel.isCurrentUserSession)
                                "$alarmCount"
                            else "0",
                            fontSize = 36.sp,
                            fontFamily = TenByEightRegularFont,
                            fontWeight = FontWeight.Normal,
                            color = grayStandardText
                        )
                    }
                }
            }

            // Activity Bar Chart Section
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(R.string.summary_view_activity_level),
                        fontSize = 16.sp,
                        fontFamily = OswaldFonts,
                        fontWeight = FontWeight.Normal,
                        color = grayStandardText
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Image(
                        painter = painterResource(id = R.drawable.indicator_activity),
                        contentDescription = "image_activity_indicator",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                if (activityData.isNotEmpty() && activityData.size >= 4) {
                    Column {
                        val segments = listOf(activityData[0].percentage, activityData[1].percentage, activityData[2].percentage, activityData[3].percentage)
                        val colors = listOf(SummaryChartColors.inactiveGray, SummaryChartColors.lightGreen, SummaryChartColors.moderateYellow, SummaryChartColors.highRed)
                        SingleSegmentedLineChart(segments = segments, segmentColors = colors)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("${activityData[0].label} ${activityData[0].percentage}%",
                                fontSize = 14.sp,
                                fontFamily = OswaldFonts,
                                fontWeight = FontWeight.Light,
                                color = SummaryChartColors.inactiveGray
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("${activityData[1].label} ${activityData[1].percentage}%",
                                fontSize = 14.sp,
                                fontFamily = OswaldFonts,
                                fontWeight = FontWeight.Light,
                                color = SummaryChartColors.lightGreen,
                                modifier = Modifier.padding(horizontal = 5.dp)
                            )
                            Text("${activityData[2].label} ${activityData[2].percentage}%",
                                fontSize = 14.sp,
                                fontFamily = OswaldFonts,
                                fontWeight = FontWeight.Light,
                                color = SummaryChartColors.moderateYellow,
                                modifier = Modifier.padding(horizontal = 5.dp)
                            )
                            Text("${activityData[3].label} ${activityData[3].percentage}%",
                                fontSize = 14.sp,
                                fontFamily = OswaldFonts,
                                fontWeight = FontWeight.Light,
                                color = SummaryChartColors.highRed
                            )
                        }
                    }
                } else {
                    SingleSegmentedLineChart(segments = listOf(100), segmentColors = listOf(Color.Gray))
                }
            }

            // Skin Temperature Bar Chart Section
            Column {
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(R.string.summary_view_skin_temp) + "(${chViewModel.userPrefsData.getUserTempUnitString()})",
                        fontSize = 16.sp,
                        fontFamily = OswaldFonts,
                        fontWeight = FontWeight.Normal,
                        color = grayStandardText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.indicator_temperature),
                        contentDescription = "image_emperature_indicator",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                if (skinTempData.isNotEmpty() && skinTempData.size >= 3) {
                    Column {
                        val segments = listOf(
                            skinTempData[0].percentage,
                            skinTempData[1].percentage,
                            skinTempData[2].percentage,
                        )
                        val colors = listOf(
                            SummaryChartColors.lightGreen,
                            SummaryChartColors.moderateYellow,
                            SummaryChartColors.highRed
                        )
                        SingleSegmentedLineChart(segments = segments, segmentColors = colors)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("${skinTempData[0].label} ${skinTempData[0].percentage}%",
                                fontSize = 14.sp,
                                fontFamily = OswaldFonts,
                                fontWeight = FontWeight.Light,
                                color = SummaryChartColors.lightGreen
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("${skinTempData[1].label} ${skinTempData[1].percentage}%",
                                fontSize = 14.sp,
                                fontFamily = OswaldFonts,
                                fontWeight = FontWeight.Light,
                                color = hexToColor("#FFC103"),
                                modifier = Modifier.padding(horizontal = 5.dp)
                            )
                            Text("${skinTempData[2].label} ${skinTempData[2].percentage}%",
                                fontSize = 14.sp,
                                fontFamily = OswaldFonts,
                                fontWeight = FontWeight.Light,
                                color = SummaryChartColors.highRed
                            )
                        }
                    }
                } else {
                    SingleSegmentedLineChart(segments = listOf(100), segmentColors = listOf(Color.Gray))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SingleSegmentedLineChart(
    segments: List<Int>,        // Each segment is a percentage (0 to 100)
    segmentColors: List<Color>? = null,
    defaultColor: Color = Color.Blue,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth().height(30.dp).padding(start = 10.dp, end = 10.dp)) {
        if (segments.isEmpty()) return@Canvas

        // Define the bar's thickness as 35% of the canvas height.
        val barHeight = size.height * 0.35f
        // Center the bar vertically.
        val top = (size.height - barHeight) / 2
        val bottom = top + barHeight

        // Use half of the bar height as the corner radius.
        val radius = barHeight / 2

        var startX = 0f

        segments.forEachIndexed { index, percentage ->
            // Clamp percentage to between 0 and 100.
            val clampedPercentage = percentage.coerceIn(0, 100)
            // Compute the width for this segment.
            val segmentWidth = (clampedPercentage / 100f) * size.width

            val currentColor = segmentColors?.getOrNull(index) ?: defaultColor

            // Create an array for corner radii in the order:
            // top-left, top-right, bottom-right, bottom-left (2 values each = 8 values total)
            val radii = FloatArray(8) { 0f }
            if (index == 0) {
                // For the first segment, round the left side.
                radii[0] = radius  // top-left x
                radii[1] = radius  // top-left y
                radii[6] = radius  // bottom-left x
                radii[7] = radius  // bottom-left y
            }
            if (index == segments.lastIndex) {
                // For the last segment, round the right side.
                radii[2] = radius  // top-right x
                radii[3] = radius  // top-right y
                radii[4] = radius  // bottom-right x
                radii[5] = radius  // bottom-right y
            }

            // Define the rectangle for the current segment.
            val rect = Rect(
                left = startX,
                top = top,
                right = startX + segmentWidth,
                bottom = bottom
            )

            // Create a path for the rectangle with custom rounded corners.
            // The addRoundRect() function will use the provided radii.
            val path = Path().apply {
                addRoundRect(rect, radii)
            }

            drawPath(path = path, color = currentColor)

            startX += segmentWidth
        }
    }
}

/**
 * Extension function for Path to add a rounded rectangle.
 * @param rect The rectangle defining the bounds.
 * @param radii An array of 8 float values defining the radii for each corner in the following order:
 * [top-left x, top-left y, top-right x, top-right y, bottom-right x, bottom-right y, bottom-left x, bottom-left y]
 */
fun Path.addRoundRect(rect: Rect, radii: FloatArray) {
    if (radii.size != 8) {
        throw IllegalArgumentException("Radii array must have exactly 8 values")
    }
    val left = rect.left
    val top = rect.top
    val right = rect.right
    val bottom = rect.bottom

    val tlRx = radii[0]
    val tlRy = radii[1]
    val trRx = radii[2]
    val trRy = radii[3]
    val brRx = radii[4]
    val brRy = radii[5]
    val blRx = radii[6]
    val blRy = radii[7]

    reset()
    // Start at top-left (taking the top-left radius into account)
    moveTo(left + tlRx, top)
    // Top edge
    lineTo(right - trRx, top)
    // Top-right corner arc
    if (trRx > 0f && trRy > 0f) {
        arcTo(
            rect = Rect(
                left = right - 2 * trRx,
                top = top,
                right = right,
                bottom = top + 2 * trRy
            ),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
    } else {
        lineTo(right, top)
    }
    // Right edge
    lineTo(right, bottom - brRy)
    // Bottom-right corner arc
    if (brRx > 0f && brRy > 0f) {
        arcTo(
            rect = Rect(
                left = right - 2 * brRx,
                top = bottom - 2 * brRy,
                right = right,
                bottom = bottom
            ),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
    } else {
        lineTo(right, bottom)
    }
    // Bottom edge
    lineTo(left + blRx, bottom)
    // Bottom-left corner arc
    if (blRx > 0f && blRy > 0f) {
        arcTo(
            rect = Rect(
                left = left,
                top = bottom - 2 * blRy,
                right = left + 2 * blRx,
                bottom = bottom
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
    } else {
        lineTo(left, bottom)
    }
    // Left edge
    lineTo(left, top + tlRy)
    // Top-left corner arc
    if (tlRx > 0f && tlRy > 0f) {
        arcTo(
            rect = Rect(
                left = left,
                top = top,
                right = left + 2 * tlRx,
                bottom = top + 2 * tlRy
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
    } else {
        lineTo(left, top)
    }
    close()
}

// Helper function to format a time value (in seconds) to "HH:MM" string
private fun generateTimeStringFromTimeStamp(timeStampInSeconds: Int): String {
    val hourString = String.format("%02d", (timeStampInSeconds / 3600))
    val minString = String.format("%02d", (timeStampInSeconds / 60) % 60)
    return "$hourString:$minString"
}

// Function to calculate bucket percentages, similar to the Swift version.
fun calculateBucketPercentages(
    data: List<Float>,
    minMaxBuckets: List<Pair<Float, Float>>
): List<Float> {
    // Initialize bucket counts.
    val bucketCounts = MutableList(minMaxBuckets.size) { 0 }

    data.forEach { temperature ->
        minMaxBuckets.forEachIndexed { index, bucket ->
            val (minTemp, maxTemp) = bucket
            if (temperature in minTemp..maxTemp) {
                bucketCounts[index] += 1
                return@forEachIndexed
            }
        }
    }

    val totalTemperatures = data.size
    if (totalTemperatures == 0) {
        return List(minMaxBuckets.size) { 0f }
    }

    // Compute raw percentages and round them.
    var bucketPercentages = bucketCounts.map { count ->
        (count.toFloat() / totalTemperatures.toFloat()) * 100f
    }.map { it.roundToInt().toFloat() }

    val totalRounded = bucketPercentages.sum()
    val difference = 100f - totalRounded

    // Adjust the largest bucket to ensure the total sums to exactly 100%.
    val maxIndex = bucketPercentages.indices.maxByOrNull { bucketPercentages[it] } ?: 0
    val adjustedList = bucketPercentages.toMutableList()
    adjustedList[maxIndex] += difference

    return adjustedList
}