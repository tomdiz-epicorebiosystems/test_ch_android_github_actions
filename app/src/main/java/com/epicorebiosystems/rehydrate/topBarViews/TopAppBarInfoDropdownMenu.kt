package com.epicorebiosystems.rehydrate.topBarViews

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.epicorebiosystems.rehydrate.InfoPopupScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts

@Composable
fun TopAppBarInfoDropdownMenu(chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor, navController: NavController, updateHideBottomBar: (Boolean) -> Unit) {
    var infoExpanded by rememberSaveable { mutableStateOf(false) }
    val isJapanese = chViewModel.getCurrentLocale() == "ja_JP"
    Box(
        Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = {
            infoExpanded = true
        }) {
            Image(
                painterResource(R.drawable.icon_info),
                contentDescription = "TopBar Info"
            )
        }
    }

    DropdownMenu(
        expanded = infoExpanded,
        onDismissRequest = { infoExpanded = false },
    ) {

        Text(
            stringResource(R.string.instructions),
            modifier = Modifier.padding(start = 5.dp),
            fontFamily = OswaldFonts,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.info_title_color)
        )

        DropdownMenuItem(onClick = {
            infoExpanded = false
            chViewModel.infoPopupScreen = InfoScreens.APP_OVERVIEW
            updateHideBottomBar(true)
            navController.navigate(InfoPopupScreens.InformationViews.route!!)
        }) {
            Text(
                stringResource(R.string.app_overview),
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = OswaldFonts,
                fontSize = if (isJapanese) 16.sp else 18.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.settingsColorCoalText)
            )
        }

        DropdownMenuItem(onClick = {
            infoExpanded = false
            chViewModel.infoPopupScreen = InfoScreens.PATCH_APP
            updateHideBottomBar(true)
            navController.navigate(InfoPopupScreens.InformationViews.route!!)
        }) {

            Spacer(modifier = Modifier.height(30.dp))

            if (ebsMonitor.getIsCHArmband()) {
                Text(
                    stringResource(R.string.patch_module_attach),
                    modifier = Modifier.padding(start = 5.dp),
                    fontFamily = OswaldFonts,
                    fontSize = if (isJapanese) 16.sp else 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.settingsColorCoalText)
                )
            }
            else {
                Text(
                    stringResource(R.string.title_patch_application),
                    modifier = Modifier.padding(start = 5.dp),
                    fontFamily = OswaldFonts,
                    fontSize = if (isJapanese) 16.sp else 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.settingsColorCoalText)
                )
            }
        }

        DropdownMenuItem(onClick = {
            infoExpanded = false
            chViewModel.infoPopupScreen = InfoScreens.MODULE_PAIRING
            updateHideBottomBar(true)
            navController.navigate(InfoPopupScreens.InformationViews.route!!)
        }) {
            Text(
                stringResource(R.string.title_module_pairing),
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = OswaldFonts,
                fontSize = if (isJapanese) 15.sp else 18.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.settingsColorCoalText)
            )
        }

        Text(
            stringResource(R.string.reference),
            modifier = Modifier.padding(start = 5.dp),
            fontFamily = OswaldFonts,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.info_title_color)
        )


        DropdownMenuItem(onClick = {
            infoExpanded = false
            chViewModel.infoPopupScreen = InfoScreens.SODIUM_EQ
            updateHideBottomBar(true)
            navController.navigate(InfoPopupScreens.InformationViews.route!!)
        }) {
            Text(
                stringResource(R.string.title_sodium_equivalents),
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = OswaldFonts,
                fontSize = if (isJapanese) 16.sp else 18.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.settingsColorCoalText)
            )
        }

        DropdownMenuItem(onClick = {
            infoExpanded = false
            chViewModel.infoPopupScreen = InfoScreens.URINE_COLOR_CHART
            updateHideBottomBar(true)
            navController.navigate(InfoPopupScreens.InformationViews.route!!)
        }) {
            Text(stringResource(R.string.urine_color_chart),
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = OswaldFonts,
                fontSize = if (isJapanese) 16.sp else 18.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.settingsColorCoalText)
            )
        }

        DropdownMenuItem(onClick = {
            infoExpanded = false
            chViewModel.infoPopupScreen = InfoScreens.SUPPORT
            updateHideBottomBar(true)
            navController.navigate(InfoPopupScreens.InformationViews.route!!)
        }) {
            Text(
                stringResource(R.string.title_support),
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = OswaldFonts,
                fontSize = if (isJapanese) 16.sp else 18.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(R.color.settingsColorCoalText)
            )
        }

    }
}