package com.example.indiasheltertask.utils

import android.app.Application

class appApplication:Application() {

    override fun onCreate() {
        super.onCreate()

        SharedPref.init(this)
    }
}