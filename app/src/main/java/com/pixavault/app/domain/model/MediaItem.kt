package com.pixavault.app.domain.model

data class MediaItem(
    val id: Long,
    val uri: String,
    val path: String,
    val mimeType: String,
    val dateAdded: Long,
    val dateModified: Long,
    val displayName: String,
    val size: Long,
    val width: Int,
    val height: Int,
    val duration: Long = 0L,
    val bucketId: Long,
    val bucketName: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isDeleted: Boolean = false,
    val deleteTimestamp: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val orientation: Int = 0
) {
    val isVideo: Boolean
        get() = mimeType.startsWith("video/")
    
    val isImage: Boolean
        get() = mimeType.startsWith("image/")
    
    val formattedDuration: String
        get() {
            if (duration <= 0) return ""
            val seconds = duration / 1000
            val minutes = seconds / 60
            val secs = seconds % 60
            return if (minutes > 0) {
                "$minutes:${secs.toString().padStart(2, '0')}"
            } else {
                "0:$secs"
            }
        }
    
    val formattedSize: String
        get() {
            return when {
                size >= 1073741824 -> String.format("%.1f GB", size / 1073741824.0)
                size >= 1048576 -> String.format("%.1f MB", size / 1048576.0)
                size >= 1024 -> String.format("%.0f KB", size / 1024.0)
                else -> "$size B"
            }
        }
}
