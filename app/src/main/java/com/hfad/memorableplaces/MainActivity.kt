package com.hfad.memorableplaces

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        var places : ArrayList<String> = ArrayList()
        var locations : ArrayList<LatLng> = ArrayList()
        lateinit var adapter : ArrayAdapter<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = this.getSharedPreferences("com.hfad.memorableplaces", Context.MODE_PRIVATE)

        var latitudes = ArrayList<String>()
        var longitudes = ArrayList<String>()

        places.clear()
        latitudes.clear()
        longitudes.clear()
        locations.clear()

        try {
            places = ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(object : ArrayList<String>(){}))) as ArrayList<String>
            latitudes = ObjectSerializer.deserialize(sharedPreferences.getString("lats", ObjectSerializer.serialize(object : ArrayList<String>(){}))) as ArrayList<String>
            longitudes = ObjectSerializer.deserialize(sharedPreferences.getString("lons", ObjectSerializer.serialize(object : ArrayList<String>(){}))) as ArrayList<String>

        }catch (e : Exception){
            e.printStackTrace()
        }

        if (places.size > 0 && latitudes.size > 0 && longitudes.size > 0){
            if (places.size == latitudes.size && places.size == longitudes.size){
                var i = 0
                while (i < places.size){
                    locations.add(LatLng(latitudes.get(i).toDouble(), longitudes.get(i).toDouble()))
                    i++
                }
            }
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, places)
        listview_places.adapter = adapter

        listview_places.onItemClickListener = object : AdapterView.OnItemClickListener{
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                intent = Intent(applicationContext, MapsActivity::class.java)
                intent.putExtra("numberPlace", position)
                startActivity(intent)
            }
        }

    }

    fun addNewPlace(view : View){
        intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("numberPlace", -100)
        startActivity(intent)
    }

}
