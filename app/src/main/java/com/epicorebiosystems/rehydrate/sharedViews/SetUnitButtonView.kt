package com.epicorebiosystems.rehydrate.sharedViews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts

enum class UnitType {
    WATER,
    SODIUM
}

@Composable
fun SetUnitButtonView(chViewModel: ModelData, unitType: UnitType) {
    if (unitType == UnitType.SODIUM) {
        /*Text(chViewModel.userPrefsData.getUserSodiumUnitTodayButtonString(),
            fontFamily = OswaldFonts,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )*/
        OutlinedButton(
            modifier = Modifier
                .width(140.dp)
                .height(50.dp),
            //elevation = ButtonDefaults.buttonElevation(
            //    disabledElevation = 1.dp
            //),
            onClick = { },
            enabled = false,
            border = BorderStroke(2.dp, Color.LightGray),
            shape = RoundedCornerShape(20),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
        ) {
            Text(chViewModel.userPrefsData.getUserSodiumUnitTodayButtonString() ,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterVertically),
                fontFamily = OswaldFonts,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.settingsColorCoalText))
        }
    }
    else {
        OutlinedButton(
            modifier = Modifier
                .width(140.dp)
                .height(50.dp),
            //elevation = ButtonDefaults.buttonElevation(
            //    disabledElevation = 1.dp
            //),
            onClick = { },
            enabled = false,
            border = BorderStroke(2.dp, Color.LightGray),
            shape = RoundedCornerShape(20),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
        ) {
            Text(chViewModel.userPrefsData.getUserSweatUnitTodayButtonString() ,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterVertically),
                fontFamily = OswaldFonts,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.settingsColorCoalText))
        }
    }
}