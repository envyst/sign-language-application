package com.example.signlanguageapp.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.signlanguageapp.repository.Repository
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val repo: Repository): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)){
            return MainViewModel(repo) as T
        }
        throw IllegalArgumentException("View Model Unknown")
    }

    companion object{
        @Volatile
        private var INSTANCE: ViewModelFactory? = null
        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: ViewModelFactory(injectRepo(context))
            }
        }

        private fun injectRepo(context: Context): Repository{
            val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
            return Repository(db)
        }
    }

}