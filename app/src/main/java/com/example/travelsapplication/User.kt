package com.example.travelsapplication

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["phoneNumber"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phoneNumber: String,
    val travelsName: String,
    val state: String,
    val city: String,
    val profilePhotoUri: String? = null,
    val role: String = "Admin" // "Admin" or "Driver"
)
