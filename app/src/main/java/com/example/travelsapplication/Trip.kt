package com.example.travelsapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val vehicleId: Int,
    val driverId: Int,
    val date: String,
    val fromLocation: String,
    val toLocation: String,
    val income: Double,
    val fuelCost: Double = 0.0,
    val driverSalary: Double = 0.0,
    val tollCost: Double = 0.0,
    val otherExpense: Double = 0.0,
    val profit: Double
)
