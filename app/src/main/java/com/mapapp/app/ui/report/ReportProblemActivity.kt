package com.mapapp.app.ui.report

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mapapp.app.databinding.ActivityReportProblemBinding

class ReportProblemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportProblemBinding
    private var photoUri: String? = null

    private val categories = arrayOf(
        "Selecione uma categoria",
        "Buraco na via",
        "Iluminação pública",
        "Lixo/Limpeza",
        "Vandalismo",
        "Outros"
    )

    // Launcher para pedir permissão de câmera
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

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Configurar Spinner de categorias
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
        // apenas simulação por enquanto
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

        // Simular envio
        reportProblem(title, description, categories[categoryPosition])
    }

    private fun reportProblem(title: String, description: String, category: String) {
        // TODO: implementar o envio real para um banco/API

        // Simular delay de envio
        binding.btnReport.isEnabled = false
        binding.btnReport.text = "ENVIANDO..."

        binding.root.postDelayed({
            Toast.makeText(
                this,
                "Problema reportado com sucesso!\n\n$title\n$category",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }, 1500)
    }
}