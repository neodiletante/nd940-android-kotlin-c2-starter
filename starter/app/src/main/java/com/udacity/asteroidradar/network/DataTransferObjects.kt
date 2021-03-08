package com.udacity.asteroidradar.network

import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.database.DatabaseAsteroid

data class NetworkAsteroidContainer(val asteroids: List<Asteroid>)

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