package com.mapapp.app.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapapp.app.databinding.ActivityRegisterBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.mapapp.app.data.firebase.FirebaseAuthManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        // Inicializar Firebase Auth
        authManager = FirebaseAuthManager()
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm = binding.etConfirm.text.toString().trim()

        // Limpar erros
        binding.nameLayout.error = null
        binding.emailLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmLayout.error = null

        when {
            name.isEmpty() -> {
                binding.nameLayout.error = "Digite seu nome"
                binding.etName.requestFocus()
                return
            }
            name.length < 3 -> {
                binding.nameLayout.error = "Nome deve ter no minimo 3 caracteres"
                binding.etName.requestFocus()
                return
            }
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
            confirm.isEmpty() -> {
                binding.confirmLayout.error = "Confirme a senha"
                binding.etConfirm.requestFocus()
                return
            }
            password != confirm -> {
                binding.confirmLayout.error = "As senhas nao coincidem"
                binding.etConfirm.requestFocus()
                return
            }
        }

        // Desabilitar botao
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "CADASTRANDO..."

        // Cadastrar no Firebase
        lifecycleScope.launch {
            val result = authManager.registerUser(name, email, password)

            result.onSuccess { user ->
                saveUser(user.displayName ?: name, user.email ?: email)
                Toast.makeText(this@RegisterActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { exception ->
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "CADASTRAR"

                val errorMessage = when {
                    exception.message?.contains("already in use") == true -> "Email ja cadastrado"
                    exception.message?.contains("weak-password") == true -> "Senha muito fraca"
                    exception.message?.contains("network") == true -> "Erro de conexao"
                    else -> "Erro ao cadastrar: ${exception.message}"
                }

                Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveUser(name: String, email: String) {
        val prefs = getSharedPreferences("MapAppPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("userName", name)
            putString("userEmail", email)
            apply()
        }
    }
}