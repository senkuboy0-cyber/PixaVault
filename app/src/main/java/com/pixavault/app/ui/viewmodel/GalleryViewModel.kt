package com.pixavault.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PhotoItem(
    val id: Long,
    val path: String,
    val uri: Uri,
    val dateAdded: Long
)

sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    data class Success(val count: Int) : LoadingState()
    data class Error(val message: String) : LoadingState()
}

class GalleryViewModel : ViewModel() {
    
    private val _photos = MutableStateFlow<List<PhotoItem>>(emptyList())
    val photos: StateFlow<List<PhotoItem>> = _photos.asStateFlow()
    
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    
    private val _selectedPhotos = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPhotos: StateFlow<Set<Long>> = _selectedPhotos.asStateFlow()
    
    fun loadPhotos(context: Context) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            
            try {
                val photoList = withContext(Dispatchers.IO) {
                    queryPhotos(context)
                }
                _photos.value = photoList
                _loadingState.value = LoadingState.Success(photoList.size)
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    private fun queryPhotos(context: Context): List<PhotoItem> {
        val photoList = mutableListOf<PhotoItem>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED
        )
        
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(dataColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val uri = android.content.ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                photoList.add(
                    PhotoItem(
                        id = id,
                        path = path,
                        uri = uri,
                        dateAdded = dateAdded
                    )
                )
            }
        }
        
        return photoList
    }
    
    fun toggleSelection(photoId: Long) {
        val currentSelection = _selectedPhotos.value.toMutableSet()
        if (currentSelection.contains(photoId)) {
            currentSelection.remove(photoId)
        } else {
            currentSelection.add(photoId)
        }
        _selectedPhotos.value = currentSelection
    }
    
    fun clearSelection() {
        _selectedPhotos.value = emptySet()
    }
    
    fun isSelected(photoId: Long): Boolean {
        return _selectedPhotos.value.contains(photoId)
    }
}
