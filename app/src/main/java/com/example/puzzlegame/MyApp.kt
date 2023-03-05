package com.example.puzzlegame

import android.app.Application
import android.os.StrictMode

class MyApp : Application() {
    fun MyApp() {
        if (BuildConfig.DEBUG)
            StrictMode.enableDefaults()
    }
}