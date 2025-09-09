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
import com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.AttachModuleTopView
import com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.armbandapplication.Step4ModuleAppTightenStrapShareInfoView
import com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.patchapplication.Step4PatchApplyShareInfoView
import com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.patchapplication.Step4PatchCleanSkinShareInfoView
import com.epicorebiosystems.rehydrate.topBarViews.InfoScreens
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts

@Composable
fun PatchApplicationView(showNextInfoPopup: (InfoScreens) -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .height(1040.dp)
            .background(colorResource(R.color.legalScreensBackground)),
        horizontalAlignment = Alignment.Start
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            stringResource(R.string.title_patch_application),
            modifier = Modifier.padding(start = 20.dp),
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

            //Step4PatchAppShareInfoView()

            Step4PatchCleanSkinShareInfoView()

            Step4PatchApplyShareInfoView()

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {

                Button(
                    onClick = {
                        showNextInfoPopup(InfoScreens.MODULE_PAIRING)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                    elevation =  ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp)
                ) {
                    Text(
                        stringResource(R.string.module_pairing),
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

@Composable
fun AttachModuleApplicationView(showNextInfoPopup: (InfoScreens) -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .height(1080.dp)
            .background(colorResource(R.color.legalScreensBackground)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(stringResource(R.string.patch_module_attach),
            modifier = Modifier.padding(top = 20.dp),
            fontFamily = OswaldFonts,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.info_view_title_color)
        )

        Spacer(modifier = Modifier.height(10.dp))

        AttachModuleTopView()

        Spacer(modifier = Modifier.height(10.dp))

        Step4ModuleAppTightenStrapShareInfoView()

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {

                Button(
                    onClick = {
                        showNextInfoPopup(InfoScreens.MODULE_PAIRING)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                    elevation =  ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp)
                ) {
                    Text(
                        stringResource(R.string.module_pairing),
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