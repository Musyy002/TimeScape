package com.example.timescape

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class InfoActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var skipButton: Button
    private lateinit var getStarted: Button

    private val animations = listOf(
        R.raw.timestore,
        R.raw.emojiss,
        R.raw.media,
        R.raw.memories
    )

    private val titles = listOf(
        R.string.heading_one,
        R.string.heading_two,
        R.string.heading_three,
        R.string.heading_fourth
    )

    private val descriptions = listOf(
        R.string.desc_one,
        R.string.desc_two,
        R.string.desc_three,
        R.string.desc_fourth
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        viewPager = findViewById(R.id.slideViewPager)
        backButton = findViewById(R.id.backbtn)
        nextButton = findViewById(R.id.nxtbtn)
        skipButton = findViewById(R.id.skipbtn)
        getStarted = findViewById(R.id.GetStarted)

        val adapter = LottieAdapter(animations, titles, descriptions)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                backButton.isEnabled = position != 0
                nextButton.text = if (position == animations.size - 1){
                    "NEXT"


                } else "NEXT"
                getStarted.visibility = if (position == animations.size - 1) View.VISIBLE else View.GONE
            }
        })

        backButton.setOnClickListener {
            viewPager.currentItem = viewPager.currentItem - 1
        }

        var reach = viewPager.currentItem

        nextButton.setOnClickListener {
            if (viewPager.currentItem < animations.size - 1) {
                viewPager.currentItem = viewPager.currentItem + 1

            }
            if (reach == animations.size){
                getStarted.visibility=View.VISIBLE
            }

            else {

            }
        }

        getStarted.setOnClickListener({
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        })

        skipButton.setOnClickListener {
            viewPager.currentItem = animations.size - 1
            getStarted.visibility=View.VISIBLE
        }
    }
}
