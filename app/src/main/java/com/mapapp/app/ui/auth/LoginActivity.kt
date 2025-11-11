package com.mapapp.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapapp.app.databinding.ActivityLoginBinding
import com.mapapp.app.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        checkIfAlreadyLoggedIn()
    }

    private fun checkIfAlreadyLoggedIn() {
        val prefs = getSharedPreferences("MapAppPrefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            goToMain()
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgot.setOnClickListener {
            Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Limpar erros anteriores
        binding.emailLayout.error = null
        binding.passwordLayout.error = null

        when {
            email.isEmpty() -> {
                binding.emailLayout.error = "Digite o email"
                binding.etEmail.requestFocus()
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailLayout.error = "Email inválido"
                binding.etEmail.requestFocus()
                return
            }
            password.isEmpty() -> {
                binding.passwordLayout.error = "Digite a senha"
                binding.etPassword.requestFocus()
                return
            }
            password.length < 6 -> {
                binding.passwordLayout.error = "Senha deve ter no mínimo 6 caracteres"
                binding.etPassword.requestFocus()
                return
            }
        }

        // Login bem-sucedido
        saveLoginState(email)
        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
        goToMain()
    }

    private fun saveLoginState(email: String) {
        val prefs = getSharedPreferences("MapAppPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("userEmail", email)
            apply()
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        // Impede voltar para onboarding/splash
        finishAffinity()
    }
}