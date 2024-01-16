package com.androidnomad.activitymap.model

data class RouteGeoData(
    val coordinates: List<Coordinate>,
    val coordinateProperties: CoordinateProperties,
    val type: String
)