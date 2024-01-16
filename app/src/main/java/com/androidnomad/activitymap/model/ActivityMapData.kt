package com.androidnomad.activitymap.model

/**
 * n-m6fH6B": {
 *       "type": "activity-map",
 *       "data": {
 *         "templateType": "media-focused",
 *         "routeGeoDataResourceId": "r-Yumbyl",
 *         "routeSamplesResourceId": "r-ZKzG1D",
 *         "routeStatistics": {
 *           "elevationGain": {
 *             "value": 519.6850443762444,
 *             "unit": "feet"
 *           },
 *           "maxDistance": {
 *             "value": 1.9266002581628552,
 *             "unit": "miles"
 *           }
 *         },
 *         "caption": "A night run around Columbia Heights in Washington, Virginia. More details here: <a href=\"https://www.traillink.com/city/columbia-heights-mn-trails/\" rel=\"noopener noreferrer\" target=\"_blank\">https://www.traillink.com/city/columbia-heights-mn-trails/</a>",
 *         "screenshots": {
 *           "PRINT": "r-CByzPL"
 *         }
 */

data class ActivityMapData(
    val id: String,
    val templateType: TemplateType,
    val routeStatistics: RouteStatistics,
    val routeSamples: RouteSamples,
    val routeGeoData: RouteGeoData,
    val caption: String,
    val mapScreenshotUrl: String
)