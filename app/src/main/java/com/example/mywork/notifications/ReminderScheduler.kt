package com.example.mywork.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mywork.data.ReminderSettings
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_NAME = "work_order_daily_reminder_work"

    fun scheduleDailyReminder(context: Context, settings: ReminderSettings) {
        val workManager = WorkManager.getInstance(context)

        if (!settings.isEnabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.hour)
            set(Calendar.MINUTE, settings.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay = target.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<WorkOrderReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
