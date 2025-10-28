package com.mapapp.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.mapapp.app.R
import com.mapapp.app.databinding.ActivityOnboardingBinding
import com.mapapp.app.ui.auth.LoginActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    private val pages = listOf(
        OnboardingPage("Reporte Problemas", "Tire foto e marque no mapa", R.mipmap.ic_launcher),
        OnboardingPage("Veja em Tempo Real", "Acompanhe problemas da cidade", R.mipmap.ic_launcher),
        OnboardingPage("Faça Parte", "Vote e comente nos problemas", R.mipmap.ic_launcher)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = OnboardingAdapter(pages)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                updateButton(position)
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < pages.size - 1) {
                binding.viewPager.currentItem += 1
            } else {
                goToLogin()
            }
        }

        binding.btnSkip.setOnClickListener {
            goToLogin()
        }
    }

    private fun updateIndicators(position: Int) {
        val active = ContextCompat.getDrawable(this, R.drawable.indicator_active)
        val inactive = ContextCompat.getDrawable(this, R.drawable.indicator_inactive)

        binding.indicator1.background = if (position == 0) active else inactive
        binding.indicator2.background = if (position == 1) active else inactive
        binding.indicator3.background = if (position == 2) active else inactive
    }

    private fun updateButton(position: Int) {
        if (position == pages.size - 1) {
            binding.btnNext.text = getString(R.string.onboarding_start)
            binding.btnSkip.visibility = View.GONE
        } else {
            binding.btnNext.text = getString(R.string.onboarding_next)
            binding.btnSkip.visibility = View.VISIBLE
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}