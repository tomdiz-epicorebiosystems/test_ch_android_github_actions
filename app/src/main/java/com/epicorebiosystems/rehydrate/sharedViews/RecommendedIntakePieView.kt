package com.epicorebiosystems.rehydrate.sharedViews

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RecommendedIntakePieView(endAngle: Float) {
    var radiusOuter: Dp = 140.dp
    var chartBarWidth: Dp = 20.dp
    Box(
        modifier = Modifier.size(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(radiusOuter * 2f)
        ) {
            drawArc(
                color = Color.Gray,
                0f,
                360f,
                useCenter = false,
                style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Butt))

            drawArc(
                color = Color.Black,
                -90f,
                endAngle,
                useCenter = false,
                style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Butt))
        }
    }
}