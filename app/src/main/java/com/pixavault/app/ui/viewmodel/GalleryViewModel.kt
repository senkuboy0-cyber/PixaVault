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
    val dateAdded: Long,
    val displayName: String = "",
    val size: Long = 0L,
    val width: Int = 0,
    val height: Int = 0
)

sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    data class Success(val count: Int) : LoadingState()
    data class Error(val message: String) : LoadingState()
}

enum class SortOption {
    DATE_DESC, DATE_ASC, NAME_ASC, NAME_DESC, SIZE_DESC, SIZE_ASC
}

enum class ViewMode {
    GRID, LIST
}

data class FilterOptions(
    val favoritesOnly: Boolean = false,
    val minSize: Long = 0,
    val dateRange: Pair<Long, Long>? = null
)

class GalleryViewModel : ViewModel() {
    
    private val _photos = MutableStateFlow<List<PhotoItem>>(emptyList())
    val photos: StateFlow<List<PhotoItem>> = _photos.asStateFlow()
    
    private val _filteredPhotos = MutableStateFlow<List<PhotoItem>>(emptyList())
    val filteredPhotos: StateFlow<List<PhotoItem>> = _filteredPhotos.asStateFlow()
    
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    
    private val _selectedPhotos = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPhotos: StateFlow<Set<Long>> = _selectedPhotos.asStateFlow()
    
    private val _favorites = MutableStateFlow<Set<Long>>(emptySet())
    val favorites: StateFlow<Set<Long>> = _favorites.asStateFlow()
    
    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    
    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()
    
    private val _filterOptions = MutableStateFlow(FilterOptions())
    val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private var lastLoadedPhotoIndex = 0
    private val batchSize = 50
    
    init {
        // Apply filters and sorting whenever relevant data changes
        viewModelScope.launch {
            photos.collect {
                applyFiltersAndSort()
            }
        }
        
        viewModelScope.launch {
            favorites.collect {
                applyFiltersAndSort()
            }
        }
        
        viewModelScope.launch {
            sortOption.collect {
                applyFiltersAndSort()
            }
        }
        
        viewModelScope.launch {
            filterOptions.collect {
                applyFiltersAndSort()
            }
        }
        
        viewModelScope.launch {
            searchQuery.collect {
                applyFiltersAndSort()
            }
        }
    }
    
    fun loadPhotos(context: Context, loadMore: Boolean = false) {
        viewModelScope.launch {
            if (loadMore) {
                _isLoadingMore.value = true
            } else {
                _loadingState.value = LoadingState.Loading
                lastLoadedPhotoIndex = 0
            }
            
            try {
                val photoList = withContext(Dispatchers.IO) {
                    queryPhotos(context, loadMore)
                }
                
                if (loadMore) {
                    _photos.value = _photos.value + photoList
                    _isLoadingMore.value = false
                } else {
                    _photos.value = photoList
                    _loadingState.value = LoadingState.Success(photoList.size)
                }
                
                lastLoadedPhotoIndex += photoList.size
            } catch (e: Exception) {
                if (loadMore) {
                    _isLoadingMore.value = false
                } else {
                    _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
    
    private fun queryPhotos(context: Context, loadMore: Boolean = false): List<PhotoItem> {
        val photoList = mutableListOf<PhotoItem>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )
        
        val sortOrder = when (_sortOption.value) {
            SortOption.DATE_DESC -> "${MediaStore.Images.Media.DATE_ADDED} DESC"
            SortOption.DATE_ASC -> "${MediaStore.Images.Media.DATE_ADDED} ASC"
            SortOption.NAME_ASC -> "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
            SortOption.NAME_DESC -> "${MediaStore.Images.Media.DISPLAY_NAME} DESC"
            SortOption.SIZE_DESC -> "${MediaStore.Images.Media.SIZE} DESC"
            SortOption.SIZE_ASC -> "${MediaStore.Images.Media.SIZE} ASC"
        }
        
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
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            
            val startIndex = if (loadMore) lastLoadedPhotoIndex else 0
            var currentIndex = 0
            var itemsAdded = 0
            
            // Move cursor to start position
            if (startIndex > 0 && cursor.moveToPosition(startIndex)) {
                currentIndex = startIndex
            }
            
            while (cursor.moveToNext() && itemsAdded < batchSize) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(dataColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val displayName = cursor.getString(displayNameColumn) ?: ""
                val size = cursor.getLong(sizeColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val uri = android.content.ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                // Filter out very small or corrupted images
                if (size > 1024) { // At least 1KB
                    photoList.add(
                        PhotoItem(
                            id = id,
                            path = path,
                            uri = uri,
                            dateAdded = dateAdded,
                            displayName = displayName,
                            size = size,
                            width = width,
                            height = height
                        )
                    )
                    itemsAdded++
                }
                
                currentIndex++
            }
        }
        
        return photoList
    }
    
    private fun applyFiltersAndSort() {
        var filtered = _photos.value.toList()
        
        // Apply filters
        val filters = _filterOptions.value
        if (filters.favoritesOnly) {
            filtered = filtered.filter { _favorites.value.contains(it.id) }
        }
        
        if (filters.minSize > 0) {
            filtered = filtered.filter { it.size >= filters.minSize }
        }
        
        filters.dateRange?.let { (start, end) ->
            filtered = filtered.filter { it.dateAdded in start..end }
        }
        
        // Apply search
        val query = _searchQuery.value.lowercase().trim()
        if (query.isNotEmpty()) {
            filtered = filtered.filter { 
                it.displayName.lowercase().contains(query) 
            }
        }
        
        // Apply sorting
        filtered = when (_sortOption.value) {
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.dateAdded }
            SortOption.DATE_ASC -> filtered.sortedBy { it.dateAdded }
            SortOption.NAME_ASC -> filtered.sortedBy { it.displayName.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.displayName.lowercase() }
            SortOption.SIZE_DESC -> filtered.sortedByDescending { it.size }
            SortOption.SIZE_ASC -> filtered.sortedBy { it.size }
        }
        
        _filteredPhotos.value = filtered
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
    
    fun selectAll() {
        _selectedPhotos.value = _filteredPhotos.value.map { it.id }.toSet()
    }
    
    fun clearSelection() {
        _selectedPhotos.value = emptySet()
    }
    
    fun isSelected(photoId: Long): Boolean {
        return _selectedPhotos.value.contains(photoId)
    }
    
    fun toggleFavorite(photoId: Long) {
        val currentFavorites = _favorites.value.toMutableSet()
        if (currentFavorites.contains(photoId)) {
            currentFavorites.remove(photoId)
        } else {
            currentFavorites.add(photoId)
        }
        _favorites.value = currentFavorites
    }
    
    fun isFavorite(photoId: Long): Boolean {
        return _favorites.value.contains(photoId)
    }
    
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }
    
    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }
    
    fun updateFilterOptions(filters: FilterOptions) {
        _filterOptions.value = filters
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun refreshPhotos(context: Context) {
        loadPhotos(context, loadMore = false)
    }
}