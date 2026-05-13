package com.pixavault.app.data.local

import android.content.Context
import android.provider.MediaStore
import androidx.core.net.toUri
import com.pixavault.app.domain.model.Album
import com.pixavault.app.domain.model.AlbumType
import com.pixavault.app.domain.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {
    
    suspend fun getAllMedia(
        limit: Int = 500,
        offset: Int = 0,
        includeVideos: Boolean = true
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<MediaItem>()
        
        // Query images
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.ORIENTATION
        )
        
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val orientationIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)
            
            var count = 0
            while (cursor.moveToNext() && count < limit) {
                if (count >= offset) {
                    val id = cursor.getLong(idIndex)
                    val path = cursor.getString(dataIndex) ?: ""
                    val displayName = cursor.getString(nameIndex) ?: ""
                    val dateAdded = cursor.getLong(dateAddedIndex)
                    val dateModified = cursor.getLong(dateModifiedIndex)
                    val size = cursor.getLong(sizeIndex)
                    val width = cursor.getInt(widthIndex)
                    val height = cursor.getInt(heightIndex)
                    val bucketId = cursor.getLong(bucketIdIndex)
                    val bucketName = cursor.getString(bucketNameIndex) ?: "Unknown"
                    val mimeType = cursor.getString(mimeTypeIndex) ?: "image/jpeg"
                    val orientation = cursor.getInt(orientationIndex)
                    
                    if (size > 0 && !path.isEmpty()) {
                        mediaList.add(
                            MediaItem(
                                id = id,
                                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                                    .appendPath(id.toString()).build().toString(),
                                path = path,
                                mimeType = mimeType,
                                dateAdded = dateAdded,
                                dateModified = dateModified,
                                displayName = displayName,
                                size = size,
                                width = width,
                                height = height,
                                bucketId = bucketId,
                                bucketName = bucketName,
                                orientation = orientation
                            )
                        )
                    }
                }
                count++
            }
        }
        
        // Query videos if needed
        if (includeVideos) {
            val videoProjection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DURATION
            )
            
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val widthIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
                val bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                
                var count = 0
                while (cursor.moveToNext() && mediaList.size < limit) {
                    if (count >= offset) {
                        val id = cursor.getLong(idIndex)
                        val path = cursor.getString(dataIndex) ?: ""
                        val displayName = cursor.getString(nameIndex) ?: ""
                        val dateAdded = cursor.getLong(dateAddedIndex)
                        val dateModified = cursor.getLong(dateModifiedIndex)
                        val size = cursor.getLong(sizeIndex)
                        val width = cursor.getInt(widthIndex)
                        val height = cursor.getInt(heightIndex)
                        val bucketId = cursor.getLong(bucketIdIndex)
                        val bucketName = cursor.getString(bucketNameIndex) ?: "Unknown"
                        val mimeType = cursor.getString(mimeTypeIndex) ?: "video/mp4"
                        val duration = cursor.getLong(durationIndex)
                        
                        if (size > 0 && !path.isEmpty()) {
                            mediaList.add(
                                MediaItem(
                                    id = id,
                                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                                        .appendPath(id.toString()).build().toString(),
                                    path = path,
                                    mimeType = mimeType,
                                    dateAdded = dateAdded,
                                    dateModified = dateModified,
                                    displayName = displayName,
                                    size = size,
                                    width = width,
                                    height = height,
                                    duration = duration,
                                    bucketId = bucketId,
                                    bucketName = bucketName
                                )
                            )
                        }
                    }
                    count++
                }
            }
        }
        
        mediaList.sortedByDescending { it.dateAdded }
    }
    
    suspend fun getAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()
        
        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        
        val selection = "${MediaStore.Images.Media.BUCKET_ID} IS NOT NULL"
        
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            
            val albumMap = mutableMapOf<Long, AlbumInfo>()
            
            while (cursor.moveToNext()) {
                val bucketId = cursor.getLong(bucketIdIndex)
                val bucketName = cursor.getString(bucketNameIndex) ?: "Unknown"
                val id = cursor.getLong(idIndex)
                val dateModified = cursor.getLong(dateModifiedIndex)
                
                if (!albumMap.containsKey(bucketId)) {
                    albumMap[bucketId] = AlbumInfo(
                        bucketId = bucketId,
                        name = bucketName,
                        coverUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                            .appendPath(id.toString()).build().toString(),
                        dateModified = dateModified,
                        count = 1
                    )
                } else {
                    val info = albumMap[bucketId]!!
                    albumMap[bucketId] = info.copy(count = info.count + 1)
                }
            }
            
            albums.addAll(albumMap.values.map { info ->
                Album(
                    id = info.bucketId,
                    name = info.name,
                    bucketId = info.bucketId,
                    coverUri = info.coverUri,
                    mediaCount = info.count,
                    dateModified = info.dateModified,
                    isSystemAlbum = false
                )
            })
        }
        
        // Add system albums
        albums.add(
            0,
            Album(
                id = -1,
                name = "All Photos",
                bucketId = -1,
                coverUri = null,
                mediaCount = albums.sumOf { it.mediaCount },
                dateModified = System.currentTimeMillis() / 1000,
                isSystemAlbum = true
            )
        )
        
        albums
    }
    
    private data class AlbumInfo(
        val bucketId: Long,
        val name: String,
        val coverUri: String,
        val dateModified: Long,
        val count: Int
    )
    
    suspend fun searchMedia(query: String): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<MediaItem>()
        val lowerQuery = query.lowercase()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE
        )
        
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            
            while (cursor.moveToNext()) {
                val displayName = cursor.getString(nameIndex) ?: ""
                val path = cursor.getString(dataIndex) ?: ""
                
                if (displayName.lowercase().contains(lowerQuery) || 
                    path.lowercase().contains(lowerQuery)) {
                    
                    val id = cursor.getLong(idIndex)
                    val dateAdded = cursor.getLong(dateAddedIndex)
                    val dateModified = cursor.getLong(dateModifiedIndex)
                    val size = cursor.getLong(sizeIndex)
                    val width = cursor.getInt(widthIndex)
                    val height = cursor.getInt(heightIndex)
                    val bucketId = cursor.getLong(bucketIdIndex)
                    val bucketName = cursor.getString(bucketNameIndex) ?: "Unknown"
                    val mimeType = cursor.getString(mimeTypeIndex) ?: "image/jpeg"
                    
                    mediaList.add(
                        MediaItem(
                            id = id,
                            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                                .appendPath(id.toString()).build().toString(),
                            path = path,
                            mimeType = mimeType,
                            dateAdded = dateAdded,
                            dateModified = dateModified,
                            displayName = displayName,
                            size = size,
                            width = width,
                            height = height,
                            bucketId = bucketId,
                            bucketName = bucketName
                        )
                    )
                }
            }
        }
        
        mediaList
    }
    
    suspend fun getMediaById(id: Long): MediaItem? = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE
        )
        
        val selection = "${MediaStore.Images.Media._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val widthIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                
                val path = cursor.getString(dataIndex) ?: return@withContext null
                val displayName = cursor.getString(nameIndex) ?: ""
                val dateAdded = cursor.getLong(dateAddedIndex)
                val dateModified = cursor.getLong(dateModifiedIndex)
                val size = cursor.getLong(sizeIndex)
                val width = cursor.getInt(widthIndex)
                val height = cursor.getInt(heightIndex)
                val bucketId = cursor.getLong(bucketIdIndex)
                val bucketName = cursor.getString(bucketNameIndex) ?: "Unknown"
                val mimeType = cursor.getString(mimeTypeIndex) ?: "image/jpeg"
                
                MediaItem(
                    id = id,
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                        .appendPath(id.toString()).build().toString(),
                    path = path,
                    mimeType = mimeType,
                    dateAdded = dateAdded,
                    dateModified = dateModified,
                    displayName = displayName,
                    size = size,
                    width = width,
                    height = height,
                    bucketId = bucketId,
                    bucketName = bucketName
                )
            } else {
                null
            }
        }
    }
}
