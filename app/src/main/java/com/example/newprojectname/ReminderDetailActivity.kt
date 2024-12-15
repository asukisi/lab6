package com.example.newprojectname

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class ReminderDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_detail)

        // Получение данных из Intent
        val title = intent.getStringExtra("title") ?: "Без заголовка"
        val message = intent.getStringExtra("message") ?: "Нет описания"

        // Инициализация элементов интерфейса
        val textViewTitle = findViewById<TextView>(R.id.textViewTitle)
        val textViewMessage = findViewById<TextView>(R.id.textViewMessage)

        // Установка текста
        textViewTitle.text = title
        textViewMessage.text = message
    }
}
