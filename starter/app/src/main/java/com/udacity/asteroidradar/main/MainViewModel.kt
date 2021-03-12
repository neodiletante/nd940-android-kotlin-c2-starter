package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.network.NasaApi
import com.udacity.asteroidradar.network.NasaApi.retrofitService
import com.udacity.asteroidradar.network.NasaService
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.launch
import timber.log.Timber

enum class NasaApiStatus { LOADING, ERROR, DONE }

class MainViewModel (application:Application): ViewModel() {
    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidsRepository(database)

    private val _status = MutableLiveData<NasaApiStatus>()

    private val _navigateToDetail = MutableLiveData<Asteroid>()
    val navigateToDetail
        get() = _navigateToDetail

    /**
     * init{} is called immediately when this ViewModel is created.
     */
    init {
        viewModelScope.launch {
            Log.d("FLUX","viewModel asteroids "+asteroidsRepository.asteroids.value?.size)
            Log.d("FLUX","viewModel dbAsteroids  "+asteroidsRepository.dbAsteroids.value?.size)
            asteroidsRepository.refreshAsteroids()
           // check("RefreshDataWorker")
        }

        getImageOfTheDay()

        Log.d("FLUX","init ViewModel")
    }

    suspend fun check(workName: String) {
        Timber.d("$workName.check")
        val workManager = WorkManager.getInstance()

        val workInfos = workManager.getWorkInfosForUniqueWork(workName).await()
        if (workInfos.size == 1) {
            // for (workInfo in workInfos) {
            val workInfo = workInfos[0]
            Timber.d("workInfo.state=${workInfo.state}, id=${workInfo.id}")
            if (workInfo.state == WorkInfo.State.BLOCKED || workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
                Timber.d("isAlive")
            } else {
                Timber.d("isDead")

            }
        } else {
            Timber.d("notFound")
        }
    }

    val asteroids = asteroidsRepository.asteroids

    val dbAsteroids = asteroidsRepository.dbAsteroids

    private val _imageOfTheDay = MutableLiveData<PictureOfDay>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val imageOfTheDay: LiveData<PictureOfDay>
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

    fun onAsteroidClicked(asteroid: Asteroid){
        _navigateToDetail.value = asteroid
    }

    fun onDetailNavigated(){
        _navigateToDetail.value = null
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