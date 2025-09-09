package com.epicorebiosystems.rehydrate.tabViews.settingsViews

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.sharedViews.BgStatusView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(navController: NavController, chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.back_settings),
                        modifier = Modifier.offset(x = -(35).dp).clickable {
                            navController.navigateUp()
                        },
                        fontFamily = OswaldFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.linkStandardText))
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.offset(x = -(15).dp, y = 2.dp),
                        onClick = trackClick(targetName = "PrivacyScreen back pressed") { navController.navigateUp() }
                    ) {
                        Image(painterResource(R.drawable.baseline_chevron_left_24),
                            contentDescription = "image_back",
                            colorFilter = ColorFilter.tint(colorResource(R.color.linkStandardText))
                        )
                    }
                }
            )
        }
    ) {
        BgStatusView(chViewModel, ebsDeviceMonitor)

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
                        Text(
                            stringResource(R.string.privacy),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp),
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Divider(color = Color.LightGray, thickness = 2.dp)

                        if (chViewModel.getCurrentLocale() == "ja_JP") {
                            val pdfState = rememberVerticalPdfReaderState(
                                resource = ResourceType.Asset(R.raw.privacypolicy_jap),
                                isZoomEnable = true
                            )

                            VerticalPDFReader(
                                state = pdfState,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .background(color = Color.White)
                            )
                        }
                        else {
                            val mUrl = "file:///android_asset/PrivacyPolicy.html"
                            AndroidView(factory = {
                                WebView(it).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    webViewClient = WebViewClient()
                                    loadUrl(mUrl)
                                }
                            }, update = {
                                it.loadUrl(mUrl)
                            }, modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}