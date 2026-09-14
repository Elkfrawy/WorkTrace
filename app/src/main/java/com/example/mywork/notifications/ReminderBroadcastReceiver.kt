package com.example.mywork.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mywork.MyWorkApplication
import com.example.mywork.data.ReminderTiming
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val app = context.applicationContext as MyWorkApplication

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsRepo = app.userPreferencesRepository
                val settings = prefsRepo.settings.value

                if (settings.isEnabled) {
                    val repository = app.repository
                    val allOrders = repository.getAllWorkOrdersWithDetails().first()

                    val cal = Calendar.getInstance()
                    val todayStart = cal.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    val tomorrowStart = todayStart + (24 * 60 * 60 * 1000L)
                    val dayAfterTomorrowStart = tomorrowStart + (24 * 60 * 60 * 1000L)

                    val upcomingOrders = allOrders.filter { detail ->
                        val isPending = !detail.workOrder.isArchived &&
                                (detail.remainingItemsCount > 0 || detail.amountDue > 0.0)
                        val dueDate = detail.workOrder.dueDate

                        val matchesTiming = when (settings.timing) {
                            ReminderTiming.DAY_BEFORE -> dueDate in tomorrowStart until dayAfterTomorrowStart
                            ReminderTiming.DAY_OF -> dueDate in todayStart until tomorrowStart
                            ReminderTiming.BOTH -> dueDate in todayStart until dayAfterTomorrowStart
                        }

                        isPending && matchesTiming
                    }

                    if (upcomingOrders.isNotEmpty()) {
                        val title = when (settings.timing) {
                            ReminderTiming.DAY_BEFORE -> "Work Orders Due Tomorrow"
                            ReminderTiming.DAY_OF -> "Work Orders Due Today"
                            ReminderTiming.BOTH -> "Upcoming Work Orders"
                        }

                        val orderTitles = upcomingOrders.take(3).joinToString(", ") { it.workOrder.title }
                        val countText = if (upcomingOrders.size > 3) " and ${upcomingOrders.size - 3} more" else ""
                        val message = "You have ${upcomingOrders.size} work order(s) scheduled: $orderTitles$countText."

                        NotificationHelper.showNotification(context, title, message)
                    }

                    // Reschedule for next day
                    ReminderScheduler.scheduleDailyReminder(context, settings)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
