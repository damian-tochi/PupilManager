package com.bridge.androidtechnicaltest.di

import com.bridge.androidtechnicaltest.db.AppDatabase
import com.bridge.androidtechnicaltest.db.IPupilRepository
import com.bridge.androidtechnicaltest.db.PupilRepository
import com.bridge.androidtechnicaltest.viewmodel.PupilListViewModel
import com.bridge.androidtechnicaltest.viewmodel.PupilSharedViewModel
import org.koin.androidx.viewmodel.dsl.viewModel

import org.koin.dsl.module

val networkModule = module {
    single { PupilAPIFactory(get()) }
    single {
        get<PupilAPIFactory>().retrofitPupil()
    }
}

val databaseModule = module {
    factory { DatabaseFactory.getDBInstance(get()) }
    single<IPupilRepository>{ PupilRepository(get(), get()) }
}

val viewModelModule = module {
    viewModel { PupilListViewModel(get()) }
}

val sharedViewModelModule = module {
    viewModel { PupilSharedViewModel(get<AppDatabase>().pupilDao) }
}