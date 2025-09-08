package com.epicorebiosystems.rehydrate.topBarViews

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TopBarView(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController, updateHideBottomBar: (Boolean) -> Unit) {
    TopAppBar(
        backgroundColor = Color.White,
        elevation = 10.dp,
        title = {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(R.drawable.epicore_ch_darkbg),
                    contentDescription = "TopBar Logo"
                )
            }
        },
        actions = {
            Row {
                TopAppBarConnectivityDropdownMenu(
                    chViewModel,
                    ebsDeviceMonitor = ebsDeviceMonitor,
                    navController,
                    updateHideBottomBar = { viewState ->
                        updateHideBottomBar(viewState)
                    })

                TopAppBarInfoDropdownMenu(
                    chViewModel,
                    ebsDeviceMonitor,
                    navController,
                    updateHideBottomBar = { viewState ->
                        updateHideBottomBar(viewState)
                    })
            }
        },
    )
}