package com.androidnomad.activitymap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.androidnomad.activitymap.activitymap.ActivityMap
import com.androidnomad.activitymap.ui.theme.ActivityMapTheme
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.LicenseKey

class MainActivity : ComponentActivity() {
    private val licenseKey = buildString {
        append("runtimelite,1000,rud7826740674,none,NKMFA0PL4SY9NERL1214")
    }
    private val apiKey = buildString {
        append("AAPKb52b61a5d3674e28a8d656fd3c50d808V-vENuyoBGOSxw7n7EO2Ec0Uo5CBTxGusKg706ay2EsFH6Grc_5gNw4uIc5arxim")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArcGISEnvironment.apiKey = ApiKey.create(apiKey)
        ArcGISEnvironment.setLicense(LicenseKey.create(licenseKey)!!)

        setContent {
            ActivityMapTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivityMap()
                }
            }
        }
    }
}