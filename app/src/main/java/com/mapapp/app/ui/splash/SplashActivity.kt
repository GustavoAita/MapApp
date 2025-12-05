package com.mapapp.app.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.mapapp.app.databinding.ActivitySplashBinding
import com.mapapp.app.ui.auth.LoginActivity
import com.mapapp.app.ui.main.MainActivity
import com.mapapp.app.ui.onboarding.OnboardingActivity
import com.mapapp.app.data.firebase.FirebaseAuthManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var authManager: FirebaseAuthManager
    private val SPLASH_DELAY = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        authManager = FirebaseAuthManager()
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserState()
        }, SPLASH_DELAY)
    }

    private fun checkUserState() {
        try {
            val prefs = getSharedPreferences("MapAppPrefs", MODE_PRIVATE)
            val isFirstTime = prefs.getBoolean("isFirstTime", true)

            // Verificar se usuario esta logado no Firebase
            val isLoggedInFirebase = authManager.isUserLoggedIn()

            when {
                isFirstTime -> goToOnboarding()
                isLoggedInFirebase -> {
                    // Atualizar SharedPreferences
                    prefs.edit().putBoolean("isLoggedIn", true).apply()
                    goToMain()
                }
                else -> goToLogin()
            }
        } catch (e: Exception) {
            goToOnboarding()
        }
    }

    private fun goToOnboarding() {
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        // Impede voltar na splash
    }
}