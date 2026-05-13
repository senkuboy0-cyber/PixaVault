package com.pixavault.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.pixavault.app.ui.components.*
import com.pixavault.app.ui.viewmodel.*
import android.graphics.BitmapFactory
import androidx.palette.graphics.Palette
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val viewModel: GalleryViewModel = viewModel()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    val permissionState = rememberPermissionState(readPermission)
    val filteredPhotosList by viewModel.filteredPhotos.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val selectedPhotos by viewModel.selectedPhotos.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPhoto by remember { mutableStateOf<PhotoItem?>(null) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(1f) }
    
    val gridState = rememberLazyGridState()
    
    // Load photos when permission is granted
    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.loadPhotos(context)
        }
    }
    
    // Update selection mode based on selected photos
    LaunchedEffect(selectedPhotos) {
        isSelectionMode = selectedPhotos.isNotEmpty()
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                TopAppBar(
                    title = {
                        if (showSearchBar) {
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = viewModel::setSearchQuery,
                                onClear = { viewModel.setSearchQuery("") }
                            )
                        } else {
                            Text(
                                text = "PixaVault",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        if (showSearchBar) {
                            IconButton(onClick = { 
                                showSearchBar = false
                                viewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (!showSearchBar) {
                            IconButton(onClick = { showSearchBar = !showSearchBar }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filter")
                            }
                            IconButton(onClick = { viewModel.setSortOption(
                                when (sortOption) {
                                    SortOption.DATE_DESC -> SortOption.NAME_ASC
                                    SortOption.NAME_ASC -> SortOption.SIZE_DESC
                                    SortOption.SIZE_DESC -> SortOption.DATE_ASC
                                    SortOption.DATE_ASC -> SortOption.NAME_DESC
                                    SortOption.NAME_DESC -> SortOption.SIZE_ASC
                                    SortOption.SIZE_ASC -> SortOption.DATE_DESC
                                }
                            ) }) {
                                Icon(Icons.Default.Sort, contentDescription = "Sort")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                        label = { Text("Photos") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        label = { Text("Albums") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
                        label = { Text("Favorites") }
                    )
                    NavigationBarItem(
                        selected = viewMode == ViewMode.LIST,
                        onClick = { 
                            viewModel.setViewMode(
                                if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                            )
                        },
                        icon = { 
                            Icon(
                                if (viewMode == ViewMode.GRID) Icons.Default.ViewList 
                                else Icons.Default.GridView, 
                                contentDescription = null
                            ) 
                        },
                        label = { Text("View") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!permissionState.status.isGranted) {
                PermissionScreen(
                    onRequestPermission = { permissionState.launchPermissionRequest() }
                )
            } else {
                when (selectedTab) {
                    0 -> {
                        PhotosContent(
                            photos = filteredPhotosList,
                            loadingState = loadingState,
                            viewModel = viewModel,
                            viewMode = viewMode,
                            onPhotoClick = { selectedPhoto = it },
                            gridState = gridState
                        )
                    }
                    1 -> AlbumsTab()
                    2 -> FavoritesTab(
                        viewModel = viewModel,
                        onPhotoClick = { selectedPhoto = it }
                    )
                }
            }
        }
        
        // Selection mode bottom bar
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            SelectionBottomBar(
                selectedCount = selectedPhotos.size,
                onClearSelection = viewModel::clearSelection,
                onSelectAll = viewModel::selectAll,
                onDeleteSelected = { /* TODO: Implement delete */ },
                onShareSelected = { /* TODO: Implement share */ }
            )
        }
        
        // Filter bottom sheet
        if (showFilterSheet) {
            FilterSheet(
                onDismiss = { showFilterSheet = false },
                currentSortOption = sortOption,
                onSortOptionChange = viewModel::setSortOption,
                currentFilters = viewModel.filterOptions.value,
                onFiltersChange = viewModel::updateFilterOptions
            )
        }
        
        // Loading indicator at bottom for infinite scroll
        if (isLoadingMore && gridState.firstVisibleItemIndex > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.BottomCenter
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }

    // Photo Detail Dialog
    AnimatedVisibility(
        visible = selectedPhoto != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        selectedPhoto?.let { photo ->
            PhotoDetailDialog(
                photo = photo,
                onDismiss = { selectedPhoto = null },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun PhotosContent(
    photos: List<PhotoItem>,
    loadingState: LoadingState,
    viewModel: GalleryViewModel,
    viewMode: ViewMode,
    onPhotoClick: (PhotoItem) -> Unit,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState
) {
    when (loadingState) {
        is LoadingState.Loading -> {
            LoadingScreen()
        }
        is LoadingState.Error -> {
            ErrorScreen(
                message = loadingState.message,
                onRetry = { viewModel.refreshPhotos(LocalContext.current) }
            )
        }
        else -> {
            if (photos.isEmpty()) {
                EmptyState()
            } else {
                if (viewMode == ViewMode.GRID) {
                    PhotoGrid(
                        photos = photos,
                        onPhotoClick = onPhotoClick,
                        viewModel = viewModel,
                        gridState = gridState
                    )
                } else {
                    PhotoList(
                        photos = photos,
                        onPhotoClick = onPhotoClick,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoGrid(
    photos: List<PhotoItem>,
    onPhotoClick: (PhotoItem) -> Unit,
    viewModel: GalleryViewModel,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        state = gridState,
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(photos, key = { it.id }) { photo ->
            PhotoItem(
                photo = photo,
                onClick = { onPhotoClick(photo) },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun PhotoList(
    photos: List<PhotoItem>,
    onPhotoClick: (PhotoItem) -> Unit,
    viewModel: GalleryViewModel
) {
    LazyColumn {
        lazyItems(photos, key = { it.id }) { photo ->
            PhotoListItem(
                photo = photo,
                onClick = { onPhotoClick(photo) },
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PhotoItem(
    photo: PhotoItem,
    onClick: () -> Unit,
    viewModel: GalleryViewModel
) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var dominantColor by remember { mutableStateOf(Color.Gray) }
    val isSelected by remember { mutableStateOf(viewModel.isSelected(photo.id)) }
    val isFavorite by remember { mutableStateOf(viewModel.isFavorite(photo.id)) }
    
    LaunchedEffect(photo.path) {
        isLoading = true
        try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
            }
            bitmap = BitmapFactory.decodeFile(photo.path, options)
            
            // Extract dominant color for placeholder
            bitmap?.let { bmp ->
                Palette.from(bmp).generate { palette ->
                    dominantColor = Color(palette?.getDominantColor(0x888888) ?: 0x888888)
                }
            }
            
            // Simulate loading delay for smoother animation
            delay(50)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .animateContentSize()
            .clip(RoundedCornerShape(4.dp))
            .background(dominantColor.copy(alpha = 0.3f))
            .combinedClickable(
                onClick = {
                    if (viewModel.selectedPhotos.value.isNotEmpty()) {
                        viewModel.toggleSelection(photo.id)
                    } else {
                        onClick()
                    }
                },
                onLongClick = { viewModel.toggleSelection(photo.id) }
            )
    ) {
        // Photo bitmap
        AnimatedVisibility(
            visible = !isLoading && bitmap != null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Loading state
        if (isLoading || bitmap == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = "Error",
                        modifier = Modifier.size(32.dp),
                        tint = Color.Gray
                    )
                }
            }
        }
        
        // Selection indicator
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(4.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        // Favorite indicator
        AnimatedVisibility(
            visible = isFavorite && viewModel.selectedPhotos.value.isEmpty(),
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PhotoListItem(
    photo: PhotoItem,
    onClick: () -> Unit,
    viewModel: GalleryViewModel
) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val isSelected by remember { mutableStateOf(viewModel.isSelected(photo.id)) }
    val isFavorite by remember { mutableStateOf(viewModel.isFavorite(photo.id)) }
    
    LaunchedEffect(photo.path) {
        isLoading = true
        try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 8
            }
            bitmap = BitmapFactory.decodeFile(photo.path, options)
            delay(50)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .combinedClickable(
                onClick = {
                    if (viewModel.selectedPhotos.value.isNotEmpty()) {
                        viewModel.toggleSelection(photo.id)
                    } else {
                        onClick()
                    }
                },
                onLongClick = { viewModel.toggleSelection(photo.id) }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                if (bitmap != null && !isLoading) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = "Error",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // Photo info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = photo.displayName.ifEmpty { "Unknown" },
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatFileSize(photo.size),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (photo.width > 0 && photo.height > 0) {
                    Text(
                        text = "${photo.width} × ${photo.height}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Actions
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Favorite button
                IconButton(
                    onClick = { viewModel.toggleFavorite(photo.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite 
                                   else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Selection indicator
                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var rotation by remember { mutableStateOf(0f) }
        val rotationAnimation by animateFloatAsState(
            targetValue = rotation,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        LaunchedEffect(Unit) {
            rotation = 360f
        }
        
        Icon(
            imageVector = Icons.Default.Photo,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer(rotationZ = rotationAnimation),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No photos found",
            fontSize = 18.sp,
            color = Color.Gray
        )}
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading your photos...",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Oops! Something went wrong",
            fontSize = 18.sp,
            color = Color.Red
        )
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun AlbumsTab() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var rotation by remember { mutableStateOf(0f) }
        val rotationAnimation by animateFloatAsState(
            targetValue = rotation,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing)
            )
        )
        
        LaunchedEffect(Unit) {
            rotation = 360f
        }
        
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer(rotationZ = rotationAnimation),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Albums coming soon",
            fontSize = 18.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun FavoritesTab(
    viewModel: GalleryViewModel,
    onPhotoClick: (PhotoItem) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val allPhotos by viewModel.photos.collectAsState()
    val favoritePhotos = allPhotos.filter { favorites.contains(it.id) }
    
    if (favoritePhotos.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No favorites yet",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Text(
                text = "Long press on photos to add favorites",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    } else {
        PhotoGrid(
            photos = favoritePhotos,
            onPhotoClick = onPhotoClick,
            viewModel = viewModel,
            gridState = rememberLazyGridState()
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1 -> "%.1f GB".format(gb)
        mb >= 1 -> "%.1f MB".format(mb)
        kb >= 1 -> "%.0f KB".format(kb)
        else -> "$bytes B"
    }
}