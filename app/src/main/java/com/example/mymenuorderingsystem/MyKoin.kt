package com.example.mymenuorderingsystem

import android.app.Application
import androidx.work.Configuration
import android.os.Build
import androidx.room.Room
import androidx.work.WorkManager
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import timber.log.Timber

// roy modify for Koin
val appModule = module {
// roy modify for WorkManager
    single { WorkManager.getInstance(get()) }
// roy modify for Room
    single {
        val builder = Room.databaseBuilder(get(), AppDatabase::class.java, "order_db")

        if(BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration(true)
        }
        builder.build()
    }
    single { get<AppDatabase>().orderDao() }

    // single<MenuRepository> { MenuRepositoryImpl() }
    single<MenuRepository> { MenuRepositoryImpl(get()) }

    viewModel { MainViewModel(get()) }
    viewModel { OrderViewModel(get(), get()) }
// roy modify for Room
    viewModel { HistoryViewModel(get()) }

// roy modify for workManager
    worker { OrderUploadWorker(get(), get(), get()) }
}

class MyOrderingApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyOrderingApplication)
// roy modify for workManager
            workManagerFactory()
            modules(listOf(appModule, networkModule))
        }

// roy modify for timber log
        if(BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree())
        }
        Timber.tag("RoyChen").d("Main onCreate: ${ BuildConfig.FLAVOR }, ${ BuildConfig.API_URL },${ BuildConfig.API_MODE }")
        Timber.d("Main onCreate: ${ Build.MANUFACTURER }, ${ BuildConfig.API_SHOW }, ${ BuildConfig.API_NAME }")
    }

// roy modify for workManager
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(get<KoinWorkerFactory>())
            .setMinimumLoggingLevel(if(BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
}
