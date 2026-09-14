package com.example.mywork.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ReminderTiming {
    DAY_BEFORE,
    DAY_OF,
    BOTH
}

data class ReminderSettings(
    val isEnabled: Boolean = true,
    val hour: Int = 9,       // 9:00 AM
    val minute: Int = 0,
    val timing: ReminderTiming = ReminderTiming.DAY_BEFORE
)

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_settings_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ReminderSettings> = _settings.asStateFlow()

    private fun loadSettings(): ReminderSettings {
        val isEnabled = prefs.getBoolean("key_reminder_enabled", true)
        val hour = prefs.getInt("key_reminder_hour", 9)
        val minute = prefs.getInt("key_reminder_minute", 0)
        val timingName = prefs.getString("key_reminder_timing", ReminderTiming.DAY_BEFORE.name)
        val timing = try {
            ReminderTiming.valueOf(timingName ?: ReminderTiming.DAY_BEFORE.name)
        } catch (e: Exception) {
            ReminderTiming.DAY_BEFORE
        }
        return ReminderSettings(isEnabled, hour, minute, timing)
    }

    fun updateSettings(
        isEnabled: Boolean = _settings.value.isEnabled,
        hour: Int = _settings.value.hour,
        minute: Int = _settings.value.minute,
        timing: ReminderTiming = _settings.value.timing
    ) {
        prefs.edit()
            .putBoolean("key_reminder_enabled", isEnabled)
            .putInt("key_reminder_hour", hour)
            .putInt("key_reminder_minute", minute)
            .putString("key_reminder_timing", timing.name)
            .apply()

        _settings.value = ReminderSettings(isEnabled, hour, minute, timing)
    }
}
