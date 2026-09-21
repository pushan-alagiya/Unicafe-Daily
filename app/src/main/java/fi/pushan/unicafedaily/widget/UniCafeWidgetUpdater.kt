package fi.pushan.unicafedaily.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UniCafeWidgetUpdater {
    private const val TAG = "UniCafeWidgetUpdater"

    suspend fun updateAll(context: Context) = withContext(Dispatchers.IO) {
        try {
            val glanceWidget = UniCafeGlanceWidget()
            glanceWidget.updateAll(context)

            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(UniCafeGlanceWidget::class.java)
            for (glanceId in glanceIds) {
                try {
                    glanceWidget.update(context, glanceId)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed updating glanceId $glanceId", e)
                }
            }

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, UniCafeWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    component = componentName
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }

            Log.d(TAG, "All UniCafe widgets updated successfully (count: ${glanceIds.size})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widgets", e)
        }
    }
}
