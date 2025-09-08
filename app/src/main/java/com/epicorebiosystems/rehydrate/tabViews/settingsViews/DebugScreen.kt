package com.epicorebiosystems.rehydrate.tabViews.settingsViews

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.DisconnectEvent
import com.epicorebiosystems.rehydrate.sharedViews.SegmentedControl
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import kotlinx.coroutines.launch

@Composable
fun DebugScreen(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController) {
    val scopeChangeServers  = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        BoxWithConstraints {
            val widthModifier = maxWidth - 20.dp
            val heightModifier = maxHeight - 200.dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
               Box(
                    Modifier
                        .height(heightModifier)
                        .width(widthModifier)
                        .offset(x = 10.dp, y = 120.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                ) {

                    Column {
                        Text("DEBUG",
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp),
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText))

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val units = listOf(
                                "Production",
                                "Staging"
                            )
                            SegmentedControl(
                                items = units,
                                defaultSelectedItemIndex = chViewModel.serverSettings.value
                            ) {
                                Log.d("ServerToggle", "Selected item : ${units[it]}")
                                chViewModel.serverSettings.value = it
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))

                        Row (
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                modifier = Modifier.width(120.dp),
                                onClick = trackClick(targetName = "DebugScreen ok pressed") {
                                    // Need to logout and clear settings
                                    scopeChangeServers.launch {
                                        chViewModel.networkManager.logOutUser()
                                        chViewModel.onboardingStep = 1
                                        chViewModel.updateOnBoardingComplete(false)
                                        ebsDeviceMonitor.onEvent(DisconnectEvent)
                                        ebsDeviceMonitor.disconnect()
                                        ebsDeviceMonitor.stopScanningJob()
                                        chViewModel.clearUserDataStore(true)
                                        chViewModel.resetModelDataMutables()
                                        chViewModel.updateServerSettings(chViewModel.serverSettings.value)
                                        ebsDeviceMonitor.scanBluetoothDevice()
                                    }
                                    navController.navigateUp()
                                },
                                border = BorderStroke(1.dp, colorResource(R.color.settingsColorHydroDarkText)),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorResource(R.color.settingsColorHydroDarkText))
                            ){
                                Text("OK" ,
                                    fontFamily = OswaldFonts,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(R.color.settingsColorCoalText))

                            }

                            Spacer(modifier = Modifier.width(50.dp))

                            OutlinedButton(
                                modifier = Modifier.width(120.dp),
                                onClick = trackClick(targetName = "DebugScreen cancel pressed") {
                                    navController.navigateUp()
                                },
                                border = BorderStroke(1.dp, colorResource(R.color.settingsColorHydroDarkText)),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorResource(R.color.settingsColorHydroDarkText))
                            ){
                                Text("CANCEL" ,
                                    fontFamily = OswaldFonts,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorResource(R.color.settingsColorCoalText))

                            }
                        }
                    }
                }
            }
        }
    }
}