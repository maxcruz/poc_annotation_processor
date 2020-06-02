package com.example.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class EventTracker(val engines: Array<String> = ["MIXPANEL"])
