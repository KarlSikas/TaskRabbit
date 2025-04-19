package com.example.taskrabbit.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskrabbit.data.AppDatabase
import com.example.taskrabbit.data.BackgroundImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BackgroundViewModel(application: Application) : AndroidViewModel(application) {

    private val backgroundDao = AppDatabase.getDatabase(application).backgroundDao()

    // MutableStateFlow for the lists of backgrounds
    private val _freeBackgrounds = MutableStateFlow<List<BackgroundImage>>(emptyList())
    val freeBackgrounds: StateFlow<List<BackgroundImage>> = _freeBackgrounds.asStateFlow()

    private val _premiumBackgrounds = MutableStateFlow<List<BackgroundImage>>(emptyList())
    val premiumBackgrounds: StateFlow<List<BackgroundImage>> = _premiumBackgrounds.asStateFlow()

    private val _userBackgrounds = MutableStateFlow<List<BackgroundImage>>(emptyList())
    val userBackgrounds: StateFlow<List<BackgroundImage>> = _userBackgrounds.asStateFlow()

    private val _selectedBackground = MutableStateFlow<BackgroundImage?>(null)
    val selectedBackground: StateFlow<BackgroundImage?> = _selectedBackground.asStateFlow()

    init {
        loadBackgrounds()
    }

    // Simplified method to load backgrounds
    private fun loadBackgrounds() {
        viewModelScope.launch {
            backgroundDao.getAssetBackgrounds().collect { backgrounds ->
                _freeBackgrounds.value = backgrounds
            }
            backgroundDao.getPremiumBackgrounds().collect { backgrounds ->
                _premiumBackgrounds.value = backgrounds
            }
            backgroundDao.getUserBackgrounds().collect { backgrounds ->
                _userBackgrounds.value = backgrounds
            }
        }
    }

    // Select background (sets the selected background state)
    fun selectBackground(background: BackgroundImage) {
        _selectedBackground.value = background
    }

    /**
     * Adds a new background from a given URI and returns the created BackgroundImage.
     */
    fun addBackgroundFromUri(uri: Uri): BackgroundImage {
        val newBackground = BackgroundImage(
            name = "User Image",
            uri = uri.toString(),
            isAsset = false,
            isPremium = false
        )

        // Insert new background in the database and refresh the user backgrounds list
        viewModelScope.launch(Dispatchers.IO) {
            backgroundDao.insertBackground(newBackground)
            loadUserBackgrounds()  // Refresh only the user backgrounds
        }

        return newBackground
    }

    // Load user backgrounds separately after adding a new one
    private fun loadUserBackgrounds() {
        viewModelScope.launch {
            backgroundDao.getUserBackgrounds().collect { backgrounds ->
                _userBackgrounds.value = backgrounds
            }
        }
    }

    // Factory to create an instance of BackgroundViewModel
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BackgroundViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BackgroundViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
