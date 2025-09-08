package com.epicorebiosystems.rehydrate.networkManager

import android.content.Context
import android.util.Log
import com.auth0.android.jwt.JWT
import com.datadog.android.rum.GlobalRumMonitor
import com.datadog.android.rum.RumErrorSource
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.networkManager.ApiClient.client
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.DisconnectEvent
import com.epicorebiosystems.rehydrate.nordicsemi.uart.view.OnRunInput
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.toByteArray
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.util.InternalAPI
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.roundToInt

enum class ServerErrorCodes(val code: Int, val reason: String) {
    OK(200, "OK"),
    CREATED(201, "Created"),
    AUTHORIZATION_ERROR(401, "User does not have access to the server."),
    PERMISSION_DENIED(403, "User does not have permission to make API call."),
    ENTITY_NOT_FOUND(404, "There was nothing found in the server database."),
    INVALID_ARGUMENTS_EXCEPTION(400, "One or more params in Url are invalid."),
    UNPROCESSABLE_ENTITY(422, "Object passed to server is not valid."),
    FAILED_DEPENDENCY(424, "FAILED_DEPENDENCY"),
    TOO_MANY_REQUESTS(429, "Client is making too many requests for server to handle."),
    INTERNAL_SERVER_ERROR(500, "Internal server error. Please try again later."),
    TEMPORARY_UNAVAILABLE(503, "The Epicore server is temporarily unavailable.");

    companion object {
        fun valueOf(code: Int): String {
            return values().find { it.code == code }.toString()
        }
    }
}

//
// login-context
//
@Serializable
data class LoginContext(
    @SerialName("user_status")
    val userStatus: String?,
    val enterprise: Enterprise?,
    val error: String?
)

//
// Enterprise json
//
@Serializable
data class Enterprise(
    @SerialName("enterprise_id")
    val enterprise_id: String,
    val name: String,
    val logo: String,
    @SerialName("sso_url")
    val sso_url: String? // This value is NULL for first release
)

//
// DB Roles json
//
@Serializable
data class DbRoles(
    @SerialName("enterprise_id")
    val enterprise_id: String,
    val role: String,
    @SerialName("site_id")
    val site_id: String?
)

//
// SendCode
//
@Serializable
data class SendCodeResult(
    var result: String?,
    var error: String?
)

//
// user-stats
//
data class UserHistoryStats(
    val status: String,
    val data: List<DayIntakeLossData>
)

data class DayIntakeLossData(
    val date: String,
    val sodium_intake_ml: Double?,
    val water_intake_ml: Double?,
    val sodium_loss_ml: Double?,
    val water_loss_ml: Double?
)

//
// enterprise sites info
//
data class EnterpriseInfo(
    val siteName: String?,
    val enterpriseName: String?,
    val error: String?
)

data class EnterpriseName(
    val name : String
)

//
// user-info : Used for privacy settings
//
data class UserPrivacyInfo(
    val userInfo: UserInfo,
    val agreements: Agreements
)

data class Agreements(
    val share_stats_with_epicore: Boolean?,
    val share_stats_with_site: Map<String, Boolean>?
)

//
// update-user
//
@Serializable
data class UserInfo(
    val first_name: String?,
    val last_name: String?,
    val email: String,
    val last_login_at: String,
    val height: String?,
    val weight: String?,
    val gender: String?
)

//
// avg-sweat-volume-sodium-concentration
//
data class DataSweatVolumeSodiumConcentration(
    val sweat_volume_ml: Double,
    val sodium_concentration_mm: Double
)

data class AvgSweatVolumeSodiumConcentration(
    val status: String,
    val data: DataSweatVolumeSodiumConcentration
)

class NetworkManager(chViewModel: ModelData) {

    val chViewModel = chViewModel
    lateinit var ebsDeviceMonitor: EBSDeviceMonitor
    lateinit var apiServerInfo: ApiServerInfo

