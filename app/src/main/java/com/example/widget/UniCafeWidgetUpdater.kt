package com.example.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll

object UniCafeWidgetUpdater {
    private const val TAG = "UniCafeWidgetUpdater"

    suspend fun updateAll(context: Context) {
        try {
            UniCafeGlanceWidget().updateAll(context)
            Log.d(TAG, "All UniCafe widgets updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widgets", e)
        }
    }
}
