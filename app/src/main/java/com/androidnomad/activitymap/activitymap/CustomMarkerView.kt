package com.androidnomad.activitymap.activitymap

import android.content.Context
import android.widget.TextView
import co.yml.charts.common.extensions.roundTwoDecimal
import com.androidnomad.activitymap.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight

class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    // Callbacks when the MarkerView is redrawn
    override fun refreshContent(entry: Entry?, highlight: Highlight?) {
        // Customize the content of the marker based on the Entry and Highlight
        tvContent.text = "Distance: ${entry?.x?.toDouble()!!.roundTwoDecimal()}" +
                "\nElevation: ${entry?.y?.toDouble()!!.roundTwoDecimal()}"
        super.refreshContent(entry, highlight)
    }
}