package com.epicorebiosystems.rehydrate.tabViews.IntakeViews

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.datadog.android.compose.trackClick
import com.epicorebiosystems.rehydrate.IntakeButtonState
import com.epicorebiosystems.rehydrate.R
import com.epicorebiosystems.rehydrate.modelData.BottleData
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.sharedViews.BottleIconRenderView
import com.epicorebiosystems.rehydrate.ui.theme.JostVariableFonts
import com.epicorebiosystems.rehydrate.ui.theme.OswaldFonts
import com.epicorebiosystems.rehydrate.ui.theme.RobotoRegularFonts

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun BottleListScreen(chViewModel: ModelData, navController: NavController, updateIntakeState: (IntakeButtonState) -> Unit) {
    val searchResults by chViewModel.bottleListSearchResults.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.select_item_s),
                            fontFamily = OswaldFonts,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.settingsColorCoalText)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = trackClick(targetName = "BottleListScreen back pressed") {
                                updateIntakeState(IntakeButtonState.INTAKE_UP)
                                navController.navigateUp()
                            }
                        ) {
                            Image(
                                painterResource(R.drawable.baseline_chevron_left_24),
                                modifier = Modifier.testTag("image_chevon_left"),
                                contentDescription = "image_chevon_left",
                                colorFilter = ColorFilter.tint(colorResource(R.color.settingsColorCoalText))
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = trackClick(targetName = "BottleListScreen close pressed") {
                                updateIntakeState(IntakeButtonState.INTAKE_UP)
                                navController.navigateUp()
                            }
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 20.dp),
                                imageVector = Icons.Filled.Close,
                                contentDescription = "bottle list close"
                            )
                        }
                    }
                )

                SearchBar(chViewModel)
            }
        }
    ) {
        LaunchedEffect(updateIntakeState) {
            updateIntakeState(IntakeButtonState.INTAKE_CANCEL)
        }

        LazyColumn(
            modifier = Modifier
                .padding(top = 40.dp, bottom = 155.dp)
                .fillMaxSize()
                .offset(y = 80.dp)
                .background(Color.LightGray),
            contentPadding = PaddingValues(5.dp)
        ) {
            itemsIndexed(searchResults) { _, bottle ->
                //val unit = chViewModel.dataStore.getUnits.collectAsState(initial = 1)
                var selected by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = 5.dp,
                    backgroundColor = if (selected) colorResource(R.color.ButtonListRowSelectColor) else Color.White,
                    onClick = trackClick(targetName = "BottleListScreen adding items") {
                        selected = !selected
                        if (selected) {
                            if (chViewModel.currentBottleListSelections.isEmpty()) {
                                updateIntakeState(IntakeButtonState.INTAKE_ADD)
                            }

                            chViewModel.currentBottleListSelections[bottle.id] = "1"

                            if (chViewModel.currentBottleListSelections.count() == 1) {
                                val bottleName = bottle.name
                                val imageName = bottle.image_name
                                val sodiumAmount = bottle.sodiumAmount
                                val sodiumSize = bottle.sodiumSize
                                val waterAmount = bottle.waterAmount
                                val waterSize = bottle.waterSize
                                val barcode = bottle.barcode
                                Log.d("BOTTLE SELECTED", "Bottle name = ${bottleName}")
                                chViewModel.newUserBottle = BottleData(
                                    id = 0,
                                    name = bottleName,
                                    image_name = imageName,
                                    barcode = barcode,
                                    sodiumAmount = sodiumAmount,
                                    sodiumSize = sodiumSize,
                                    waterAmount = waterAmount,
                                    waterSize = waterSize
                                )
                            }
                        } else {
                            val keyExists =
                                chViewModel.currentBottleListSelections[bottle.id] != null
                            if (keyExists) {
                                chViewModel.currentBottleListSelections.remove(bottle.id)
                            }

                            if (chViewModel.currentBottleListSelections.isEmpty()) {
                                updateIntakeState(IntakeButtonState.INTAKE_CANCEL)
                            }
                        }
                    }
                ) {
                    Row {
                        BottleIconRenderView(
                            chViewModel = chViewModel,
                            bottle,
                            showName = false,
                            isIntake = false,
                            isClickable = false
                        )
                        Column(Modifier.padding(8.dp)) {
                            Text(
                                text = bottle.name,
                                fontFamily = JostVariableFonts,
                                fontSize = 20.sp,
                            )
                            if (bottle.sodiumAmount > 0.0) {
                                Text(
                                    stringResource(R.string.sodium) + String.format(
                                        "%.1f",
                                        bottle.sodiumAmount
                                    ) + " mg",
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Light,
                                )
                            } else {
                                Text(
                                    "",
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Light,
                                )
                            }
                            if (bottle.waterAmount > 0.0) {
                                Text(
                                    stringResource(R.string.water) + String.format(
                                        "%.1f",
                                        chViewModel.userPrefsData.handleUserSweatConversionMl(ml = (bottle.waterAmount).toDouble())
                                    ) + " " + chViewModel.userPrefsData.getUserSweatUnitString(),
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Light,
                                )
                            } else {
                                Text(
                                    "",
                                    fontFamily = RobotoRegularFonts,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Light,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(chViewModel: ModelData) {
    // Immediately update and keep track of query from text field changes.
    var query: String by rememberSaveable { mutableStateOf("") }
    var showClearIcon by rememberSaveable { mutableStateOf(false) }

    if (query.isEmpty()) {
        showClearIcon = false
    } else if (query.isNotEmpty()) {
        showClearIcon = true
    }

    TextField(
        value = query,
        onValueChange = { onQueryChanged ->
            query = onQueryChanged
            if (onQueryChanged.isNotEmpty()) {
                // Pass latest query to refresh search results.
                chViewModel.onSearchQueryChange(onQueryChanged)
            }
        },
        maxLines = 1,
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
        },
        trailingIcon = {
            if (showClearIcon) {
                IconButton(onClick = trackClick(targetName = "BottleListScreen search query") {
                    query = ""
                    chViewModel.onSearchQueryChange(query)
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        tint = MaterialTheme.colors.onBackground,
                        contentDescription = "Clear icon"
                    )
                }
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent
        ),
        placeholder = {
            Text(stringResource(R.string.search))
        },
        modifier = Modifier
            .background(color = Color.White, shape = RectangleShape)
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    )
}