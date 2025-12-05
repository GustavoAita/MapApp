package com.mapapp.app

import android.app.Application
import com.mapapp.app.data.ProblemRepository
import com.mapapp.app.data.database.AppDatabase
import com.mapapp.app.data.firebase.FirestoreManager
import com.mapapp.app.data.firebase.SyncManager

class MapAppApplication : Application() {

    lateinit var syncManager: SyncManager
        private set

    override fun onCreate() {
        super.onCreate()

        // Inicializar componentes
        val database = AppDatabase.getDatabase(this)
        val repository = ProblemRepository(database.problemDao())
        val firestoreManager = FirestoreManager()

        // Inicializar SyncManager
        syncManager = SyncManager(this, repository, firestoreManager)

        // Iniciar sincronizacao automatica
        syncManager.startAutoSync()
    }
}