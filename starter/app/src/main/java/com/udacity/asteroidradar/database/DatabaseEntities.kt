package com.udacity.asteroidradar.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.udacity.asteroidradar.Asteroid

@Entity
data class DatabaseAsteroid constructor(
    @PrimaryKey
    val id: Long,
    val name: String,
    val absolute_magnitude: Double,
    val close_approach_date: String,
    val estimated_diameter_max: Double,
    val is_potentially_hazardous_asteroid: Boolean,
    val kilometers_per_second: Double,
    val astronomical: Double
)

fun List<DatabaseAsteroid>.asDomainModel(): List<Asteroid>{
    return map {
        Asteroid(
            id = it.id,
            codename = it.name,
            closeApproachDate = it.close_approach_date,
            absoluteMagnitude = it.absolute_magnitude,
            estimatedDiameter = it.estimated_diameter_max,
            isPotentiallyHazardous = it.is_potentially_hazardous_asteroid,
            relativeVelocity = it.kilometers_per_second,
            distanceFromEarth = it.astronomical
        )
    }
}