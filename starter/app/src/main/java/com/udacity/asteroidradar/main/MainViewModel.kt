package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.network.ImageOfTheDay
import com.udacity.asteroidradar.network.NasaApi
import com.udacity.asteroidradar.network.NasaApi.retrofitService
import com.udacity.asteroidradar.network.NasaService
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.launch

enum class NasaApiStatus { LOADING, ERROR, DONE }

class MainViewModel (application:Application): ViewModel() {
    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidsRepository(database)

    private val _status = MutableLiveData<NasaApiStatus>()

    /**
     * init{} is called immediately when this ViewModel is created.
     */
    init {
        viewModelScope.launch {
            Log.d("FLUX","viewModel asteroids "+asteroidsRepository.asteroids.value?.size)
            Log.d("FLUX","viewModel dbAsteroids  "+asteroidsRepository.dbAsteroids.value?.size)
            asteroidsRepository.refreshAsteroids()
        }

        getImageOfTheDay()

        Log.d("FLUX","init ViewModel")
    }

    val asteroids = asteroidsRepository.asteroids

    val dbAsteroids = asteroidsRepository.dbAsteroids

    private val _imageOfTheDay = MutableLiveData<ImageOfTheDay>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val imageOfTheDay: LiveData<ImageOfTheDay>
        get() = _imageOfTheDay

    private fun getImageOfTheDay() {
        viewModelScope.launch {
            _status.value = NasaApiStatus.LOADING
            try {
                val image = NasaApi.retrofitServiceImageOfTheDay.getImageOfTheDay(API_KEY)
                Log.d("FLUX",image.title)
                Log.d("FLUX",image.mediaType)
                Log.d("FLUX",image.url)

                _imageOfTheDay.value = image
                _status.value = NasaApiStatus.DONE
            } catch (e: Exception) {
                Log.d("FLUX",e.message.toString())
                _status.value = NasaApiStatus.ERROR
            }
        }
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}