package com.example.signlanguageapp.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signlanguageapp.repository.Repository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel(private val repo: Repository): ViewModel() {

    val result = repo.getResult()

    fun addImage(reference: StorageReference, uri: Uri) = viewModelScope.launch(Dispatchers.IO){
        repo.addImage(reference, uri)
    }
}