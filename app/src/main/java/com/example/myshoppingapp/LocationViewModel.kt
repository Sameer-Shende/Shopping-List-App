package com.example.myshoppingapp

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LocationViewModel: ViewModel() {
    private val _location = mutableStateOf<LocationData?>(null)
    val location: State<LocationData?> = _location

    fun updateLocation (newLocation: LocationData){
        _location.value = newLocation
    }

    private val _address = mutableStateOf(listOf<GeocodingResult>())
    val address: State<List<GeocodingResult>> = _address

    fun fetchAddress(latlng: String){
        try{
           viewModelScope.launch {
                val result = RetrofitClient.create().getAddressFromCoordinates(
                    latlng,
                    "AIzaSyAnF28QUpnt7W6dqIH0OGPjGQDNF0e9_aY"
                )

               _address.value = result.results
           }
        }catch (e: Exception){
            Log.d("res1", "${e.cause} ${e.message}")
        }
    }
}