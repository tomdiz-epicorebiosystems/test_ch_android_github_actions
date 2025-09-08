package com.epicorebiosystems.rehydrate.topBarViews

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.topBarViews.infoViews.AppOverviewView
import com.epicorebiosystems.rehydrate.topBarViews.infoViews.AttachModuleApplicationView
import com.epicorebiosystems.rehydrate.topBarViews.infoViews.ModulePairingView
import com.epicorebiosystems.rehydrate.topBarViews.infoViews.PatchApplicationView
import com.epicorebiosystems.rehydrate.topBarViews.infoViews.SodiumEqView
import com.epicorebiosystems.rehydrate.topBarViews.infoViews.SupportView
import com.epicorebiosystems.rehydrate.topBarViews.infoViews.UrineColorChartView

enum class InfoScreens {
    APP_OVERVIEW,
    PATCH_APP,
    MODULE_PAIRING,
    SODIUM_EQ,
    URINE_COLOR_CHART,
    //hydrationGuides,
    SUPPORT,
}

@Composable
fun InformationViews(chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor, navController: NavController, screen: InfoScreens, updateHideBottomBar: (Boolean) -> Unit) {
    var showScreen by rememberSaveable { mutableStateOf(screen) }

    BackHandler {
        navController.navigateUp()
        updateHideBottomBar(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.legalScreensBackground)),
        contentAlignment = Alignment.TopCenter,
    ) {

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(modifier = Modifier.weight(1f))

                Image(
                    painterResource(R.drawable.info_epic_logo_large_2),
                    contentDescription = "",
                    contentScale = ContentScale.None,
                    modifier = Modifier.padding(start = 80.dp, top = 10.dp, bottom = 10.dp))

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        navController.navigateUp()
                        updateHideBottomBar(false) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.legalScreensBackground)),
                    elevation =  ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp)
                ) {
                    Image(
                        painterResource(R.drawable.baseline_close_24),
                        modifier = Modifier.width(40.dp).height(40.dp),
                        contentDescription = "close",
                        colorFilter = ColorFilter.tint(Color.White))
                }
            }

            when (showScreen) {
                InfoScreens.APP_OVERVIEW -> {
                    AppOverviewView(chViewModel, ebsMonitor, showNextInfoPopup = { screen ->
                        showScreen = screen
                    })
                }
                InfoScreens.PATCH_APP -> {
                    if (ebsMonitor.getIsCHArmband()) {
                        AttachModuleApplicationView(showNextInfoPopup = { screen ->
                            showScreen = screen
                        })
                    }
                    else {
                        PatchApplicationView(showNextInfoPopup = { screen ->
                            showScreen = screen
                        })
                    }
                }
                InfoScreens.MODULE_PAIRING -> {
                    ModulePairingView(showNextInfoPopup = { screen ->
                        showScreen = screen
                    })
                }
                InfoScreens.SODIUM_EQ -> {
                    SodiumEqView(chViewModel, showNextInfoPopup = { screen ->
                        showScreen = screen
                    })
                }
                InfoScreens.URINE_COLOR_CHART -> {
                    UrineColorChartView(chViewModel, showNextInfoPopup = { screen ->
                        showScreen = screen
                    })
                }
                InfoScreens.SUPPORT -> {
                    SupportView(navController)
                }
            }

        }
    }
}