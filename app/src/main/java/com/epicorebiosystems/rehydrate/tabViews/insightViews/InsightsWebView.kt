package com.epicorebiosystems.rehydrate.tabViews.insightViews

import android.graphics.Color
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.sharedViews.HydrationStatus

@Composable
fun InsightsWebView(chViewModel: ModelData) {
    var hydrationStatus = chViewModel._sweatDashboardViewStatus.collectAsStateWithLifecycle()
    val isImperialOn = chViewModel.currentUnits.value == 1
    var backgroundStatusColor = "11314c"
    if (hydrationStatus.value == HydrationStatus.AT_RISK.ordinal) {
        backgroundStatusColor = "d7b20c"
    }
    else if (hydrationStatus.value == HydrationStatus.DEHYDRATED.ordinal) {
        backgroundStatusColor = "b02023"
    }

    val mUrl = "${chViewModel.networkManager.apiServerInfo.getServerBaseApi()}/mobile/insights?color=$backgroundStatusColor&imperial=$isImperialOn"

    val token = chViewModel.encryptedPreferences.getString("access_token", "")
        ?.replace("\"", "")
        ?: return
    val cookieAuthString = "authorization=Bearer $token; path=/mobile/insights"
    val cookieSelectedRoles = "selectedUserRoles=[{\"enterprise_id\": \"${chViewModel.jwtEnterpriseID.value}\",\"role\":\"CH_USER\",\"site_id\": \"${chViewModel.jwtSiteID.value}\"}]; path=/mobile/insights"

    var language = "en"
    if (chViewModel.getCurrentLocale() == "ja_JP") {
        language = "ja"
    }
    val cookieLanguage = "language=$language; path=/mobile/insights"

    /*
    // Grab HTML
    wvbrowser.evaluateJavascript(
        "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
         new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String html) {
                Log.d("HTML", html);
                // code here
            }
    })
    */

    Surface() {
        Box {

            AndroidView(factory = {
                WebView(it).apply {
//                layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//                )
                    webViewClient = WebViewClient()
                    loadUrl(mUrl)
                    setBackgroundColor(Color.TRANSPARENT)
                    CookieManager.getInstance().setCookie(url, cookieAuthString)
                    CookieManager.getInstance().setCookie(url, cookieSelectedRoles)
                    CookieManager.getInstance().setCookie(url, cookieLanguage)
                    settings.javaScriptEnabled = true
                    settings.setSupportZoom(false)
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                }
            }, update = {
                it.loadUrl(mUrl)
            })
        }
    }
}