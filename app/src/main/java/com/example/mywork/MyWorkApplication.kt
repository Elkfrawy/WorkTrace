package com.example.mywork

import android.app.Application
import com.example.mywork.data.AppDatabase
import com.example.mywork.data.SampleDataSeeder
import com.example.mywork.data.UserPreferencesRepository
import com.example.mywork.data.WorkRepository
import com.example.mywork.notifications.NotificationHelper
import com.example.mywork.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyWorkApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: WorkRepository by lazy {
        WorkRepository(database.customerDao(), database.workOrderDao())
    }
    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        ReminderScheduler.scheduleDailyReminder(this, userPreferencesRepository.settings.value)
        CoroutineScope(Dispatchers.IO).launch {
            SampleDataSeeder.seedIfEmpty(database)
        }
    }
}
