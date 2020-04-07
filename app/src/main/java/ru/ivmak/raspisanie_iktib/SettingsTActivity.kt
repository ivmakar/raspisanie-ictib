package ru.ivmak.raspisanie_iktib

import android.app.TimePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SettingsTActivity : AppCompatActivity() {

    private lateinit var reminderSwitch: Switch
    private lateinit var timeLayout: LinearLayout
    private lateinit var timeTextView: TextView

    private val sdf = SimpleDateFormat(Constants.TIME_FORMAT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_t)

        reminderSwitch = findViewById(R.id.reminder_sw)
        timeLayout = findViewById(R.id.time_layout)
        timeTextView = findViewById(R.id.time_tv_value)

        val prefs = getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE)
        val time = prefs.getString(Constants.PREF_NOTIF_TIME, Constants.DEF_NOTIF_TIME)
        timeTextView.text = time

        timeLayout.setOnClickListener {
            val date = sdf.parse(timeTextView.text.toString())
            showTimePickerDialog(this, date.hours, date.minutes)
        }

        reminderSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            timeLayout.isEnabled = isChecked
        }
    }

    private fun showTimePickerDialog(context: Context, hours: Int, minutes: Int) {
        val timePicker = TimePickerDialog(
            context,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

                val date = Calendar.getInstance()
                date.set(0, 0, 0, hourOfDay, minute)
                timeTextView.text = sdf.format(Date(date.timeInMillis))
            },
            hours,
            minutes,
            true
        )
        timePicker.show()
    }

    override fun onStop() {
        super.onStop()

        val prefs = getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE).edit()

        val isNotif = reminderSwitch.isChecked
        prefs.putBoolean(Constants.PREF_IS_NOTIF, isNotif)

        val time = timeTextView.text.toString()
        prefs.putString(Constants.PREF_NOTIF_TIME, time)
        prefs.apply()

        if (!isNotif) {
            WorkManager.getInstance().cancelAllWorkByTag(Constants.WORKER_TAG)
            return
        }

        Utils.scheduleNotification(this, Utils.getDuration(time))
    }
}
