package com.example.timescape

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.logging.Handler

class ConfirmationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        android.os.Handler().postDelayed({
            val intent = Intent(this, MainUserInterface::class.java)
            startActivity(intent)
            finish()
        }, 6000)
    }
}