package com.pixavault.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pixavault.app.ui.viewmodel.FilterOptions
import com.pixavault.app.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    onDismiss: () -> Unit,
    currentSortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    currentFilters: FilterOptions,
    onFiltersChange: (FilterOptions) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & Sort",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sort options
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOption.values().forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSortOption == option,
                            onClick = { onSortOptionChange(option) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = getSortOptionLabel(option))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Filter options
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Favorites Only
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = currentFilters.favoritesOnly,
                    onCheckedChange = { checked ->
                        onFiltersChange(currentFilters.copy(favoritesOnly = checked))
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Favorites only")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Apply button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
        }
    }
}

private fun getSortOptionLabel(option: SortOption): String {
    return when (option) {
        SortOption.DATE_DESC -> "Date (Newest first)"
        SortOption.DATE_ASC -> "Date (Oldest first)"
        SortOption.NAME_ASC -> "Name (A to Z)"
        SortOption.NAME_DESC -> "Name (Z to A)"
        SortOption.SIZE_DESC -> "Size (Largest first)"
        SortOption.SIZE_ASC -> "Size (Smallest first)"
    }
}