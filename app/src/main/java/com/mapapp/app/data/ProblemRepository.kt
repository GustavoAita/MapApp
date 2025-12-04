package com.mapapp.app.data

import com.mapapp.app.data.database.ProblemDao
import com.mapapp.app.data.model.Problem
import com.mapapp.app.data.model.toEntity
import com.mapapp.app.data.model.toProblem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProblemRepository(private val problemDao: ProblemDao) {

    // Observa todos os problemas (atualiza automaticamente)
    val allProblems: Flow<List<Problem>> = problemDao.getAllProblems()
        .map { entities -> entities.map { it.toProblem() } }

    // Inserir novo problema
    suspend fun insertProblem(problem: Problem) {
        problemDao.insertProblem(problem.toEntity())
    }

    // Buscar problema por ID
    suspend fun getProblemById(id: String): Problem? {
        return problemDao.getProblemById(id)?.toProblem()
    }

    // Atualizar problema
    suspend fun updateProblem(problem: Problem) {
        problemDao.updateProblem(problem.toEntity())
    }

    // Deletar problema
    suspend fun deleteProblem(problem: Problem) {
        problemDao.deleteProblem(problem.toEntity())
    }

    // Contar problemas
    suspend fun getProblemsCount(): Int {
        return problemDao.getProblemsCount()
    }

    // Deletar todos (útil para testes)
    suspend fun deleteAllProblems() {
        problemDao.deleteAllProblems()
    }
}