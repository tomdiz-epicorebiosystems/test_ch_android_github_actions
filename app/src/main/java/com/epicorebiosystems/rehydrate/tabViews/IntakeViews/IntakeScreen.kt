package com.epicorebiosystems.rehydrate.tabViews.IntakeViews

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.epicorebiosystems.rehydrate.IntakeButtonState
import com.epicorebiosystems.rehydrate.IntakeScreens
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.TabScreen
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.sharedViews.BgStatusView
import com.epicorebiosystems.rehydrate.sharedViews.BottleIconRenderView
import com.epicorebiosystems.rehydrate.sharedViews.RecommendedIntakePieView
import com.epicorebiosystems.rehydrate.sharedViews.SetUnitButtonView
import com.epicorebiosystems.rehydrate.sharedViews.UnitType
import com.epicorebiosystems.rehydrate.topBarViews.NotificationView
import com.epicorebiosystems.rehydrate.topBarViews.TopBarView
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoFonts
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IntakeScreen(chViewModel: ModelData, ebsDeviceMonitor: EBSDeviceMonitor, navController: NavController,
                 bottomSheetScaffoldState: BottomSheetScaffoldState,
                 items: List<TabScreen>, onItemClick: (TabScreen) -> Unit,
                 updateIntakeState: (IntakeButtonState) -> Unit,
                 updateHideBottomBar: (Boolean) -> Unit) {
    val intakeOpenCoroutineScope = rememberCoroutineScope()
    val presetScrollState = rememberScrollState()
    val intakeScrollState = rememberScrollState()
    val intakeBottleUpdated by chViewModel.updateCurrentUserIntakeItems.collectAsState()
    val isSaveButtonShowing by chViewModel.isSaveButtonShowing.collectAsState()
    var recommendedWaterIntakePercentage = 0f
    var recommendedWaterIntakeDegrees = 0.0
    var recommendedSodiumIntakePercentage = 0f
    var recommendedSodiumIntakeDegrees = 0.0
    val listState = rememberLazyListState()
    val coroutineScopeBottleList = rememberCoroutineScope()
    var isIntakeButtonUp by remember { mutableStateOf(false) }
    var isCancelButtonPressed by remember { mutableStateOf(false) }

    Scaffold (
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopBarView(chViewModel, ebsDeviceMonitor, navController, updateHideBottomBar = { viewState ->
                updateHideBottomBar(viewState)
            })

            NotificationView(chViewModel)

        }
    ) {
        BoxWithConstraints {
            val heightModifier = maxHeight - 120.dp
            BottomSheetScaffold(
                sheetGesturesEnabled = false,
                scaffoldState = bottomSheetScaffoldState,
                sheetShape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp),
                sheetContent = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(heightModifier)
                            .background(Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .height(840.dp),
                        ) {
                            Row (modifier = Modifier.padding(end = 20.dp))
                            {
                                Text(
                                    text = stringResource(R.string.menu),
                                    Modifier.padding(start = 20.dp, top = 10.dp),
                                    fontFamily = OswaldFonts,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.settingsColorCoalText)
                                )

                                IconButton(
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = colorResource(R.color.settingsColorCoalText)
                                    ),
                                    onClick = trackClick(targetName = "Open IntakeScreens.AddMenuItem") {
                                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                            navController.navigate(IntakeScreens.AddMenuItem.route!!)
                                        }
                                    }
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .size(size = 40.dp)
                                            .padding(start = 15.dp, top = 5.dp),
                                        imageVector = Icons.Filled.Add,
                                        tint = colorResource(R.color.settingsColorCoalText),
                                        contentDescription = "Add bottle Icon"
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                IconButton(
                                    modifier = Modifier
//                                        .offset(x = 80.dp) // moves the column to the left
                                        .width(60.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = colorResource(R.color.settingsColorCoalText)
                                    ),
                                    onClick = trackClick(targetName = "IntakeScreens cancel/close pressed") {
                                        chViewModel.totalWaterAmount = 0.0
                                        chViewModel.totalSodiumAmount = 0.0
                                        chViewModel.currentUserIntakeItems.clear()
                                        chViewModel.newBottlesItemsAdded.clear()
                                        updateIntakeState(IntakeButtonState.INTAKE_UP)
                                        onItemClick(items[0])
                                        intakeOpenCoroutineScope.launch {
                                            bottomSheetScaffoldState.bottomSheetState.collapse()
                                        }
                                        chViewModel._isSaveButtonShowing.value = false
                                        isCancelButtonPressed = true
                                    }
                                ) {
                                    if (isSaveButtonShowing) {
                                        Text(stringResource(R.string.sensor_cancel),
                                            fontFamily = OswaldFonts,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = colorResource(R.color.settingsColorCoalText))
                                    }
                                    else {
                                        Icon(
                                            modifier = Modifier
                                                .size(size = 32.dp)
                                                .padding(top = 5.dp),
                                            imageVector = Icons.Filled.Close,
                                            tint = colorResource(R.color.settingsColorCoalText),
                                            contentDescription = "Close Intake Screen"
                                        )
                                    }
                                }
                            }   // Top row for add/close menu

                            // User preset bottle list view
                            Column(
                                modifier = Modifier.scrollable(
                                    state = presetScrollState,
                                    orientation = Orientation.Horizontal
                                )
                            ) {
                                if (intakeBottleUpdated || !intakeBottleUpdated) {
                                    if (chViewModel.currentBottleMenuItems.isEmpty()) {
                                        Row(
                                            modifier = Modifier.padding(start = 60.dp, top = 20.dp)
                                                .offset(x = 10.dp)
                                        ) {
                                            Image(
                                                painterResource(id = R.drawable.intake_arrow),
                                                modifier = Modifier.testTag("image_add_intake_arrow"),
                                                contentDescription = "image_add_intake_arrow",
                                            )

                                            Text(
                                                stringResource(R.string.tap_above_to_add_to_your_first_preset),
                                                Modifier.padding(start = 10.dp, end = 15.dp),
                                                fontFamily = RobotoFonts,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colorResource(R.color.settingsColorCoalText)
                                            )
                                        }
                                    } else {
                                        BoxWithConstraints {
                                            LazyRow(
                                                modifier = Modifier
                                                    .background(colorResource(R.color.lightGrayStandardBackground))
                                                    .width(maxWidth)
                                                    .padding(top = 5.dp),
                                                state = listState
                                            ) {
                                                items(chViewModel.currentBottleMenuItems) { bottle ->
                                                    BottleIconRenderView(
                                                        chViewModel = chViewModel,
                                                        bottle,
                                                        showName = true,
                                                        isIntake = false,
                                                        isClickable = true
                                                    )
                                                }
                                            }
                                        }

                                        if (chViewModel.newBottlesItemsAdded.size > 0) {
                                            coroutineScopeBottleList.launch {
                                                listState.animateScrollToItem(index = chViewModel.currentBottleMenuItems.size)
                                            }
                                        }
                                    }
                                }
                            }

                            // Intake bottles added view
                            Text(text = stringResource(R.string.intake),
                                Modifier.padding(start = 20.dp, top = 20.dp, bottom = 20.dp),
                                fontFamily = OswaldFonts,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.settingsColorCoalText)
                            )

                            Column(
                                modifier = Modifier.scrollable(state = intakeScrollState, orientation = Orientation.Horizontal)
                                    .fillMaxHeight()
                                    .defaultMinSize(minHeight = 200.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {

                                if (intakeBottleUpdated || !intakeBottleUpdated) {

                                    if (chViewModel.currentUserIntakeItems.size == 0) {
                                        if (isIntakeButtonUp) {
                                            updateIntakeState(IntakeButtonState.INTAKE_UP)
                                        }
                                        else {
                                            if ((chViewModel.totalWaterAmount == 0.0 || chViewModel.totalSodiumAmount == 0.0)
                                                && !isCancelButtonPressed
                                                && !chViewModel.isTabButtonPressed.value) {
                                                updateIntakeState(IntakeButtonState.INTAKE_DOWN)
                                            }
                                        }
                                        Column(
                                            modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                stringResource(R.string.tap_item_above_to_add_to_your_recent_intake),
                                                fontFamily = RobotoFonts,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center,
                                                color = colorResource(R.color.settingsColorCoalText)
                                            )

                                            Text(
                                                stringResource(R.string.long_press_item_for_partial_amounts),
                                                modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                                                fontFamily = RobotoFonts,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center,
                                                color = colorResource(R.color.settingsColorCoalText)
                                            )
                                        }
                                    } else if (chViewModel.currentUserIntakeItems.size < 2) {
                                        Row(
                                            modifier = Modifier.background(Color.White).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            BottleIconRenderView(chViewModel = chViewModel, chViewModel.currentUserIntakeItems[0], showName = true, isIntake = true, isClickable = true)

                                            Text(
                                                stringResource(R.string.tap_to_remove),
                                                Modifier.padding(start = 10.dp, end = 10.dp),
                                                fontFamily = RobotoFonts,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colorResource(R.color.settingsColorCoalText)
                                            )

                                        }
                                    } else if (chViewModel.currentUserIntakeItems.size < 3) {
                                        BoxWithConstraints {
                                            LazyRow(
                                                modifier = Modifier
                                                    .background(Color.White)
                                                    .width(maxWidth)
                                                    .padding(top = 5.dp)
                                            ) {
                                                items(chViewModel.currentUserIntakeItems) { bottle ->
                                                    BottleIconRenderView(
                                                        chViewModel = chViewModel,
                                                        bottle,
                                                        showName = true,
                                                        isIntake = true,
                                                        isClickable = true
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        BoxWithConstraints {
                                            LazyRow(
                                                modifier = Modifier
                                                    .background(Color.White)
                                                    .width(maxWidth)
                                                    .padding(top = 5.dp)
                                            ) {
                                                items(chViewModel.currentUserIntakeItems) { bottle ->
                                                    BottleIconRenderView(
                                                        chViewModel = chViewModel,
                                                        bottle,
                                                        showName = true,
                                                        isIntake = true,
                                                        isClickable = true
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Divider(
                                    Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                                    color = Color.LightGray,
                                    thickness = 2.dp)

                                if (chViewModel.totalWaterAmount > 0 || chViewModel.totalSodiumAmount > 0) {
                                    updateIntakeState(IntakeButtonState.INTAKE_SAVE)
                                }
                                else {
                                    if (isIntakeButtonUp) {
                                        updateIntakeState(IntakeButtonState.INTAKE_UP)
                                    }
                                }

                                if (chViewModel.totalWaterAmount / 29.574 >= 48.0) {
                                    Text(chViewModel.userPrefsData.getUserExceedWarningString(),
                                        Modifier.padding(start = 10.dp, end = 20.dp, bottom = 10.dp),
                                        fontFamily = RobotoFonts,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red)
                                }

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Water
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(stringResource(R.string.intake_water),
                                            Modifier.padding(top = 10.dp),
                                            fontFamily = OswaldFonts,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorResource(R.color.settingsColorCoalText))

                                        if ((chViewModel.totalWaterAmount >= 1000) || (chViewModel.totalSodiumAmount >= 1000)) {
                                            Text(String.format("%.1f", chViewModel.userPrefsData.getTotalWaterIntake(amount = chViewModel.totalWaterAmount)),
                                                Modifier.padding(
//                                                    start = 10.dp,
//                                                    end = 20.dp,
                                                    bottom = 10.dp
                                                ),
                                                fontFamily = RobotoFonts,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorResource(R.color.settingsColorCoalText))
                                        }
                                        else {
                                            Text(String.format("%.1f", chViewModel.userPrefsData.getTotalWaterIntake(amount = chViewModel.totalWaterAmount)),
                                                Modifier.padding(
                                                    start = 10.dp,
                                                    end = 20.dp,
                                                    bottom = 10.dp
                                                ),
                                                fontFamily = RobotoFonts,
                                                fontSize = 38.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorResource(R.color.settingsColorCoalText))
                                        }

                                        SetUnitButtonView(chViewModel, UnitType.WATER)

                                        Spacer(modifier = Modifier.height(30.dp))

                                        Row (
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val currentFluidDeficit = chViewModel.fluidDeficitInOz
                                            if (currentFluidDeficit.toFloat() <= 0) {
                                                recommendedWaterIntakePercentage = 0f
                                            }
                                            else {
                                                val mlToOz = chViewModel.totalWaterAmount / 29.574
                                                recommendedWaterIntakePercentage = ((mlToOz.toFloat() / currentFluidDeficit.toFloat()) * 100).roundToInt().toFloat()
                                                recommendedWaterIntakeDegrees = (recommendedWaterIntakePercentage.toDouble() / 100) * 360
                                            }

                                            RecommendedIntakePieView(endAngle = recommendedWaterIntakeDegrees.toFloat())

                                            Spacer(modifier = Modifier.width(20.dp))

                                            Text("$recommendedWaterIntakePercentage%",
                                                fontFamily = RobotoFonts,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = colorResource(R.color.settingsColorCoalText))
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Text(
                                            stringResource(R.string.intake_recommended),
                                            textAlign = TextAlign.Center,
                                            fontFamily = RobotoFonts,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colorResource(R.color.settingsColorCoalText))

//                                        Text(
//                                            stringResource(R.string.intake_intake),
//                                            textAlign = TextAlign.Center,
//                                            fontFamily = RobotoFonts,
//                                            fontSize = 12.sp,
//                                            fontWeight = FontWeight.Medium,
//                                            color = colorResource(R.color.settingsColorCoalText))
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Sodium
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            stringResource(R.string.intake_sodium),
                                            Modifier.padding(top = 10.dp),
                                            fontFamily = OswaldFonts,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorResource(R.color.settingsColorCoalText))

                                        if ((chViewModel.totalSodiumAmount >= 1000) || (chViewModel.totalWaterAmount >= 1000)) {
                                            Text(String.format("%.1f", chViewModel.userPrefsData.getTotalSodiumIntake(amount = chViewModel.totalSodiumAmount)),
                                                Modifier.padding(
                                                    bottom = 10.dp
                                                ),
                                                fontFamily = RobotoFonts,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorResource(R.color.settingsColorCoalText))
                                        }
                                        else {
                                            Text(String.format("%.1f", chViewModel.userPrefsData.getTotalSodiumIntake(amount = chViewModel.totalSodiumAmount)),
                                                Modifier.padding(
                                                    start = 10.dp,
                                                    end = 20.dp,
                                                    bottom = 10.dp
                                                ),
                                                fontFamily = RobotoFonts,
                                                fontSize = 38.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorResource(R.color.settingsColorCoalText))
                                        }

                                        SetUnitButtonView(chViewModel, UnitType.SODIUM)

                                        Spacer(modifier = Modifier.height(30.dp))

                                        Row (
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val currentSodiumDeficit = chViewModel.sweatSodiumDeficitInMg
                                            if (currentSodiumDeficit.toFloat() <= 0) {
                                                recommendedSodiumIntakePercentage = 0f
                                            }
                                            else {
                                                recommendedSodiumIntakePercentage = ((chViewModel.totalSodiumAmount.toFloat() / currentSodiumDeficit.toFloat()) * 100).roundToInt().toFloat()
                                                recommendedSodiumIntakeDegrees = (recommendedSodiumIntakePercentage.toDouble() / 100) * 360
                                            }

                                            RecommendedIntakePieView(endAngle = recommendedSodiumIntakeDegrees.toFloat())

                                            Spacer(modifier = Modifier.width(20.dp))

                                            Text("$recommendedSodiumIntakePercentage%",
                                                fontFamily = RobotoFonts,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = colorResource(R.color.settingsColorCoalText))

                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Text(stringResource(R.string.intake_recommended),
                                            textAlign = TextAlign.Center,
                                            fontFamily = RobotoFonts,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colorResource(R.color.settingsColorCoalText))

//                                        Text(stringResource(R.string.intake_intake),
//                                            textAlign = TextAlign.Center,
//                                            fontFamily = RobotoFonts,
//                                            fontSize = 12.sp,
//                                            fontWeight = FontWeight.Medium,
//                                            color = colorResource(R.color.settingsColorCoalText))
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                }
                            }
                        }
                    }
                },
                sheetPeekHeight = 0.dp
            ) {
                BgStatusView(chViewModel, ebsDeviceMonitor)

                LaunchedEffect(bottomSheetScaffoldState) {
                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                        isCancelButtonPressed = false
                        isIntakeButtonUp = false
                    } else {
                        updateIntakeState(IntakeButtonState.INTAKE_UP)
                        isIntakeButtonUp = true
                        chViewModel.isTabButtonPressed.value = false
                        onItemClick(items[0])
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }
            }
        }
    }
 }