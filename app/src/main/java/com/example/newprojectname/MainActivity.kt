package com.example.newprojectname

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.newprojectname.DataBase.ReminderDatabaseHelper
import com.example.newprojectname.Notification.ReminderReceiver
import java.util.*
import android.app.TimePickerDialog


class MainActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSetDate: Button
    private lateinit var buttonSetTime: Button
    private lateinit var buttonSaveReminder: Button
    private lateinit var buttonViewReminders: Button

    private var reminderDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация UI элементов
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSetDate = findViewById(R.id.buttonSetDate)
        buttonSetTime = findViewById(R.id.buttonSetTime)
        buttonSaveReminder = findViewById(R.id.buttonSaveReminder)
        buttonViewReminders = findViewById(R.id.buttonViewReminders)

        // Проверка разрешения для уведомлений (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        // Создание канала уведомлений
        createNotificationChannel()

        // Установка обработчиков кнопок
        buttonSetDate.setOnClickListener { showDatePicker() }
        buttonSetTime.setOnClickListener { showTimePicker() }
        buttonSaveReminder.setOnClickListener { saveReminder() }
        buttonViewReminders.setOnClickListener {
            startActivity(Intent(this, ReminderListActivity::class.java))
        }
    }

    // Создание канала уведомлений
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Напоминания",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал для уведомлений о напоминаниях"
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Выбор даты
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CustomDatePickerDialog,
            { _, year, month, dayOfMonth ->
                reminderDate.set(Calendar.YEAR, year)
                reminderDate.set(Calendar.MONTH, month)
                reminderDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            },
            reminderDate.get(Calendar.YEAR),
            reminderDate.get(Calendar.MONTH),
            reminderDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Выбор времени
    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            this,
            R.style.CustomTimePickerDialog,
            { _, hourOfDay, minute ->
                reminderDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                reminderDate.set(Calendar.MINUTE, minute)
            },
            reminderDate.get(Calendar.HOUR_OF_DAY),
            reminderDate.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    // Сохранение напоминания
    private fun saveReminder() {
        val title = editTextTitle.text.toString().trim()
        val message = editTextMessage.text.toString().trim()

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = ReminderDatabaseHelper(this)
        val db = dbHelper.writableDatabase
        try {
            val values = ContentValues().apply {
                put(ReminderDatabaseHelper.COLUMN_TITLE, title)
                put(ReminderDatabaseHelper.COLUMN_MESSAGE, message)
                put(ReminderDatabaseHelper.COLUMN_DATE, reminderDate.timeInMillis)
            }

            val newRowId = db.insert(ReminderDatabaseHelper.TABLE_NAME, null, values)

            if (newRowId != -1L) {
                Toast.makeText(this, "Напоминание сохранено", Toast.LENGTH_SHORT).show()
                setReminderAlarm(reminderDate.timeInMillis, title, message)
            } else {
                Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
            }
        } finally {
            db.close()
        }
    }

    // Установка уведомления
    @SuppressLint("MissingPermission")
    private fun setReminderAlarm(reminderTime: Long, title: String, message: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminderTime.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }
}
