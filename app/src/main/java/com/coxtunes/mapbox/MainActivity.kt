package com.coxtunes.mapbox

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Address
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

    private var mapView: MapView? = null
    private var permissionsManager: PermissionsManager? = null
    private var mapboxMap: MapboxMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

        // Location Callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLocation = locationResult.locations[0]
                val addressList: List<Address?> = GpsUtils.getAddressFromCurrentLocation(
                    application,
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
                )
                if (!addressList.isEmpty()) {

                    Toast.makeText(
                        this@MainActivity,
                        addressList[0]!!.getAddressLine(0),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // If location changed
        startLocationUpdates()

        // Marker location picker
        button.setOnClickListener {
            val intent = PlacePicker.IntentBuilder()
                .accessToken(Mapbox.getAccessToken()!!)
                .placeOptions(
                    PlacePickerOptions.builder()
                        .statingCameraPosition(
                            CameraPosition.Builder()
                                .target(LatLng(40.7544, -73.9862))
                                .zoom(16.0)
                                .build()
                        )
                        .build()
                )
                .build(this)
            startActivityForResult(intent, 100)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {

            // Retrieve the information from the selected location's CarmenFeature
            val carmenFeature: CarmenFeature? = PlacePicker.getPlace(data)
            Toast.makeText(this, carmenFeature!!.geometry()!!.toJson(), Toast.LENGTH_LONG).show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponent: LocationComponent = mapboxMap!!.getLocationComponent()
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(
                    this,
                    loadedMapStyle
                ).build()
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 2000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.Builder().fromUri("mapbox://styles/mapbox/streets-v11")) { style: Style? ->
            enableLocationComponent(style!!)

            val symbolManager = SymbolManager(mapView!!, mapboxMap, style)
            symbolManager.iconAllowOverlap = true
            style.addImage("myMarker", BitmapFactory.decodeResource(resources, R.drawable.location))
            symbolManager.create(
                SymbolOptions()
                    .withLatLng(LatLng(21.476682, 91.991548))
                    .withIconImage("myMarker")
                    .withTextField("jelelelel")
            )
        }
//        val latLng = LatLng(21.476682,91.991548)
//        mapboxMap.addMarker(MarkerOptions().position(latLng).title("MY HOME"))
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            var gps_enabled = false
            try
            {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            if (!gps_enabled)
            {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

            mapboxMap!!.getStyle { style -> enableLocationComponent(style) }

        } else {
            Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

}
