package com.example.travelsapplication

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {
    @Insert
    suspend fun insert(driver: Driver)

    @Update
    suspend fun update(driver: Driver)

    @Delete
    suspend fun delete(driver: Driver)

    @Query("SELECT * FROM drivers")
    fun getAllDrivers(): Flow<List<Driver>>

    @Query("SELECT * FROM drivers WHERE phoneNumber = :phone AND pin = :pin LIMIT 1")
    suspend fun getDriverByPhoneAndPin(phone: String, pin: String): Driver?

    @Query("SELECT * FROM drivers WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getDriverByPhone(phone: String): Driver?
}
