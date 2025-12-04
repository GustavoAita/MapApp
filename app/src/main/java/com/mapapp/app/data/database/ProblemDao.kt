package com.mapapp.app.data.database

import androidx.room.*
import com.mapapp.app.data.model.ProblemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {

    @Query("SELECT * FROM problems ORDER BY createdAt DESC")
    fun getAllProblems(): Flow<List<ProblemEntity>>

    @Query("SELECT * FROM problems WHERE id = :problemId")
    suspend fun getProblemById(problemId: String): ProblemEntity?

    @Query("SELECT * FROM problems WHERE category = :category ORDER BY createdAt DESC")
    fun getProblemsByCategory(category: String): Flow<List<ProblemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: ProblemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(problems: List<ProblemEntity>)

    @Update
    suspend fun updateProblem(problem: ProblemEntity)

    @Delete
    suspend fun deleteProblem(problem: ProblemEntity)

    @Query("DELETE FROM problems")
    suspend fun deleteAllProblems()

    @Query("SELECT COUNT(*) FROM problems")
    suspend fun getProblemsCount(): Int
}