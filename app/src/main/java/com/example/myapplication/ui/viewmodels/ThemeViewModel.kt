package com.example.myapplication.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : AndroidViewModel(context as Application) {
    
    private val sharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    private val _isDarkMode = MutableStateFlow(sharedPreferences.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode
    
    init {
        android.util.Log.d("ThemeViewModel", "Initialisation avec mode sombre: ${_isDarkMode.value}")
    }
    
    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        android.util.Log.d("ThemeViewModel", "Changement de thème: $newValue")
        
        _isDarkMode.value = newValue
        
        sharedPreferences.edit().putBoolean("is_dark_mode", newValue).commit()
        
        android.util.Log.d("ThemeViewModel", "Nouvelle valeur sauvegardée: ${sharedPreferences.getBoolean("is_dark_mode", false)}")
    }
} 