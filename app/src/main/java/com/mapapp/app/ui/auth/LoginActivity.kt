package com.mapapp.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapapp.app.databinding.ActivityLoginBinding
import com.mapapp.app.ui.main.MainActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.mapapp.app.data.firebase.FirebaseAuthManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        // Inicializar Firebase Auth
        authManager = FirebaseAuthManager()
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

        // Limpar erros
        binding.emailLayout.error = null
        binding.passwordLayout.error = null

        when {
            email.isEmpty() -> {
                binding.emailLayout.error = "Digite o email"
                binding.etEmail.requestFocus()
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailLayout.error = "Email invalido"
                binding.etEmail.requestFocus()
                return
            }
            password.isEmpty() -> {
                binding.passwordLayout.error = "Digite a senha"
                binding.etPassword.requestFocus()
                return
            }
            password.length < 6 -> {
                binding.passwordLayout.error = "Senha deve ter no minimo 6 caracteres"
                binding.etPassword.requestFocus()
                return
            }
        }

        // Desabilitar botao
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "ENTRANDO..."

        // Login com Firebase
        lifecycleScope.launch {
            val result = authManager.loginUser(email, password)

            result.onSuccess { user ->
                saveLoginState(user.email ?: email, user.displayName ?: "Usuario")
                Toast.makeText(this@LoginActivity, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                goToMain()
            }.onFailure { exception ->
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "ENTRAR"

                val errorMessage = when {
                    exception.message?.contains("password") == true -> "Senha incorreta"
                    exception.message?.contains("user") == true -> "Usuario nao encontrado"
                    exception.message?.contains("network") == true -> "Erro de conexao"
                    else -> "Erro ao fazer login: ${exception.message}"
                }

                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveLoginState(email: String, name: String) {
        val prefs = getSharedPreferences("MapAppPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("userEmail", email)
            putString("userName", name)
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