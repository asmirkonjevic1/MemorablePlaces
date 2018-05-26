package com.hfad.memorableplaces

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*
import android.location.Geocoder
import java.text.SimpleDateFormat
import android.location.LocationManager
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    }

    fun updateLocation(location : Location, title : String){
        if (location != null) {
            val userLocation = LatLng(location.latitude, location.longitude)
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(userLocation).title(title))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val intent = getIntent()
        val numberPlace = intent.getIntExtra("numberPlace", 0)

        if (numberPlace == -100) {
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    updateLocation(location!!, "Your Location")
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                }

                override fun onProviderEnabled(provider: String?) {
                }

                override fun onProviderDisabled(provider: String?) {
                }
            }


            mMap.setOnMapLongClickListener(this)

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1000f, locationListener)
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                updateLocation(lastKnownLocation, "Your Location")
            }
        }else{
            val placeLocation = Location(LocationManager.GPS_PROVIDER)
            placeLocation.latitude = MainActivity.locations[numberPlace].latitude
            placeLocation.longitude = MainActivity.locations[numberPlace].longitude

            updateLocation(placeLocation, MainActivity.places[numberPlace])
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        val geocoder = Geocoder(applicationContext, Locale.getDefault())

        var address = ""

        try {

            val listAdddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (listAdddresses != null && listAdddresses.size > 0) {
                if (listAdddresses[0].thoroughfare != null) {
                    if (listAdddresses[0].subThoroughfare != null) {
                        address += listAdddresses[0].subThoroughfare + " "
                    }
                    address += listAdddresses[0].thoroughfare
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }


        if (address == "") {
            val sdf = SimpleDateFormat("HH:mm yyyy-MM-dd", Locale.ENGLISH)
            address += sdf.format(Date())
        }

        mMap.addMarker(MarkerOptions().position(latLng).title(address))

        MainActivity.places.add(address)
        MainActivity.locations.add(latLng)

        MainActivity.adapter.notifyDataSetChanged()

        val sharedPreferences : SharedPreferences = this.getSharedPreferences("com.hfad.memorableplaces", Context.MODE_PRIVATE)
        try {

            val latitudes = ArrayList<String>()
            val longitudes = ArrayList<String>()

            for (latLong in MainActivity.locations){
                latitudes.add(latLng.latitude.toString())
                longitudes.add(latLng.longitude.toString())
            }

            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply()
            sharedPreferences.edit().putString("lats", ObjectSerializer.serialize(latitudes)).apply()
            sharedPreferences.edit().putString("lons", ObjectSerializer.serialize(longitudes)).apply()

        }catch (e : Exception){

        }

        Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startListening()
        }
    }

    fun startListening(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1000f, locationListener)
        }
    }
}
