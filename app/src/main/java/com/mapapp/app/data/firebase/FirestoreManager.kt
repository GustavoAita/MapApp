package com.mapapp.app.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mapapp.app.data.model.Problem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreManager {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val problemsCollection = firestore.collection("problems")

    suspend fun addProblem(problem: Problem): Result<String> {
        return try {
            val documentRef = problemsCollection.document(problem.id)
            documentRef.set(problem).await()
            Result.success(problem.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProblem(problem: Problem): Result<Unit> {
        return try {
            problemsCollection.document(problem.id).set(problem).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProblem(problemId: String): Result<Unit> {
        return try {
            problemsCollection.document(problemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProblemById(problemId: String): Result<Problem?> {
        return try {
            val document = problemsCollection.document(problemId).get().await()
            val problem = document.toObject(Problem::class.java)
            Result.success(problem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllProblemsFlow(): Flow<List<Problem>> = callbackFlow {
        val listener = problemsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val problems = snapshot.documents.mapNotNull { document ->
                        document.toObject(Problem::class.java)
                    }
                    trySend(problems)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun getAllProblems(): Result<List<Problem>> {
        return try {
            val snapshot = problemsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val problems = snapshot.documents.mapNotNull { document ->
                document.toObject(Problem::class.java)
            }
            Result.success(problems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProblemsByCategory(category: String): Result<List<Problem>> {
        return try {
            val snapshot = problemsCollection
                .whereEqualTo("category", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val problems = snapshot.documents.mapNotNull { document ->
                document.toObject(Problem::class.java)
            }
            Result.success(problems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementVotes(problemId: String): Result<Unit> {
        return try {
            val documentRef = problemsCollection.document(problemId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(documentRef)
                val currentVotes = snapshot.getLong("votesCount") ?: 0
                transaction.update(documentRef, "votesCount", currentVotes + 1)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}