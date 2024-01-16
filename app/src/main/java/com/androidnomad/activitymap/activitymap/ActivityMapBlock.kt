package com.androidnomad.activitymap.activitymap

import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import co.yml.charts.common.extensions.roundTwoDecimal
import com.androidnomad.activitymap.R
import com.androidnomad.activitymap.model.ActivityMapData
import com.androidnomad.activitymap.model.Units
import com.androidnomad.activitymap.utils.ColorUtils
import com.androidnomad.activitymap.utils.MappingUtils
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.EnvelopeBuilder
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.PolylineBuilder
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.MapView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ActivityMapBlock(
    modifier: Modifier,
    activityMapData: ActivityMapData,
) {
    val backgroundColor = "#121212"
    val themeColor1 = "#49ECA6"
    val bodyMutedColor = "#A0A0A0"
    val titleColor = "#F3F3F3"
    val basemapPrimary = "darkGrayCanvas"

    val themeBackgroundColor = ColorUtils.getColorFromHex(backgroundColor)
    val themeThemeColor1 = ColorUtils.getColorFromHex(themeColor1)
    val themeMutedColor = ColorUtils.getColorFromHex(bodyMutedColor)
    val chartBackgroundColor = ColorUtils.lighten(themeBackgroundColor.toArgb(), 0.04f)
    val outlineColor = ColorUtils.getColorFromHex(themeColor1).toArgb()
    val chartLabelColor = ColorUtils.hexcodeToHSL(titleColor, 0.0f)
    val chartHighlightColor = ColorUtils.getColorFromHex(themeColor1).copy(alpha = 0.2f).toArgb()
    val chartFillColor = ColorUtils.forceLighten(themeThemeColor1.toArgb(), 0.85f)
    val infoTintColor = themeThemeColor1.toArgb()
    val infoValueColor = themeThemeColor1.toArgb()
    val infoLabelColor = themeMutedColor.toArgb()

    val lifecycleOwner = LocalLifecycleOwner.current
    val basemap = MappingUtils.getBasemapFromTheme(basemapPrimary)
    val arcGISMap by remember { mutableStateOf(ArcGISMap(basemap)) }

    val graphicsOverlay by remember { mutableStateOf(GraphicsOverlay()) }
    val pointGraphicsOverlay by remember { mutableStateOf(GraphicsOverlay()) }
    val view = LocalView.current
    val totalTime = activityMapData.routeGeoData.coordinateProperties.timestamps.let {
        it.lastOrNull()?.time?.minus(it.firstOrNull()?.time ?: 0L)
    } ?: 0L

    val points = activityMapData.routeGeoData.coordinates.map {
        Point(
            it.latitude,
            it.longitude,
            SpatialReference.wgs84()
        )
    }

    // line symbol
    val polylineSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, com.arcgismaps.Color(ColorUtils.getColorFromHexString(themeColor1).toArgb()), 2f)
    val polylineBuilder = PolylineBuilder(SpatialReference.wgs84()) {
        points.forEach { addPoint(it) }
    }
    val polyline = polylineBuilder.toGeometry()
    val polylineGraphic = Graphic(polyline, polylineSymbol)
    graphicsOverlay.graphics.add(polylineGraphic)

    // point symbol
    val opacitySymbolSize = 26f
    val outlineSymbolSize = 16f
    val innerSymbolSize = 10f
    val opacitySymbolColor = com.arcgismaps.Color(Color.White.copy(alpha = 0.2f).toArgb())
    val outlineSymbolColor = com.arcgismaps.Color(Color.White.toArgb())
    val innerSymbolColor = com.arcgismaps.Color(ColorUtils.getColorFromHexString(themeColor1).toArgb())
    val opacityMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Circle, opacitySymbolColor, opacitySymbolSize)
    val outlineMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Circle, outlineSymbolColor, outlineSymbolSize)
    val innerMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Circle, innerSymbolColor, innerSymbolSize)

    val routeSamples = activityMapData.routeSamples.routeSampleData
    val entryData = arrayListOf<Entry>()
    routeSamples.forEach {
        entryData.add(Entry(it.distance.toFloat(), it.elevation.toFloat()))
    }

    val lineDataSet = LineDataSet(entryData, null)
    lineDataSet.apply {
        setDrawCircles(false)
        setDrawValues(false)
        setDrawFilled(true)
        // vertical highlight indicator
        setDrawHighlightIndicators(true)
        isHighlightEnabled = true
        highLightColor = chartHighlightColor
        highlightLineWidth = 1.5f
//        enableDashedLine(10f, 10f, 0f)

        setDrawHorizontalHighlightIndicator(false)
        setDrawVerticalHighlightIndicator(true)
        setDrawIcons(false)
        setDrawIcons(false)
        setDrawIcons(false)
        setDrawIcons(false)

        // border around the graph
        color = outlineColor
        lineWidth = 1.5f

        // fill color
        fillColor = chartFillColor
        fillAlpha = 255

        mode = LineDataSet.Mode.CUBIC_BEZIER
    }
    val lineData = LineData(listOf(lineDataSet))

    val customMarkerView = CustomMarkerView(
        context = LocalContext.current,
        layoutResource = R.layout.custom_marker_view
    )

    var indexOfCoordinate by remember { mutableIntStateOf(-1) }

    fun addPoint(point: Point): List<Graphic> {
        val opacityMarkerSymbolGraphic = Graphic(point, opacityMarkerSymbol)
        val outlineMarkerSymbolGraphic = Graphic(point, outlineMarkerSymbol)
        val innerMarkerSymbolGraphic = Graphic(point, innerMarkerSymbol)

        return listOf(
            opacityMarkerSymbolGraphic,
            outlineMarkerSymbolGraphic,
            innerMarkerSymbolGraphic
        )
    }

    LaunchedEffect(key1 = indexOfCoordinate) {
        if (indexOfCoordinate > -1) {
            val total = activityMapData.routeSamples.routeSampleData.size
            val pointList = activityMapData.routeGeoData.coordinates.size
            val quotient = total / pointList
            val coordinateIndex = indexOfCoordinate / quotient
            if (coordinateIndex > -1 && coordinateIndex < pointList) {
                pointGraphicsOverlay.graphics.clear()
                points.let {
                    pointGraphicsOverlay.graphics.addAll(addPoint(it[coordinateIndex]))
                }
            }
        }
    }

    val extent = polyline.extent
    val enveloperBuilder = EnvelopeBuilder(
        Envelope(
            xMin = extent.xMin,
            yMin = extent.yMin,
            xMax = extent.xMax,
            yMax = extent.yMax,
            spatialReference = SpatialReference.wgs84()
        )
    ).apply {
        this.expand(1.2)
    }

    Column(
        modifier = Modifier
            .background(Color(themeBackgroundColor.toArgb()))
            .padding(top = 8.dp, bottom = 8.dp)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(start = 8.dp, end = 8.dp),
            factory = { context ->
                Log.d("ActivityMapBlock", "AndroidView for MapView")

                MapView(context).also {
                    it.interactionOptions.isEnabled = false
                    lifecycleOwner.lifecycle.addObserver(it)
                    it.map = arcGISMap
                    it.graphicsOverlays.add(graphicsOverlay)
                    it.graphicsOverlays.add(pointGraphicsOverlay)
                    it.setViewpoint(Viewpoint(enveloperBuilder.extent))
                    // it.setViewpoint(Viewpoint(extent))
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(start = 8.dp, end = 8.dp),
            factory = { context ->
                LineChart(context).apply {
                    customMarkerView.chartView = this
                    customMarkerView.alpha = 0.5f
                    customMarkerView.setBackgroundColor(chartBackgroundColor)
                    customMarkerView.clipToOutline = false
                    customMarkerView.isHapticFeedbackEnabled = true

                    // Configure the graph
                    setTouchEnabled(true)
                    setPinchZoom(false)
                    setDrawGridBackground(false)
                    setDrawBorders(false)
                    setScaleEnabled(false)
                    setDrawMarkers(true)
                    setBackgroundColor(chartBackgroundColor)
                    legend.isEnabled = false
                    isHighlightPerTapEnabled = true
                    isHighlightPerDragEnabled = true
                    marker = customMarkerView
                    description = Description().apply {
                        text = ""
                        // text = "$xAxisShortFormUnit vs $yAxisShortFormUnit"
                        // text = "distance vs elevation"
                    }
                    extraBottomOffset = 8f

                    // Configure the x-axis
                    xAxis.apply {
                        setDrawGridLines(false)
                        setDrawAxisLine(true)
                        setDrawLabels(true)
                        setDrawLimitLinesBehindData(false)
                        position = XAxis.XAxisPosition.BOTTOM

                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return value.toDouble().roundTwoDecimal().toString() + " ${activityMapData.routeStatistics.maxDistanceUnits.unit}"
                            }
                        }
                        setLabelCount(6, true)
                        textColor = chartLabelColor
                    }

                    // Configure the y-axis
                    axisLeft.apply {
                        setDrawGridLines(true)
                        setDrawAxisLine(false)
                        setDrawLabels(true)
                        setDrawLimitLinesBehindData(true)
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val rounded = (value / 10.0).roundToInt() * 10
                                return rounded.toString() + " ${activityMapData.routeStatistics.elevationGainUnits.unit}"
                            }
                        }
                        setLabelCount(4, true)
                        textColor = chartLabelColor
                        setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    }

                    axisRight.apply {
                        setDrawGridLines(false)
                        setDrawAxisLine(false)
                        setDrawLabels(false)
                        setDrawLimitLinesBehindData(false)
                    }

                    // Add the initial data
                    data = lineData

                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(e: Entry?, h: Highlight?) {
                            if (e != null) {
                                routeSamples.forEachIndexed { index, it ->
                                    if (it.distance.toFloat() == e.x) {
                                        indexOfCoordinate = index
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    }
                                }
                            }
                        }

                        override fun onNothingSelected() {
                            Log.d("ActivityMap", "onNothingSelected")
                        }
                    }
                    )

                }
            },
            update = {
                it.data = lineData
                it.notifyDataSetChanged()
                it.invalidate()
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val chipModifier = Modifier
                .background(Color(chartBackgroundColor))
                .fillMaxWidth()
                .weight(1f)
                .wrapContentHeight()
                .padding(start = 8.dp, end = 8.dp)

            DistanceChip(
                modifier = chipModifier,
                distance = activityMapData.routeStatistics.maxDistance,
                units = activityMapData.routeStatistics.maxDistanceUnits,
                tint = Color(infoTintColor),
                valueColor = Color(infoValueColor),
                labelColor = Color(infoLabelColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            DurationChip(
                modifier = chipModifier,
                totalTime = totalTime,
                tint = Color(infoTintColor),
                valueColor = Color(infoValueColor),
                labelColor = Color(infoLabelColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            ElevationGainChip(
                modifier = chipModifier,
                elevationGain = activityMapData.routeStatistics.elevationGain,
                units = activityMapData.routeStatistics.elevationGainUnits,
                tint = Color(infoTintColor),
                valueColor = Color(infoValueColor),
                labelColor = Color(infoLabelColor)
            )
        }
    }
}

@Composable
fun DistanceChip(
    modifier: Modifier,
    distance: Double,
    units: Units,
    tint: Color,
    valueColor: Color,
    labelColor: Color
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.drawable.ic_measure),
            contentDescription = null,
            tint = tint
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = distance.roundTwoDecimal().toString() + " ${units.unit}",
            color = valueColor,
            fontWeight = FontWeight.W700,
            fontSize = 19.sp,
            lineHeight = 32.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Distance",
            color = labelColor,
            fontWeight = FontWeight.W400,
            fontSize = 13.sp,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

}

@Composable
fun DurationChip(
    modifier: Modifier,
    totalTime: Long,
    tint: Color,
    valueColor: Color,
    labelColor: Color
) {
    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.drawable.ic_duration),
            contentDescription = null,
            tint = tint
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatToDigitalClock(totalTime),
            color = valueColor,
            fontWeight = FontWeight.W700,
            fontSize = 19.sp,
            lineHeight = 32.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Time",
            color = labelColor,
            fontWeight = FontWeight.W400,
            fontSize = 13.sp,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ElevationGainChip(
    modifier: Modifier,
    elevationGain: Double,
    units: Units,
    tint: Color,
    valueColor: Color,
    labelColor: Color
) {
    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.drawable.ic_elevation),
            contentDescription = null,
            tint = tint
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = elevationGain.roundTwoDecimal().toString() + " ${units.unit}",
            color = valueColor,
            fontWeight = FontWeight.W700,
            fontSize = 19.sp,
            lineHeight = 32.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Elevation gain",
            color = labelColor,
            fontWeight = FontWeight.W400,
            fontSize = 13.sp,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun formatToDigitalClock(milliSeconds: Long): String {
    return java.lang.String.format(
        Locale.US,
        "%02d:%02d:%02d", milliSeconds / (3600 * 1000),
        milliSeconds / (60 * 1000) % 60,
        milliSeconds / 1000 % 60
    )
}