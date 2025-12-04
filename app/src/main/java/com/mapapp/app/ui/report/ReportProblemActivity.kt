package com.mapapp.app.ui.report

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mapapp.app.data.ProblemRepository
import com.mapapp.app.data.database.AppDatabase
import com.mapapp.app.data.model.Problem
import com.mapapp.app.databinding.ActivityReportProblemBinding
import kotlinx.coroutines.launch
import java.util.UUID
import com.mapapp.app.utils.LocationHelper
class ReportProblemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportProblemBinding
    private lateinit var repository: ProblemRepository
    private var photoUri: String? = null
    private lateinit var locationHelper: LocationHelper
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private val categories = arrayOf(
        "Selecione uma categoria",
        "Buraco na via",
        "Iluminação pública",
        "Lixo/Limpeza",
        "Vandalismo",
        "Outros"
    )

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportProblemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Repository
        val database = AppDatabase.getDatabase(this)
        repository = ProblemRepository(database.problemDao())

        // Inicializar LocationHelper
        locationHelper = LocationHelper(this)

        // Obter localização atual
        if (locationHelper.hasLocationPermission()) {
            obtainCurrentLocation()
        } else {
            // Solicitar permissão se necessário
            requestLocationPermission()
        }

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.cardPhoto.setOnClickListener {
            checkCameraPermission()
        }

        binding.btnReport.setOnClickListener {
            validateAndReport()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        Toast.makeText(this, "Funcionalidade de câmera em desenvolvimento", Toast.LENGTH_SHORT).show()
        binding.tvPhotoHint.text = "📷\n\nFoto capturada!"
    }

    private fun validateAndReport() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val categoryPosition = binding.spinnerCategory.selectedItemPosition

        when {
            title.isEmpty() -> {
                Toast.makeText(this, "Digite um título", Toast.LENGTH_SHORT).show()
                return
            }
            description.isEmpty() -> {
                Toast.makeText(this, "Digite uma descrição", Toast.LENGTH_SHORT).show()
                return
            }
            categoryPosition == 0 -> {
                Toast.makeText(this, "Selecione uma categoria", Toast.LENGTH_SHORT).show()
                return
            }
        }

        reportProblem(title, description, categories[categoryPosition])
    }

    private fun reportProblem(title: String, description: String, category: String) {
        // Desabilitar botão
        binding.btnReport.isEnabled = false
        binding.btnReport.text = "SALVANDO..."

        // Obter dados do usuário
        val prefs = getSharedPreferences("MapAppPrefs", MODE_PRIVATE)
        val userName = prefs.getString("userName", "Usuário Anônimo") ?: "Usuário"
        val userEmail = prefs.getString("userEmail", "usuario@email.com") ?: "email"

        // Criar objeto Problem
        val problem = Problem(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            category = category,
            userName = userName,
            userEmail = userEmail,
            photoUrl = photoUri,
            latitude = currentLatitude,
            longitude = currentLongitude,
            status = "active",
            votesCount = 0,
            createdAt = System.currentTimeMillis()
        )

        // Salvar no banco de dados
        lifecycleScope.launch {
            try {
                repository.insertProblem(problem)

                runOnUiThread {
                    Toast.makeText(
                        this@ReportProblemActivity,
                        "✅ Problema reportado com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@ReportProblemActivity,
                        "❌ Erro ao salvar: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnReport.isEnabled = true
                    binding.btnReport.text = "REPORTAR PROBLEMA"
                }
            }
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                obtainCurrentLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                obtainCurrentLocation()
            }
            else -> {
                binding.tvLocation.text = "📍 Localização não disponível (permissão negada)"
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun obtainCurrentLocation() {
        binding.tvLocation.text = "📍 Obtendo localização..."

        locationHelper.getLastKnownLocation { location ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude

                runOnUiThread {
                    binding.tvLocation.text = "📍 Localização capturada: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}"
                }
            } else {
                // Tentar obter localização atual
                lifecycleScope.launch {
                    val currentLocation = locationHelper.getCurrentLocation()
                    if (currentLocation != null) {
                        currentLatitude = currentLocation.latitude
                        currentLongitude = currentLocation.longitude

                        runOnUiThread {
                            binding.tvLocation.text = "📍 Localização capturada: ${String.format("%.6f", currentLatitude)}, ${String.format("%.6f", currentLongitude)}"
                        }
                    } else {
                        runOnUiThread {
                            binding.tvLocation.text = "📍 Localização não disponível. Ative o GPS."
                        }
                    }
                }
            }
        }
    }
}