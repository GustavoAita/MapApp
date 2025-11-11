package com.mapapp.app.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapapp.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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

        // Limpar erros anteriores
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
                binding.nameLayout.error = "Nome deve ter no mínimo 3 caracteres"
                binding.etName.requestFocus()
                return
            }
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
            confirm.isEmpty() -> {
                binding.confirmLayout.error = "Confirme a senha"
                binding.etConfirm.requestFocus()
                return
            }
            password != confirm -> {
                binding.confirmLayout.error = "As senhas não coincidem"
                binding.etConfirm.requestFocus()
                return
            }
        }

        // Cadastro bem-sucedido
        saveUser(name, email)
        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
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