@file:Suppress("DEPRECATION")

package com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.OnboardingScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.SettingsSubScreens
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.DisconnectEvent
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@Composable
fun Step3PairModuleMainView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, isNewPair: Boolean, navController: NavController, isOnboarding: Boolean, updateHideBottomBar: (Boolean) -> Unit) {
    val context = LocalContext.current
    var isBluetoothEnabled = remember { mutableStateOf(true) }

    BackHandler {
        if (isNewPair) {
            navController.navigateUp()
            updateHideBottomBar(false)
        }
    }

    val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    if (!mBluetoothAdapter.isEnabled) {
        isBluetoothEnabled.value = false
    }

    DisposableEffect(Unit) {

        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

        val mBluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                        BluetoothAdapter.STATE_OFF -> {
                            isBluetoothEnabled.value = false
                            //Log.i("Bluetooth", "State OFF")
                        }
                        BluetoothAdapter.STATE_ON -> {
                            isBluetoothEnabled.value = true
                            //Log.i("Bluetooth", "State ON")
                        }
                    }

                }
            }
        }

        context.registerReceiver(mBluetoothStateReceiver, intentFilter)

        onDispose {
            context.unregisterReceiver(mBluetoothStateReceiver)
        }
    }

    Card {
        if (!isBluetoothEnabled.value) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    isBluetoothEnabled.value = false
                },
                title = {
                    Text(stringResource(R.string.bluetooth))
                },
                text = {
                    Text(stringResource(R.string.turn_on_bluetooth))
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = trackClick(targetName = "Bluetooth disabled settings pressed") {
                            isBluetoothEnabled.value = false
                            val i = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                            context.startActivity(i)
                        }) {
                        Text(stringResource(R.string.tab_settings))
                    }
                },
                dismissButton = { }
            )
        }
        else {
            isBluetoothEnabled.value = true
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.onboardingVeryDarkBackground)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(stringResource(R.string.pair_module),
                modifier = Modifier.testTag("text_step3pairmodulemainview_pairmodule"),
                textAlign = TextAlign.Center,
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = colorResource(R.color.onboardingLtGrayColor), thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painterResource(R.drawable.progress_bar_4_5),
                    contentDescription = "image_step3pairmodulemainview_progress_3",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(bottom = 20.dp)
                        .testTag("image_step3pairmodulemainview_progress_3"))

                Step3PairModuleShareInfoView()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = trackClick(targetName = "Step3PairModuleMainView disconnecting device - next button pressed"){
                            if (!isNewPair) {
                                ebsDeviceMonitor.onEvent(DisconnectEvent)
                                ebsDeviceMonitor.disconnect()
                                chViewModel.deviceSN.value = ""
                                chViewModel.isCHDeviceConnected = false
                                chViewModel._isSensorConnected.value = false

                                chViewModel._qrPairingState.value = chViewModel._qrPairingState.value.copy(pairedMoveView = false)

                                if (isOnboarding) {
                                    navController.navigate(OnboardingScreens.LogInModuleScanView.route)
                                }
                                else {
                                    navController.navigate(OnboardingScreens.Step3PairModuleScanView.route)
                                }

                                ebsDeviceMonitor.scanBluetoothDevice()

                            }
                            else {
                                ebsDeviceMonitor.onEvent(DisconnectEvent)
                                ebsDeviceMonitor.disconnect()
                                chViewModel.deviceSN.value = ""
                                chViewModel.isCHDeviceConnected = false
                                chViewModel._isSensorConnected.value = false

                                chViewModel._qrPairingState.value = chViewModel._qrPairingState.value.copy(pairedMoveView = false)

                                if (isOnboarding) {
                                    navController.navigate(OnboardingScreens.LogInModuleScanView.route)
                                }
                                else {
                                    navController.navigate(SettingsSubScreens.PairNewModuleScanView.route!!)
                                }

//                                ebsDeviceMonitor.stopScanningJob()
                                ebsDeviceMonitor.scanBluetoothDevice()
                            }

                            chViewModel.initDeviceOnce.value = false
                            ebsDeviceMonitor.clearHistoricalDataSet()
                        },
                        modifier = Modifier
                            .size(width = 220.dp, height = 60.dp)
                            .padding(bottom = 10.dp)
                            .testTag("button_step3pairmodulemainview_mymodule"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color.White
                        )) {
                        Text(stringResource(R.string.module_is_on),
                            modifier = Modifier.align(Alignment.CenterVertically)
                                .testTag("text_step3pairmodulemainview_mymodule"),
                            textAlign = TextAlign.Center,
                            fontFamily = OswaldFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.onboardingLtBlueColor)
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun Step3PairModuleShareInfoView() {
    Text(
        stringResource(R.string.press_the_power_button_on_the_module),
        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, end = 20.dp)

            .testTag("text_pairmoduleshareinfo1view_press"),
        fontFamily = RobotoRegularFonts,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        color = Color.White)

    Text(
        stringResource(R.string.a_light_will_flash_on_startup),
        modifier = Modifier.padding(start = 20.dp, bottom = 20.dp, end = 20.dp)
            .testTag("text_pairmoduleshareinfo1view_flash"),
        fontFamily = RobotoRegularFonts,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Normal,
        color = Color.White)

    Image(
        painterResource(R.drawable.pair_module_on),
        contentDescription = "",
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.padding(bottom = 20.dp)
            .testTag("image_pairmoduleshareinfo1view_pair"))
}