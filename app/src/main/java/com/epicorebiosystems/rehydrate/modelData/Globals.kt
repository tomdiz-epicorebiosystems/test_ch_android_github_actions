package com.epicorebiosystems.rehydrate.modelData

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.datadog.android.rum.GlobalRumMonitor
import com.datadog.android.rum.RumErrorSource
import com.epicorebiosystems.rehydrate.IntakeButtonState
import com.epicorebiosystems.rehydrate.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.regex.Pattern
import kotlin.math.roundToInt

fun HandleMenuAddOption(chViewModel: ModelData, intakeButtonState: IntakeButtonState) {
    if (intakeButtonState == IntakeButtonState.INTAKE_ADD) {
        if (chViewModel.currentBottleListSelections.isNotEmpty()) {
            addSelectedBottlesMenuItem(chViewModel)
        }
        else {
            addNewUserBottleMenuItem(chViewModel)
        }
        chViewModel.currentBottleListSelections = mutableMapOf<Int, String>()
    }
}

fun addNewUserBottleMenuItem(chViewModel: ModelData) {
    // Add the new bottle the user created
    print(chViewModel.userBottleMenuItems)
    if (chViewModel.newUserBottle.sodiumAmount == 0F)  {
        chViewModel.newUserBottle.sodiumAmount = 0F
    }
    if (chViewModel.newUserBottle.waterAmount == 0F)  {
        chViewModel.newUserBottle.waterAmount = 0F
    }

    if (chViewModel.waterAmountEnterManual.isEmpty() && chViewModel.sodiumAmountEnterManual.isEmpty() && chViewModel.manualUserBottle.name.isEmpty()) {
        return
    }
    else {
        if (chViewModel.waterAmountEnterManual.isEmpty()) chViewModel.waterAmountEnterManual = "0"
        if (chViewModel.sodiumAmountEnterManual.isEmpty()) chViewModel.sodiumAmountEnterManual = "0"
    }

    // Handle manually entered bottle
    if (chViewModel.waterAmountEnterManual != "0") {
        val ml = chViewModel.waterAmountEnterManual.toDouble()
        val conv = chViewModel.userPrefsData.handleUserSweatConversionMl(ml)
        chViewModel.newUserBottle.waterAmount = conv.toFloat()
        if (chViewModel.userPrefsData.getUnits().value == 1) {
            chViewModel.newUserBottle.waterAmount = (chViewModel.waterAmountEnterManual.toFloat() * 29.574).roundToInt().toFloat()
        }
        else {
            chViewModel.newUserBottle.waterAmount = chViewModel.waterAmountEnterManual.toFloat()
        }
        chViewModel.newUserBottle.name = chViewModel.manualUserBottle.name
        chViewModel.newUserBottle.sodiumAmount = 0F
        chViewModel.newUserBottle.sodiumSize = "mg"
        chViewModel.newUserBottle.waterSize = "ml"
        chViewModel.newUserBottle.barcode = ""
        chViewModel.newUserBottle.image_name = chViewModel.manualUserBottle.image_name
        chViewModel.waterAmountEnterManual = ""
    }
    if (chViewModel.sodiumAmountEnterManual != "0") {
        chViewModel.newUserBottle.sodiumAmount = chViewModel.sodiumAmountEnterManual.toFloat()
        chViewModel.newUserBottle.name = chViewModel.manualUserBottle.name
        chViewModel.newUserBottle.image_name = chViewModel.manualUserBottle.image_name
        chViewModel.sodiumAmountEnterManual = ""
    }

    chViewModel.manualUserBottle.name = ""
    chViewModel.manualUserBottle.image_name = ""
    chViewModel.waterAmountEnterManual = "0"
    chViewModel.sodiumAmountEnterManual = "0"

    // Only add new item when either water or sodium amount is not 0
    if (chViewModel.newUserBottle.waterAmount != 0F || chViewModel.newUserBottle.sodiumAmount != 0F) {

        // Use a bigger number to differentiate the IDs of user created and scanned drinks from preset list.
        chViewModel.newUserBottle.id = 100000 + chViewModel.userTotalBottleMenuItems.value

        // Used for new bottle intake border so users know new item
        chViewModel.newBottlesAdded.add(chViewModel.newUserBottle.id)

        chViewModel.currentBottleMenuItems.add(chViewModel.newUserBottle)
        chViewModel.userTotalBottleMenuItems.value += 1
        chViewModel.updateUserTotalBottleMenuItems(chViewModel.userTotalBottleMenuItems.value)

        chViewModel.newBottlesItemsAdded.add(chViewModel.newUserBottle.id)

        val gson = Gson()
        val jsonString: String = gson.toJson(chViewModel.currentBottleMenuItems)

        GlobalRumMonitor.get().addError("addNewUserBottleMenuItem()", RumErrorSource.LOGGER, null, mapOf("addNewUserBottleMenuItem" to "$jsonString"))

        chViewModel.userBottleMenuItems = jsonString
        chViewModel.writeJSONtoFile("users_bottle_list.json", chViewModel.currentBottleMenuItems)
        //chViewModel.updateUserBottleMenuItems(jsonString)
        // Reinitialize newUserBottle
        chViewModel.newUserBottle = BottleData(id = 0, name = "", image_name = chViewModel.initialPreviewBottleName, barcode = chViewModel.newUserBottle.barcode, sodiumAmount = 0F, sodiumSize = "mg", waterAmount = 0F, waterSize = "oz")
    }
}

