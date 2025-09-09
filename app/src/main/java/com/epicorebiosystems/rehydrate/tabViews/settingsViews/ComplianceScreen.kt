package com.epicorebiosystems.rehydrate.tabViews.settingsViews

import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.sharedViews.BgStatusView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceScreen(navController: NavController, chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.legal_regulatory),
                    modifier = Modifier.offset(x = -(35).dp).clickable {
                        navController.navigateUp()
                    },
                    fontFamily = OswaldFonts,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.linkStandardText)
                )
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.offset(x = -(15).dp, y = 2.dp),
                        onClick = trackClick(targetName = "ComplianceScreen back pressed") { navController.navigateUp() }
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
                            stringResource(R.string.fcc_compliance),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 20.dp),
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Divider(color = Color.LightGray, thickness = 2.dp)

                        Text(
                            stringResource(R.string.compliance_statements),
                            Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.attention),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.modifications_made_to_this_device),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(stringResource(R.string.attention),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.for_class_b_unintentional_radiators),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.this_device_complies_with_part_15),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(stringResource(R.string.attention),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.ices_003_class_b_notice),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(stringResource(R.string.attention),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.note_this_equipment_has_been_tested),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.reorient_or_relocate),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.increase_the_separation_between),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.connect_the_equipment_into),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.consult_the_dealer_or_an_experienced_radio),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.radiation_hazard),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(stringResource(R.string.attention),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.in_order_to_satisfy_the_fcc_ised),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(stringResource(R.string.attention),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.this_device_complies_with_industry_canada),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.le_pr_sent_appareil_est_conform),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(stringResource(R.string.attention),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.this_radio_transmitter_the_connected_hydration),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.le_pr_sent_metteur_radio_connected_hydration),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.type_of_antenna),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(stringResource(R.string.attention),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.under_industry_canada_regulations),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.warning),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.there_is_danger_of_explosion_if_batterie),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.do_not_disassemble_batteries),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.dispose_of_batteries_properly),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(stringResource(R.string.warning),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.do_not_incinerate_or_subject_battery),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(stringResource(R.string.warning),
                            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.explosion_hazard_batteries_must_only),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                        Text(
                            stringResource(R.string.avertissement_risque_d_explosion),
                            Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                            fontFamily = RobotoRegularFonts,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorResource(R.color.grayStandardText)
                        )

                    }
                }
            }
        }
    }
}