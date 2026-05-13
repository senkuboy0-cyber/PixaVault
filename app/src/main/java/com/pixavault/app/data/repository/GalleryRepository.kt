package com.pixavault.app.data.repository

import android.content.Context
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.pixavault.app.data.local.MediaRepository
import com.pixavault.app.data.local.PreferencesManager
import com.pixavault.app.domain.model.Album
import com.pixavault.app.domain.model.FilterOptions
import com.pixavault.app.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GalleryRepository(context: Context) {
    
    private val mediaRepository = MediaRepository(context)
    private val preferencesManager = PreferencesManager(context)
    
    private val _allMedia = MutableStateFlow<List<MediaItem>>(emptyList())
    val allMedia: StateFlow<List<MediaItem>> = _allMedia.asStateFlow()
    
    private val _favorites = MutableStateFlow<Set<Long>>(emptySet())
    val favorites: StateFlow<Set<Long>> = _favorites.asStateFlow()
    
    private val _hiddenMedia = MutableStateFlow<Set<Long>>(emptySet())
    val hiddenMedia: StateFlow<Set<Long>> = _hiddenMedia.asStateFlow()
    
    private val _deletedMedia = MutableStateFlow<List<DeletedMediaItem>>(emptyList())
    val deletedMedia: StateFlow<List<DeletedMediaItem>> = _deletedMedia.asStateFlow()
    
    data class DeletedMediaItem(
        val mediaItem: MediaItem,
        val deleteTimestamp: Long
    )
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        _favorites.value = preferencesManager.getFavorites()
        _hiddenMedia.value = preferencesManager.getHiddenMedia()
        _deletedMedia.value = preferencesManager.getDeletedMedia().map { 
            DeletedMediaItem(it.first, it.second) 
        }
    }
    
    suspend fun loadMedia(
        limit: Int = 500,
        offset: Int = 0,
        includeVideos: Boolean = true
    ): List<MediaItem> {
        val media = mediaRepository.getAllMedia(limit, offset, includeVideos)
        
        // Apply favorite and hidden status
        val enrichedMedia = media.map { item ->
            item.copy(
                isFavorite = _favorites.value.contains(item.id),
                isHidden = _hiddenMedia.value.contains(item.id)
            )
        }
        
        if (offset == 0) {
            _allMedia.value = enrichedMedia
        } else {
            _allMedia.value = _allMedia.value + enrichedMedia
        }
        
        return enrichedMedia
    }
    
    suspend fun getAlbums(): List<Album> {
        return mediaRepository.getAlbums()
    }
    
    suspend fun searchMedia(query: String): List<MediaItem> {
        return mediaRepository.searchMedia(query).map { item ->
            item.copy(
                isFavorite = _favorites.value.contains(item.id),
                isHidden = _hiddenMedia.value.contains(item.id)
            )
        }
    }
    
    suspend fun getMediaById(id: Long): MediaItem? {
        return mediaRepository.getMediaById(id)?.copy(
            isFavorite = _favorites.value.contains(id),
            isHidden = _hiddenMedia.value.contains(id)
        )
    }
    
    fun toggleFavorite(mediaId: Long) {
        val currentFavorites = _favorites.value.toMutableSet()
        if (currentFavorites.contains(mediaId)) {
            currentFavorites.remove(mediaId)
        } else {
            currentFavorites.add(mediaId)
        }
        _favorites.value = currentFavorites
        preferencesManager.saveFavorites(currentFavorites)
        
        // Update in allMedia
        _allMedia.value = _allMedia.value.map { item ->
            if (item.id == mediaId) {
                item.copy(isFavorite = currentFavorites.contains(mediaId))
            } else {
                item
            }
        }
    }
    
    fun isFavorite(mediaId: Long): Boolean {
        return _favorites.value.contains(mediaId)
    }
    
    fun hideMedia(mediaId: Long) {
        val currentHidden = _hiddenMedia.value.toMutableSet()
        currentHidden.add(mediaId)
        _hiddenMedia.value = currentHidden
        preferencesManager.saveHiddenMedia(currentHidden)
        
        // Update in allMedia
        _allMedia.value = _allMedia.value.map { item ->
            if (item.id == mediaId) {
                item.copy(isHidden = true)
            } else {
                item
            }
        }
    }
    
    fun unhideMedia(mediaId: Long) {
        val currentHidden = _hiddenMedia.value.toMutableSet()
        currentHidden.remove(mediaId)
        _hiddenMedia.value = currentHidden
        preferencesManager.saveHiddenMedia(currentHidden)
        
        // Update in allMedia
        _allMedia.value = _allMedia.value.map { item ->
            if (item.id == mediaId) {
                item.copy(isHidden = false)
            } else {
                item
            }
        }
    }
    
    suspend fun deleteMedia(context: Context, mediaItems: List<MediaItem>) {
        val deletedItems = mutableListOf<DeletedMediaItem>()
        val timestamp = System.currentTimeMillis()
        
        for (item in mediaItems) {
            try {
                val uri = item.uri.toUri()
                context.contentResolver.delete(uri, null, null)
                
                deletedItems.add(DeletedMediaItem(item, timestamp))
                
                // Remove from favorites if present
                val currentFavorites = _favorites.value.toMutableSet()
                if (currentFavorites.contains(item.id)) {
                    currentFavorites.remove(item.id)
                    _favorites.value = currentFavorites
                    preferencesManager.saveFavorites(currentFavorites)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Add to deleted list for recycle bin
        val currentDeleted = _deletedMedia.value.toMutableList()
        currentDeleted.addAll(deletedItems)
        _deletedMedia.value = currentDeleted
        preferencesManager.saveDeletedMedia(currentDeleted.map { it.mediaItem.id to it.deleteTimestamp })
    }
    
    suspend fun restoreFromBin(context: Context, mediaId: Long): Boolean {
        // Note: Actual restoration requires keeping a copy of the file
        // This is a simplified implementation
        val deletedItem = _deletedMedia.value.find { it.mediaItem.id == mediaId }
        return deletedItem != null
    }
    
    fun clearDeletedMedia() {
        _deletedMedia.value = emptyList()
        preferencesManager.saveDeletedMedia(emptyList())
    }
    
    fun filterMedia(options: FilterOptions): List<MediaItem> {
        var filtered = _allMedia.value
        
        // Filter by media type
        filtered = when (options.mediaType) {
            com.pixavault.app.domain.model.MediaType.IMAGES -> 
                filtered.filter { it.isImage }
            com.pixavault.app.domain.model.MediaType.VIDEOS -> 
                filtered.filter { it.isVideo }
            com.pixavault.app.domain.model.MediaType.GIFS -> 
                filtered.filter { it.mimeType.equals("image/gif", ignoreCase = true) }
            else -> filtered
        }
        
        // Filter favorites
        if (options.favoritesOnly) {
            filtered = filtered.filter { it.isFavorite }
        }
        
        // Filter hidden
        filtered = filtered.filter { !it.isHidden }
        
        // Filter by minimum size
        if (options.minSize > 0) {
            filtered = filtered.filter { it.size >= options.minSize }
        }
        
        // Filter by date range
        options.dateRange?.let { (start, end) ->
            filtered = filtered.filter { it.dateAdded in start..end }
        }
        
        // Sort
        filtered = when (options.sortBy) {
            com.pixavault.app.domain.model.SortOption.DATE_DESC -> 
                filtered.sortedByDescending { it.dateAdded }
            com.pixavault.app.domain.model.SortOption.DATE_ASC -> 
                filtered.sortedBy { it.dateAdded }
            com.pixavault.app.domain.model.SortOption.NAME_ASC -> 
                filtered.sortedBy { it.displayName.lowercase() }
            com.pixavault.app.domain.model.SortOption.NAME_DESC -> 
                filtered.sortedByDescending { it.displayName.lowercase() }
            com.pixavault.app.domain.model.SortOption.SIZE_DESC -> 
                filtered.sortedByDescending { it.size }
            com.pixavault.app.domain.model.SortOption.SIZE_ASC -> 
                filtered.sortedBy { it.size }
        }
        
        return filtered
    }
    
    fun getFavoritesList(): List<MediaItem> {
        return _allMedia.value.filter { it.isFavorite }
    }
    
    fun getHiddenList(): List<MediaItem> {
        return _allMedia.value.filter { it.isHidden }
    }
    
    suspend fun moveToAlbum(context: Context, mediaIds: List<Long>, albumName: String) {
        // Implementation for moving media to custom albums
        // This would involve copying/moving files to appropriate directories
    }
    
    suspend fun createAlbum(context: Context, albumName: String): Boolean {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val newAlbumDir = java.io.File(downloadsDir, albumName)
        
        return if (!newAlbumDir.exists()) {
            newAlbumDir.mkdirs()
        } else {
            false
        }
    }
    
    fun getStats(): com.pixavault.app.domain.model.GalleryStats {
        val photos = _allMedia.value.filter { it.isImage && !it.isHidden }
        val videos = _allMedia.value.filter { it.isVideo && !it.isHidden }
        
        return com.pixavault.app.domain.model.GalleryStats(
            totalPhotos = photos.size,
            totalVideos = videos.size,
            totalSize = (photos + videos).sumOf { it.size },
            favoriteCount = _favorites.value.size,
            hiddenCount = _hiddenMedia.value.size,
            deletedCount = _deletedMedia.value.size,
            albumCount = _allMedia.value.groupBy { it.bucketId }.size
        )
    }
}
