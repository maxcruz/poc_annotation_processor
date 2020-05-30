package com.example.app.di

import com.example.app.MainTracker
import com.example.app.MainTrackerDecorator
import com.example.tracking.FakeTracker
import com.example.tracking.Tracker
import dagger.Module
import dagger.Provides

@Module
object Module {

    @Provides
    fun providesTracker(fakeTracker: FakeTracker): Tracker = fakeTracker

    @Provides
    fun providesMainTracker(mainTrackerDecorator: MainTrackerDecorator): MainTracker = mainTrackerDecorator
}
