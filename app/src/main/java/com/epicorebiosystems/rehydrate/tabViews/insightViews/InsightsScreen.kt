package com.epicorebiosystems.rehydrate.tabViews.insightViews

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.sharedViews.BgStatusView
import com.epicorebiosystems.rehydrate.topBarViews.NotificationView
import com.epicorebiosystems.rehydrate.topBarViews.TopBarView

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InsightsScreen(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController, updateHideBottomBar: (Boolean) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopBarView(chViewModel, ebsDeviceMonitor = ebsDeviceMonitor, navController, updateHideBottomBar = { viewState ->
                updateHideBottomBar(viewState)
            })

            NotificationView(chViewModel)

        }
    ) {
        BgStatusView(chViewModel, ebsDeviceMonitor)

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .height(1780.dp),
        ) {

            UserSweatProfileView(chViewModel)

            InsightsWebView(chViewModel)

        }
    }
}