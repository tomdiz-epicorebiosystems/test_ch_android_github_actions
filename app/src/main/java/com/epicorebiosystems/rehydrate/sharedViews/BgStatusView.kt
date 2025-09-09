package com.epicorebiosystems.rehydrate.sharedViews

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.isCHArmBand
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnRunInput
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.toByteArray
import com.epicorebiosystems.rehydrate.topBarViews.NotificationConstants
import com.epicorebiosystems.rehydrate.topBarViews.NotificationData
import com.epicorebiosystems.rehydrate.topBarViews.NotificationLocation
import com.epicorebiosystems.rehydrate.topBarViews.NotificationShowOptions
import com.epicorebiosystems.rehydrate.topBarViews.NotificationType
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts

enum class HydrationStatus {
    HYDRATED, AT_RISK, DEHYDRATED
}

@Composable
fun BgStatusView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor) {
    var hydrationStatus = chViewModel._sweatDashboardViewStatus.collectAsStateWithLifecycle()
    //val updateUserInfoFromDevice by chViewModel.updateUserInfoFromDevice.collectAsState()

    // Handle file upload
    val uartStateMgr = ebsDeviceMonitor.uartState.collectAsState().value

    var statusColorId = R.color.BgStatusHydrated
    if (hydrationStatus.value == HydrationStatus.AT_RISK.ordinal) {
        statusColorId = R.color.BgStatusAtRisk
    } else if (hydrationStatus.value == HydrationStatus.DEHYDRATED.ordinal) {
        statusColorId = R.color.BgStatusDehydrated
    }

    if (uartStateMgr.uartManagerState.intakeLogResponseReceived) {
        Log.d("BgStatusView", "Intake was logged!")
        // Vibrate device
        val haptic = LocalHapticFeedback.current
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        // Play sound
        val audioManager =
            LocalContext.current.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
        uartStateMgr.uartManagerState.intakeLogResponseReceived = false

        // Poll device status right after intake event is confirmed
        val getDeviceStatusCommandBytes: ByteArray = byteArrayOf(0x51, 0xA5.toByte())
        ebsDeviceMonitor.onEvent(OnRunInput(getDeviceStatusCommandBytes))
    }
    /*
    if (updateUserInfoFromDevice) {
        Log.d("BgStatusView", "updateUserInfoFromDevice")
        LaunchedEffect(Unit) {
            //delay(30000L)
            // Get system and user information command
            val getSystemAndUserInfoCommandBytes : ByteArray = byteArrayOf(0x50)
            ebsDeviceMonitor.onEvent(OnRunInput(getSystemAndUserInfoCommandBytes))

            chViewModel._updateCurrentUserIntakeItems.value = false

            //chViewModel._updateUserInfoFromDevice.value = false

        }
    }
*/
    if (uartStateMgr.uartManagerState.userInfoUpdate) {
        Log.d("BgStatusView", "userInfoUpdate")

        val userInfo = ebsDeviceMonitor.getUserInfoFromDevice() ?: return

        val localWeight = chViewModel.userWeightKg.value.toInt()
        val localHeight = chViewModel.userHeightCm.value.toInt()
        val localHeightIn = chViewModel.userHeightIn.value.toInt()
        val localHeightFt = chViewModel.userHeightFt.value.toInt()
        val weightRange =
            (if (userInfo.subjectWeightInKg == 0) 0 else userInfo.subjectWeightInKg - 1)..(if (userInfo.subjectWeightInKg == 0) 0 else userInfo.subjectWeightInKg + 1)
        val heightRange =
            (if (userInfo.subjectHeightInCm == 0) 0 else userInfo.subjectHeightInCm - 1)..(if (userInfo.subjectHeightInCm == 0) 0 else userInfo.subjectHeightInCm + 1)

        // Make sure device values match local/server API values since they a source of truth
        if (!weightRange.contains(localWeight) || !heightRange.contains(localHeight) ||
            !userInfo.subjectGender.equals(chViewModel.userGender.value, true) ||
            localHeightIn != userInfo.subjectHeightInches || localHeightFt != userInfo.subjectHeightFeet ||
            localHeight != userInfo.subjectHeightInCm || localWeight != userInfo.subjectWeightInKg
        ) {
            // Update user information here
            val paddedSize = 16
            val paddedHexZeros =
                ByteArray(paddedSize) { 0xFF.toByte() }   // Create the padded array of trailing 0x00's
            val userHeightInCms: ByteArray = byteArrayOf(
                (if (chViewModel.userHeightCm.value == "") {
                    "175"
                } else {
                    chViewModel.userHeightCm.value
                }).toInt().toByte()
            )

            val userWeightInKg: ByteArray = (if (chViewModel.userWeightKg.value == "") {
                "75"
            } else {
                chViewModel.userWeightKg.value
            }).toUShort().toByteArray()

            val userGender: ByteArray = byteArrayOf(
                if (chViewModel.userGender.value == "Male") {
                    0x00
                } else {
                    0x01
                }
            )

            val userAge: ByteArray = byteArrayOf(0)

            val userClothTypeCode: ByteArray = byteArrayOf(0)

            val setUserInfoCommand: ByteArray =
                byteArrayOf(0x55).plus(paddedHexZeros).plus(userGender).plus(userHeightInCms)
                    .plus(userWeightInKg).plus(userAge).plus(userClothTypeCode)

            ebsDeviceMonitor.onEvent(OnRunInput(setUserInfoCommand))
        }

        uartStateMgr.uartManagerState.userInfoUpdate = false
    }

    if (uartStateMgr.uartManagerState.sweatStatusUpdate) {
        Log.d("BgStatusView", "Update sweatStatusPacket")
        val sweatStatusPacket = ebsDeviceMonitor.getSweatStatusPacket()
        chViewModel.fluidDeficitInOz = sweatStatusPacket?.fluidDeficitInOz ?: 0.0

        //Log.d("BgStatusView", "${sweatStatusPacket?.currentTEWLInMl ?: 0}")

        // Limit the deficit display to only non-negative numbers.
        if (chViewModel.fluidDeficitInOz <= 0) {
            chViewModel.sweatVolumeDeficitInMl = 0
            chViewModel.fluidDeficitInOz = 0.0
        } else {
            chViewModel.sweatVolumeDeficitInMl = sweatStatusPacket?.sweatVolumeDeficitInMl ?: 0
        }

        // Sodium deficit
        chViewModel.sweatSodiumDeficitInMg = sweatStatusPacket?.sweatSodiumDeficitInMg ?: 0
        if (chViewModel.sweatSodiumDeficitInMg <= 0) {
            chViewModel.sweatSodiumDeficitInMg = 0
        } else {
            chViewModel.sweatSodiumDeficitInMg = sweatStatusPacket?.sweatSodiumDeficitInMg ?: 0
        }

        //chViewModel._sweatDashboardViewStatus.value = sweatStatusPacket?.hydrationStatus?.toInt() ?: 0
        chViewModel.setSweatDashboardViewStatus(sweatStatusPacket?.hydrationStatus?.toInt() ?: 0)

        // Check for error type 11 from device and notify user
        //if (true) {
        //    chViewModel.notificationData = NotificationData(id = NotificationConstants.BLE_ERROR_CODE_11_NOTIFICATION, title = "Error", detail = "The CH BLE Device has detected an issue. The currently attached patch already has sweat in it. Either dry off completely and apply a new patch or start again tomorrow.", type = NotificationType.Error, notificationLocation = NotificationLocation.Top, showOnce = true, showSeconds = NotificationShowOptions.showNoClose, appUrl = null)
        //    chViewModel.showNotification()
        //}
        //else {
        //    chViewModel.hideNotification()
        //}

        chViewModel.sweatVolumeTotalLossInMl = sweatStatusPacket?.sweatVolumeTotalLossInMl ?: 0u
        chViewModel.sweatSodiumTotalLossInMg = sweatStatusPacket?.sweatSodiumTotalLossInMg ?: 0u
        chViewModel.fluidTotalIntakeInMl = sweatStatusPacket?.fluidTotalIntakeInMl ?: 0u
        chViewModel.sodiumTotalIntakeInMg = sweatStatusPacket?.sodiumTotalIntakeInMg ?: 0u
        chViewModel.averageSkinTempInF = sweatStatusPacket?.averageSkinTempInF ?: 0.0
        chViewModel.currentTEWLInMl = sweatStatusPacket?.currentTEWLInMl ?: 0

        uartStateMgr.uartManagerState.sweatStatusUpdate = false

        // Set sodium deficit cap here when the water loss is more than 10L
        if ((chViewModel.sweatVolumeTotalLossInMl >= 10000u) && (chViewModel.capSodiumValue.value == 0)) {
            chViewModel.capSodiumValue.value = chViewModel.sweatSodiumTotalLossInMg.toInt()
            chViewModel.updateSodiumDeficitCap(chViewModel.capSodiumValue.value)
        }

        // If total sweat volume loss is less than 10L and the sodium was set before, this mean that a new session started and the sodium cap needs to be reset
        else if ((chViewModel.sweatVolumeTotalLossInMl.toUInt() < 10000u) && chViewModel.capSodiumValue.value != 0) {
            chViewModel.capSodiumValue.value = 0
            chViewModel.updateSodiumDeficitCap(chViewModel.capSodiumValue.value)
        }

        chViewModel.timeToSyncHistoricalData = true
    }

    // If user is not owner of current session on a module disconnect them
    if (!chViewModel.isCurrentUserSession) {
        Log.d("BGisAlreadyInSession", "TRUE")
        if (chViewModel.notificationData.id != NotificationConstants.BLE_SESSION_RUNNING_NOTIFICATION) {
//            ebsDeviceMonitor.onEvent(DisconnectEvent)
//            ebsDeviceMonitor.disconnect()
//            ebsDeviceMonitor.stopScanningJob()

            chViewModel.notificationData = NotificationData(
                id = NotificationConstants.BLE_SESSION_RUNNING_NOTIFICATION,
                title = "Error",
                detail = "The module is currently recording another users session.",
                type = NotificationType.Error,
                notificationLocation = NotificationLocation.Top,
                showOnce = false,
                showSeconds = NotificationShowOptions.showNoClose,
                appUrl = null
            )
            chViewModel.showNotification()
        }
    }
    else {
        Log.d("BGisAlreadyInSession", "FALSE")
        if (chViewModel.notificationData.id == NotificationConstants.BLE_SESSION_RUNNING_NOTIFICATION) {
            chViewModel.hideNotification()
            chViewModel.notificationData = NotificationData(id = "empty_notification", title = "Notification Title", detail = "Notification detail text for the user.", type = NotificationType.Error, notificationLocation = NotificationLocation.Middle, showOnce = true, showSeconds = NotificationShowOptions.showClose, appUrl = null)
        }
    }

    Box {
        Box(
            Modifier
                .fillMaxSize()
                .background(colorResource(statusColorId))
                .zIndex(0f)
        )

        Row (
            Modifier
                .offset(x = -(15).dp, y = 60.dp)
                .padding(top = 10.dp, bottom = 5.dp)
                .zIndex(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
            ) {

            Text(
                stringResource(R.string.hydration_status),
                modifier = Modifier.padding(start = 30.dp),
                fontFamily = OswaldFonts,
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.width(10.dp))

            when (hydrationStatus.value) {
                HydrationStatus.HYDRATED.ordinal -> {
                    Text(
                        stringResource(R.string.hydration_ok),
                        fontFamily = OswaldFonts,
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                HydrationStatus.AT_RISK.ordinal -> {
                    Text(
                        stringResource(R.string.hydration_at_risk),
                        fontFamily = OswaldFonts,
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                else -> {
                    Text(
                        stringResource(R.string.hydration_dehydrated),
                        fontFamily = OswaldFonts,
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            when (hydrationStatus.value) {
                HydrationStatus.HYDRATED.ordinal -> {
                    Spacer(modifier = Modifier.width(160.dp))

                    Image(painterResource(id = R.drawable.icon_alert_ok),
                        contentDescription = "hydrated image",
                        modifier = Modifier.offset(y = 5.dp))
                }
                HydrationStatus.AT_RISK.ordinal -> {
                    Spacer(modifier = Modifier.width(140.dp))

                    Image(painterResource(id = R.drawable.icon_alert_risk),
                        contentDescription = "hydrated image",
                        modifier = Modifier.offset(y = 10.dp))
                }
                else -> {
                    Spacer(modifier = Modifier.width(120.dp))

                    Image(painterResource(id = R.drawable.icon_alert_dehydrated),
                        contentDescription = "hydrated image",
                        modifier = Modifier.offset(y = 10.dp))
                }
            }
        }
    }
}