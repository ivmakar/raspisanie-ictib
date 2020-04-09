package ru.ivmak.raspisanie_iktib.utils

class Constants {
    companion object {

        const val NOTIFICATION_CHANNEL_ID = "notification_time_table"
        const val NOTIFICATION_NAME = "timetable notification"

        const val WORKER_TAG = "NotifyWorker"

        const val TIME_FORMAT = "HH:mm"
        const val DEF_NOTIF_TIME = "07:00"

        const val APP_PREF = "my_app_preferences"
        const val LAST_TT = "last_time_table"
        const val PREF_IS_NOTIF = "is_notify"
        const val PREF_NOTIF_TIME = "notify_time"

    }
}