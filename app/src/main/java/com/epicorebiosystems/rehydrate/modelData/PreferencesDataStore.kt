package com.epicorebiosystems.rehydrate.modelData

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Class that handles saving and retrieving CH preferences
 */
class PreferencesDataStore(private val context: Context) {

    companion object {
        private const val CH_PREFERENCES_NAME = "ch_preferences"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(CH_PREFERENCES_NAME)
        //val SERVER_SETTINGS = intPreferencesKey("server_settings")
        val USER_EMAIL = stringPreferencesKey("user_email")
        //val USER_MENU_BOTTLE_LIST = stringPreferencesKey("user_menu_bottle_list")
        val USER_TOTAL_BOTTLE_MENU_ITEMS = intPreferencesKey("user_total_bottle_menu_items")
        val USER_BUTTON_PRESS_WATER_INTAKE = booleanPreferencesKey("button_press_for_water_intake")
        val USER_BUTTON_PRESS_WATER_VALUE = intPreferencesKey("button_press_for_water_value")
        val USER_PASSIVE_WATER_LOSS = booleanPreferencesKey("user_passive_water_loss")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val USE_UNITS = intPreferencesKey("use_units")    // 0 = metric / 1 = imperial
        val USER_WEIGHT_LB = stringPreferencesKey("user_weight_lb")
        val USER_WEIGHT_KG = stringPreferencesKey("user_weight_kg")
        val USER_HEIGHT_FT = stringPreferencesKey("user_height_ft")
        val USER_HEIGHT_IN = stringPreferencesKey("user_height_in")
        val USER_HEIGHT_CM = stringPreferencesKey("user_height_cm")
        val USER_GENDER = stringPreferencesKey("user_height_gender")
        val CH_DEVICE_NAME = stringPreferencesKey("ch_device_name")
        val USER_ID = stringPreferencesKey("user_id")
        val ENTERPRISE_ID = stringPreferencesKey("enterprise_id")
        val JWT_ENTERPRISE_ID = stringPreferencesKey("jwt_enterprise_id")
        val ENTERPRISE_NAME = stringPreferencesKey("enterprise_name")
        val SITE_NAME = stringPreferencesKey("site_name")
        val JWT_SITE_ID = stringPreferencesKey("jwt_site_id")
        val NOTIFICATION_STATE = stringPreferencesKey("notification_state")
        val LAST_APP_UPDATE_NOTIFICATION = intPreferencesKey("last_app_update_notification")
        val USER_SODIUM_CAP_MG = intPreferencesKey("user_sodium_cap_mg")
        val CURRENT_AUTH_API_SERVER = intPreferencesKey("current_auth_api_server")
        val CURRENT_AUTH_USER_EMAIL = stringPreferencesKey("current_auth_user_email")
        val CURRENT_AUTH_USER_ROLE = stringPreferencesKey("current_auth_user_role")
    }
    
    val getCurrentAuthUserRole: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_AUTH_USER_ROLE] ?: ""
    }

    suspend fun saveCurrentAuthUserRole(jwtSiteId: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_AUTH_USER_ROLE] = jwtSiteId
        }
    }

    val getJwtSiteId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[JWT_SITE_ID] ?: ""
    }

    suspend fun saveJwtSiteId(jwtSiteId: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_SITE_ID] = jwtSiteId
        }
    }

    val getEnterpriseName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ENTERPRISE_NAME] ?: ""
    }

    suspend fun saveEnterpriseName(eName: String) {
        context.dataStore.edit { preferences ->
            preferences[ENTERPRISE_NAME] = eName
        }
    }

    val getSiteName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SITE_NAME] ?: ""
    }

    suspend fun saveSiteName(siteName: String) {
        context.dataStore.edit { preferences ->
            preferences[SITE_NAME] = siteName
        }
    }

    val getJwtEnterpriseId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[JWT_ENTERPRISE_ID] ?: ""
    }

    suspend fun saveJwtEnterpriseId(jwtEId: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_ENTERPRISE_ID] = jwtEId
        }
    }

    val getCurrentAuthUserEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_AUTH_USER_EMAIL] ?: ""
    }

    suspend fun saveCurrentAuthUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_AUTH_USER_EMAIL] = email
        }
    }

    val getEnterpriseId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ENTERPRISE_ID] ?: ""
    }

    suspend fun saveEnterpriseId(eId: String) {
        context.dataStore.edit { preferences ->
            preferences[ENTERPRISE_ID] = eId
        }
    }
