package com.mapapp.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mapapp.app.data.ProblemRepository
import com.mapapp.app.data.database.AppDatabase
import com.mapapp.app.data.model.Problem
import com.mapapp.app.databinding.FragmentFeedBinding
import com.mapapp.app.ui.detail.ProblemDetailActivity
import kotlinx.coroutines.launch

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProblemAdapter
    private lateinit var repository: ProblemRepository

    private var allProblems: List<Problem> = emptyList()
    private var filteredProblems: List<Problem> = emptyList()

    private var currentCategory: String? = null
    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        repository = ProblemRepository(database.problemDao())

        setupRecyclerView()
        setupSearchBar()
        setupFilters()
        observeProblems()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setupRecyclerView() {
        adapter = ProblemAdapter { problem ->
            onProblemClick(problem)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s.toString().trim()
                applyFilters()
            }
        })
    }

    private fun setupFilters() {
        // Limpar filtros
        binding.btnClearFilters.setOnClickListener {
            clearAllFilters()
        }

        // Chip Todos
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = null
                applyFilters()
            }
        }

        // Chip Buracos
        binding.chipPothole.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = "Buraco na via"
                applyFilters()
            }
        }

        // Chip Iluminação
        binding.chipLighting.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = "Iluminação pública"
                applyFilters()
            }
        }

        // Chip Lixo
        binding.chipGarbage.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = "Lixo/Limpeza"
                applyFilters()
            }
        }

        // Chip Vandalismo
        binding.chipVandalism.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = "Vandalismo"
                applyFilters()
            }
        }

        // Chip Outros
        binding.chipOther.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = "Outros"
                applyFilters()
            }
        }
    }

    private fun observeProblems() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.allProblems.collect { problems ->
                allProblems = problems
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        var filtered = allProblems

        // Aplicar filtro de categoria
        if (currentCategory != null) {
            filtered = filtered.filter { it.category == currentCategory }
        }

        // Aplicar busca por texto
        if (currentSearchQuery.isNotEmpty()) {
            filtered = filtered.filter { problem ->
                problem.title.contains(currentSearchQuery, ignoreCase = true) ||
                        problem.description.contains(currentSearchQuery, ignoreCase = true) ||
                        problem.category.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        filteredProblems = filtered
        updateUI()
    }

    private fun updateUI() {
        if (filteredProblems.isEmpty()) {
            showEmptyState()
        } else {
            showProblems()
        }

        // Mostrar/ocultar botão de limpar filtros
        val hasActiveFilters = currentCategory != null || currentSearchQuery.isNotEmpty()
        binding.btnClearFilters.isVisible = hasActiveFilters

        // Mostrar contador de resultados (se houver filtros)
        if (hasActiveFilters) {
            binding.tvResultCount.isVisible = true
            val count = filteredProblems.size
            binding.tvResultCount.text = "$count ${if (count == 1) "problema encontrado" else "problemas encontrados"}"
        } else {
            binding.tvResultCount.isVisible = false
        }
    }

    private fun clearAllFilters() {
        // Limpar busca
        binding.etSearch.setText("")
        currentSearchQuery = ""

        // Selecionar "Todos"
        binding.chipAll.isChecked = true
        currentCategory = null

        applyFilters()
    }

    private fun showEmptyState() {
        binding.recyclerView.isVisible = false
        binding.emptyState.isVisible = true

        // Mensagem customizada baseada no contexto
        val message = when {
            currentSearchQuery.isNotEmpty() -> "Nenhum resultado para \"$currentSearchQuery\""
            currentCategory != null -> "Nenhum problema nesta categoria"
            else -> "Nenhum problema reportado ainda"
        }
        binding.tvEmptyMessage.text = message
    }

    private fun showProblems() {
        binding.recyclerView.isVisible = true
        binding.emptyState.isVisible = false
        adapter.submitList(filteredProblems)
    }

    private fun onProblemClick(problem: Problem) {
        val intent = Intent(requireContext(), ProblemDetailActivity::class.java)
        intent.putExtra(ProblemDetailActivity.EXTRA_PROBLEM_ID, problem.id)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}