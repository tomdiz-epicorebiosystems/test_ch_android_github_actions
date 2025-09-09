package com.epicorebiosystems.rehydrate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.epicorebiosystems.rehydrate.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val JostVariableFonts = FontFamily(
    Font(R.font.jost_variablefont_wght)
)

val JostItalicFonts = FontFamily(
    Font(R.font.jost_italic_variablefont_wght)
)

val OswaldFonts = FontFamily(
    Font(R.font.oswald_variablefont_wght)
)

val OrbitronExtraBoldFont = FontFamily(
    Font(R.font.orbitron_extrabold),
)

val OrbitronMediumFont = FontFamily(
    Font(R.font.orbitron_medium),
)

val OrbitronRegularFont = FontFamily(
    Font(R.font.orbitron_regular)
)

val TenByEightRegularFont = FontFamily(
    Font(R.font.tenby_eight)
)

val TenByEightLightFont = FontFamily(
    Font(R.font.tenby_eight_light)
)

val RobotoFonts = FontFamily(
    Font(R.font.roboto_black, weight = FontWeight.Black),
    Font(R.font.roboto_thin, weight = FontWeight.Thin),
    Font(R.font.roboto_thinitalic),
    Font(R.font.roboto_blackitalic),
    Font(R.font.roboto_bold, weight = FontWeight.Bold),
    Font(R.font.roboto_medium),
    Font(R.font.roboto_mediumitalic),
    Font(R.font.roboto_light),
    Font(R.font.roboto_lightitalic),
    Font(R.font.roboto_bolditalic)
)

val RobotoCondensedFonts = FontFamily(
    Font(R.font.robotocondensed_regular)
)

val RobotoRegularFonts = FontFamily(
    Font(R.font.roboto_regular)
)

var RobotoMediumFonts = FontFamily(
    Font(R.font.roboto_medium)
)