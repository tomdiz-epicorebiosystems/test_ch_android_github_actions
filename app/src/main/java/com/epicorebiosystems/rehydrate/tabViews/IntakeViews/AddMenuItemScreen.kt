package com.epicorebiosystems.rehydrate.tabViews.IntakeViews

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.IntakeButtonState
import com.epicorebiosystems.rehydrate.IntakeScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuItemScreen(navController: NavController, updateIntakeState: (IntakeButtonState) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_a_menu_item),
                        fontFamily = OswaldFonts,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.settingsColorCoalText)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = trackClick(targetName = "AddMenuItemScreen back pressed") {
                            updateIntakeState(IntakeButtonState.INTAKE_DOWN)
                            navController.navigateUp()
                        }
                    ) {
                        Image(
                            painterResource(R.drawable.baseline_chevron_left_24),
                            contentDescription = "image_chevon_left",
                            modifier = Modifier.testTag("image_chevon_left"),
                        colorFilter = ColorFilter.tint(colorResource(R.color.settingsColorCoalText))
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = trackClick(targetName = "add item close pressed") {
                            updateIntakeState(IntakeButtonState.INTAKE_DOWN)
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(end = 20.dp),
                            imageVector = Icons.Filled.Close,
                            contentDescription = "add item close")
                    }
                }
            )
        }
    ) {
        BoxWithConstraints {
            val widthModifier = maxWidth - 20.dp
            val heightModifier = maxHeight - 200.dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LaunchedEffect(updateIntakeState) {
                    // change intake button to cancel
                    updateIntakeState(IntakeButtonState.INTAKE_CANCEL)
                }

                Box(
                    Modifier
                        .height(heightModifier)
                        .width(widthModifier)
                        .offset(x = 10.dp, y = 120.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            stringResource(R.string.pick_method),
                            Modifier.padding(bottom = 10.dp),
                        fontFamily = OswaldFonts,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.settingsColorCoalText))

                        Button(
                            modifier = Modifier
                                .height(150.dp)
                                .width(widthModifier),
                            onClick = trackClick(targetName = "Open IntakeScreens.BottleListAdd") {
                                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                    navController.navigate(IntakeScreens.BottleListAdd.route!!)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                            shape = RoundedCornerShape(15.dp),
                            elevation =  ButtonDefaults.elevation(
                                defaultElevation = 10.dp,
                                pressedElevation = 15.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Row {
                                Image(
                                    modifier = Modifier
                                        .size(80.dp).testTag("image_back"),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_list_24),
                                    contentDescription = "image_back",
                                    colorFilter = ColorFilter.tint(colorResource(R.color.settingsColorCoalText)))

                                Text(text = stringResource(R.string.select_item_from_list),
                                    Modifier.padding(start = 20.dp, top = 25.dp),
                                    fontFamily = OswaldFonts,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.settingsColorCoalText))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            modifier = Modifier
                                .height(150.dp)
                                .width(widthModifier),
                            onClick = trackClick(targetName = "Open IntakeScreens.EnterBottleManually") {
                                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                    navController.navigate(IntakeScreens.EnterBottleManually.route!!)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                            shape = RoundedCornerShape(15.dp),
                            elevation =  ButtonDefaults.elevation(
                                defaultElevation = 10.dp,
                                pressedElevation = 15.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Row {
                                Image(
                                    modifier = Modifier
                                        .size(80.dp).testTag("image_back"),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_edit_24),
                                    contentDescription = "image_back",
                                    colorFilter = ColorFilter.tint(colorResource(R.color.settingsColorCoalText)))

                                Text(text = stringResource(R.string.enter_item_manually),
                                    Modifier.padding(start = 20.dp, top = 25.dp),
                                    fontFamily = OswaldFonts,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.settingsColorCoalText))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddMenuItemScreenPreview() {
    val navController = rememberNavController()
    var intakeButtonState by rememberSaveable { mutableStateOf(IntakeButtonState.INTAKE_UP) }
    AddMenuItemScreen(navController = navController, updateIntakeState = { newIntakeState -> intakeButtonState = newIntakeState})
}