fun addSelectedBottlesMenuItem(chViewModel: ModelData) {
    for (bottleId in chViewModel.currentBottleListSelections.keys) {
        // Get bottle from bottles
        val index = chViewModel.bottleList.indexOfFirst { it.id == bottleId }
        if (index != -1) {
            val newUserBottle = chViewModel.bottleList[index]
            println(chViewModel.userBottleMenuItems)
            // Don't append if already in list
            if (!bottleAlreadyExistInList(chViewModel, newUserBottle.id)) {
                chViewModel.currentBottleMenuItems.add(newUserBottle)
                //chViewModel.userTotalBottleMenuItems.value++
                // Used for new bottle intake border so users know new item
                chViewModel.newBottlesAdded.add(newUserBottle.id)
                chViewModel.newBottlesItemsAdded.add(newUserBottle.id)
            } else {
                println("Bottle already exists")
            }
        } else {
            println("Bottle not found")
        }
    }
    // Write out new user bottles
    val gson = Gson()
    val jsonString: String = gson.toJson(chViewModel.currentBottleMenuItems)
    chViewModel.userBottleMenuItems = jsonString
    chViewModel.writeJSONtoFile("users_bottle_list.json", chViewModel.currentBottleMenuItems)
    // Reinitialize newUserBottle
    chViewModel.newUserBottle = BottleData(id = 0, name = "", image_name = chViewModel.initialPreviewBottleName, barcode = "", sodiumAmount=  0F, sodiumSize = "mg", waterAmount = 0F, waterSize = "oz")
}

fun deleteUserBottleMenuItem(chViewModel: ModelData, id: Int) {
    try {
        chViewModel.currentBottleMenuItems.withIndex().toList().forEach { (index, bottle) ->
            if (bottle.id == id) {
                chViewModel.currentBottleMenuItems.removeAt(index)
            }
        }
        val gson = Gson()
        val jsonString: String = gson.toJson(chViewModel.currentBottleMenuItems)
        chViewModel.userBottleMenuItems = jsonString
        chViewModel.writeJSONtoFile("users_bottle_list.json", chViewModel.currentBottleMenuItems)
        //val jsonString = Json.encodeToString(chViewModel.currentBottleMenuItems)
        //chViewModel.updateUserBottleMenuItems(jsonString)
        //chViewModel.userBottleMenuItems = String(jsonString.toByteArray(), StandardCharsets.UTF_8)
    } catch (error: Exception) {
        println(error.localizedMessage)
        Log.d("JSONEncoder", error.localizedMessage)
    }
}

fun bottleAlreadyExistInList(chViewModel: ModelData, id: Int): Boolean {
    for (bottle in chViewModel.currentBottleMenuItems) {
        if (bottle.id == id) {
            return true
        }
    }
    return false
}

fun Context.dial(phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
        startActivity(intent)
    } catch (t: Throwable) {
        // TODO: Handle potential exceptions
    }
}

fun Context.sendMail() {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "vnd.android.cursor.item/email" // or "message/rfc822"
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // TODO: Handle case where no email app is available
    } catch (t: Throwable) {
        // TODO: Handle potential other type of exceptions
    }
}

fun isValidDeviceSerialNumber(serialNumber: String): Boolean {
    val snRegEx = "^[A-Z0-9]{8}(?!.*-)"
    val snPattern = Pattern.compile(snRegEx)
    val snMatcher = snPattern.matcher(serialNumber)
    val isValidSN = snMatcher.matches()
    return isValidSN
}

fun getCurrentDeviceNetworkImage(isNetworkConnected: Boolean, isDeviceConnected: Boolean, isFileUploading: Boolean): Int {
    //Log.d("getCurrentDeviceImage", "$isNetworkConnected $isDeviceConnected $isFileUploading")
    return if (isFileUploading) {
        R.drawable.icon_device_syncing
    } else if (isNetworkConnected && isDeviceConnected) {
        R.drawable.icon_device_check
    } else if (isNetworkConnected && !isDeviceConnected) {
        R.drawable.icon_device_no_connex
    } else if (!isNetworkConnected && isDeviceConnected) {
        R.drawable.icon_device_no_net
    } else {
        R.drawable.icon_device_gray
    }
}

