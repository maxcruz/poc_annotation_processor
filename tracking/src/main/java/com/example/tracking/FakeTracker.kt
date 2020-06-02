package com.example.tracking

import javax.inject.Inject

class FakeTracker @Inject constructor(): Tracker {
    override fun track(event: String, properties: Map<String, String>?) {
        println("EVENT: $event PROPERTIES: $properties")
    }
    override fun mapExtraEvents(events: Map<String, List<TrackerEnum>>) {
        println("TRACK EXTRA EVENTS: $events")
    }
}
