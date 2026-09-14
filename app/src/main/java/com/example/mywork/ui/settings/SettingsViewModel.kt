package com.example.mywork.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mywork.MyWorkApplication
import com.example.mywork.data.ReminderSettings
import com.example.mywork.data.ReminderTiming
import com.example.mywork.notifications.NotificationHelper
import com.example.mywork.notifications.ReminderScheduler
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = getApplication<MyWorkApplication>()
    private val prefsRepo = app.userPreferencesRepository

    val settings: StateFlow<ReminderSettings> = prefsRepo.settings

    fun toggleReminder(isEnabled: Boolean) {
        prefsRepo.updateSettings(isEnabled = isEnabled)
        ReminderScheduler.scheduleDailyReminder(app, prefsRepo.settings.value)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefsRepo.updateSettings(hour = hour, minute = minute)
        ReminderScheduler.scheduleDailyReminder(app, prefsRepo.settings.value)
    }

    fun setReminderTiming(timing: ReminderTiming) {
        prefsRepo.updateSettings(timing = timing)
        ReminderScheduler.scheduleDailyReminder(app, prefsRepo.settings.value)
    }

    fun sendTestNotification() {
        NotificationHelper.showNotification(
            context = app,
            title = "Test Work Order Reminder",
            message = "This is a test notification from MyWork! Your reminders are working correctly."
        )
    }
}
