package com.example.passaquiz
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_page)

        val textViewResult: TextView = findViewById(R.id.firstTextView)
        val secondTextView: TextView = findViewById(R.id.secondTextView)

        val resultValue = intent.getStringExtra("result")
        val subTitle = intent.getStringExtra("subTitle")
        textViewResult.text = resultValue
        secondTextView.text = subTitle
    }
}
