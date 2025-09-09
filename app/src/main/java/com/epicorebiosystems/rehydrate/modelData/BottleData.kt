package com.epicorebiosystems.rehydrate.modelData

import kotlinx.serialization.Serializable

@Serializable
data class BottleData(
    var id: Int,
    var barcode: String,
    var image_name: String,
    var name: String,
    var sodiumAmount: Float,
    var sodiumSize: String,
    var waterAmount: Float,
    var waterSize: String
)