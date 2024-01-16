package com.androidnomad.activitymap.utils

import com.arcgismaps.mapping.Basemap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal

object MappingUtils {

    /**
     * The function that creates and returns a basemap based on the input theme's [basemapStyle] [Documentation here: https://mercator1.atlassian.net/wiki/spaces/SMXMOBILE/pages/3149037576/Feature+Plan+Frame+Theme+customization#New-themes-list]
     * @param basemapStyle The unique string identifier which denotes the type of the basemap to be returned
     */
    fun getBasemapFromTheme(basemapStyle: String?): Basemap {
        val portal = Portal(url = "https://www.arcgis.com", connection = Portal.Connection.Anonymous)

        return if (!basemapStyle.isNullOrEmpty()) {
            when (basemapStyle) {
                "imagery" -> {
                    Basemap(BasemapStyle.ArcGISImageryStandard)
                }
                "humanGeographyLight" -> {
                    Basemap(BasemapStyle.ArcGISLightGray)
                }
                "canvasDark" -> {
                    Basemap(BasemapStyle.ArcGISDarkGray)
                }
                "community" -> {
                    Basemap(BasemapStyle.ArcGISCommunity)
                }
                "nova" -> {
                    Basemap(BasemapStyle.ArcGISNova)
                }
                "worldImagery" -> {
                    Basemap(BasemapStyle.ArcGISImageryStandard)
                }
                "lightGrayCanvas" -> {
                    Basemap(BasemapStyle.ArcGISLightGray)
                }
                "navigation" -> {
                    Basemap(BasemapStyle.ArcGISNavigation)
                }
                "nationalGeographic" -> {
                    Basemap(PortalItem(portal = portal, itemId = "f33a34de3a294590ab48f246e99958c9"))
                }
                "stamenToner" -> {
                    Basemap(BasemapStyle.ArcGISLightGray)
                }
                "oceans" -> {
                    Basemap(BasemapStyle.ArcGISOceans)
                }
                "chartedTerritory" -> {
                    Basemap(BasemapStyle.ArcGISChartedTerritory)
                }
                "modernAntique" -> {
                    Basemap(BasemapStyle.ArcGISModernAntique)
                }
                "darkGrayCanvas" -> {
                    Basemap(BasemapStyle.ArcGISDarkGray)
                }
                "waterColor" -> {
                    Basemap(PortalItem(portal = portal, itemId = "21812b28afea4091bc57472297aa73d4"))
                }
                "firefly" -> {
                    Basemap(PortalItem(portal = portal, itemId = "9e557abc61ce41c9b8ec8b15800c20d3"))
                }
                else -> {
                    Basemap(BasemapStyle.ArcGISLightGray)
                }
            }
        } else {
            Basemap(BasemapStyle.ArcGISLightGray)
        }
    }
}