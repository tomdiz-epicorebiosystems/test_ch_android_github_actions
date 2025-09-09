package com.epicorebiosystems.rehydrate.topBarViews

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.jsonStringToMapWithGson
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable


object NotificationConstants {
    const val MAX_DEFICIT_INTAKE_NOTIFICATION = "maxDeficitIntakeNotification"
    const val APP_UPDATE_AVAIL_NOTIFICATION = "appUpdateAvailNotification"
    const val BLE_ERROR_CODE_11_NOTIFICATION  = "bleErrorCode_11_Notification"
    const val BLE_SESSION_RUNNING_NOTIFICATION  = "bleSessionRunningNotification"
}

object AppUpdateUrls {
    const val epicoreCH_GooglePlayLink = "market://details?id=com.epicorebiosystems.rehydrate"
    const val epicoreCH_WebLink = "https://play.google.com/store/apps/details?id=com.epicorebiosystems.rehydrate"
}

@Serializable
data class NotificationData(
    var id: String,
    var title: String,
    var detail: String,
    var type: NotificationType,
    var notificationLocation: NotificationLocation,
    var showOnce: Boolean,
    var showSeconds: Int,        // NotificationShowOptions.showClose - no timeout and need to close using 'X'
    var appUrl: String?
)

enum class NotificationLocation {
    Top,
    Middle,
    Bottom
}

object NotificationShowOptions {
    val showClose = -1
    val showNoClose = -2
}

enum class NotificationType {
    Info {
        override val tintColor: Color
            get() = Color(red = 67, green = 154, blue = 215)
    },
    Success {
        override val tintColor: Color
            get() = Color.Green
    },
    Warning {
        override val tintColor: Color
            get() = Color.Yellow
    },
    Error {
        override val tintColor: Color
            get() = Color.Red
    };

    abstract val tintColor: Color
}

@Composable
fun NotificationView(chViewModel: ModelData) {

    val context = LocalContext.current
    val showNotification = chViewModel.showNotification.collectAsStateWithLifecycle()
    var notificationState: HashMap<String, Boolean> = jsonStringToMapWithGson(chViewModel.notificationStateString.value)
    var data = chViewModel.notificationData
    var height = if (data.appUrl != null) 120.dp else if (data.id == NotificationConstants.BLE_ERROR_CODE_11_NOTIFICATION) 120.dp else 100.dp

    if (data.showSeconds > 0 && showNotification.value && data.showSeconds != NotificationShowOptions.showNoClose) {
        LaunchedEffect(Unit) {
            delay(data.showSeconds * 1000L)
            chViewModel.showNotification.value = false
            handleShowOnce(chViewModel, notificationState)
        }
    }

    if (showNotification.value) {
        AnimatedVisibility(
            visible = showNotification.value,
            enter = slideInVertically(
                animationSpec = tween(
                    durationMillis = 800,
                    easing = LinearEasing
                )
            ),
            //exit = slideOutVertically(
            //    animationSpec = tween(
            //        durationMillis = 800,
            //        easing = LinearEasing
            //    )
            //)
        ) {
            //BoxWithConstraints
            Box(
                modifier = Modifier
                    .clickable {
                        if (data.showSeconds == NotificationShowOptions.showNoClose) {
                        } else if (data.showSeconds == NotificationShowOptions.showClose) {
                            chViewModel.showNotification.value = false
                            handleShowOnce(chViewModel, notificationState)
                        }
                    }
                    .height(height)
                    .fillMaxWidth()
                    .background(data.type.tintColor)
                ) {

                Column {
                    Row {
                        Text(
                            data.title,
                            modifier = Modifier.padding(top = 10.dp, start = 5.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (data.showSeconds == NotificationShowOptions.showClose) {
                            Button(
                                onClick = {
                                    chViewModel.showNotification.value = false
                                    handleShowOnce(chViewModel, notificationState)
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = data.type.tintColor),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                Image(
                                    painterResource(R.drawable.baseline_close_24),
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(40.dp),
                                    contentDescription = "close",
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }
                        }
                    }

                    Text(
                        data.detail,
                        modifier = Modifier.padding(start = 5.dp),
                        fontFamily = RobotoRegularFonts,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )

                    if (data.appUrl != null) {
                        Button(
                            onClick = trackClick(targetName = "Update Now - app update clicked") {
                                try {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(AppUpdateUrls.epicoreCH_GooglePlayLink)
                                        )
                                    )
                                } catch (ex: ActivityNotFoundException) {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(AppUpdateUrls.epicoreCH_WebLink)
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(red = 67, green = 154, blue = 215)
                            ),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            elevation = ButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Text(
                                "Update Now",
                                fontFamily = RobotoRegularFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                style = TextStyle(textDecoration = TextDecoration.Underline),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

    }
}

fun handleShowOnce(chViewModel: ModelData, notificationState: HashMap<String, Boolean>) {
    notificationState[chViewModel.notificationData.id] = chViewModel.notificationData.showOnce
    val newNotificationState = Gson().toJson(notificationState).toString()
    chViewModel.updateNotificationState(newNotificationState)
}