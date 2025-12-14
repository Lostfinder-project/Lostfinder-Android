package com.example.lostfinder.ui.map

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.lostfinder.R
import com.example.lostfinder.databinding.ActivityMapSelectBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapSelectActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapSelectBinding
    private lateinit var map: GoogleMap
    private var currentLatLng: LatLng? = null

    private val locationPermissionCode = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment

        mapFragment.getMapAsync(this)

        binding.btnSelect.setOnClickListener {
            currentLatLng?.let {
                intent.putExtra("lat", it.latitude)
                intent.putExtra("lng", it.longitude)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // 권한 체크
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
            return
        }

        map.isMyLocationEnabled = true

        // 현재 위치 가져오기
        val fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        fusedLocation.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val myPos = LatLng(location.latitude, location.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 16f))
            }
        }

        // 지도 클릭 시 마커
        map.setOnMapClickListener { latLng ->
            map.clear()
            currentLatLng = latLng
            map.addMarker(MarkerOptions().position(latLng).title("선택한 위치"))
        }
    }
}
