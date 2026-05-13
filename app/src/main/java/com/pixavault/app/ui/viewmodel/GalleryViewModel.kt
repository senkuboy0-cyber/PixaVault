package com.pixavault.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pixavault.app.data.repository.GalleryRepository
import com.pixavault.app.domain.model.FilterOptions
import com.pixavault.app.domain.model.MediaItem
import com.pixavault.app.domain.model.SortOption
import com.pixavault.app.domain.model.ViewMode
import com.pixavault.app.domain.model.UiState
import com.pixavault.app.domain.model.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = GalleryRepository(application)
    
    private val _uiState = MutableStateFlow<UiState<List<MediaItem>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<MediaItem>>> = _uiState.asStateFlow()
    
    private val _allMedia = MutableStateFlow<List<MediaItem>>(emptyList())
    val allMedia: StateFlow<List<MediaItem>> = _allMedia.asStateFlow()
    
    private val _filteredMedia = MutableStateFlow<List<MediaItem>>(emptyList())
    val filteredMedia: StateFlow<List<MediaItem>> = _filteredMedia.asStateFlow()
    
    private val _selectedMedia = MutableStateFlow<Set<Long>>(emptySet())
    val selectedMedia: StateFlow<Set<Long>> = _selectedMedia.asStateFlow()
    
    private val _filterOptions = MutableStateFlow(FilterOptions())
    val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()
    
    private var currentPage = 0
    private val itemsPerPage = 100
    
    init {
        observeDataChanges()
    }
    
    private fun observeDataChanges() {
        viewModelScope.launch {
            repository.allMedia.collect { media ->
                _allMedia.value = media
                applyFilters()
            }
        }
        
        viewModelScope.launch {
            repository.favorites.collect { 
                applyFilters()
            }
        }
        
        viewModelScope.launch {
            _filterOptions.collect {
                applyFilters()
            }
        }
        
        viewModelScope.launch {
            _searchQuery.collect {
                applyFilters()
            }
        }
    }
    
    fun loadMedia(includeVideos: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                currentPage = 0
                val media = repository.loadMedia(
                    limit = itemsPerPage,
                    offset = 0,
                    includeVideos = includeVideos
                )
                
                _uiState.value = UiState.Success(media)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun loadMoreMedia(includeVideos: Boolean = true) {
        if (_isLoadingMore.value) return
        
        viewModelScope.launch {
            _isLoadingMore.value = true
            
            try {
                currentPage++
                val newMedia = repository.loadMedia(
                    limit = itemsPerPage,
                    offset = currentPage * itemsPerPage,
                    includeVideos = includeVideos
                )
                
                if (newMedia.isEmpty()) {
                    _isLoadingMore.value = false
                    return@launch
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingMore.value = false
            }
        }
    }
    
    fun refreshMedia(includeVideos: Boolean = true) {
        loadMedia(includeVideos)
    }
    
    private fun applyFilters() {
        var filtered = _allMedia.value
        
        // Apply search query
        val query = _searchQuery.value.trim().lowercase()
        if (query.isNotEmpty()) {
            filtered = filtered.filter { 
                it.displayName.lowercase().contains(query) ||
                it.bucketName.lowercase().contains(query)
            }
        }
        
        // Apply media type filter
        filtered = when (_filterOptions.value.mediaType) {
            MediaType.IMAGES -> filtered.filter { it.isImage }
            MediaType.VIDEOS -> filtered.filter { it.isVideo }
            MediaType.GIFS -> filtered.filter { it.mimeType.equals("image/gif", ignoreCase = true) }
            MediaType.ALL -> filtered
        }
        
        // Apply favorites filter
        if (_filterOptions.value.favoritesOnly) {
            filtered = filtered.filter { it.isFavorite }
        }
        
        // Filter out hidden media
        filtered = filtered.filter { !it.isHidden }
        
        // Apply minimum size filter
        if (_filterOptions.value.minSize > 0) {
            filtered = filtered.filter { it.size >= _filterOptions.value.minSize }
        }
        
        // Apply date range filter
        _filterOptions.value.dateRange?.let { (start, end) ->
            filtered = filtered.filter { it.dateAdded in start..end }
        }
        
        // Apply sorting
        filtered = when (_filterOptions.value.sortBy) {
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.dateAdded }
            SortOption.DATE_ASC -> filtered.sortedBy { it.dateAdded }
            SortOption.NAME_ASC -> filtered.sortedBy { it.displayName.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.displayName.lowercase() }
            SortOption.SIZE_DESC -> filtered.sortedByDescending { it.size }
            SortOption.SIZE_ASC -> filtered.sortedBy { it.size }
        }
        
        _filteredMedia.value = filtered
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateFilterOptions(options: FilterOptions) {
        _filterOptions.value = options
    }
    
    fun setMediaType(type: MediaType) {
        _filterOptions.value = _filterOptions.value.copy(mediaType = type)
    }
    
    fun toggleFavoritesOnly() {
        _filterOptions.value = _filterOptions.value.copy(
            favoritesOnly = !_filterOptions.value.favoritesOnly
        )
    }
    
    fun setSortOption(option: SortOption) {
        _filterOptions.value = _filterOptions.value.copy(sortBy = option)
    }
    
    fun setViewMode(mode: ViewMode) {
        _filterOptions.value = _filterOptions.value.copy(viewMode = mode)
    }
    
    // Selection management
    fun toggleSelection(mediaId: Long) {
        val currentSelection = _selectedMedia.value.toMutableSet()
        if (currentSelection.contains(mediaId)) {
            currentSelection.remove(mediaId)
        } else {
            currentSelection.add(mediaId)
        }
        _selectedMedia.value = currentSelection
        _isSelectionMode.value = currentSelection.isNotEmpty()
    }
    
    fun selectAll() {
        _selectedMedia.value = _filteredMedia.value.map { it.id }.toSet()
        _isSelectionMode.value = true
    }
    
    fun clearSelection() {
        _selectedMedia.value = emptySet()
        _isSelectionMode.value = false
    }
    
    fun isSelected(mediaId: Long): Boolean {
        return _selectedMedia.value.contains(mediaId)
    }
    
    fun getSelectedItems(): List<MediaItem> {
        return _filteredMedia.value.filter { it.id in _selectedMedia.value }
    }
    
    // Favorites management
    fun toggleFavorite(mediaId: Long) {
        repository.toggleFavorite(mediaId)
    }
    
    fun isFavorite(mediaId: Long): Boolean {
        return repository.isFavorite(mediaId)
    }
    
    fun getFavorites(): List<MediaItem> {
        return repository.getFavoritesList()
    }
    
    // Hide/Unhide media
    fun hideMedia(mediaId: Long) {
        repository.hideMedia(mediaId)
    }
    
    fun unhideMedia(mediaId: Long) {
        repository.unhideMedia(mediaId)
    }
    
    // Delete operations
    suspend fun deleteMedia(context: Context, mediaItems: List<MediaItem>) {
        repository.deleteMedia(context, mediaItems)
        clearSelection()
    }
    
    suspend fun deleteSelectedMedia(context: Context) {
        val selectedItems = getSelectedItems()
        deleteMedia(context, selectedItems)
    }
    
    // Stats
    fun getStats(): com.pixavault.app.domain.model.GalleryStats {
        return repository.getStats()
    }
    
    // Search
    suspend fun search(query: String): List<MediaItem> {
        return repository.searchMedia(query)
    }
}
