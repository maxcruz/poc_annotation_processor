package com.example.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    //@Inject
    lateinit var tracker: MainTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject()

        tracker.onMainScreenLoaded()
        setContentView(R.layout.activity_main)
    }

    private fun inject() {
        (application as POCApplication).appComponent.inject(this)
    }
}