/*
    val getServerSettings: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SERVER_SETTINGS] ?: 0
    }

    suspend fun saveServerSettings(server: Int) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_SETTINGS] = server
        }
    }
*/
    val getCurrentAuthAPIServer: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_AUTH_API_SERVER] ?: 0
    }

    suspend fun saveCurrentAuthAPIServer(server: Int) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_AUTH_API_SERVER] = server
        }
    }

    val getUserId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_ID] ?: ""
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    val getCHDeviceName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CH_DEVICE_NAME] ?: ""
    }

    suspend fun saveCHDeviceName(deviceName: String) {
        context.dataStore.edit { preferences ->
            preferences[CH_DEVICE_NAME] = deviceName
        }
    }

    val getUserEmailAddress: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL] ?: ""
    }

    suspend fun saveUserEmailAddress(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
        }
    }

//    val getUserBottleMenuItems: Flow<String> = context.dataStore.data.map { preferences ->
//        preferences[USER_MENU_BOTTLE_LIST] ?: ""
//    }

//    suspend fun saveUserBottleMenuItems(bottles: String) {
//        context.dataStore.edit { preferences ->
//            preferences[USER_MENU_BOTTLE_LIST] = bottles
//        }
//    }

    val getUserTotalBottleMenuItems: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[USER_TOTAL_BOTTLE_MENU_ITEMS] ?: 0
    }

    suspend fun saveUserTotalBottleMenuItems(num: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_TOTAL_BOTTLE_MENU_ITEMS] = num
        }
    }

    val getButtonPressWaterIntakeState: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_BUTTON_PRESS_WATER_INTAKE] ?: true
    }

    suspend fun saveButtonPressWaterIntakeState(state: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_BUTTON_PRESS_WATER_INTAKE] = state
        }
    }

    val getButtonPressWaterIntakeValue: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[USER_BUTTON_PRESS_WATER_VALUE] ?: 500
    }

    suspend fun saveButtonPressWaterIntakeValue(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_BUTTON_PRESS_WATER_VALUE] = value
        }
    }

    val getPassiveWaterLossState: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_PASSIVE_WATER_LOSS] ?: true
    }

    suspend fun savePassiveWaterLossState(state: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_PASSIVE_WATER_LOSS] = state
        }
    }

    val getOnBoardingComplete: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETE] ?: false
    }

    suspend fun saveOnBoardingComplete(done: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETE] = done
        }
    }

    val getUnits: Flow<Int> = context.dataStore.data.map { preferences ->
        Log.d("PREFS_GET ", "units = $preferences")
        preferences[USE_UNITS] ?: 1
    }

    suspend fun saveUnits(units: Int) {
        context.dataStore.edit { preferences ->
            Log.d("PREFS_SAVE ", "units = $preferences")
            preferences[USE_UNITS] = units
        }
    }

    val getUserWeightLb: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_WEIGHT_LB] ?: "165"
    }

    suspend fun saveUserWeightLb(weight: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_WEIGHT_LB] = weight
        }
    }

    val getUserWeightKg: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_WEIGHT_KG] ?: "75"
    }

    suspend fun saveUserWeightKg(weight: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_WEIGHT_KG] = weight
        }
    }

    val getUserHeightFt: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_HEIGHT_FT] ?: "5"
    }

    suspend fun saveUserHeightFt(feet: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_HEIGHT_FT] = feet
        }
    }

    val getUserHeightIn: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_HEIGHT_IN] ?: "9"
    }

    suspend fun saveUserHeightIn(inches: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_HEIGHT_IN] = inches
        }
    }

    val getUserGender: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_GENDER] ?: "Male"
    }

    suspend fun saveUserGender(gender: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_GENDER] = gender
        }
    }

    val getUserHeightCm: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_HEIGHT_CM] ?: "175"
    }

    suspend fun saveUserHeightCm(cm: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_HEIGHT_CM] = cm
        }
    }

    val getNotificationState: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_STATE] ?: ""
    }

    suspend fun saveNotificationState(jsonState: String) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_STATE] = jsonState
        }
    }

    val getLastAppUpdateNotificationState: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[LAST_APP_UPDATE_NOTIFICATION] ?: 0
    }

    suspend fun saveLastAppUpdateNotificationState(date: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_APP_UPDATE_NOTIFICATION] = date
        }
    }

    val getUserSodiumCap: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[USER_SODIUM_CAP_MG] ?: 0
    }

    suspend fun saveUserSodiumDeficitCap(num: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_SODIUM_CAP_MG] = num
        }
    }

}