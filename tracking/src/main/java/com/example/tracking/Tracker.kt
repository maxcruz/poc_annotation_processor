package com.example.tracking

interface Tracker {
    fun track(event: String, properties: Map<String, String>?)
}
