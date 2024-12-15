package com.example.newprojectname

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newprojectname.DataBase.ReminderDatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class ReminderListActivity : AppCompatActivity() {

    private lateinit var listViewReminders: ListView
    private lateinit var reminderAdapter: ArrayAdapter<String>
    private val reminders = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_list)

        listViewReminders = findViewById(R.id.listViewReminders)

        // Загрузка напоминаний из базы данных
        loadReminders()

        // Установка адаптера
        reminderAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, reminders)
        listViewReminders.adapter = reminderAdapter

        // Обработчик кликов на элемент списка
        listViewReminders.setOnItemClickListener { _, _, position, _ ->
            val reminderInfo = reminders[position]
            val reminderId = reminderInfo.split("|")[0].toLong() // Извлечение ID
            deleteReminder(reminderId)
            reminders.removeAt(position)
            reminderAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Напоминание удалено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadReminders() {
        val dbHelper = ReminderDatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery("SELECT ${ReminderDatabaseHelper.COLUMN_ID}, ${ReminderDatabaseHelper.COLUMN_TITLE}, ${ReminderDatabaseHelper.COLUMN_DATE} FROM ${ReminderDatabaseHelper.TABLE_NAME}", null)
        cursor.use {
            reminders.clear()
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(ReminderDatabaseHelper.COLUMN_ID))
                val title = it.getString(it.getColumnIndexOrThrow(ReminderDatabaseHelper.COLUMN_TITLE))
                val dateInMillis = it.getLong(it.getColumnIndexOrThrow(ReminderDatabaseHelper.COLUMN_DATE))
                val formattedDate = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(dateInMillis))
                reminders.add("$id|$title ($formattedDate)")
            }
        }
    }

    private fun deleteReminder(reminderId: Long) {
        val dbHelper = ReminderDatabaseHelper(this)
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete(ReminderDatabaseHelper.TABLE_NAME, "${ReminderDatabaseHelper.COLUMN_ID} = ?", arrayOf(reminderId.toString()))
        if (rowsDeleted > 0) {
            Toast.makeText(this, "Напоминание успешно удалено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Ошибка при удалении напоминания", Toast.LENGTH_SHORT).show()
        }
    }
}
