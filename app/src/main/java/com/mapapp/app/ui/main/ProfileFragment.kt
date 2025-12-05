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
import com.mapapp.app.data.firebase.FirebaseAuthManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: FirebaseAuthManager

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
        authManager = FirebaseAuthManager()

        loadUserData()
        setupLogoutButton()
    }

    private fun loadUserData() {
        val name = authManager.getUserName() ?: "Usuario"
        val email = authManager.getUserEmail() ?: "email@exemplo.com"

        binding.tvName.text = name
        binding.tvEmail.text = email
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
        // Fazer logout do Firebase
        authManager.logoutUser()

        // Limpar SharedPreferences
        val prefs = requireActivity().getSharedPreferences("MapAppPrefs", 0)
        prefs.edit().clear().apply()

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