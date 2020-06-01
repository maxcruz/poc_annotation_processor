package com.example.app

import com.example.annotation.Event
import com.example.annotation.EventTracker
import com.example.annotation.Property

@EventTracker(["MIXPANEL"])
interface DetailTracker {

    @Event("Detail_View")
    fun onDetailScreenLoaded()

    @Event("Detail_Click_Back")
    fun onBackButtonClicked(
        @Property("Something") somethind: String,
        @Property("Another") another: String
    )
}
