package fi.pushan.unicafedaily.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import fi.pushan.unicafedaily.data.repository.MenuFetchResult
import fi.pushan.unicafedaily.data.repository.UniCafeRepository
import fi.pushan.unicafedaily.widget.UniCafeWidgetUpdater
import java.util.concurrent.TimeUnit

class MenuSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "MenuSyncWorker"
        private const val PERIODIC_WORK_NAME = "UniCafeMenuPeriodicSync"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<MenuSyncWorker>(
                repeatInterval = 30,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = 10,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.d(TAG, "Scheduled periodic menu sync every 30 minutes")
        }

        fun syncImmediately(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<MenuSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeRequest)
            Log.d(TAG, "Enqueued immediate menu sync")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "MenuSyncWorker executing...")
        val repository = UniCafeRepository.getInstance(appContext)

        return try {
            val result = repository.fetchRestaurants(forceNetwork = true)
            when (result) {
                is MenuFetchResult.Success -> {
                    Log.d(TAG, "Menu sync successful, updating widgets")
                    UniCafeWidgetUpdater.updateAll(appContext)
                    Result.success()
                }
                is MenuFetchResult.Error -> {
                    Log.w(TAG, "Menu sync had error: ${result.message}")
                    UniCafeWidgetUpdater.updateAll(appContext)
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during MenuSyncWorker", e)
            Result.retry()
        }
    }
}
