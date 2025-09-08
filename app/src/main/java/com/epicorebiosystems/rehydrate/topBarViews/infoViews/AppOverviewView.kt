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
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.onboarding.step5_overview.Step5OverviewEndOfShift
import com.epicorebiosystems.rehydrate.onboarding.step5_overview.Step5OverviewMainShareInfoView
import com.epicorebiosystems.rehydrate.onboarding.step5_overview.Step5OverviewShareInfoView
import com.epicorebiosystems.rehydrate.onboarding.step5_overview.Step5TrackIntakeShareInfoView
import com.epicorebiosystems.rehydrate.topBarViews.InfoScreens
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts

@Composable
fun AppOverviewView(chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor, showNextInfoPopup: (InfoScreens) -> Unit) {

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .height(1800.dp)
            .background(colorResource(R.color.legalScreensBackground)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            stringResource(R.string.app_overview),
            modifier = Modifier.padding(top = 20.dp),
            textAlign = TextAlign.Center,
            fontFamily = OswaldFonts,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.info_view_title_color))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(10.dp))

            Step5OverviewMainShareInfoView(ebsMonitor)

            Spacer(Modifier.height(10.dp))

            Step5OverviewShareInfoView(ebsMonitor)

            Spacer(Modifier.height(10.dp))

            Step5TrackIntakeShareInfoView()

            Spacer(Modifier.height(10.dp))

            Step5OverviewEndOfShift(ebsMonitor)

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {

                Button(
                    onClick = {
                        showNextInfoPopup(InfoScreens.PATCH_APP)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                    elevation =  ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp)
                ) {

                    Spacer(modifier = Modifier.height(30.dp))

                    if (ebsMonitor.getIsCHArmband()) {
                        Text(
                            stringResource(R.string.patch_module_attach_next),
                            modifier = Modifier
                                .padding(end = 10.dp),
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    }
                    else {
                        Text(
                            stringResource(R.string.info_patch_application),
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
}