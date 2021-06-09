package com.example.signlanguageapp.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

class Repository(private val db: FirebaseFirestore) {
    fun getResult(): CollectionReference{
        return db.collection("sign-language")
    }

    suspend fun addImage(reference: StorageReference, uri: Uri){
        try {
            reference.putFile(uri).await()
        } catch (e: Exception){
            e.message?.let { Log.d("Add Image Failed", it) }
        }
    }
}