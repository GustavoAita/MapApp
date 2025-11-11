package com.mapapp.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mapapp.app.databinding.FragmentProfileBinding
import com.mapapp.app.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
        setupLogoutButton()
    }

    private fun loadUserData() {
        val prefs = requireActivity().getSharedPreferences("MapAppPrefs", 0)
        val name = prefs.getString("userName", "Usuário")
        val email = prefs.getString("userEmail", "email@exemplo.com")

        binding.tvName.text = name ?: "Usuário"
        binding.tvEmail.text = email ?: "email@exemplo.com"
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sair")
            .setMessage("Deseja realmente sair da sua conta?")
            .setPositiveButton("Sim") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performLogout() {
        // Limpar dados de login
        val prefs = requireActivity().getSharedPreferences("MapAppPrefs", 0)
        prefs.edit().apply {
            putBoolean("isLoggedIn", false)
            remove("userEmail")
            apply()
        }

        Toast.makeText(requireContext(), "Logout realizado", Toast.LENGTH_SHORT).show()

        // Ir para login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}