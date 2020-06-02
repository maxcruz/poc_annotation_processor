package com.example.app

import com.example.annotation.Event
import com.example.annotation.EventTracker
import com.example.annotation.Property

@EventTracker
interface MainTracker {

    @Event("Main_View")
    fun onMainScreenLoaded()

    @Event("Main_Click_More")
    fun onButtonClicked(@Property("Click_Time") time: String)
}
