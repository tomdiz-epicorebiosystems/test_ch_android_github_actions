package com.epicorebiosystems.rehydrate.topBarViews.infoViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3ModuleScanShareInfoView
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleShareInfoView
import com.epicorebiosystems.rehydrate.topBarViews.InfoScreens
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun ModulePairingView(showNextInfoPopup: (InfoScreens) -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .height(840.dp)
            .background(colorResource(R.color.legalScreensBackground)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(stringResource(R.string.title_module_pairing),
            modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp),
            textAlign = TextAlign.Center,
            fontFamily = OswaldFonts,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.info_view_title_color)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            Step3PairModuleShareInfoView()

            Text(
                stringResource(R.string.scan_the_qr_code_on_the_back_of_the_module),
                fontFamily = RobotoRegularFonts,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp, end = 20.dp),
                color = Color.White)

            Step3ModuleScanShareInfoView()

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {

                Button(
                    onClick = {
                        showNextInfoPopup(InfoScreens.SODIUM_EQ)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                    elevation =  ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp)
                ) {
                    Text(
                        stringResource(R.string.sodium_equivalents),
                        modifier = Modifier
                            .padding(end = 10.dp),
                        fontFamily = OswaldFonts,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModulePairingViewPreview() {
    ModulePairingView(showNextInfoPopup = {})
}