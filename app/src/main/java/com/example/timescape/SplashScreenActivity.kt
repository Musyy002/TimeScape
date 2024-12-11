package com.example.timescape

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var typingTextView: TextView
    private val textToType = "TimeScape"
    private var currentIndex = 0
    private val delayMillis: Long = 450

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        android.os.Handler().postDelayed({
            val anim = findViewById<LottieAnimationView>(R.id.lottieAnimationView)
            anim.visibility= View.GONE
            val img = findViewById<ImageView>(R.id.imageView)
            img.visibility=View.VISIBLE
        }, 5000)

        typingTextView = findViewById(R.id.typingTextView)

        startTypingAnimation()
    }

    private fun startTypingAnimation() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (currentIndex <= textToType.length) {
                    typingTextView.text = textToType.substring(0, currentIndex++)
                    handler.postDelayed(this, delayMillis)

                } else {
                    startActivity(Intent(this@SplashScreenActivity,InfoActivity::class.java))
                    openMainActivity()
                }
            }
        }
        handler.postDelayed(runnable, delayMillis)
    }

    private fun openMainActivity() {

        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser!=null){
            val intent = Intent(this,MainUserInterface::class.java)
            startActivity(intent)
        }
        else{
            val intent = Intent(this,InfoActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}