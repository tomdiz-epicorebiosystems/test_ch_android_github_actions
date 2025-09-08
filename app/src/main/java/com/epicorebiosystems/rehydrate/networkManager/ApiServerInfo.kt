package com.epicorebiosystems.rehydrate.networkManager

import com.epicorebiosystems.rehydrate.modelData.ModelData


class ApiServerInfo(chModel: ModelData) {

    var chViewModel = chModel

    fun getServerBaseApi(): String {
        return if (chViewModel.serverSettings.value == 0) {
            ApiRoutes.PROD_BASE_URL
        } else {
            ApiRoutes.STAGING_BASE_URL
        }
    }

    fun getServerApiKey(): String {
        return if (chViewModel.serverSettings.value == 0) {
            ApiRoutes.PROD_CH_PHONE_API_KEY
        } else {
            ApiRoutes.STAGING_CH_PHONE_API_KEY
        }
    }

    fun getServerApiJwtKey(): String {
        return if (chViewModel.serverSettings.value == 0) {
            ApiRoutes.PROD_CH_PHONE_API_JWT_SECRET
        } else {
            ApiRoutes.STAGING_CH_PHONE_API_JWT_SECRET
        }
    }

    fun getAuth0Url(): String {
        return if (chViewModel.serverSettings.value == 0) {
            ApiRoutes.PROD_AUTH0_URL
        } else {
            ApiRoutes.STAGING_AUTH0_URL
        }
    }

    fun getClientId(): String {
        return if (chViewModel.serverSettings.value == 0) {
            ApiRoutes.PROD_CLIENT_ID
        } else {
            ApiRoutes.STAGING_CLIENT_ID
        }
    }

    fun getLoginContextUrl(): String {
        return if (chViewModel.serverSettings.value == 0) {
            "${ApiRoutes.PROD_BASE_URL}${ApiRoutes.LOGIN_CONTEXT_GET}"
        } else {
            "${ApiRoutes.STAGING_BASE_URL}${ApiRoutes.LOGIN_CONTEXT_GET}"
        }
    }

    fun getEnterpriseNameUrl(): String {
        return if (chViewModel.serverSettings.value == 0) {
            "${ApiRoutes.PROD_BASE_URL}${ApiRoutes.ENTERPRISE_NAME_GET}"
        } else {
            "${ApiRoutes.STAGING_BASE_URL}${ApiRoutes.ENTERPRISE_NAME_GET}"
        }
    }

    fun getSendCodeUrl(): String {
        return if (chViewModel.serverSettings.value == 0) {
            "${ApiRoutes.PROD_BASE_URL}${ApiRoutes.SEND_CODE_PUT}"
        } else {
            "${ApiRoutes.STAGING_BASE_URL}${ApiRoutes.SEND_CODE_PUT}"
        }
    }

    fun getAuthenticateCodeUrl(): String {
        return if (chViewModel.serverSettings.value == 0) {
            "${ApiRoutes.PROD_BASE_URL}${ApiRoutes.AUTHENTICATE_WITH_CODE_POST}"
        } else {
            "${ApiRoutes.STAGING_BASE_URL}${ApiRoutes.AUTHENTICATE_WITH_CODE_POST}"
        }
    }

    fun getUserHistoryUrl(): String {
        return if (chViewModel.serverSettings.value == 0) {
            "${ApiRoutes.PROD_BASE_URL}${ApiRoutes.USER_HISTORY_STATS_GET}"
        } else {
            "${ApiRoutes.STAGING_BASE_URL}${ApiRoutes.USER_HISTORY_STATS_GET}"
        }
    }

    fun getAvgSweatConcentrationUrl(): String {
        return if (chViewModel.serverSettings.value == 0) {
            "${ApiRoutes.PROD_BASE_URL}${ApiRoutes.AVG_SWEAT_CONCENTRATION_GET}"
        } else {
            "${ApiRoutes.STAGING_BASE_URL}${ApiRoutes.AVG_SWEAT_CONCENTRATION_GET}"
        }
    }

    fun getUpdateUserUrl(): String {
        return if (chViewModel.serverSettings.value == 0) {
            "${ApiRoutes.PROD_BASE_URL}${ApiRoutes.UPDATE_USER_PUT}"
        } else {
            "${ApiRoutes.STAGING_BASE_URL}${ApiRoutes.UPDATE_USER_PUT}"
        }
    }

    fun getUserInfoUrl(): String {
        return if (chViewModel.serverSettings.value == 0) {
            "${ApiRoutes.PROD_BASE_URL}${ApiRoutes.USER_PRIVACY}"
        } else {
            "${ApiRoutes.STAGING_BASE_URL}${ApiRoutes.USER_PRIVACY}"
        }
    }

}