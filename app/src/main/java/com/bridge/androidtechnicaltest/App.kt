package com.bridge.androidtechnicaltest

import android.app.Application
import com.bridge.androidtechnicaltest.di.databaseModule
import com.bridge.androidtechnicaltest.di.networkModule
import com.bridge.androidtechnicaltest.di.sharedViewModelModule
import com.bridge.androidtechnicaltest.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import kotlin.collections.*
import kotlin.collections.listOf

class App : Application() {

    private val appComponent : List<Module> = listOf(networkModule, databaseModule, viewModelModule, sharedViewModelModule)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(appComponent)
        }
    }
}