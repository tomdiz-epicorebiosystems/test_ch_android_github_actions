package com.epicorebiosystems.rehydrate.tabViews.settingsViews

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.SettingsSubScreens
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.sharedViews.BgStatusView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoCondensedFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoFonts

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalRegulatoryScreen(navController: NavController, chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor) {
    var legal: MutableList<String> = mutableListOf(stringResource(R.string.compliance),
        stringResource(
            R.string.legal_screen_license
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings),
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
                        onClick = trackClick(targetName = "LegalRegulatoryScreen back pressed") { navController.navigateUp() }
                    ) {
                        Image(
                            painterResource(R.drawable.baseline_chevron_left_24),
                            modifier = Modifier.testTag("image_back"),
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
            val heightModifier = maxHeight - 240.dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .height(heightModifier)
                        .width(widthModifier)
                        .offset(x = 10.dp, y = 120.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(bottom = 10.dp)
                ) {

                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            stringResource(R.string.legal_regulatory),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp),
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .width(widthModifier)
                                .background(Color.LightGray)
                        )
                        {
                            Text(
                                stringResource(R.string.connected_hydration),
                                Modifier.padding(start = 10.dp, top = 5.dp),
                                fontFamily = OswaldFonts,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(
                            Modifier.padding(start = 10.dp, top = 10.dp)
                        ) {
                            for (item in legal) {
                                Row(
                                    Modifier
                                        .padding(top = 10.dp, bottom = 15.dp)
                                        .background(Color.White)
                                        .clickable {
                                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                                if (item == legal[0]) {
                                                    navController.navigate(SettingsSubScreens.Compliance.route!!)
                                                } else if (item == legal[1]) {
                                                    navController.navigate(SettingsSubScreens.License.route!!)
                                                }
                                            }
                                        }) {
                                    Text(item,
                                        fontFamily = RobotoFonts,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Left,
                                        color = colorResource(R.color.settingsColorHydroDarkText)
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Image(painterResource(R.drawable.baseline_chevron_right_24),
                                        contentDescription = "image_chevron_right",
                                        colorFilter = ColorFilter.tint(colorResource(R.color.settingsColorHydroDarkText))
                                    )
                                }

                                if (item == legal[0]) {
                                    Divider(color = Color.LightGray, thickness = 2.dp)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .width(widthModifier)
                                .background(Color.LightGray)
                        )
                        {
                            Text(
                                stringResource(R.string.regulatory_certification),
                                Modifier.padding(start = 10.dp, top = 5.dp),
                                fontFamily = OswaldFonts,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(
                            Modifier.padding(start = 10.dp, top = 10.dp)
                        ) {
                            Row(
                                Modifier
                                    .padding(top = 10.dp, bottom = 10.dp)
                                    .background(Color.White)
                            ) {
                                Text(
                                    stringResource(R.string.model),
                                    fontFamily = RobotoFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Left,
                                    color = colorResource(R.color.settingsColorCoalText)
                                )

                                Spacer(modifier = Modifier.width(100.dp))

                                Text("ASY-0215",
                                    fontFamily = RobotoCondensedFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Left,
                                    color = colorResource(R.color.settingsColorCoalText)
                                )
                            }
                            Divider(color = Color.LightGray, thickness = 2.dp)
                        }

                        Column(
                            Modifier.padding(start = 10.dp, top = 10.dp)
                        ) {
                            Row(
                                Modifier
                                    .padding(top = 10.dp, bottom = 10.dp)
                                    .background(Color.White)
                            ) {
                                Text("United States",
                                    fontFamily = RobotoFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Left,
                                    color = colorResource(R.color.settingsColorCoalText)
                                )

                                Spacer(modifier = Modifier.width(40.dp))

                                Text("FCC ID: 2BANDCHASY0215",
                                    fontFamily = RobotoCondensedFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Left,
                                    color = colorResource(R.color.settingsColorCoalText)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Image(painterResource(id = R.drawable.fcc_logo_blue_2020),
                                contentDescription = "image_fcc_logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.width(340.dp))

                            Spacer(modifier = Modifier.height(20.dp))

                            Divider(color = Color.LightGray, thickness = 2.dp)
                        }

                        Column(
                            Modifier.padding(start = 10.dp, top = 10.dp)
                        ) {
                            Row(
                                Modifier
                                    .padding(top = 10.dp, bottom = 10.dp)
                                    .background(Color.White)
                            ) {
                                Text("Canada",
                                    fontFamily = RobotoFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Left,
                                    color = colorResource(R.color.settingsColorCoalText)
                                )

                                Spacer(modifier = Modifier.width(90.dp))

                                Text("IC: 31273-CHASY0215",
                                    fontFamily = RobotoCondensedFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Left,
                                    color = colorResource(R.color.settingsColorCoalText)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Divider(color = Color.LightGray, thickness = 2.dp)
                        }

                        Column(
                            Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp)
                        ) {
                                Text("810 Memorial Drive Suite 100",
                                    fontFamily = RobotoFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Left,
                                    color = colorResource(R.color.settingsColorCoalText)
                                )

                                Text("Cambridge, MA 02139",
                                    fontFamily = RobotoFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Left,
                                    color = colorResource(R.color.settingsColorCoalText)
                                )

                            Text("USA",
                                fontFamily = RobotoFonts,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Left,
                                color = colorResource(R.color.settingsColorCoalText)
                            )
                        }

                    }
                }
            }
        }
    }
}