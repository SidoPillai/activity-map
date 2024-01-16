package com.androidnomad.activitymap.activitymap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnomad.activitymap.model.ActivityMapData
import com.androidnomad.activitymap.model.Coordinate
import com.androidnomad.activitymap.model.CoordinateProperties
import com.androidnomad.activitymap.model.RouteGeoData
import com.androidnomad.activitymap.model.RouteSampleData
import com.androidnomad.activitymap.model.RouteSamples
import com.androidnomad.activitymap.model.RouteStatistics
import com.androidnomad.activitymap.model.TemplateType
import com.androidnomad.activitymap.model.Timestamp
import com.androidnomad.activitymap.model.Units
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.httpcore.Request
import com.arcgismaps.httpcore.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ActivityMapViewModel() : ViewModel() {

    val itemId = "4925a71f61e84bffb548c7d3fce07ade"
    val geoDataResourceId = "9ed73274-f4b8-4d4d-97d5-95131ee67ca3.json"
    val samplesResourceId = "route-samples-k4Hg2O.json"

    private val _activityMapUiState = MutableStateFlow<ActivityMapUiState>(ActivityMapUiState.Loading)
    val activityMapUiState: StateFlow<ActivityMapUiState> = _activityMapUiState.asStateFlow()

    private suspend fun fetchResourceData(itemId: String, resourceId: String): Result<Response> {
        val request = Request
            .builder()
            .url("https://www.arcgis.com/sharing/rest/content/items/$itemId/resources/$resourceId?f=json")
        return ArcGISEnvironment.arcGISHttpClient.execute(request.build())
    }

    init {
        fetchActivityMapData()
    }

    private fun fetchActivityMapData() {
        viewModelScope.launch {
            val coordinatesFlow = fetchRouteCoordinates(itemId, geoDataResourceId)
            val samplesFlow = fetchRouteSamples(itemId, samplesResourceId)

            combine(coordinatesFlow, samplesFlow) { geodata, samples ->
                val activityMapData = ActivityMapData(
                    id = "",
                    templateType = TemplateType.MAP_FOCUSSED,
                    routeStatistics = RouteStatistics(
                        elevationGain = 519.6850443762444,
                        elevationGainUnits = Units.FEET,
                        maxDistance = 1.9266002581628552,
                        maxDistanceUnits = Units.MILES
                    ),
                    routeSamples = samples,
                    routeGeoData = geodata,
                    caption = "",
                    mapScreenshotUrl = ""
                )
//                Log.d("ActivityMapViewModel", "Success")
                _activityMapUiState.value = ActivityMapUiState.Success(activityMapData)
            }.catch {
                _activityMapUiState.value = ActivityMapUiState.Error
            }.onStart {
                _activityMapUiState.value = ActivityMapUiState.Loading
            }.collect()
        }
    }

    private suspend fun fetchRouteCoordinates(
        itemId: String,
        resourceId: String
    ): Flow<RouteGeoData> = flow {
        fetchResourceData(itemId, resourceId)
            .onSuccess {
                val coordinates = mutableListOf<Coordinate>()
                val stringResponse = it.body?.string()
                val jObj = JSONObject(stringResponse)
                val features = jObj.get("features") as JSONArray
                val feature = features[0] as JSONObject
                val geometry = feature.get("geometry") as JSONObject
                val properties = feature.get("properties") as JSONObject
                val coordinateProperties = properties.get("coordinateProperties") as JSONObject
                val times = coordinateProperties.get("times") as JSONArray
                val _times = times[0] as JSONArray
                val coordinatesList = geometry.get("coordinates") as JSONArray
                val _coordinates = coordinatesList[0] as JSONArray
                for (i in 0 until _coordinates.length()) {
                    val _coordinate = _coordinates[i] as JSONArray
                    val latitude = _coordinate[0] as Double
                    val longitude = _coordinate[1] as Double
                    val elevation = _coordinate[2] as Double
                    coordinates.add(Coordinate(latitude, longitude, elevation))
                }
                val timeStampList = mutableListOf<Timestamp>()
                for (i in 0 until _times.length()) {
                    val _time = _times[i] as Long
                    timeStampList.add(Timestamp(_time))
                }
                val routeGeoData = RouteGeoData(coordinates, CoordinateProperties(timeStampList), "LineString")
//                Log.d("ActivityMapViewModel", "routeGeoData: $routeGeoData")
                emit(routeGeoData)
            }
            .onFailure {
                error("Something went wrong")
            }
    }

    private suspend fun fetchRouteSamples(itemId: String, resourceId: String): Flow<RouteSamples> =
        flow {
            fetchResourceData(itemId, resourceId)
                .onSuccess {
                    val stringResponse = it.body?.string()
                    val jObj = JSONArray(stringResponse)
                    val routeSamples = mutableListOf<RouteSampleData>()
                    for (i in 0 until jObj.length()) {
                        val routeSample = jObj[i] as JSONArray
                        if (routeSample.length() > 0) {
                            val distance = routeSample.getDouble(0)
                            val elevation = routeSample.getDouble(1)
//                            Log.d("ActivityMapViewModel", "distance: $distance, elevation: $elevation")
                            val routeSampleData = RouteSampleData(distance, elevation)
                            routeSamples.add(routeSampleData)
                        }
                    }
                    val routeSample = RouteSamples(routeSamples)
//                    Log.d("ActivityMapViewModel", "routeSample: $routeSample")
                    emit(routeSample)
                }
                .onFailure {
                    error("Something went wrong")
                }
        }
}

/**
 * Defined UI States for the [ActivityMapUiState]
 */
sealed interface ActivityMapUiState {
    data class Success(val data: ActivityMapData) : ActivityMapUiState
    data object Error : ActivityMapUiState
    data object Loading : ActivityMapUiState
}