    suspend fun getUserLoginContext(email: String): LoginContext {
        if (!chViewModel.isNetworkConnected) {
            return LoginContext(userStatus = null, enterprise = null, error = "No Internet Available.")
        }
        val serverUrl = apiServerInfo.getLoginContextUrl()
        val serverKey = apiServerInfo.getServerApiKey()
        var cookie = ""
        if (chViewModel.getCurrentLocale() == "ja_JP") {
            cookie += "language=\"ja\""
        }

        return try {
            client.get(serverUrl) {
                parameter("email", email)
                headers {
                    append("ch-phone-api-key", serverKey)
                    append(HttpHeaders.Cookie, cookie)
                }
            }.body()

        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getUserLoginContext() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return LoginContext(userStatus = null, enterprise = null, error = ServerErrorCodes.valueOf(ex.response.status.value))
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getUserLoginContext() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return LoginContext(userStatus = null, enterprise = null, error = ServerErrorCodes.valueOf(ex.response.status.value))
        } catch (ex: ServerResponseException) {
            // 5xx - response
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getUserLoginContext() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return LoginContext(userStatus = null, enterprise = null, error = ServerErrorCodes.valueOf(ex.response.status.value))
        } catch (cause: HttpRequestTimeoutException) {
            println("Error: Server is down! Please try again later.")
            GlobalRumMonitor.get().addError("getUserLoginContext() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return LoginContext(userStatus = null, enterprise = null, error = "Server is down! Please try again later.")
        } catch (cause: ConnectException) {
            println("Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("getUserLoginContext() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return LoginContext(userStatus = null, enterprise = null, error = "Internet is available, but the device can not connect to ch.epicorebiosystems.com")
        } catch (cause: SocketException) {
            println("Error: Internet access is not available.")
            GlobalRumMonitor.get().addError("getUserLoginContext() - Error: Internet access is not available.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return LoginContext(userStatus = null, enterprise = null, error = "Error: Internet access is not available.")
        } catch (cause: UnknownHostException) {
            println("Error: Unknown host DNS lookup failed.")
            GlobalRumMonitor.get().addError("getUserLoginContext() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return LoginContext(userStatus = null, enterprise = null, error = "Error: Unknown host DNS lookup failed.")
        } catch (cause: Exception) {
            println("Error: {cause.localizedMessage}")
            GlobalRumMonitor.get().addError("getUserLoginContext() - Error: {cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return LoginContext(userStatus = null, enterprise = null, error = "Error: {cause.localizedMessage}")
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun sendCode(email: String, enterpriseCode: String): SendCodeResult {
        if (!chViewModel.isNetworkConnected) {
            return SendCodeResult(result = null, error = "No Internet Available.")
        }
        try {
            // Header
            val header: Map<String, String> = mapOf("alg" to "HS256", "typ" to "JWT")
            val headerJson = JSONObject(header)
            val encodedHeader = Base64.encode(headerJson.toString().toByteArray())

            // Payload
            val payload = JSONObject()
            payload.put("email", email)
            payload.put("enterpriseCode", enterpriseCode)
            val encodedPayload = Base64.encode(payload.toString().toByteArray())

            // Signature
            val signature = "$encodedHeader.$encodedPayload"

            // Create a HMAC-SHA256 digest
            val mac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(apiServerInfo.getServerApiJwtKey().toByteArray(), "HmacSHA256")
            mac.init(secretKey)
            val encryptedSignature = Base64.encode(mac.doFinal(signature.toByteArray()))

            // Token
            val token = "$signature.$encryptedSignature"

            // Cookie
            var cookie = ""
            if (chViewModel.getCurrentLocale() == "ja_JP") {
                cookie += "language=\"ja\""
            }

            val serverReturnString: String = client.put(apiServerInfo.getSendCodeUrl()) {
                setBody("{ \"encodedData\" : \"$token\"}")
                headers {
                    append("ch-phone-api-key", apiServerInfo.getServerApiKey())
                    append(HttpHeaders.Cookie, cookie)
                }
            }.body()

            return if (serverReturnString.contains("success")) {
                SendCodeResult(result = serverReturnString, error = null)
            } else {
                val jsonObject = JsonParser.parseString(serverReturnString).asJsonObject
                if (serverReturnString.contains("message")) {
                    val errorString = jsonObject.get("message").toString()
                    GlobalRumMonitor.get().addError("sendCode() - Error: $errorString", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                    SendCodeResult(result = null, error = errorString)
                } else {
                    val errorString = jsonObject.get("error").toString()
                    GlobalRumMonitor.get().addError("sendCode() - Error: $errorString", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                    SendCodeResult(result = null, error = errorString)
                }
            }

        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("sendCode() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return SendCodeResult(result = null, error = ServerErrorCodes.valueOf(ex.response.status.value))
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("sendCode() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return SendCodeResult(result = null, error = ServerErrorCodes.valueOf(ex.response.status.value))
        } catch (ex: ServerResponseException) {
            // 5xx - response
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("sendCode() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return SendCodeResult(result = null, error = ServerErrorCodes.valueOf(ex.response.status.value))
        } catch (cause: HttpRequestTimeoutException) {
            println("Error: Server is down! Please try again later.")
            GlobalRumMonitor.get().addError("sendCode() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return SendCodeResult(result = null, error = "Server is down! Please try again later.")
        } catch (cause: ConnectException) {
            println("Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("sendCode() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return SendCodeResult(result = null, error = "Internet is available, but the device can not connect to ch.epicorebiosystems.com")
        } catch (cause: SocketException) {
            println("Error: Internet access is not available.")
            GlobalRumMonitor.get().addError("sendCode() - Error: Internet access is not available.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return SendCodeResult(result = null, error = "Error: Internet access is not available.")
        } catch (cause: UnknownHostException) {
            println("Error: Unknown host DNS lookup failed.")
            GlobalRumMonitor.get().addError("sendCode() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return SendCodeResult(result = null, error = "Error: Unknown host DNS lookup failed.")
        } catch (cause: Exception) {
            println("Error: {cause.localizedMessage}")
            GlobalRumMonitor.get().addError("sendCode() - Error: {cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return SendCodeResult(result = null, error = "Error: {cause.localizedMessage}")
        }
    }

    suspend fun authenticateWithCode(email: String, verificationCode: String): String? {
        if (!chViewModel.isNetworkConnected) {
            return "No Internet Available."
        }
        try {
            // Header
            val requestBody: Map<String, String> = mapOf("email" to email, "verificationCode" to verificationCode)
            val jsonData = JSONObject(requestBody).toString()

            var cookie = ""
            if (chViewModel.getCurrentLocale() == "ja_JP") {
                cookie += "language=\"ja\""
            }

            val serverReturnString: String = client.post(apiServerInfo.getAuthenticateCodeUrl()) {
                setBody(jsonData)
                headers {
                    append("ch-phone-api-key", apiServerInfo.getServerApiKey())
                    append(HttpHeaders.Cookie, cookie)
                }
            }.body()

            val jsonObject = JsonParser.parseString(serverReturnString).asJsonObject
            if (serverReturnString.contains("error")) {
                GlobalRumMonitor.get().addError("authenticateWithCode() - Error: $serverReturnString", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                return if (serverReturnString.contains("message")) {
                    jsonObject.get("message").toString()
                } else {
                    jsonObject.get("error").toString()
                }
            } else {
                val token = jsonObject.get("token").toString()
                val refreshToken = jsonObject.get("refresh_token").toString()

                println("token = $token")
                println("refreshToken = $refreshToken")

                chViewModel.encryptedPreferences.edit().apply {
                    putString("access_token", token)
                    putString("refresh_token", refreshToken)
                }.apply()

//                chViewModel.userExistsKeystore = true

                return retrieveUserInfoFromToken(token, null, null)
            }

        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("authenticateWithCode() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("authenticateWithCode() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ServerResponseException) {
            // 5xx - response
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("authenticateWithCode() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (cause: HttpRequestTimeoutException) {
            println("Error: Server is down! Please try again later.")
            GlobalRumMonitor.get().addError("authenticateWithCode() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Server is down! Please try again later."
        } catch (cause: ConnectException) {
            println("Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("authenticateWithCode() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet is available, but the device can not connect to ch.epicorebiosystems.com"
        } catch (cause: SocketException) {
            println("Error: Internet access is not available.")
            GlobalRumMonitor.get().addError("authenticateWithCode() - Error: Internet access is not available.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet access is not available."
        } catch (cause: UnknownHostException) {
            println("Error: Unknown host DNS lookup failed.")
            GlobalRumMonitor.get().addError("authenticateWithCode() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Unknown host DNS lookup failed."
        } catch (cause: Exception) {
            println("Error: " + cause.localizedMessage)
            GlobalRumMonitor.get().addError("authenticateWithCode() - Error: " + cause.localizedMessage, RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: " + cause.localizedMessage
        }
    }

    suspend fun getUserHistoryStats(): String? {
        if (!chViewModel.isNetworkConnected) {
            return null
        }
        try {
            val token = chViewModel.encryptedPreferences.getString("access_token", "")
                ?.replace("\"", "")
                ?: return null
            val startLocalDate = generateCurrentLocalDateMinus30Days()
            val endLocalDate = generateCurrentLocalDate()
            var cookie =
                "selectedUserRoles=[{\"enterprise_id\": \"${chViewModel.jwtEnterpriseID.value}\",\"role\":\"CH_USER\",\"site_id\": \"${chViewModel.jwtSiteID.value}\"}]"
            if (chViewModel.getCurrentLocale() == "ja_JP") {
                cookie += "; language=\"ja\""
            }
            val serverReturnString: String = client.get(apiServerInfo.getUserHistoryUrl()) {
                parameter("start", startLocalDate)
                parameter("end", endLocalDate)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.Cookie, cookie)
                }
            }.body()

            Log.d("getUserHistoryStats", serverReturnString)

            // null is SUCCESS - if there is a string then it is the error returned from the server
            return if (serverReturnString.contains("error")) {
                GlobalRumMonitor.get().addError("getUserHistoryStats() - Error: $serverReturnString", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                val jsonObject = JsonParser.parseString(serverReturnString).asJsonObject
                jsonObject.get("error").toString()
            } else {
                chViewModel.userHistoryStats = Gson().fromJson<UserHistoryStats>(
                    serverReturnString,
                    UserHistoryStats::class.java
                )
                null
            }
        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getUserHistoryStats() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getUserHistoryStats() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ServerResponseException) {
            // 5xx - response
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getUserHistoryStats() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (cause: HttpRequestTimeoutException) {
            println("Error: Server is down! Please try again later.")
            GlobalRumMonitor.get().addError("getUserHistoryStats() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Server is down! Please try again later."
        } catch (cause: ConnectException) {
            println("Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("getUserHistoryStats() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet is available, but the device can not connect to ch.epicorebiosystems.com"
        } catch (cause: SocketException) {
            println("Error: Internet access is not available.")
            GlobalRumMonitor.get().addError("getUserHistoryStats() - Error: Internet access is not available.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet access is not available."
        } catch (cause: UnknownHostException) {
            println("Error: Unknown host DNS lookup failed.")
            GlobalRumMonitor.get().addError("getUserHistoryStats() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Unknown host DNS lookup failed."
        } catch (cause: Exception) {
            println("Error: {cause.localizedMessage}")
            GlobalRumMonitor.get().addError("getUserHistoryStats() - Error: {cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: {cause.localizedMessage}"
        }
    }

    suspend fun getAvgSweatVolumeSodiumConcentration(): String? {
        if (!chViewModel.isNetworkConnected) {
            return null
        }

        // Handle test account here.
        if (chViewModel.isTestAccount()) {
            return null
        }

        try {
            val token = chViewModel.encryptedPreferences.getString("access_token", "")
                ?.replace("\"", "")
                ?: return null
            var cookie =
                "selectedUserRoles=[{\"enterprise_id\": \"${chViewModel.jwtEnterpriseID.value}\",\"role\":\"CH_USER\",\"site_id\": \"${chViewModel.jwtSiteID.value}\"}]"
            if (chViewModel.getCurrentLocale() == "ja_JP") {
                cookie += "; language=\"ja\""
            }
            val serverReturnString: String = client.get(apiServerInfo.getAvgSweatConcentrationUrl()) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.Cookie, cookie)
                }
            }.body()

            Log.d("getAvgSweatVolumeSodium", serverReturnString)

            // null is SUCCESS - if there is a string then it is the error returned from the server
            return if (serverReturnString.contains("error")) {
                GlobalRumMonitor.get().addError("getAvgSweatVolumeSodiumConcentration() - Error: $serverReturnString", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                val jsonObject = JsonParser.parseString(serverReturnString).asJsonObject
                jsonObject.get("message").toString()
            } else {
                chViewModel.userAvgSweatSodiumConcentration =
                    Gson().fromJson<AvgSweatVolumeSodiumConcentration>(
                        serverReturnString,
                        AvgSweatVolumeSodiumConcentration::class.java
                    )
                null
            }
        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getAvgSweatVolumeSodiumConcentration() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getAvgSweatVolumeSodiumConcentration() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ServerResponseException) {
            // 5xx - response
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getAvgSweatVolumeSodiumConcentration() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (cause: HttpRequestTimeoutException) {
            println("Error: Server is down! Please try again later.")
            GlobalRumMonitor.get().addError("getAvgSweatVolumeSodiumConcentration() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Server is down! Please try again later."
        } catch (cause: ConnectException) {
            println("Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("getAvgSweatVolumeSodiumConcentration() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet is available, but the device can not connect to ch.epicorebiosystems.com"
        } catch (cause: SocketException) {
            println("Error: Internet access is not available.")
            GlobalRumMonitor.get().addError("getAvgSweatVolumeSodiumConcentration() - Error: Internet access is not available.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet access is not available."
        } catch (ex: NullPointerException) {
            return "Error: user account is not authenticated"
        } catch (cause: UnknownHostException) {
            println("Error: Unknown host DNS lookup failed.")
            GlobalRumMonitor.get().addError("getAvgSweatVolumeSodiumConcentration() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Unknown host DNS lookup failed."
        } catch (cause: Exception) {
            println("Error: {cause.localizedMessage}")
            GlobalRumMonitor.get().addError("getAvgSweatVolumeSodiumConcentration() - Error: {cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: {cause.localizedMessage}"
        }
    }

    suspend fun updateUser(enterpriseId: String, siteId: String, userInfo: Map<String, Any>): String? {
        try {
            chViewModel.isUpdateUserCalled = true
            val token = chViewModel.encryptedPreferences.getString("access_token", "")
                ?.replace("\"", "")
                ?: return null
            val refreshToken = chViewModel.encryptedPreferences.getString("refresh_token", "")
                ?.replace("\"", "")
                ?: return null
            val requestBody: Map<String, Any> = mapOf(
                "enterpriseCode" to "$enterpriseId-$siteId",
                "userInfo" to userInfo,
                "refreshToken" to refreshToken
            )
            val jsonData = JSONObject(requestBody).toString()
            Log.d("updateUser-put", jsonData)
            var cookie =
                "selectedUserRoles=[{\"enterprise_id\": \"${chViewModel.jwtEnterpriseID.value}\",\"role\":\"CH_USER\",\"site_id\": \"${chViewModel.jwtSiteID.value}\"}]"
            if (chViewModel.getCurrentLocale() == "ja_JP") {
                cookie += "; language=\"ja\""
            }
            Log.d("updateUser-cookie", cookie)
            Log.d("updateUser-server", apiServerInfo.getUpdateUserUrl())
            val serverReturnString: String = client.put(apiServerInfo.getUpdateUserUrl()) {
                setBody(jsonData)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.Cookie, cookie)
                }
            }.body()

            Log.d("updateUser-return", serverReturnString)

            // null is SUCCESS - if there is a string then it is the error returned from the server
            val jsonObject = JsonParser.parseString(serverReturnString).asJsonObject
            return if (serverReturnString.contains("error")) {
                chViewModel.updateUserSuccess = false
                if (serverReturnString.contains("message")) {
                    GlobalRumMonitor.get().addError("updateUser() - Error: ${jsonObject.get("message")}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                    jsonObject.get("message").toString()
                }
                else {
                    GlobalRumMonitor.get().addError("updateUser() - Error: ${jsonObject.get("error")}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                    jsonObject.get("error").toString()
                }
            } else {
                val newToken = jsonObject.get("token").toString()
                val newRefreshToken = jsonObject.get("refresh_token").toString()

                chViewModel.updateUserSuccess = true

                //println("token = $token")
                //println("refreshToken = $refreshToken")

                chViewModel.encryptedPreferences.edit().apply {
                    putString("access_token", newToken)
                    putString("refresh_token", newRefreshToken)
                }.apply()

                return retrieveUserInfoFromToken(token, enterpriseId, siteId)
            }
        } catch (ex: RedirectResponseException) {
            chViewModel.updateUserSuccess = false
            // 3xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("updateUser() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ClientRequestException) {
            chViewModel.updateUserSuccess = false
            // 4xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("updateUser() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ServerResponseException) {
            chViewModel.updateUserSuccess = false
            // 5xx - response
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("updateUser() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (cause: HttpRequestTimeoutException) {
            chViewModel.updateUserSuccess = false
            println("Error: Server is down! Please try again later.")
            GlobalRumMonitor.get().addError("updateUser() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Server is down! Please try again later."
        } catch (cause: ConnectException) {
            chViewModel.updateUserSuccess = false
            println("Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("updateUser() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet is available, but the device can not connect to ch.epicorebiosystems.com"
        } catch (cause: SocketException) {
            chViewModel.updateUserSuccess = false
            println("Error: Internet access is not available.")
            GlobalRumMonitor.get().addError("updateUser() - Error: Internet access is not available.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet access is not available."
        } catch (cause: UnknownHostException) {
            chViewModel.updateUserSuccess = false
            println("Error: Unknown host DNS lookup failed.")
            GlobalRumMonitor.get().addError("updateUser() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Unknown host DNS lookup failed."
        } catch (cause: Exception) {
            chViewModel.updateUserSuccess = false
            println("Error: {cause.localizedMessage}")
            GlobalRumMonitor.get().addError("updateUser() - Error: {cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: {cause.localizedMessage}"
        }
    }

    suspend fun getUserInfo(): String? {
        if (!chViewModel.isNetworkConnected) {
            return null
        }
        try {
            val token = chViewModel.encryptedPreferences.getString("access_token", "")
                ?.replace("\"", "")
                ?: return null
            var cookie =
                "selectedUserRoles=[{\"enterprise_id\": \"${chViewModel.jwtEnterpriseID.value}\",\"role\":\"CH_USER\",\"site_id\": \"${chViewModel.jwtSiteID.value}\"}]"
            if (chViewModel.getCurrentLocale() == "ja_JP") {
                cookie += "; language=\"ja\""
            }
            Log.d("getUserInfo-get", cookie)
            val serverReturnString: String = client.get(apiServerInfo.getUserInfoUrl()) {
                headers {
                    append("api-version", "2")
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.Cookie, cookie)
                }
            }.body()

            Log.d("getUserInfo-return", serverReturnString)

            // null is SUCCESS - if there is a string then it is the error returned from the server
            return if (serverReturnString.contains("error")) {
                //val jsonObject = JsonParser.parseString(serverReturnString).asJsonObject
                //jsonObject.get("error").toString()
                GlobalRumMonitor.get().addError("getUserInfo() - Error: ERROR getUserInfo", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                "ERROR getUserInfo"
            } else {
                val userPrivacy =
                    Gson().fromJson(
                        serverReturnString,
                        UserPrivacyInfo::class.java
                    )

                // Update the stored user info
                var heightCm: UByte = (userPrivacy.userInfo.height?.toUByteOrNull() ?: 175u)
                if (heightCm == 0.toUByte()) {
                    heightCm = 175u
                }

                val heightInInches = heightCm.toDouble() / 2.54
                val heightFeet = (heightInInches / 12.0).toInt().toString()
                val heightInch = (heightInInches % 12.0).roundToInt().toString()

                var weight = (userPrivacy.userInfo.weight ?: 0).toString()
                if (weight == "0") {
                    weight = "75"
                }

                val serverGender = (userPrivacy.userInfo.gender ?: "male").toString()
                val gender = if (serverGender == "male") "Male" else "Female"

                if ((heightCm.toString() != chViewModel.userHeightCm.value) || (weight != chViewModel.userWeightKg.value) || (gender != chViewModel.userGender.value))
                {
                    chViewModel.userPrefsData.setUserWeight(weight)
                    chViewModel.userHeightFt.value = heightFeet
                    chViewModel.userHeightIn.value = heightInch
                    chViewModel.userHeightCm.value = heightCm.toString()
                    chViewModel.userGender.value = gender

                    val metricLb = (weight.toDoubleOrNull() ?: 75.0) * 2.205
                    chViewModel.onboardingWeightLb.value = "%d".format(metricLb.roundToInt())
                    chViewModel.onboardingWeightKg.value = weight
                    chViewModel.onboardingHeightFt.value = heightFeet
                    chViewModel.onboardingHeightIn.value = heightInch
                    chViewModel.onboardingHeightCm.value = heightCm.toString()
                    chViewModel.onboardingGender.value = gender

                    chViewModel.oldUserHeightFt = heightFeet
                    chViewModel.oldUserHeightIn = heightInch
                    chViewModel.oldUserHeightCm = heightCm.toString()
                    chViewModel.oldUserWeightLb = "%d".format(metricLb.roundToInt())
                    chViewModel.oldUserWeightKg = weight
                    chViewModel.oldUserGender = gender

                    chViewModel.savePhysiologyChangedValues(
                        chViewModel.userWeightLb.value,
                        chViewModel.userWeightKg.value,
                        chViewModel.userHeightFt.value,
                        chViewModel.userHeightIn.value,
                        chViewModel.userHeightCm.value,
                        chViewModel.userGender.value
                    )
                    ebsDeviceMonitor.setUserInfoForCSVFile(
                        chViewModel.userGender.value,
                        chViewModel.userHeightCm.value.toInt(),
                        chViewModel.userWeightKg.value.toInt()
                    )

                    if (chViewModel.isCHDeviceConnected) {
                        // Update user information here
                        val paddedSize = 16
                        val paddedHexZeros =
                            ByteArray(paddedSize) { 0xFF.toByte() }   // Create the padded array of trailing 0x00's
                        val userHeightInCms: ByteArray = byteArrayOf(
                            (if ((chViewModel.userHeightCm.value == "") || chViewModel.userHeightCm.value == "0") {
                                "175"
                            } else {
                                chViewModel.userHeightCm.value
                            }).toInt().toByte()
                        )

                        val userWeightInKg: ByteArray =
                            (if ((chViewModel.userWeightKg.value == "") || (chViewModel.userWeightKg.value == "0")) {
                                "75"
                            } else {
                                chViewModel.userWeightKg.value
                            }).toUShort().toByteArray()

                        val userGender: ByteArray = byteArrayOf(
                            if (chViewModel.userGender.value == "Male") {
                                0x00
                            } else {
                                0x01
                            }
                        )

                        val userAge: ByteArray = byteArrayOf(0)

                        val userClothTypeCode: ByteArray = byteArrayOf(0)

                        val setUserInfoCommand: ByteArray =
                            byteArrayOf(0x55).plus(paddedHexZeros).plus(userGender)
                                .plus(userHeightInCms).plus(userWeightInKg).plus(userAge)
                                .plus(userClothTypeCode)

                        ebsDeviceMonitor.onEvent(OnRunInput(setUserInfoCommand))
                    }
                }
                else {
                    val metricLb = (weight.toDoubleOrNull() ?: 75.0) * 2.205
                    chViewModel.oldUserHeightFt = heightFeet
                    chViewModel.oldUserHeightIn = heightInch
                    chViewModel.oldUserHeightCm = heightCm.toString()
                    chViewModel.oldUserWeightLb = "%d".format(metricLb.roundToInt())
                    chViewModel.oldUserWeightKg = weight
                    chViewModel.oldUserGender = gender

                    // Set to values returned from server
                    chViewModel.userHeightFt.value = chViewModel.oldUserHeightFt
                    chViewModel.userHeightIn.value = chViewModel.oldUserHeightIn
                    chViewModel.userHeightCm.value = chViewModel.oldUserHeightCm
                    chViewModel.userWeightLb.value = chViewModel.oldUserWeightLb
                    chViewModel.userWeightKg.value = chViewModel.oldUserWeightKg
                    chViewModel.userGender.value = chViewModel.oldUserGender
                }

                val enterpriseDataShare = userPrivacy.agreements.share_stats_with_site
                chViewModel.switchShareAnonymousDataEnterprise = enterpriseDataShare?.get(chViewModel.enterpriseId.value) ?: true
                chViewModel.switchShareAnonymousDataEpicore = userPrivacy.agreements.share_stats_with_epicore ?: true
                null
            }
        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getUserInfo() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getUserInfo() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ServerResponseException) {
            // 5xx - response
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getUserInfo() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (cause: HttpRequestTimeoutException) {
            println("Error: Server is down! Please try again later.")
            GlobalRumMonitor.get().addError("getUserInfo() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Server is down! Please try again later."
        } catch (cause: ConnectException) {
            println("Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("getUserInfo() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet is available, but the device can not connect to ch.epicorebiosystems.com"
        } catch (cause: SocketException) {
            println("Error: Internet access is not available.")
            GlobalRumMonitor.get().addError("getUserInfo() - Error: Internet access is not available.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet access is not available."
        } catch (cause: UnknownHostException) {
            println("Error: Unknown host DNS lookup failed.")
            GlobalRumMonitor.get().addError("getUserPrivacyInfo() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Unknown host DNS lookup failed."
        } catch (cause: Exception) {
            println("Error: {cause.localizedMessage}")
            GlobalRumMonitor.get().addError("getUserPrivacyInfo() - Error: {cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: {cause.localizedMessage}"
        }
    }

    suspend fun setUserInfo(epicore: Boolean, site: Boolean): String? {
        if (!chViewModel.isNetworkConnected) {
            return null
        }
        try {
            val token = chViewModel.encryptedPreferences.getString("access_token", "")
                ?.replace("\"", "")
                ?: return null
            val httpBody =
                "[\"agreements\": [\"share_stats_with_epicore\": ${epicore}, \"share_stats_with_site\": [${chViewModel.enterpriseId.value} : ${site}]]"
            var cookie =
                "selectedUserRoles=[{\"enterprise_id\": \"${chViewModel.jwtEnterpriseID.value}\",\"role\":\"CH_USER\",\"site_id\": \"${chViewModel.jwtSiteID.value}\"}]"
            if (chViewModel.getCurrentLocale() == "ja_JP") {
                cookie += "; language=\"ja\""
            }
            val serverReturnString: String = client.patch(apiServerInfo.getUserInfoUrl()) {
                headers {
                    setBody(httpBody)
                    append("api-version", "2")
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.Cookie, cookie)
                }
            }.body()

            // null is SUCCESS - if there is a string then it is the error returned from the server
            return if (serverReturnString.contains("error")) {
                val jsonObject = JsonParser.parseString(serverReturnString).asJsonObject
                GlobalRumMonitor.get().addError("setUserInfo() - Error: ${jsonObject.get("message")}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                jsonObject.get("message").toString()
            } else if (serverReturnString.contains("Invalid JSON")) {
                GlobalRumMonitor.get().addError("setUserInfo() - Error: Invalid JSON", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                "Invalid JSON"
            } else {
                val userPrivacy =
                    Gson().fromJson(
                        serverReturnString,
                        UserPrivacyInfo::class.java)
                val enterpriseDataShare = userPrivacy.agreements.share_stats_with_site
                chViewModel.switchShareAnonymousDataEnterprise = enterpriseDataShare?.get(chViewModel.enterpriseId.value) ?: false
                chViewModel.switchShareAnonymousDataEpicore = userPrivacy.agreements.share_stats_with_epicore ?: false
                null
            }
        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("setUserInfo() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("setUserInfo() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ServerResponseException) {
            // 5xx - response
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("setUserInfo() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (cause: HttpRequestTimeoutException) {
            println("Error: Server is down! Please try again later.")
            GlobalRumMonitor.get().addError("setUserInfo() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Server is down! Please try again later."
        } catch (cause: ConnectException) {
            println("Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("setUserInfo() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet is available, but the device can not connect to ch.epicorebiosystems.com"
        } catch (cause: SocketException) {
            println("Error: Internet access is not available.")
            GlobalRumMonitor.get().addError("setUserInfo() - Error: Internet access is not available.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet access is not available."
        } catch (cause: UnknownHostException) {
            println("Error: Unknown host DNS lookup failed.")
            GlobalRumMonitor.get().addError("setUserPrivacyInfo() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Unknown host DNS lookup failed."
        } catch (cause: Exception) {
            println("Error: {cause.localizedMessage}")
            GlobalRumMonitor.get().addError("setUserPrivacyInfo() - Error: {cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: {cause.localizedMessage}"
        }
    }

    // Auth0 - refresh token renewal
    // New standard using: https://auth0.com/docs/get-started/authentication-and-authorization-flow/call-your-api-using-the-authorization-code-flow-with-pkce#example-post-to-token-url
    @OptIn(InternalAPI::class)
    suspend fun getNewRefreshToken(): String? {
        if (!chViewModel.isNetworkConnected) {
            return null
        }
        try {
            // PKCE
            val currRefreshToken = chViewModel.encryptedPreferences.getString("refresh_token", "")
                ?.replace("\"", "")
                ?: return null
            val urlString = "https://${apiServerInfo.getAuth0Url()}/oauth/token"
            val httpBody =
                "grant_type=refresh_token${apiServerInfo.getClientId()}&refresh_token=${currRefreshToken}"
            val serverReturnString: String = client.post(urlString) {
                setBody(httpBody)
                headers {
                    append(HttpHeaders.ContentType, "application/x-www-form-urlencoded")
                }
            }.body()

            // null is SUCCESS - if there is a string then it is the error returned from the server
            val jsonObject = JsonParser.parseString(serverReturnString).asJsonObject
            if (serverReturnString.contains("error")) {
                //Log.d("RefreshTokenError", jsonObject.get("error").toString())
                if (serverReturnString.contains("invalid_grant")) {
                    ebsDeviceMonitor.onEvent(DisconnectEvent)
                    ebsDeviceMonitor.disconnect()
                    ebsDeviceMonitor.stopScanningJob()
                    chViewModel.clearUserDataStore(true)
                    chViewModel.resetModelDataMutables()
                    ebsDeviceMonitor.clearHistoricalDataSet()
                    chViewModel.networkManager.logOutUser()
                    with(chViewModel.encryptedPreferences.edit()) {
                        clear()
                        apply() // or commit()
                    }
                    chViewModel.onboardingStep = 1
                    chViewModel.updateOnBoardingComplete(false)
                    ebsDeviceMonitor.scanBluetoothDevice()
                    return null
                }
                else {
                    GlobalRumMonitor.get().addError("getNewRefreshToken() - Error: ${jsonObject.get("error")}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                    return jsonObject.get("error").toString()
                }
            } else {
                val token = jsonObject.get("access_token").toString()
                val refreshToken = jsonObject.get("refresh_token").toString()

                chViewModel.encryptedPreferences.edit().apply {
                    putString("access_token", token)
                    putString("refresh_token", refreshToken)
                }.apply()

                //println("token = $token")
                //println("refreshToken = $refreshToken")

                return null
            }
        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getNewRefreshToken() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getNewRefreshToken() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (ex: ServerResponseException) {
            // 5xx - response
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getNewRefreshToken() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return ServerErrorCodes.valueOf(ex.response.status.value)
        } catch (cause: HttpRequestTimeoutException) {
            println("Error: Server is down! Please try again later.")
            GlobalRumMonitor.get().addError("getNewRefreshToken() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Server is down! Please try again later."
        } catch (cause: ConnectException) {
            println("Error: Internet is available, but the device can not connect to auth.ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("getNewRefreshToken() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Internet is available, but the device can not connect to auth.ch.epicorebiosystems.com"
        } catch (cause: SocketTimeoutException) {
            println("Error: Socket timeout, the device can not connect to auth.ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("getNewRefreshToken() - Error: Socket timeout, the device can not connect to auth.ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Socket timeout, the device can not connect to auth.ch.epicorebiosystems.com"
        } catch (cause: UnknownHostException) {
            println("Error: Unknown host DNS lookup failed.")
            GlobalRumMonitor.get().addError("getNewRefreshToken() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: Unknown host DNS lookup failed."
        } catch (cause: Exception) {
            println("Error: {cause.localizedMessage}")
            GlobalRumMonitor.get().addError("getNewRefreshToken() - Error: {cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return "Error: {cause.localizedMessage}"
        }
    }

    suspend fun logOutUser() {
        if (!chViewModel.isNetworkConnected) {
            return
        }
        try {
            val currRefreshToken = chViewModel.encryptedPreferences.getString("refresh_token", "")
                ?.replace("\"", "")
            val httpBody =
                "grant_type=refresh_token${apiServerInfo.getClientId()}&refresh_token=${currRefreshToken}"
            val serverReturnString: String = client.post("https://${apiServerInfo.getAuth0Url()}/logout") {
                setBody(httpBody)
                headers {
                    append(
                        HttpHeaders.ContentType,
                        "application/x-www-form-urlencoded${currRefreshToken}"
                    )
                }
            }.body()

            println("Logout = $serverReturnString")

        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            GlobalRumMonitor.get().addError("logOutUser() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Error: ${ex.response.status.description}")
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            GlobalRumMonitor.get().addError("logOutUser() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Error: ${ex.response.status.description}")
        } catch (ex: ServerResponseException) {
            // 5xx - response
            GlobalRumMonitor.get().addError("logOutUser() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Error: ${ex.response.status.description}")
        } catch (cause: HttpRequestTimeoutException) {
            GlobalRumMonitor.get().addError("logOutUser() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Error: Server is down! Please try again later.")
        } catch (cause: ConnectException) {
            GlobalRumMonitor.get().addError("logOutUser() - Error: Internet is available, but the device can not connect to auth.ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Internet is available, but the device can not connect to auth.ch.epicorebiosystems.com")
        } catch (cause: SocketTimeoutException) {
            GlobalRumMonitor.get().addError("logOutUser() - Error: Socket timeout, the device can not connect to auth.ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Socket timeout, the device can not connect to auth.ch.epicorebiosystems.com")
        } catch (cause: UnknownHostException) {
            GlobalRumMonitor.get().addError("logOutUser() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Error: Unknown host DNS lookup failed.")
        } catch (cause: Exception) {
            GlobalRumMonitor.get().addError("logOutUser() - Error: {cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Error: {cause.localizedMessage}")
        }
    }

    suspend fun getEnterpriseName(enterpriseId: String): EnterpriseInfo {
        if (!chViewModel.isNetworkConnected) {
            return EnterpriseInfo(siteName = null, enterpriseName = null, error = "No internet connection")
        }
        val serverUrl = apiServerInfo.getEnterpriseNameUrl() + "/${enterpriseId}"
        val serverKey = apiServerInfo.getServerApiKey()
        var cookie = ""
        if (chViewModel.getCurrentLocale() == "ja_JP") {
            cookie += "language=\"ja\""
        }

        try {

            val serverReturnString: String = client.get(serverUrl) {
                headers {
                    append("ch-phone-api-key", serverKey)
                    append(HttpHeaders.Cookie, cookie)
                }
            }.body()

            val jsonObject = JsonParser.parseString(serverReturnString).asJsonObject
            if (serverReturnString.contains("error")) {
                val errorMsg = jsonObject.get("message").toString().stripQuotes()
                return EnterpriseInfo(siteName = null, enterpriseName = null, error = errorMsg)
            }
            else {
                val siteName = jsonObject.get("name").toString().stripQuotes()
                val enterpriseName = jsonObject.get("enterprise").asJsonObject.get("name").toString().stripQuotes()
                return EnterpriseInfo(siteName = siteName, enterpriseName = enterpriseName, error = null)
            }

        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getEnterpriseName() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return EnterpriseInfo(siteName = null, enterpriseName = null, error = ex.response.status.description)
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getEnterpriseName() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return EnterpriseInfo(siteName = null, enterpriseName = null, error = ex.response.status.description)
        } catch (ex: ServerResponseException) {
            // 5xx - response
            println("Error: ${ex.response.status.description}")
            GlobalRumMonitor.get().addError("getEnterpriseName() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return EnterpriseInfo(siteName = null, enterpriseName = null, error = ex.response.status.description)
        } catch (cause: HttpRequestTimeoutException) {
            println("Error: Server is down! Please try again later.")
            GlobalRumMonitor.get().addError("getEnterpriseName() - Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return EnterpriseInfo(siteName = null, enterpriseName = null, error = "Server is down! Please try again later.")
        } catch (cause: ConnectException) {
            println("Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com")
            GlobalRumMonitor.get().addError("getEnterpriseName() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return EnterpriseInfo(siteName = null, enterpriseName = null, error = "Internet is available, but the device can not connect to ch.epicorebiosystems.com")
        } catch (cause: SocketException) {
            println("Error: Internet access is not available.")
            GlobalRumMonitor.get().addError("getEnterpriseName() - Error: Internet access is not available.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return EnterpriseInfo(siteName = null, enterpriseName = null, error = "Internet access is not available.")
        } catch (cause: UnknownHostException) {
            println("Error: Unknown host DNS lookup failed.")
            GlobalRumMonitor.get().addError("getEnterpriseName() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return EnterpriseInfo(siteName = null, enterpriseName = null, error = "Unknown host DNS lookup failed.")
        } catch (cause: Exception) {
            println("Error: ${cause.localizedMessage}")
            GlobalRumMonitor.get().addError("getUserLoginContext() - Error: ${cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            return EnterpriseInfo(siteName = null, enterpriseName = null, error = cause.localizedMessage)
        }
    }

    fun String.stripQuotes(): String {
        return this.removeSurrounding("\"")
    }

    fun isTokenValid(): Boolean {
        val token = chViewModel.encryptedPreferences.getString("access_token", "")
            ?.replace("\"", "")
            ?: return false
        return try {
            val jwt = JWT(token)
            !jwt.isExpired(0)
        } catch(ex: Exception) {
            false
        }
    }

    suspend fun uploadSensorCSVFile(context: Context, csvFileName: String) {
        chViewModel.networkUploadFailed.value = false

        if (!chViewModel.isNetworkConnected) {
            chViewModel.syncFileTime = System.currentTimeMillis()
            return
        }

        chViewModel.setCsvFileIsUploading(true)

        // To upload multi-part form data - file. See:
        // https://ktor.io/docs/request.html#upload_file
        val token = chViewModel.encryptedPreferences.getString("access_token", "")
            ?.replace("\"", "")
            ?: return
        var cookie =
            "selectedUserRoles=[{\"enterprise_id\":\"${chViewModel.jwtEnterpriseID.value}\",\"role\":\"CH_USER\",\"site_id\":\"${chViewModel.jwtSiteID.value}\"}]"
        if (chViewModel.getCurrentLocale() == "ja_JP") {
            cookie += "; language=\"ja\""
        }

        val uuid = UUID.randomUUID()
        val boundary = "Boundary-$uuid"//$lineBreak"

        val client = HttpClient(CIO)

        //Log.d("FILE_UPLOAD", "$cookie")
        //Log.d("FILE_UPLOAD", "$token")

        try {
            val response: HttpResponse =
                client.post("${apiServerInfo.getServerBaseApi()}/api/external/upload") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                        append(HttpHeaders.Cookie, cookie)
                        append(HttpHeaders.Accept, "application/json")
                        append(HttpHeaders.ContentType, "multipart/form-data; boundary=$boundary")
                        //append("api-version", "2")
                    }
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append(csvFileName, File(context.filesDir, csvFileName).readBytes(),
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "text/csv")
                                        append(HttpHeaders.ContentDisposition, "filename=$csvFileName")
                                        //append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=$csvFileName")
                                    }
                                )
                            },
                            boundary = boundary
                        )
                    )
                    onUpload { bytesSentTotal, contentLength ->
                        //println("Sent $bytesSentTotal bytes from $contentLength")
                        //Log.d("FILE_UPLOAD", "Sent $bytesSentTotal bytes from $contentLength")
                    }
                }

            //chViewModel.updateDate = Date()
            chViewModel.syncFileTime = System.currentTimeMillis()

            //Log.d("FILE_UPLOAD", "Server response: $response")
            Log.d("FILE_UPLOAD", "Data sync end time: " + ebsDeviceMonitor.generateCurrentTimeStamp())

//            chViewModel.fileManager.deleteFile(csvFileName)

            if (response.status.value == 200 || response.status.value == 202) {
                chViewModel.updateDate = Date()
                chViewModel.isSweatDataDownloadProgressAlertShowing = false
                chViewModel.networkUploadSuccess.value = true
                chViewModel.networkUploadFailed.value = false
            }
            else {
                GlobalRumMonitor.get().addError("uploadSensorCSVFile() - Error: $response", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
                chViewModel.networkUploadSuccess.value = false
                chViewModel.networkUploadFailed.value = true
                chViewModel.networkUploadFailedMsg = "$response"
            }

            chViewModel.setCsvFileIsUploading(false)

        } catch (ex: RedirectResponseException) {
            // 3xx - responses
            chViewModel.networkUploadSuccess.value = false
            chViewModel.networkUploadFailed.value = true
            chViewModel.networkUploadFailedMsg = "${ex.response.status.description}"
            GlobalRumMonitor.get().addError("uploadSensorCSVFile() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            //Log.d("FILE_UPLOAD","Upload Error: ${ex.response.status.description}")
        } catch (ex: ClientRequestException) {
            // 4xx - responses
            chViewModel.networkUploadSuccess.value = false
            chViewModel.networkUploadFailed.value = true
            chViewModel.networkUploadFailedMsg = "${ex.response.status.description}"
            GlobalRumMonitor.get().addError("uploadSensorCSVFile() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            //Log.d("FILE_UPLOAD","Upload Error: ${ex.response.status.description}")
        } catch (ex: ServerResponseException) {
            // 5xx - response
            chViewModel.networkUploadSuccess.value = false
            chViewModel.networkUploadFailed.value = true
            chViewModel.networkUploadFailedMsg = "${ex.response.status.description}"
            GlobalRumMonitor.get().addError("uploadSensorCSVFile() - Error: ${ex.response.status.description}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            //Log.d("FILE_UPLOAD","Upload Error: ${ex.response.status.description}")
        } catch (cause: HttpRequestTimeoutException) {
            chViewModel.networkUploadSuccess.value = false
            chViewModel.networkUploadFailed.value = true
            chViewModel.networkUploadFailedMsg = "Server is down! Please try again later."
            GlobalRumMonitor.get().addError("uploadSensorCSVFile() - Error: Upload Error: Server is down! Please try again later.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            //Log.d("FILE_UPLOAD","Upload Error: Server is down! Please try again later.")
        } catch (cause: ConnectException) {
            chViewModel.networkUploadSuccess.value = false
            chViewModel.networkUploadFailed.value = true
            chViewModel.networkUploadFailedMsg = "Internet is available, but the device can not connect to ch.epicorebiosystems.com"
            GlobalRumMonitor.get().addError("uploadSensorCSVFile() - Error: Internet is available, but the device can not connect to ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
        } catch (cause: FileNotFoundException) {
            chViewModel.networkUploadSuccess.value = false
            chViewModel.networkUploadFailed.value = true
            chViewModel.networkUploadFailedMsg = "File not found exception caused by system glitch"
            GlobalRumMonitor.get().addError("uploadSensorCSVFile() - Error: File not found exception caused by system glitch", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("File not found exception caused by system glitch")
        } catch (cause: SocketTimeoutException) {
            GlobalRumMonitor.get().addError("uploadSensorCSVFile() - Error: Socket timeout, the device can not connect to auth.ch.epicorebiosystems.com", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Socket timeout, the device can not connect to auth.ch.epicorebiosystems.com")
        } catch (cause: UnknownHostException) {
            GlobalRumMonitor.get().addError("uploadSensorCSVFile() - Error: Unknown host DNS lookup failed.", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Error: Unknown host DNS lookup failed.")
        } catch (cause: Exception) {
            GlobalRumMonitor.get().addError("uploadSensorCSVFile() - Error: {cause.localizedMessage}", RumErrorSource.LOGGER, null, emptyMap<String, Any>())
            println("Error: {cause.localizedMessage}")
        }
    }

    private fun generateCurrentLocalDate(): String {
        // Get the current date-time in the system's default time zone
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Format the date in "yyyy-MM-dd" format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDate.toJavaLocalDate().format(formatter)
    }

    private fun generateCurrentLocalDateMinus30Days(): String {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Subtract 29 days from the current date
        val dateMinus30Days = currentDate.minus(29, DateTimeUnit.DAY)

        // Format the date in "yyyy-MM-dd" format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return dateMinus30Days.toJavaLocalDate().format(formatter)
    }

    // Retrieve and save user account information from JWT token
    private fun retrieveUserInfoFromToken(token: String, enterpriseId: String?, siteId: String?): String? {
        val jwt = JWT(token)
        chViewModel.currentAuthUserId.value = jwt.getClaim("epicore_custom/user_id").asString()!!
        chViewModel.updateUserId(chViewModel.currentAuthUserId.value)

        //println(jwt.getClaim("epicore_custom/enterprises").toString())

        val enterpriseClaim = jwt.getClaim("epicore_custom/enterprises")
        val enterpriseList = enterpriseClaim.asList(Enterprise::class.java)
        println(enterpriseList)

        //chViewModel.updateEnterpriseId(chViewModel.enterpriseId.value)

        var eId = enterpriseId
        var sId = siteId
        var userRole = "CH_USER"
        if (eId == null && sId == null) {
            val splitCode = chViewModel.enterpriseId.value.split("-")

            if (splitCode.isEmpty()) {
                return "Enterprise ID is not valid."
            }

            eId = splitCode[0]
            sId = splitCode[1]
        }
        else {
            eId = enterpriseId
            sId = siteId
        }

        for (enterprise in enterpriseList) {
            //println(enterprise)
            if (eId == enterprise.enterprise_id) {
                chViewModel.CH_EnterpriseName.value = enterprise.name
                break
            }
        }

        //println(jwt.getClaim("epicore_custom/db_roles").toString())

        val rolesClaim = jwt.getClaim("epicore_custom/db_roles")
        val rolesList = rolesClaim.asList(DbRoles::class.java)
        for (role in rolesList) {
            //println(role)
            if (role.enterprise_id == eId && role.site_id == sId) {
                userRole = role.role
                eId = role.enterprise_id
                sId = role.site_id.toString()
            }
        }

        chViewModel.jwtEnterpriseID.value = eId.toString()
        chViewModel.jwtSiteID.value = sId.toString()
        chViewModel.CH_UserRole.value = userRole

        val newEnterpriseId = "$eId-$sId"
        chViewModel.updateEnterpriseId(newEnterpriseId)
        ebsDeviceMonitor.setEnterpriseId(newEnterpriseId)
        chViewModel.enterpriseId.value = newEnterpriseId

        chViewModel.updateJwtEnterpriseId(chViewModel.jwtEnterpriseID.value)
        chViewModel.updateEnterpriseName(chViewModel.CH_EnterpriseName.value)
        chViewModel.updateJwtSiteId(chViewModel.jwtSiteID.value)

        chViewModel.updateEmailAddress(chViewModel.usersEmailAddress.value)

        chViewModel.updateCurrentAuthUserRole(chViewModel.CH_UserRole.value)
        chViewModel.updateCurrentAuthAPIServer(chViewModel.serverSettings.value)
        chViewModel.updateCurrentAuthUserEmail(chViewModel.usersEmailAddress.value)
        chViewModel.updateCurrentAuthUserRole(chViewModel.CH_UserRole.value)

        return null
    }

    fun destroyNetworkData() {
        if (this::ebsDeviceMonitor.isInitialized) {
            ebsDeviceMonitor.onEvent(DisconnectEvent)
            ebsDeviceMonitor.disconnect()
            ebsDeviceMonitor.stopScanningJob()
        }
    }

}