fun jsonStringToMapWithGson(json: String): HashMap<String, Boolean> {
    val gson = Gson()
    val type = object : TypeToken<Map<String, Any>>() {}.type
    return if (json.isEmpty()) {
        HashMap()
    }
    else {
        val map: Map<String, Boolean> = gson.fromJson(json, type)
        return map.toMutableMap() as HashMap<String, Boolean>
    }
}

// Add to a List
operator fun <T> Collection<T>.plus(element: T): List<T> {
    val result = ArrayList<T>(size + 1)
    result.addAll(this)
    result.add(element)
    return result
}

fun getCHDeviceBatteryLevel(days: Int): Int {
    // 30 days total battery life or is there a better way
    if (days <= 5) {
        return R.drawable.icon_battery_0
    }
    else if (days <= 30) {
        return R.drawable.icon_battery_1
    }
    else if (days <= 40) {
        return R.drawable.icon_battery_2
    }
    else if (days <= 50) {
        return R.drawable.icon_battery_3
    }

    return R.drawable.icon_battery_4
}

// Battery life in days lookup table
// --------------------------- 0     1     2     3     4     5     6     7     8     9    10     11    12    13    14    15    16    17    18    19    20    21    22    23    24    25    26    27    28    29    30    31    32    33    34    35
val batteryLifeLookUpTable = listOf(1.90, 2.04, 2.27, 2.36, 2.42, 2.49, 2.53, 2.57, 2.59, 2.62, 2.64, 2.66, 2.67, 2.68, 2.69, 2.70, 2.71, 2.72, 2.73, 2.73, 2.74, 2.75, 2.75, 2.76, 2.77, 2.78, 2.79, 2.79, 2.80, 2.80, 2.80, 2.80, 2.80, 2.80, 2.81, 2.95)

fun getBatteryLifeLeftInDays(battLevelInV: Double): Int {
    // If battery data is not available yet right after power up, set battery life to maximum 70 days.
    if (battLevelInV == 0.0) {
        return 70
    }
    // Calculate number of days in battery life with lookup table
    var numberOfDaysLeft = 0
    for (i in (batteryLifeLookUpTable.indices).reversed()) {
        if (battLevelInV > batteryLifeLookUpTable[i]) {
            numberOfDaysLeft = i * 2
            break
        }
    }

    GlobalRumMonitor.get().addError("getBatteryLifeLeftInDays()", RumErrorSource.LOGGER, null, mapOf("numberOfDaysLeft" to "$numberOfDaysLeft"))

    return numberOfDaysLeft
}

fun getChartSessionSessionStart(epoch: UInt): String {
    val convert = epoch.toLong()
    if (convert == 0L) {
        return ""
    }

    // Convert epoch to Instant
    val instant = Instant.fromEpochSeconds(convert)

    // Convert Instant to LocalDateTime in the system's default time zone
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    // Check the year to avoid "1969" or "1970" dates
    if (localDateTime.year == 1969 || localDateTime.year == 1970) {
        return ""
    }

    // Format the time as "h:mm a" (e.g., "3:45 PM")
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    return localDateTime.toJavaLocalDateTime().format(formatter)
}

fun getChartSessionHour(epoch: UInt): Int {
    // Convert epoch to Instant
    val instant = Instant.fromEpochSeconds(epoch.toLong())

    // Convert Instant to LocalDateTime in the system's default time zone
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    // Retrieve and return the hour (in 24-hour format)
    return localDateTime.hour
}

fun getChartDateTime(epoch: UInt, seconds: UShort): Date {
    // Convert the epoch time to Instant
    val instant = Instant.fromEpochSeconds(epoch.toLong())

    // Add the specified seconds to the Instant
    val updatedInstant = instant.plus(seconds.toLong(), DateTimeUnit.SECOND)

    // Convert the updated Instant to java.util.Date
    return Date.from(updatedInstant.toJavaInstant())
}

fun validateEmail(email: String): Boolean {
    val regex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}".toRegex()
    return regex.matches(email)
}

fun isValidEnterpriseCode(code: String): Boolean {
    val codeRegEx = "[a-zA-Z0-9]{3,4}-[a-zA-Z0-9]{3,4}"
    val codePattern = Pattern.compile(codeRegEx)
    val codeMatcher = codePattern.matcher(code)
    return codeMatcher.matches()
}

fun isCHArmBand(version: String): Boolean {
    // Extract the major version prefix, e.g., "v3" from "v3.233"
    val majorVersion = version.split(".").firstOrNull() ?: return false

    // Return true for v6, v7; false for v3, v4, v5 or others
    return when (majorVersion) {
        "v6", "v7" -> true
        "v3", "v4", "v5" -> false
        else -> false
    }
}
