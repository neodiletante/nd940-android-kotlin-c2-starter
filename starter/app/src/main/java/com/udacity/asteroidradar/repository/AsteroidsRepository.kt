package com.udacity.asteroidradar.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.DatabaseAsteroid
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.network.NasaApi
import com.udacity.asteroidradar.network.NasaService
import com.udacity.asteroidradar.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AsteroidsRepository(private var database: AsteroidsDatabase) {

    val dbAsteroids = database.asteroidDao.getAsteroids()

    val asteroids: LiveData<List<Asteroid>> =
            Transformations.map(database.asteroidDao.getAsteroids()){
                it.asDomainModel()
            }

    suspend fun getImageOfTheDay(){
        withContext(Dispatchers.IO){
            val image = NasaApi.retrofitServiceImageOfTheDay.getImageOfTheDay(API_KEY)
            Log.d("FLUX", image.toString())
            return@withContext image
        }
    }

    suspend fun refreshAsteroids(){
        val calendar = Calendar.getInstance()
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        val startDate = dateFormat.format(currentTime)
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val sevenDaysTime = calendar.time
        val endDate = dateFormat.format(sevenDaysTime)
        Log.d("FLUX",startDate)
        Log.d("FLUX",endDate)

        withContext(Dispatchers.IO){
            val jsonString = NasaApi.retrofitService.getAsteroids(startDate,endDate,API_KEY)
            //Log.d("FLUX",jsonString)
            val asteroids =
                parseAsteroidsJsonResult(JSONObject(jsonString))
            Log.d("FLUX","parsed asteroids "+asteroids.size)
            val asterix =  asteroids.map {
            DatabaseAsteroid(
                    id = it.id,
                    name = it.codename,
                    absolute_magnitude = it.absoluteMagnitude,
                    close_approach_date = it.closeApproachDate,
                    estimated_diameter_max = it.estimatedDiameter,
                    is_potentially_hazardous_asteroid = it.isPotentiallyHazardous,
                    kilometers_per_second = it.relativeVelocity,
                    astronomical = it.distanceFromEarth
            )
            }.toTypedArray()

            Log.d("FLUX","repository mapped asteroids "+asterix.size)
            Log.d("FLUX","repository db asteroids "+database.asteroidDao.getAsteroids().value?.size)


            database.asteroidDao.insertAll(*asteroids.map {
                DatabaseAsteroid(
                        id = it.id,
                        name = it.codename,
                        absolute_magnitude = it.absoluteMagnitude,
                        close_approach_date = it.closeApproachDate,
                        estimated_diameter_max = it.estimatedDiameter,
                        is_potentially_hazardous_asteroid = it.isPotentiallyHazardous,
                        kilometers_per_second = it.relativeVelocity,
                        astronomical = it.distanceFromEarth
                )
            }.toTypedArray())



        }
    }
}