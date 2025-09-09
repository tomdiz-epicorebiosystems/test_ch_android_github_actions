package com.epicorebiosystems.rehydrate.sharedViews

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState

enum class EpicoreLegalScreens {
    FAQ,
    TROUBLESHOOTING,
    TERMS,
    PRIVACY
}

@Composable
fun EpicoreRequiredView(chViewModel: ModelData, navController: NavController, screen: EpicoreLegalScreens) {
    var mUrl = "file:///android_asset/TermsConditions.html"

    if (screen == EpicoreLegalScreens.PRIVACY) {
        mUrl = "file:///android_asset/PrivacyPolicy.html"
    }
    else if (screen == EpicoreLegalScreens.TROUBLESHOOTING) {
        mUrl = "file:///android_asset/troubleshooting.html"
    }
    else if (screen == EpicoreLegalScreens.FAQ) {
        mUrl = "file:///android_asset/faq.html"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.legalScreensBackground)),
        contentAlignment = Alignment.TopCenter,
        ) {

        BoxWithConstraints {
            val widthModifier = maxWidth - 20.dp
            val heightModifier = maxHeight - 100.dp

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
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))

                    Spacer(modifier = Modifier.width(40.dp))

                    Button(
                        onClick = trackClick(targetName = "EpicoreRequiredView back pressed") {
                            navController.navigateUp()
                        },
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
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }

                Column(
                    Modifier
                        .height(heightModifier)
                        .width(widthModifier)
                        .offset(x = 10.dp)
                        .background(Color.White, RoundedCornerShape(10.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (chViewModel.getCurrentLocale() == "ja_JP") {
                        var pdfResId = R.raw.terms_jap
                        if (screen == EpicoreLegalScreens.PRIVACY) {
                            pdfResId = R.raw.privacypolicy_jap
                        }
                        else if (screen == EpicoreLegalScreens.TROUBLESHOOTING) {
                            pdfResId = R.raw.toubleshooting_jap
                        }
                        else if (screen == EpicoreLegalScreens.FAQ) {
                            pdfResId = R.raw.faq_jap
                        }
                        val pdfState = rememberVerticalPdfReaderState(
                            resource = ResourceType.Asset(pdfResId),
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
                        AndroidView(
                            factory = {
                                WebView(it).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    webViewClient = WebViewClient()
                                    loadUrl(mUrl)
                                }
                            },
                            update = {
                                it.loadUrl(mUrl)
                            },
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

        }
    }
}