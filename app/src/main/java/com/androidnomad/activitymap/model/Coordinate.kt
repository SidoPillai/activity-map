package com.androidnomad.activitymap.model

import kotlinx.serialization.Serializable

@Serializable
data class Coordinate(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double
)
