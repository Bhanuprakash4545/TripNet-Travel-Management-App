package com.example.travelsapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drivers")
data class Driver(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val age: Int,
    val pin: String = "",
    val deviceId: String? = null // Locked to this specific phone
)
