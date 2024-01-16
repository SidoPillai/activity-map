package com.androidnomad.activitymap.activitymap

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ActivityMap() {
    val viewModel = ActivityMapViewModel()
    val uiState: ActivityMapUiState by viewModel.activityMapUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            is ActivityMapUiState.Loading -> {
                Log.d("ActivityMap", "Loading")
            }

            is ActivityMapUiState.Success -> {
                val data = (uiState as ActivityMapUiState.Success).data
                ActivityMapBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    activityMapData = data,
                )
            }

            is ActivityMapUiState.Error -> {
                Log.d("ActivityMap", "Error")
            }
        }
    }
}