package com.example.app.di

import com.example.app.MainActivity
import dagger.Component

@Component(modules = [Module::class])
interface ApplicationComponent {

    fun inject(activity: MainActivity)
}
