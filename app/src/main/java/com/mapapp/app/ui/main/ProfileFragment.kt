package com.mapapp.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        setupProfile()
        setupLogout()
    }

    private fun setupProfile() {
        val prefs = requireActivity().getSharedPreferences("MapAppPrefs", 0)
        val name = prefs.getString("userName", "Usuário")
        val email = prefs.getString("userEmail", "email@exemplo.com")

        binding.tvName.text = name
        binding.tvEmail.text = email
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        val prefs = requireActivity().getSharedPreferences("MapAppPrefs", 0)
        prefs.edit().clear().apply()

        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}