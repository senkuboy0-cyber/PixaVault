package com.pixavault.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManActivityResultCallbacks
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pixavault.app.ui.viewmodel.GalleryViewModel
import com.pixavault.app.ui.viewmodel.PhotoItem
import android.graphics.BitmapFactory
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.palette.graphics.Palette
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailDialog(
    photo: PhotoItem,
    onDismiss: () -> Unit,
    viewModel: GalleryViewModel
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isFavorite by remember { mutableStateOf(false) }
    var dominantColor by remember { mutableStateOf(Color.Black) }
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle share result if needed
    }
    
    // Load bitmap
    LaunchedEffect(photo.path) {
        isLoading = true
        isFavorite = viewModel.isFavorite(photo.id)
        
        try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 1 // Full quality for detail view
            }
            bitmap = BitmapFactory.decodeFile(photo.path, options)
            
            // Extract dominant color for background
            bitmap?.let { bmp ->
                Palette.from(bmp).generate { palette ->
                    dominantColor = Color(palette?.getDominantColor(0xFF000000.toInt()) ?: 0xFF000000.toInt())
                }
            }
            
            delay(100) // Brief delay for smooth animation
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    // Watch for favorites changes
    LaunchedEffect(photo.id) {
        snapshotFlow { viewModel.isFavorite(photo.id) }.collect { isFavorite = it }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dominantColor.copy(alpha = 0.9f))
                .clickable(onClick = onDismiss)
        ) {
            // Top bar with actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Back button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .shadow(8.dp, CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Favorite button
                IconButton(
                    onClick = {
                        viewModel.toggleFavorite(photo.id)
                        isFavorite = !isFavorite
                    },
                    modifier = Modifier
                        .shadow(8.dp, CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
                
                // Share button
                IconButton(
                    onClick = { sharePhoto(context, photo, shareLauncher) },
                    modifier = Modifier
                        .shadow(8.dp, CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
                
                // More options button
                IconButton(
                    onClick = { /* TODO: Implement more options */ },
                    modifier = Modifier
                        .shadow(8.dp, CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = Color.White
                    )
                }
            }
            
            // Photo display area
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null && !isLoading) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Photo Detail",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.8f)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale *= zoom
                                    offsetX += pan.x
                                    offsetY += pan.y
                                    
                                    // Limit scale
                                    scale = scale.coerceIn(0.5f, 5f)
                                    
                                    // Limit offset
                                    val maxOffset = 100f
                                    offsetX = offsetX.coerceIn(-maxOffset, maxOffset)
                                    offsetY = offsetY.coerceIn(-maxOffset, maxOffset)
                                }
                            }
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                            .clickable(onClick = onDismiss)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 3.dp,
                                color = Color.White
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BrokenImage,
                                    contentDescription = "Error",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Failed to load image",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            
            // Bottom info bar
            if (!isLoading && bitmap != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        color = Color.Black.copy(alpha = 0.7)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Photo name
                            Text(
                                text = photo.displayName.ifEmpty { "Unknown" },
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Photo details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${photo.width} × ${photo.height}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = formatFileSize(photo.size),
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = formatDate(photo.dateAdded),
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sharePhoto(context: android.content.Context, photo: PhotoItem, launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
    try {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, photo.uri)
            putExtra(Intent.EXTRA_TEXT, "Check out this photo: ${photo.displayName}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        launcher.launch(Intent.createChooser(shareIntent, "Share Photo Via"))
    } catch (e: Exception) {
        // Handle sharing error
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

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp * 1000)
    val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return format.format(date)
}