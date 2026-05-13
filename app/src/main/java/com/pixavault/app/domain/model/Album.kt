package com.pixavault.app.domain.model

data class Album(
    val id: Long,
    val name: String,
    val bucketId: Long,
    val coverUri: String?,
    val mediaCount: Int,
    val dateModified: Long,
    val isSystemAlbum: Boolean = false,
    val isHidden: Boolean = false
) {
    val formattedDate: String
        get() {
            val date = java.util.Date(dateModified * 1000)
            val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            return format.format(date)
        }
}

enum class AlbumType {
    ALL_PHOTOS,
    ALL_VIDEOS,
    CAMERA,
    SCREENSHOTS,
    DOWNLOADS,
    WHATSAPP,
    TELEGRAM,
    INSTAGRAM,
    TWITTER,
    SNAPCHAT,
    FAVORITES,
    RECENTLY_DELETED,
    HIDDEN,
    CUSTOM
}
