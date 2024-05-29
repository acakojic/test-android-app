package com.acakojic.zadataktcom.data

data class Vehicle(
    val vehicleID: Int,
    val vehicleTypeID: Int,
    val imageURL: String,
    val name: String,
    val location: Location,
    val rating: Double,
    val price: Double,
    val isFavorite: Boolean
)