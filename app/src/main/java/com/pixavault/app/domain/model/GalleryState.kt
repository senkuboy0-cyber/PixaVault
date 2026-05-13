package com.pixavault.app.domain.model

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
}

data class PaginationState(
    val page: Int = 0,
    val totalItems: Int = 0,
    val itemsPerPage: Int = 50,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false
)

enum class SortOption {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC
}

enum class ViewMode {
    GRID,
    LIST,
    TIMELINE
}

enum class MediaType {
    ALL,
    IMAGES,
    VIDEOS,
    GIFS
}

data class FilterOptions(
    val mediaType: MediaType = MediaType.ALL,
    val favoritesOnly: Boolean = false,
    val minSize: Long = 0,
    val dateRange: Pair<Long, Long>? = null,
    val sortBy: SortOption = SortOption.DATE_DESC,
    val viewMode: ViewMode = ViewMode.GRID,
    val gridColumns: Int = 4
)

data class GalleryStats(
    val totalPhotos: Int,
    val totalVideos: Int,
    val totalSize: Long,
    val favoriteCount: Int,
    val hiddenCount: Int,
    val deletedCount: Int,
    val albumCount: Int
) {
    val formattedTotalSize: String
        get() {
            return when {
                totalSize >= 1073741824 -> String.format("%.1f GB", totalSize / 1073741824.0)
                totalSize >= 1048576 -> String.format("%.1f MB", totalSize / 1048576.0)
                totalSize >= 1024 -> String.format("%.0f KB", totalSize / 1024.0)
                else -> "$totalSize B"
            }
        }
}
