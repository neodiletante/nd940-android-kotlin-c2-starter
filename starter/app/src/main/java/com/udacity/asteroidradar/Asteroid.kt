package com.udacity.asteroidradar

import android.os.Parcelable
import com.udacity.asteroidradar.database.DatabaseAsteroid
import com.udacity.asteroidradar.network.NetworkAsteroidContainer
import kotlinx.android.parcel.Parcelize
@Parcelize
data class Asteroid(val id: Long, val codename: String, val closeApproachDate: String,
                    val absoluteMagnitude: Double, val estimatedDiameter: Double,
                    val relativeVelocity: Double, val distanceFromEarth: Double,
                    val isPotentiallyHazardous: Boolean) : Parcelable{

    fun NetworkAsteroidContainer.asDatabaseModel(): Array<DatabaseAsteroid>{
        return asteroids.map {
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
    }
}


