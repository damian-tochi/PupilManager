package com.bridge.androidtechnicaltest.ui

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.bridge.androidtechnicaltest.BaseFragment
import com.bridge.androidtechnicaltest.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay


class MapsFragment : BaseFragment() {

    private lateinit var mapView: MapView
    private var selectedPoint: GeoPoint? = null

    private val sharedPrefs by lazy {
        requireContext().getSharedPreferences("location_cache", Context.MODE_PRIVATE)
    }

    override fun setLayoutView(): Int {
        return R.layout.fragment_maps
    }

    override fun initialize(view: View, savedInstanceState: Bundle?) {
        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        mapView = view.findViewById(R.id.mapView)
        val confirmButton = view.findViewById<Button>(R.id.confirmButton)
        val backButton = view.findViewById<ImageView>(R.id.back_button)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(15.0)

        val cachedLat = sharedPrefs.getString("lat", null)?.toDoubleOrNull()
        val cachedLng = sharedPrefs.getString("lng", null)?.toDoubleOrNull()
        val defaultPoint = if (cachedLat != null && cachedLng != null) {
            GeoPoint(cachedLat, cachedLng)
        } else {
            GeoPoint(6.5244, 3.3792)
        }

        selectedPoint = defaultPoint
        mapController.setCenter(defaultPoint)
        drawMarker(defaultPoint)

        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val tappedPoint = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                selectedPoint = tappedPoint
                drawMarker(tappedPoint)
                return true
            }
        })

        confirmButton.setOnClickListener {
            selectedPoint?.let { point ->
                sharedPrefs.edit()
                    .putString("lat", point.latitude.toString())
                    .putString("lng", point.longitude.toString())
                    .apply()

                try{
                    sharedViewModel.updateLatLng(point.latitude, point.longitude)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                showToast("Location Saved: ${point.latitude}, ${point.longitude}")
                backToPrevious()
            } ?:
            showToast("Tap to select a location")
        }

        backButton.setOnClickListener {
            backToPrevious()
        }

    }

    private fun drawMarker(geoPoint: GeoPoint) {
        mapView.overlays.removeAll { it is Marker }
        val marker = Marker(mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

}