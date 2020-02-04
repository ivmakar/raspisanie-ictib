package ru.ivmak.raspisanie_iktib

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.work.OneTimeWorkRequest
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.TimePicker
import androidx.work.Data
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.app.DatePickerDialog
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.preference.*
import java.text.SimpleDateFormat
import java.util.*


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onStop() {
        super.onStop()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val isNotif = prefs.getBoolean("is_notify", false)

        if (!isNotif) {
            WorkManager.getInstance().cancelAllWorkByTag(Constants.WORKER_TAG)
            return
        }

        val time = prefs.getString("notify_time", "7:00")

        val formatter = SimpleDateFormat("hh:mm")

        val date = formatter.parse(time)
        if (date.hours >= 0 && date.minutes >= 0) {

            val curDate = Date()
            var duration = (date.hours + 24 - curDate.hours) % 24 * 60 + (date.minutes - curDate.minutes)
            if (duration == 0) duration = 24*60

            val sPref = getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE)
            val data = Data.Builder()
                .putString(Constants.LAST_TT, sPref.getString(Constants.LAST_TT, "{\"result\": \"no_entries\"}"))
                .build()

            WorkManager.getInstance().cancelAllWorkByTag(Constants.WORKER_TAG)
            val myWorkRequest = OneTimeWorkRequest.Builder(NotifyWorker::class.java)
                .setInitialDelay(duration.toLong(), TimeUnit.MINUTES)
                .setInputData(data)
                .addTag(Constants.WORKER_TAG)
                .build()
            WorkManager.getInstance().enqueue(myWorkRequest)
        }

    }
}