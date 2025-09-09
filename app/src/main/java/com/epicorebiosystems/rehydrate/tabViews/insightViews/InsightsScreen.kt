package com.epicorebiosystems.rehydrate.tabViews.insightViews

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.TabScreen
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.sharedViews.BgStatusView
import com.epicorebiosystems.rehydrate.topBarViews.NotificationView
import com.epicorebiosystems.rehydrate.topBarViews.TopBarView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoFonts

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InsightsScreen(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController, updateHideBottomBar: (Boolean) -> Unit, items: List<TabScreen>, onItemClick: (TabScreen) -> Unit) {
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"
    val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.insights_blur)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        Scaffold(
            modifier = Modifier.fillMaxWidth(),
            topBar = {
                AnimatedVisibility(visible = chViewModel.switchShareAnonymousDataEpicore) {
                    TopBarView(
                        chViewModel,
                        ebsDeviceMonitor = ebsDeviceMonitor,
                        navController,
                        updateHideBottomBar = { viewState ->
                            updateHideBottomBar(viewState)
                        })

                    NotificationView(chViewModel)
                }
            }
        ) {
            if (!chViewModel.switchShareAnonymousDataEpicore) {
                LegacyBlurImage(bitmap, 25f)
            }
            else {
                BgStatusView(chViewModel, ebsDeviceMonitor)

                Column(
                    modifier = Modifier
                        .verticalScroll(
                            rememberScrollState(),
                            enabled = chViewModel.switchShareAnonymousDataEpicore
                        )
                        .height(2000.dp)
                        .offset(y = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    UserSweatProfileView(chViewModel)

                    InsightsWebView(chViewModel)

                }
            }

            if (!chViewModel.switchShareAnonymousDataEpicore) {

                if (chViewModel.isDemoOnboardingFlow.value) {
                    Text(
                        text = "No analysis available in demo mode.",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 10.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        maxLines = 2,
                        lineHeight = 50.sp,
                        fontFamily = OswaldFonts,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val placeholder = stringResource(R.string.epicore_share_settings)
                    val globalText = stringResource(R.string.epicore_share_disabled, placeholder)
                    val start = globalText.indexOf(placeholder)
                    val spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(textDecoration = TextDecoration.Underline),
                            start = start,
                            end = start + placeholder.length
                        )
                    )

                    if (isJapanese) {
                        Text(
                            stringResource(R.string.epicore_share_disabled),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 10.dp, end = 10.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .clickable {
                                    chViewModel.scrollEnableShareSettingsView = true
                                    onItemClick(items[3])
                                },
                            maxLines = 4,
                            lineHeight = 50.sp,
                            fontFamily = OswaldFonts,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = AnnotatedString(
                                text = globalText,
                                spanStyles = spanStyles
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 10.dp, end = 10.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .clickable {
                                    chViewModel.scrollEnableShareSettingsView = true
                                    onItemClick(items[3])
                                },
                            maxLines = 4,
                            lineHeight = 50.sp,
                            fontFamily = OswaldFonts,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    else {
        Scaffold(
            modifier = Modifier.fillMaxWidth()
                .blur(if (!chViewModel.switchShareAnonymousDataEpicore) 10.dp else 0.dp),
            topBar = {
                TopBarView(
                    chViewModel,
                    ebsDeviceMonitor = ebsDeviceMonitor,
                    navController,
                    updateHideBottomBar = { viewState ->
                        updateHideBottomBar(viewState)
                    })

                NotificationView(chViewModel)

            }
        ) {
            BgStatusView(chViewModel, ebsDeviceMonitor)

            Column(
                modifier = Modifier
                    .verticalScroll(
                        rememberScrollState(),
                        enabled = chViewModel.switchShareAnonymousDataEpicore
                    )
                    .height(2060.dp)
                    .offset(y = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                UserSweatProfileView(chViewModel)

                InsightsWebView(chViewModel)

            }

        }

        if (!chViewModel.switchShareAnonymousDataEpicore) {

            if (chViewModel.isDemoOnboardingFlow.value) {
                Text(
                    text = "No analysis available in demo mode.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    maxLines = 2,
                    lineHeight = 50.sp,
                    fontFamily = OswaldFonts,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            } else {
                val placeholder = stringResource(R.string.epicore_share_settings)
                val globalText = stringResource(R.string.epicore_share_disabled, placeholder)
                val start = globalText.indexOf(placeholder)
                val spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(textDecoration = TextDecoration.Underline),
                        start = start,
                        end = start + placeholder.length
                    )
                )

                if (isJapanese) {
                    Text(
                        stringResource(R.string.epicore_share_disabled),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 10.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .clickable {
                                chViewModel.scrollEnableShareSettingsView = true
                                onItemClick(items[3])
                            },
                        maxLines = 4,
                        lineHeight = 50.sp,
                        fontFamily = OswaldFonts,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = AnnotatedString(
                            text = globalText,
                            spanStyles = spanStyles
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 10.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .clickable {
                                chViewModel.scrollEnableShareSettingsView = true
                                onItemClick(items[3])
                            },
                        maxLines = 4,
                        lineHeight = 50.sp,
                        fontFamily = OswaldFonts,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun BlurImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Composable
fun LegacyBlurImage(
    bitmap: Bitmap,
    blurRadio: Float,
    modifier: Modifier = Modifier.fillMaxSize()
) {

    val renderScript = RenderScript.create(LocalContext.current)
    val bitmapAlloc = Allocation.createFromBitmap(renderScript, bitmap)
    ScriptIntrinsicBlur.create(renderScript, bitmapAlloc.element).apply {
        setRadius(blurRadio)
        setInput(bitmapAlloc)
        forEach(bitmapAlloc)
    }
    bitmapAlloc.copyTo(bitmap)
    renderScript.destroy()

    BlurImage(bitmap, modifier)
}