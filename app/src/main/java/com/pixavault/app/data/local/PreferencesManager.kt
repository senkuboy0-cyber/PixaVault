package com.pixavault.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pixavault.app.domain.model.MediaItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pixavault_prefs")

class PreferencesManager(private val context: Context) {
    
    companion object {
        private val FAVORITES_KEY = longSetPreferencesKey("favorites")
        private val HIDDEN_MEDIA_KEY = longSetPreferencesKey("hidden_media")
        private val DELETED_MEDIA_KEY = stringPreferencesKey("deleted_media")
        private val PIN_KEY = stringPreferencesKey("vault_pin")
        private val USE_BIOMETRIC_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("use_biometric")
        private val THEME_MODE_KEY = androidx.datastore.preferences.core.intPreferencesKey("theme_mode")
        private val GRID_COLUMNS_KEY = androidx.datastore.preferences.core.intPreferencesKey("grid_columns")
        private val VIEW_MODE_KEY = androidx.datastore.preferences.core.intPreferencesKey("view_mode")
    }
    
    suspend fun getFavorites(): Set<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[FAVORITES_KEY] ?: emptySet()
        }.first()
    }
    
    suspend fun saveFavorites(favorites: Set<Long>) {
        context.dataStore.edit { preferences ->
            preferences[FAVORITES_KEY] = favorites
        }
    }
    
    suspend fun getHiddenMedia(): Set<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[HIDDEN_MEDIA_KEY] ?: emptySet()
        }.first()
    }
    
    suspend fun saveHiddenMedia(hidden: Set<Long>) {
        context.dataStore.edit { preferences ->
            preferences[HIDDEN_MEDIA_KEY] = hidden
        }
    }
    
    suspend fun getDeletedMedia(): List<Pair<Long, Long>> {
        return context.dataStore.data.map { preferences ->
            val data = preferences[DELETED_MEDIA_KEY] ?: ""
            if (data.isEmpty()) {
                emptyList()
            } else {
                data.split(",").mapNotNull { pair ->
                    val parts = pair.split(":")
                    if (parts.size == 2) {
                        try {
                            parts[0].toLong() to parts[1].toLong()
                        } catch (e: NumberFormatException) {
                            null
                        }
                    } else {
                        null
                    }
                }
            }
        }.first()
    }
    
    suspend fun saveDeletedMedia(deleted: List<Pair<Long, Long>>) {
        val data = deleted.joinToString(",") { "${it.first}:${it.second}" }
        context.dataStore.edit { preferences ->
            preferences[DELETED_MEDIA_KEY] = data
        }
    }
    
    suspend fun getVaultPin(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PIN_KEY]
        }.first()
    }
    
    suspend fun setVaultPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_KEY] = pin
        }
    }
    
    suspend fun clearVaultPin() {
        context.dataStore.edit { preferences ->
            preferences.remove(PIN_KEY)
        }
    }
    
    suspend fun shouldUseBiometric(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[USE_BIOMETRIC_KEY] ?: false
        }.first()
    }
    
    suspend fun setUseBiometric(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_BIOMETRIC_KEY] = use
        }
    }
    
    suspend fun getThemeMode(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_MODE_KEY] ?: 0 // 0 = System, 1 = Light, 2 = Dark
        }.first()
    }
    
    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }
    
    suspend fun getGridColumns(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[GRID_COLUMNS_KEY] ?: 4
        }.first()
    }
    
    suspend fun setGridColumns(columns: Int) {
        context.dataStore.edit { preferences ->
            preferences[GRID_COLUMNS_KEY] = columns
        }
    }
    
    suspend fun getViewMode(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[VIEW_MODE_KEY] ?: 0 // 0 = Grid, 1 = List, 2 = Timeline
        }.first()
    }
    
    suspend fun setViewMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[VIEW_MODE_KEY] = mode
        }
    }
}
