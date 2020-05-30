package com.example.app

import android.app.Application
import com.example.app.di.ApplicationComponent
import com.example.app.di.DaggerApplicationComponent

class POCApplication : Application() {

    val appComponent: ApplicationComponent = DaggerApplicationComponent.create()

}
