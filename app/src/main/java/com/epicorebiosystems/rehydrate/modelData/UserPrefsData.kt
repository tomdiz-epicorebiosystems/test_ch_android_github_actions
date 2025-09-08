package com.epicorebiosystems.rehydrate.modelData

import androidx.compose.runtime.State
import com.epicorebiosystems.rehydrate.R
import java.lang.Math.round
import java.math.RoundingMode
import kotlin.math.roundToInt

class UserPrefsData() {

    lateinit var chViewModel: ModelData

    fun getUnits(): State<Int> {
        return chViewModel.currentUnits
    }

    fun getUserWeightString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            "kg"
        } else {
            "lb."
        }
    }

    fun getUserHeightMajorUnitString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            "cm"
        } else {
            "ft."
        }
    }

    fun getUserHeightMinorUnitString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            "cm"
        } else {
            "in."
        }
    }

    fun setUserWeight(weight: String) {
        //if (chViewModel.currentUnits.value == 0) {
            chViewModel.userWeightKg.value = weight
            val metriclb = (weight.toDoubleOrNull() ?: 23.0) * 2.205
            chViewModel.userWeightLb.value = "%d".format(metriclb.roundToInt())
        //} else {
        //    chViewModel.userWeightLb.value = weight
        //    val metricKg = (weight.toDoubleOrNull() ?: 50.0) / 2.2
        //    chViewModel.userWeightKg.value = "%d".format(metricKg.roundToInt())
        //}
    }

    fun setUserWeightFromSensor(weightInKg: UShort) {
        chViewModel.userWeightKg.value = "$weightInKg"
        val metriclb = weightInKg.toDouble() * 2.205
        chViewModel.userWeightLb.value = "%.0f".format(metriclb.roundToInt())
    }

    fun setUserWeight(weight: String, units: Int): String {
        return if (units == 1) {
            val metriclb = (weight.toDoubleOrNull() ?: 23.0) * 2.205
            chViewModel.userWeightLb.value = "%.0f".format(round(metriclb))
            chViewModel.userWeightLb.value
        } else {
            val metricKg = (weight.toDoubleOrNull() ?: 50.0) / 2.2
            chViewModel.userWeightKg.value = "%.0f".format(round(metricKg))
            chViewModel.userWeightKg.value
        }
    }

    fun setUserHeightFeet(feet: String) {
        chViewModel.userHeightFt.value = feet
        if (chViewModel.userHeightIn.value == "12") {
            chViewModel.userHeightIn.value = "0"
            chViewModel.userHeightFt.value = "${feet.toInt() + 1}"
        }
    }

    fun setUserHeightInch(inches: String) {
        chViewModel.userHeightIn.value = inches
    }

    fun setUserHeightCm(cm: Byte) {
        chViewModel.userHeightCm.value = cm.toString()
    }

    fun getTotalWaterIntake(amount: Double): Double {
        return handleUserSweatConversionMl(ml = amount)
    }

    fun handleUserSweatConversionOz(oz: Double): Double {
        return if (chViewModel.currentUnits.value == 0) {
            (oz * 29.574).roundToInt().toDouble()
        } else {
            oz
        }
    }

    fun getTotalSodiumIntake(amount: Double): Double {
        return amount
    }

    fun handleUserSweatConversionMl(ml: Double): Double {
        return if (chViewModel.currentUnits.value == 0) {
            ml
        } else {
            ((ml / 29.574).toBigDecimal().setScale(1, RoundingMode.HALF_UP)).toDouble()
        }
    }

    fun getUserSweatUnitString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            "ml"
        }
        else {
            "oz"
        }
    }

    fun getUserSweatUnitFullString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            chViewModel.applicationContext!!.getString(R.string.milliliters)
        } else {
            chViewModel.applicationContext!!.getString(R.string.ounces)
        }
    }

    fun getUserSweatUnitTodayButtonString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            chViewModel.applicationContext!!.getString(R.string.milliliters)
        } else {
            chViewModel.applicationContext!!.getString(R.string.ounces)
        }
    }

    fun getUserSodiumUnitFullString(): String {
        return chViewModel.applicationContext!!.getString(R.string.milligrams)
    }

    fun getUserSodiumUnitTodayButtonString(): String {
        return chViewModel.applicationContext!!.getString(R.string.milligrams)
    }

    fun getUserTempUnitString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            "\u00B0C"
        } else {
            "\u00B0F"
        }
    }

    fun getUserSodiumUnitString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            "mg"
        }
        else {
            "mg"
        }
    }

    fun getUserTemperature(fahrenheit: Double): Float {
        return if (chViewModel.currentUnits.value == 0) {
            if(fahrenheit < 32.0) 0.0f else (((fahrenheit - 32) / 1.8000).toFloat())
        } else {
            fahrenheit.toFloat()
        }
    }

    fun getUserTemperatureInF(celsius: Double): Double {
        return (celsius  * (9/5)) + 32
    }

    fun getUserTemperatureC(celsius: Double): Double {
        return if (chViewModel.currentUnits.value == 0) {
            celsius
        } else {
            //return ((celsius * 0.005) * 1.8 + 32.0).toBigDecimal().setScale(1, RoundingMode.DOWN).toDouble()
            return (celsius * (9.0 / 5)) + 32//((celsius  * (9/5)) + 32)//.toFloat()
        }
    }

    fun getFluidDeficitString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            "10000+"
        }
        else {
            "338+"
        }
    }

    fun getFluidDeficitAlertString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            chViewModel.applicationContext!!.getString(R.string.please_don_t_exceed_1500ml)
        }
        else {
            chViewModel.applicationContext!!.getString(R.string.please_don_t_exceed_48oz)
        }
    }

    fun getUserExceedWarningString(): String {
        return if (chViewModel.currentUnits.value == 0) {
            chViewModel.applicationContext!!.getString(R.string.please_don_t_exceed_1500ml)
        }
        else {
            chViewModel.applicationContext!!.getString(R.string.please_don_t_exceed_48oz)
        }
    }

    fun handleUserSodiumConversion(mg: UShort): Double {
        return if (chViewModel.currentUnits.value == 0) {
            mg.toDouble()
        }
        else {
            mg.toDouble()
        }
    }

    fun handleUserSweatConversion(oz: Double): Double {
        return if (chViewModel.currentUnits.value == 0) {
            (oz * 29.574).roundToInt().toDouble()
        }
        else {
            oz
        }
    }

}