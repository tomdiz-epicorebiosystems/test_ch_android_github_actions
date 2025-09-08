package com.epicorebiosystems.rehydrate.networkManager

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object ApiClient {

    // Configure the HttpClient
    @OptIn(ExperimentalSerializationApi::class, InternalAPI::class)
    var client = HttpClient(Android) {

        // For Logging
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

        // Timeout plugin
        install(HttpTimeout) {
            requestTimeoutMillis = 15000L
            connectTimeoutMillis = 15000L
            socketTimeoutMillis = 15000L
        }

        // JSON Response properties
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }

        install(ResponseObserver) {
            onResponse { response ->
                println("HTTP status: ${response.status.value}")
            }
        }

        // Default request for POST, PUT, DELETE,etc...
        install(DefaultRequest) {
            //header(HttpHeaders.ContentType, ContentType.Application.Json)
            header("Content-Type", "application/json; charset=utf-8")
            //header("ch-phone-api-key", ApiRoutes.CH_PHONE_API_KEY)
            // Add this accept() for accept Json Body or Raw Json as Request Body
            accept(ContentType.Application.Json)
        }

        HttpResponseValidator {
            validateResponse {
                val statusCode = it.status.value
                when (statusCode) {
                    in 200..299 -> print(it.content.toString())
                    in 300..399 -> print(it.content.toString())
                    in 400..499 -> {
                        print(it.content.toString())
                    }
                    in 500..599 -> print(it.content.toString())
                }
            }
            handleResponseExceptionWithRequest { exception, _ ->
                val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                val exceptionResponse = clientException.response
                if (exceptionResponse.status == HttpStatusCode.NotFound) {
                    val exceptionResponseText = exceptionResponse.bodyAsText()
                    print(exceptionResponseText)
                 }
            }
        }
    }
}