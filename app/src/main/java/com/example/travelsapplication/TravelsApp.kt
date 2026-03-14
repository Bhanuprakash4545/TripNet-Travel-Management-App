package com.example.travelsapplication

import android.app.Application
import com.google.firebase.FirebaseApp

class TravelsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // App Check is currently disabled to prevent "Missing valid app identifier" error.
        // Once your SHA-256 is correctly propagated in Firebase and you want to re-enable it:
        // 1. Uncomment the following lines.
        // 2. Add 'com.google.firebase:firebase-appcheck-playintegrity' to dependencies.
        
        /*
        val firebaseAppCheck = com.google.firebase.appcheck.FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        */
    }
}
