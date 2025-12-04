package com.mapapp.app.ui.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.mapapp.app.data.ProblemRepository
import com.mapapp.app.data.database.AppDatabase
import com.mapapp.app.data.model.Problem
import com.mapapp.app.databinding.ActivityProblemDetailBinding
import kotlinx.coroutines.launch

class ProblemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProblemDetailBinding
    private lateinit var repository: ProblemRepository
    private var currentProblem: Problem? = null

    companion object {
        const val EXTRA_PROBLEM_ID = "problem_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProblemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Repository
        val database = AppDatabase.getDatabase(this)
        repository = ProblemRepository(database.problemDao())

        // Obter ID do problema
        val problemId = intent.getStringExtra(EXTRA_PROBLEM_ID)
        if (problemId == null) {
            Toast.makeText(this, "Erro ao carregar problema", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        loadProblem(problemId)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnUpvote.setOnClickListener {
            upvoteProblem()
        }

        binding.btnDownvote.setOnClickListener {
            downvoteProblem()
        }
    }

    private fun loadProblem(problemId: String) {
        lifecycleScope.launch {
            val problem = repository.getProblemById(problemId)

            if (problem != null) {
                currentProblem = problem
                runOnUiThread {
                    displayProblem(problem)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@ProblemDetailActivity,
                        "Problema nao encontrado",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun displayProblem(problem: Problem) {
        // Categoria e emoji
        binding.tvCategory.text = problem.category

        // Titulo
        binding.tvTitle.text = problem.title

        // Informacoes do usuario e tempo
        binding.tvUserInfo.text = "Reportado por ${problem.userName} - ${problem.getTimeAgo()}"

        // Descricao
        binding.tvDescription.text = problem.description

        // Localizacao
        if (problem.latitude != 0.0 && problem.longitude != 0.0) {
            binding.tvLocation.text = "Localizacao: ${String.format("%.6f", problem.latitude)}, ${String.format("%.6f", problem.longitude)}"
        } else {
            binding.tvLocation.text = "Localizacao nao disponivel"
        }

        // Status
        binding.tvStatus.text = when (problem.status) {
            "active" -> "Ativo"
            "in_progress" -> "Em Andamento"
            "resolved" -> "Resolvido"
            else -> "Desconhecido"
        }

        // Votos
        binding.tvVotes.text = problem.votesCount.toString()

        // Foto
        if (problem.photoUrl != null) {
            // TODO: Carregar foto real quando implementar camera
            binding.tvPhotoPlaceholder.isVisible = true
            binding.tvPhotoPlaceholder.text = "Foto do problema\n(em desenvolvimento)"
        } else {
            binding.tvPhotoPlaceholder.isVisible = true
            binding.tvPhotoPlaceholder.text = "Sem foto"
        }
    }

    private fun upvoteProblem() {
        val problem = currentProblem ?: return

        // Incrementar votos
        val updatedProblem = problem.copy(
            votesCount = problem.votesCount + 1
        )

        // Salvar no banco
        lifecycleScope.launch {
            repository.updateProblem(updatedProblem)

            runOnUiThread {
                currentProblem = updatedProblem
                binding.tvVotes.text = updatedProblem.votesCount.toString()
                Toast.makeText(
                    this@ProblemDetailActivity,
                    "Voto registrado!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun downvoteProblem() {
        val problem = currentProblem ?: return

        // Decrementar votos (minimo 0)
        val newVoteCount = maxOf(0, problem.votesCount - 1)
        val updatedProblem = problem.copy(
            votesCount = newVoteCount
        )

        // Salvar no banco
        lifecycleScope.launch {
            repository.updateProblem(updatedProblem)

            runOnUiThread {
                currentProblem = updatedProblem
                binding.tvVotes.text = updatedProblem.votesCount.toString()
                Toast.makeText(
                    this@ProblemDetailActivity,
                    "Voto removido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}