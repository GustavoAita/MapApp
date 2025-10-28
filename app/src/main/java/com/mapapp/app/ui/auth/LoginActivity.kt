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
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateLogin(email, password)) {
                saveLoginState(email)
                goToMain()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgot.setOnClickListener {
            Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateLogin(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
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
        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}