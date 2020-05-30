package com.example.tracking

class FakeTracker: Tracker {
    override fun track(event: String, properties: Map<String, String>?) {
        println("EVENT: $event PROPERTIES: $properties")
    }
}
