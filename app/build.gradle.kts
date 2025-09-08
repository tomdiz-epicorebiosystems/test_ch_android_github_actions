plugins {
    alias(libs.plugins.nordic.application.compose)
    alias(libs.plugins.nordic.hilt)
//    alias(libs.plugins.nordic.feature)
//    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"    // NOTE: This needs to match kotlin version
}

android {
    namespace = "com.epicorebiosystems.rehydrate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.epicorebiosystems.rehydrate"
        minSdk = 26
        targetSdk = 34
        versionCode = 33
        versionName = "3.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("String","VERSION_NAME", "\"3.1.0 (Debug)\"")
            // *** NOTE(tsd): WHEN DOING QA BUILDS FOR AUTOMATED TESTING CHANGE THIS TO 'true' ***
            buildConfigField("Boolean","QA_TESTING", "false")
            buildConfigField("String","VERSION_CODE", "\"Build 9\"")
            isMinifyEnabled = false
        }
        release {
            buildConfigField("String","VERSION_NAME", "\"3.1.0\"")
            // *** NOTE(tsd): THIS SHOULD ALWAYS BE 'FALSE' FOR RELEASE BUILDS ***
            buildConfigField("Boolean","QA_TESTING", "false")
            buildConfigField("String","VERSION_CODE", "\"Build 9\"")
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.8.0-beta01")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.security:security-crypto:1.0.0")

    // App permissions for bluetooth, location
    //implementation("pub.devrel:easypermissions:3.0.0")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.0.2")
    implementation("androidx.camera:camera-lifecycle:1.0.2")
    implementation("androidx.camera:camera-view:1.0.0-alpha31")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Ktor Internet library
    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
    implementation("io.ktor:ktor-client-logging:2.3.6")
    implementation("io.ktor:ktor-client-android:2.3.6")
    implementation("io.ktor:ktor-client-cio:2.3.6")

    // GPS location - lat/long
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Number Picker
    implementation("com.chargemap.compose:numberpicker:1.0.3")

    // JWT Decode
    implementation("com.auth0.android:jwtdecode:2.0.2")

    // Charting
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Datadog
    implementation("com.datadoghq:dd-sdk-android-trace:2.8.0")
    implementation("com.datadoghq:dd-sdk-android-rum:2.19.2")
    implementation("com.datadoghq:dd-sdk-android-compose:2.19.0")

    // Google Play In-App Update
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // PDF Viewer
    implementation("io.github.grizzi91:bouquet:1.1.2")

    // kotlinx-datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    implementation(libs.nordic.core)
    implementation(libs.nordic.theme)
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.blek.uiscanner)
    implementation(libs.nordic.uilogger)
    implementation(libs.nordic.ble.common)
    implementation(libs.nordic.ble.ktx)
    implementation(libs.nordic.blek.client)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("no.nordicsemi.android.kotlin.ble:scanner:1.0.6")

    // Nordic semi service
    implementation(libs.nordic.blek.core)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.androidx.core)

    implementation(libs.nordic.blek.client)
    implementation(libs.nordic.blek.profile)
    implementation(libs.nordic.blek.core)
    implementation(libs.nordic.blek.server)
    implementation(libs.nordic.blek.advertiser)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation("org.simpleframework:simple-xml:2.7.1") {
        exclude(group = "stax", module = "stax-api")
        exclude(group = "xpp3", module = "xpp3")
    }

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")
}
