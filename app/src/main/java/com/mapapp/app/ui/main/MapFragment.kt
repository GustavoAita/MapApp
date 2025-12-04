package com.mapapp.app.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mapapp.app.R
import com.mapapp.app.data.ProblemRepository
import com.mapapp.app.data.database.AppDatabase
import com.mapapp.app.data.model.Problem
import com.mapapp.app.databinding.FragmentMapBinding
import com.mapapp.app.ui.detail.ProblemDetailActivity
import kotlinx.coroutines.launch
import com.mapapp.app.utils.LocationHelper

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private lateinit var repository: ProblemRepository
    private var problems: List<Problem> = emptyList()
    private var selectedProblem: Problem? = null
    private var currentCategory: String? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                enableMyLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                enableMyLocation()
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    "Permissão de localização negada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar Repository
        val database = AppDatabase.getDatabase(requireContext())
        repository = ProblemRepository(database.problemDao())

        // Inicializar mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        setupListeners()
    }

    private fun setupListeners() {
        binding.fabMyLocation.setOnClickListener {
            moveToMyLocation()
        }

        binding.btnCardViewDetails.setOnClickListener {
            selectedProblem?.let { problem ->
                openProblemDetails(problem)
            }
        }

        // Filtros
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = null
                applyFilter()
            }
        }

        binding.chipPothole.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = "Buraco na via"
                applyFilter()
            }
        }

        binding.chipLighting.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = "Iluminação pública"
                applyFilter()
            }
        }

        binding.chipGarbage.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = "Lixo/Limpeza"
                applyFilter()
            }
        }

        binding.chipVandalism.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = "Vandalismo"
                applyFilter()
            }
        }

        binding.chipOther.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentCategory = "Outros"
                applyFilter()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configurar mapa
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = false
            isMapToolbarEnabled = true
        }

        // Estilo do mapa
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Click em marcador
        map.setOnMarkerClickListener { marker ->
            val problem = marker.tag as? Problem
            if (problem != null) {
                showProblemCard(problem)
                // Centralizar no marcador
                map.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
                true
            } else {
                false
            }
        }

        // Click no mapa (fechar card)
        map.setOnMapClickListener {
            hideProblemCard()
        }

        // Verificar permissões e habilitar localização
        checkLocationPermission()

        // Carregar problemas
        loadProblems()

        // Tentar obter localização atual para centralizar mapa
        if (hasLocationPermission()) {
            val locationHelper = LocationHelper(requireContext())
            locationHelper.getLastKnownLocation { location ->
                if (location != null) {
                    val myLocation = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13f))
                } else {
                    // Fallback para São Paulo
                    val defaultLocation = LatLng(-23.550520, -46.633308)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
                }
            }
        } else {
            // Sem permissão, usar posição padrão
            val defaultLocation = LatLng(-23.550520, -46.633308)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        }
    }

    private fun checkLocationPermission() {
        when {
            hasLocationPermission() -> {
                enableMyLocation()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun enableMyLocation() {
        try {
            googleMap?.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Toast.makeText(
                requireContext(),
                "Erro ao habilitar localização",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun moveToMyLocation() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        // Criar LocationHelper
        val locationHelper = com.mapapp.app.utils.LocationHelper(requireContext())

        // Tentar obter última localização conhecida
        locationHelper.getLastKnownLocation { location ->
            if (location != null) {
                val myLocation = LatLng(location.latitude, location.longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))

                Toast.makeText(
                    requireContext(),
                    "📍 Localização atual obtida",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Se falhar, tentar obter localização atual
                viewLifecycleOwner.lifecycleScope.launch {
                    val currentLocation = locationHelper.getCurrentLocation()
                    if (currentLocation != null) {
                        val myLocation = LatLng(currentLocation.latitude, currentLocation.longitude)
                        activity?.runOnUiThread {
                            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
                        }
                    } else {
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Não foi possível obter localização. Verifique se o GPS está ativo.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun loadProblems() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.allProblems.collect { problemList ->
                problems = problemList
                addMarkersToMap()
            }
        }
    }

    private fun applyFilter() {
        addMarkersToMap()
    }

    private fun addMarkersToMap() {
        val map = googleMap ?: return

        // Limpar marcadores existentes
        map.clear()

        // Aplicar filtro
        val filteredProblems = if (currentCategory != null) {
            problems.filter { it.category == currentCategory }
        } else {
            problems
        }

        var hasValidLocation = false
        var validProblemsCount = 0

        // Adicionar marcadores para cada problema filtrado
        filteredProblems.forEach { problem ->
            if (problem.latitude != 0.0 && problem.longitude != 0.0) {
                hasValidLocation = true
                validProblemsCount++

                val position = LatLng(problem.latitude, problem.longitude)

                val markerColor = when (problem.category) {
                    "Buraco na via" -> BitmapDescriptorFactory.HUE_RED
                    "Iluminação pública" -> BitmapDescriptorFactory.HUE_YELLOW
                    "Lixo/Limpeza" -> BitmapDescriptorFactory.HUE_GREEN
                    "Vandalismo" -> BitmapDescriptorFactory.HUE_BLUE
                    else -> BitmapDescriptorFactory.HUE_CYAN
                }

                val marker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(problem.title)
                        .snippet(problem.category)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                )

                marker?.tag = problem
            }
        }

        // Atualizar contador
        updateProblemCounter(validProblemsCount)

        // Mensagens
        if (!hasValidLocation && problems.isNotEmpty()) {
            if (currentCategory != null) {
                Toast.makeText(
                    requireContext(),
                    "Nenhum problema desta categoria com localizacao GPS",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Nenhum problema com localizacao GPS disponivel",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateProblemCounter(count: Int) {
        if (currentCategory != null && count > 0) {
            binding.tvProblemCount.isVisible = true
            binding.tvProblemCount.text = "$count ${if (count == 1) "problema" else "problemas"} no mapa"
        } else {
            binding.tvProblemCount.isVisible = false
        }
    }

    private fun showProblemCard(problem: Problem) {
        selectedProblem = problem

        binding.cardInfo.isVisible = true
        binding.tvCardEmoji.text = problem.getCategoryEmoji()
        binding.tvCardTitle.text = problem.title
        binding.tvCardCategory.text = problem.category
    }

    private fun hideProblemCard() {
        binding.cardInfo.isVisible = false
        selectedProblem = null
    }

    private fun openProblemDetails(problem: Problem) {
        val intent = Intent(requireContext(), ProblemDetailActivity::class.java)
        intent.putExtra(ProblemDetailActivity.EXTRA_PROBLEM_ID, problem.id)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}