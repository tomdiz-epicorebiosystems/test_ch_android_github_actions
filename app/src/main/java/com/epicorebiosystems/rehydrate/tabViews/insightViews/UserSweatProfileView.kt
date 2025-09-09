package com.epicorebiosystems.rehydrate.tabViews.insightViews

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.tabViews.insightViews.SweatChartUtils.getSodiumConcentrationColor
import com.epicorebiosystems.rehydrate.tabViews.insightViews.SweatChartUtils.getSodiumConcentrationString
import com.epicorebiosystems.rehydrate.tabViews.insightViews.SweatChartUtils.getSweatConcentrationColor
import com.epicorebiosystems.rehydrate.tabViews.insightViews.SweatChartUtils.getSweatConcentrationString
//import com.epicorebiosystems.rehydrate.tabViews.insightViews.SweatChartUtils.scaleSodiumLowValue
//import com.epicorebiosystems.rehydrate.tabViews.insightViews.SweatChartUtils.scaleSweatLowValue
import com.epicorebiosystems.rehydrate.tabViews.todayViews.hexToColor
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import android.util.DisplayMetrics.DENSITY_XHIGH

object ChYourSweatProfileColors {
    val WATER_LIGHT = hexToColor("#bfe7f6")
    val WATER_MODERATE = hexToColor("#66c4ea")
    val WATER_HEAVY = hexToColor("#00a0e2")

    val SODIUM_LOW = hexToColor("#dbd1e5")
    val SODIUM_MEDIUM = hexToColor("#a792c1")
    val SODIUM_HIGH = hexToColor("#73479c")
}

enum class SweatConcentrationState { LIGHT, MODERATE, HEAVY }
enum class SodiumConcentrationState { LOW, MEDIUM, HIGH }

object SweatChartUtils {

    fun getSweatConcentrationString(context: Context, state: SweatConcentrationState): String = when (state) {
        SweatConcentrationState.LIGHT -> context.getString(R.string.insights_light)
        SweatConcentrationState.MODERATE -> context.getString(R.string.insights_moderate)
        SweatConcentrationState.HEAVY -> context.getString(R.string.insights_heavy)
    }

    fun getSweatConcentrationColor(state: SweatConcentrationState): Color = when (state) {
        SweatConcentrationState.LIGHT    -> ChYourSweatProfileColors.WATER_LIGHT
        SweatConcentrationState.MODERATE -> ChYourSweatProfileColors.WATER_MODERATE
        SweatConcentrationState.HEAVY   -> ChYourSweatProfileColors.WATER_HEAVY
    }

    fun getSodiumConcentrationString(context: Context, state: SodiumConcentrationState): String = when (state) {
        SodiumConcentrationState.LOW    -> context.getString(R.string.insights_low)
        SodiumConcentrationState.MEDIUM -> context.getString(R.string.insights_medium)
        SodiumConcentrationState.HIGH   -> context.getString(R.string.insights_high)
    }

    fun getSodiumConcentrationColor(state: SodiumConcentrationState): Color = when (state) {
        SodiumConcentrationState.LOW    -> ChYourSweatProfileColors.SODIUM_LOW
        SodiumConcentrationState.MEDIUM -> ChYourSweatProfileColors.SODIUM_MEDIUM
        SodiumConcentrationState.HIGH   -> ChYourSweatProfileColors.SODIUM_HIGH
    }
}

