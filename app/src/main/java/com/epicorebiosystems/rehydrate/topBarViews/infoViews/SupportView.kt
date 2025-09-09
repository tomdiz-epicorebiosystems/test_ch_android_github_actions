package com.epicorebiosystems.rehydrate.topBarViews.infoViews

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.epicorebiosystems.rehydrate.InfoPopupScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.dial
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun SupportView(navController: NavController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            //.verticalScroll(rememberScrollState())
            //.height(1600.dp)
            .background(colorResource(R.color.legalScreensBackground)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            stringResource(R.string.title_support),
            modifier = Modifier.padding(bottom = 40.dp, top = 20.dp),
            fontFamily = OswaldFonts,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.info_view_title_color)
        )

        Column(
            modifier = Modifier
            .fillMaxWidth().padding(start = 20.dp),
            horizontalAlignment = Alignment.Start

        ) {

            Button(
                onClick = {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(InfoPopupScreens.EpicoreRequiredTroubleshootingView.route!!)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Text(
                    stringResource(R.string.title_troubleshooting),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(InfoPopupScreens.EpicoreRequiredFaqView.route!!)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Text(
                    stringResource(R.string.frequently_asked_questions),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    val i = Intent(Intent.ACTION_SEND)
                    i.putExtra(Intent.EXTRA_EMAIL, "support@epicorebiosystems.com")
                    i.putExtra(Intent.EXTRA_SUBJECT, "")
                    i.putExtra(Intent.EXTRA_TEXT, "")
                    i.type = "message/rfc822"
                    context.startActivity(Intent.createChooser(i, "Choose an Email client : "))
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Text(
                    "support@epicorebiosystems.com",
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    context.dial(phone = "+1-617-397-3756")
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Text(
                    stringResource(R.string.support_phone_number),
                    fontFamily = RobotoRegularFonts,
                    fontSize = 16.sp,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun SupportViewPreview() {
    val navController = rememberNavController()
    SupportView(navController)
}