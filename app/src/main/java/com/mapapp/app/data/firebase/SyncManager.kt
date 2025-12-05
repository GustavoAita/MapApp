package com.mapapp.app.data.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.mapapp.app.data.ProblemRepository
import com.mapapp.app.data.model.Problem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncManager(
    private val context: Context,
    private val repository: ProblemRepository,
    private val firestoreManager: FirestoreManager
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun syncProblemsFromFirestore() {
        if (!isOnline()) {
            return
        }

        scope.launch {
            try {
                val result = firestoreManager.getAllProblems()

                result.onSuccess { firestoreProblems ->
                    // Salvar todos no Room
                    firestoreProblems.forEach { problem ->
                        repository.insertProblem(problem)
                    }
                }
            } catch (e: Exception) {
                // Falha silenciosa - app funciona offline
            }
        }
    }

    suspend fun syncProblemToFirestore(problem: Problem): Result<Unit> {
        return withContext(Dispatchers.IO) {
            if (!isOnline()) {
                return@withContext Result.failure(Exception("Sem conexao com internet"))
            }

            try {
                val result = firestoreManager.addProblem(problem)
                result.map { Unit }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun syncLocalProblemsToFirestore() {
        if (!isOnline()) {
            return
        }

        withContext(Dispatchers.IO) {
            try {
                // Pegar count local
                val localCount = repository.getProblemsCount()

                if (localCount > 0) {
                    // Tentar sincronizar problemas locais que nao estao na nuvem
                    // Por enquanto, apenas log
                }
            } catch (e: Exception) {
                // Falha silenciosa
            }
        }
    }

    fun startAutoSync() {
        // Sincronizar do Firestore ao iniciar
        syncProblemsFromFirestore()
    }
}