fun getExplorerYourTypeString(context: Context, sweat: SweatConcentrationState, sodium: SodiumConcentrationState): String = when (sweat to sodium) {
    SweatConcentrationState.LIGHT to SodiumConcentrationState.LOW -> context.getString(R.string.insights_string_low_low)

    SweatConcentrationState.LIGHT to SodiumConcentrationState.MEDIUM -> context.getString(R.string.insights_string_low_medium)

    SweatConcentrationState.LIGHT to SodiumConcentrationState.HIGH -> context.getString(R.string.insights_string_low_high)

    SweatConcentrationState.MODERATE to SodiumConcentrationState.LOW -> context.getString(R.string.insights_string_moderate_low)

    SweatConcentrationState.MODERATE to SodiumConcentrationState.MEDIUM -> context.getString(R.string.insights_string_moderate_medium)

    SweatConcentrationState.MODERATE to SodiumConcentrationState.HIGH -> context.getString(R.string.insights_string_moderate_high)

    SweatConcentrationState.HEAVY to SodiumConcentrationState.LOW -> context.getString(R.string.insights_string_heavy_low)

    SweatConcentrationState.HEAVY to SodiumConcentrationState.MEDIUM -> context.getString(R.string.insights_string_heavy_medium)

    SweatConcentrationState.HEAVY to SodiumConcentrationState.HIGH -> context.getString(R.string.insights_string_heavy_high)

    else -> {
        ""
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserSweatProfileView(chViewModel: ModelData,
                         chartHeight: Dp = 24.dp // height of each chart bar; pass the same into SweatLossRate and SodiumConcentration
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var sweatConcentrationState by remember { mutableStateOf(SweatConcentrationState.LIGHT) }
    var sodiumConcentrationState by remember { mutableStateOf(SodiumConcentrationState.LOW) }
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"

    val sodiumConcentration = chViewModel.userAvgSweatSodiumConcentration?.data?.sodium_concentration_mm ?: 0.0
    val sweatConcentration = chViewModel.userAvgSweatSodiumConcentration?.data?.sweat_volume_ml ?: 0.0
    val mlConversionToL = sweatConcentration / 1000

    val phoneScreenDPI = context.resources.displayMetrics.densityDpi

    BoxWithConstraints(
        modifier = Modifier.padding(bottom = 5.dp, start = 10.dp, end = 10.dp)
    ) {
        val widthModifier = maxWidth
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            sweatConcentrationState = if (mlConversionToL < 0.6) {
                SweatConcentrationState.LIGHT
            } else if (mlConversionToL in 0.6..1.8) {
                SweatConcentrationState.MODERATE
            } else {
                SweatConcentrationState.HEAVY
            }

            sodiumConcentrationState = if (sodiumConcentration <= 24) {
                SodiumConcentrationState.LOW
            } else if (sodiumConcentration > 24 && sodiumConcentration < 64) {
                SodiumConcentrationState.MEDIUM
            } else {
                SodiumConcentrationState.HIGH
            }

            Box(
                Modifier
                    .height(if (phoneScreenDPI > DENSITY_XHIGH) (if (isExpanded) 620.dp else 420.dp) else (if (isExpanded) 680.dp else 480.dp))
                    .width(widthModifier)
                    .background(Color.White, RoundedCornerShape(10.dp))
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = stringResource(R.string.your_sweat_profile),
                        fontFamily = OswaldFonts,
                        fontSize = 20.sp,
                        color = colorResource(R.color.grayStandardText),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp, bottom = 10.dp, start = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Reuse your Compose translations
                    SweatLossRate(
                        sweatConcentrationState = sweatConcentrationState,
                        sweatConcentration = mlConversionToL,
                        chartHeight = chartHeight
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SodiumConcentration(
                        sodiumConcentrationState = sodiumConcentrationState,
                        sodiumConcentration = sodiumConcentration,
                        chartHeight = chartHeight
                    )

                    Spacer(modifier = Modifier.height(if (phoneScreenDPI > DENSITY_XHIGH) 60.dp else 120.dp))

                    // Expanded insight panel
                    if (isExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color.LightGray, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(start = 40.dp, top = 5.dp, end = 40.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${stringResource(R.string.insights_string_type)} ${
                                        getSweatConcentrationString(context, sweatConcentrationState)
                                    }/" +
                                            getSodiumConcentrationString(context, sodiumConcentrationState),
                                    fontFamily = RobotoRegularFonts,
                                    fontWeight = Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 5.dp)
                                )

                                Text(
                                    text = getExplorerYourTypeString(
                                        context = context,
                                        sweat = sweatConcentrationState,
                                        sodium = sodiumConcentrationState
                                    ),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                )
                            }
                        }
                    }
                }

                // Toggle button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        enabled = chViewModel.switchShareAnonymousDataEpicore,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = if (isExpanded) (-180).dp else 20.dp)
                    ) {
                        val res = when {
                            isExpanded && isJapanese -> R.drawable.sweat_profile_down_jp
                            isExpanded -> R.drawable.sweat_profile_down
                            !isExpanded && isJapanese -> R.drawable.sweat_profile_up_jp
                            else -> R.drawable.sweat_profile_up
                        }
                        Image(
                            painter = painterResource(res),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SweatLossRate(sweatConcentrationState: SweatConcentrationState, sweatConcentration: Double, chartHeight: Dp) {
    val segmentWidth = 120.dp
    val clamped = sweatConcentration.coerceIn(0.0, 3.1) / 3.1

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        // Title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.sweat_profile_drop),
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.insights_string_sweat_loss),
                fontFamily = OswaldFonts,
                fontSize = 16.sp,
                color = colorResource(R.color.grayStandardText),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Concentration label
        Text(
            text = getSweatConcentrationString(LocalContext.current, sweatConcentrationState),
            fontFamily = RobotoRegularFonts,
            fontSize = 36.sp,
            fontWeight = Bold,
            color = getSweatConcentrationColor(sweatConcentrationState),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp)
        )

        // The three-segment bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)
        ) {
            listOf(
                ChYourSweatProfileColors.WATER_LIGHT    to LocalContext.current.getString(R.string.insights_light),
                ChYourSweatProfileColors.WATER_MODERATE to LocalContext.current.getString(R.string.insights_moderate),
                ChYourSweatProfileColors.WATER_HEAVY   to LocalContext.current.getString(R.string.insights_heavy)
            ).forEach { (color, label) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(segmentWidth)
                        .height(chartHeight)
                        .background(color)
                ) {
                    Text(
                        text = label,
                        fontFamily = OswaldFonts,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        // The moving arrow
        Image(
            painter = painterResource(R.drawable.sweat_profile_arrow),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 20.dp + (360.dp * clamped.toFloat()), y = 5.dp)
        )
    }
}

@Composable
fun SodiumConcentration(sodiumConcentrationState: SodiumConcentrationState, sodiumConcentration: Double, chartHeight: Dp) {
    val segmentWidth = 120.dp
    val clamped = sodiumConcentration.coerceIn(0.0, 120.0) * 3.5

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        // Header row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.indicator_sodium),
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.sodium_concentration),
                fontFamily = OswaldFonts,
                fontSize = 16.sp,
                color = colorResource(R.color.grayStandardText),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Concentration value
        Text(
            text = getSodiumConcentrationString(LocalContext.current, sodiumConcentrationState),
            fontFamily = RobotoRegularFonts,
            fontSize = 36.sp,
            fontWeight = Bold,
            color = getSodiumConcentrationColor(sodiumConcentrationState),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp)
        )

        // Three-segment bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)
        ) {
            listOf(
                ChYourSweatProfileColors.SODIUM_LOW    to LocalContext.current.getString(R.string.insights_low),
                ChYourSweatProfileColors.SODIUM_MEDIUM to LocalContext.current.getString(R.string.insights_medium),
                ChYourSweatProfileColors.SODIUM_HIGH   to LocalContext.current.getString(R.string.insights_high)
            ).forEach { (color, label) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(segmentWidth)
                        .height(chartHeight)
                        .background(color)
                ) {
                    Text(
                        text = label,
                        fontFamily = OswaldFonts,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Moving arrow
        Image(
            painter = painterResource(R.drawable.sweat_profile_arrow),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 20.dp + clamped.dp, y = 5.dp)
        )
    }
}