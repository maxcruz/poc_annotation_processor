package com.example.app.di

import com.example.app.MainActivity
import dagger.Component

@Component(modules = [MainModule::class])
interface ApplicationComponent {

    fun inject(activity: MainActivity)
}
