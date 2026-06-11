package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Folder
import com.example.data.Task
import com.example.ui.PlannerViewModel
import com.example.util.AppStrings
import com.example.util.CalendarHelper
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchDialog(
    viewModel: PlannerViewModel,
    onDismiss: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val isFa = AppStrings.activeLanguage == "fa"

    var searchQuery by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf<Int?>(null) } // null = All
    var selectedFolderId by remember { mutableStateOf<Long?>(null) } // null = All
    var selectedDateText by remember { mutableStateOf("") }
    var selectedDateTimestamp by remember { mutableStateOf<Long?>(null) }
    var sortByPriority by remember { mutableStateOf(false) } // False = Sort by Date, True = Sort by Importance (High to Low)

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTaskForDetails by remember { mutableStateOf<Task?>(null) }

    // Advanced filtering state computation
    val filteredTasks = remember(tasks, searchQuery, selectedPriority, selectedFolderId, selectedDateTimestamp, sortByPriority) {
        var list = tasks.filter { task ->
            val matchQuery = searchQuery.isEmpty() ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.description.contains(searchQuery, ignoreCase = true)
            
            val matchPriority = selectedPriority == null || task.priority == selectedPriority
            
            val matchFolder = selectedFolderId == null || task.folderId == selectedFolderId
            
            val matchDate = selectedDateTimestamp == null || (task.dueDate != null && run {
                val taskCal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
                val targetCal = Calendar.getInstance().apply { timeInMillis = selectedDateTimestamp!! }
                taskCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                        taskCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
            })

            matchQuery && matchPriority && matchFolder && matchDate
        }

        // Sorting: Importance (High 2 first) OR Date (Closest first)
        list = if (sortByPriority) {
            list.sortedWith(compareByDescending<Task> { it.priority }.thenBy { it.dueDate ?: Long.MAX_VALUE })
        } else {
            list.sortedBy { it.dueDate ?: Long.MAX_VALUE }
        }

        list
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        AppStrings.get("search_advanced"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Query Text Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(AppStrings.get("search_query")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                )

                // Date Filtering option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Date", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedDateText.isEmpty()) AppStrings.get("due_date") else selectedDateText,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (selectedDateTimestamp != null) {
                        IconButton(
                            onClick = {
                                selectedDateText = ""
                                selectedDateTimestamp = null
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Clear Date", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Category Folders selection row
                Text(
                    text = AppStrings.get("folder") + ":",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterBadge(
                            label = AppStrings.get("all_categories"),
                            selected = selectedFolderId == null,
                            onClick = { selectedFolderId = null }
                        )
                    }
                    items(folders) { folder ->
                        FilterBadge(
                            label = folder.name,
                            selected = selectedFolderId == folder.id,
                            onClick = { selectedFolderId = folder.id }
                        )
                    }
                }

                // Priority levels filters
                Text(
                    text = AppStrings.get("priority") + ":",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        null to AppStrings.get("all_priorities"),
                        0 to AppStrings.get("priority_low"),
                        1 to AppStrings.get("priority_med"),
                        2 to AppStrings.get("priority_high")
                    ).forEach { (pCode, pLabel) ->
                        val isSel = selectedPriority == pCode
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)
                                )
                                .clickable { selectedPriority = pCode }
                        ) {
                            Text(
                                pLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Sorting selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        AppStrings.get("order_by") + ":",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterBadge(
                            label = AppStrings.get("order_date"),
                            selected = !sortByPriority,
                            onClick = { sortByPriority = false }
                        )
                        FilterBadge(
                            label = AppStrings.get("order_priority"),
                            selected = sortByPriority,
                            onClick = { sortByPriority = true }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Results list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredTasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    AppStrings.get("no_results"),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(filteredTasks) { task ->
                            val folder = folders.find { it.id == task.folderId }
                            SearchTaskRow(
                                task = task,
                                folder = folder,
                                onClick = { selectedTaskForDetails = task }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Date Picker triggered
    if (showDatePicker) {
        BilingualDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { timestamp, label ->
                selectedDateTimestamp = timestamp
                selectedDateText = label
                showDatePicker = false
            }
        )
    }

    // Modal Detail view triggered
    if (selectedTaskForDetails != null) {
        val task = selectedTaskForDetails!!
        val folder = folders.find { it.id == task.folderId }
        TaskDetailDialog(
            task = task,
            folder = folder,
            viewModel = viewModel,
            onDismiss = { selectedTaskForDetails = null }
        )
    }
}

@Composable
fun FilterBadge(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SearchTaskRow(
    task: Task,
    folder: Folder?,
    onClick: () -> Unit
) {
    val isFa = AppStrings.activeLanguage == "fa"

    val priorityLabel = when (task.priority) {
        2 -> AppStrings.get("priority_high")
        1 -> AppStrings.get("priority_med")
        else -> AppStrings.get("priority_low")
    }

    val priorityColor = when (task.priority) {
        2 -> MaterialTheme.colorScheme.error
        1 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = priorityLabel,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }
                }

                if (task.dueDate != null) {
                    val taskDateText = run {
                        val gregCal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
                        val jal = CalendarHelper.gregorianToJalali(gregCal.get(Calendar.YEAR), gregCal.get(Calendar.MONTH) + 1, gregCal.get(Calendar.DAY_OF_MONTH))
                        "${jal[0]}/${"%02d".format(jal[1])}/${"%02d".format(jal[2])} (${AppStrings.get("jalali")})"
                    }
                    Text(
                        text = taskDateText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Icon(Icons.Default.Info, contentDescription = "Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

// Full Detail Popup Dialog
@Composable
fun TaskDetailDialog(
    task: Task,
    folder: Folder?,
    viewModel: PlannerViewModel,
    onDismiss: () -> Unit
) {
    val isFa = AppStrings.activeLanguage == "fa"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        AppStrings.get("edit_task"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Name and desc
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    if (task.description.isNotEmpty()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Priority & Folder indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Priority tag
                    val pLabel = when (task.priority) {
                        2 -> AppStrings.get("priority_high")
                        1 -> AppStrings.get("priority_med")
                        else -> AppStrings.get("priority_low")
                    }
                    val pColor = when (task.priority) {
                        2 -> MaterialTheme.colorScheme.error
                        1 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.outline
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(pColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(pLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = pColor)
                    }

                    // Folder tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = folder?.name ?: AppStrings.get("no_folder"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Date and Alerts info
                if (task.dueDate != null) {
                    val taskDateText = run {
                        val gregCal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
                        val jal = CalendarHelper.gregorianToJalali(gregCal.get(Calendar.YEAR), gregCal.get(Calendar.MONTH) + 1, gregCal.get(Calendar.DAY_OF_MONTH))
                        "${jal[0]}/${"%02d".format(jal[1])}/${"%02d".format(jal[2])} (${AppStrings.get("jalali")})"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DateRange, "Date", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "${AppStrings.get("due_date")}: $taskDateText",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Complete toggle and Deletion actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.toggleTaskCompletion(task)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            if (task.isCompleted) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = "Toggle Complete"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (task.isCompleted) {
                                if (isFa) "تغییر به معلق" else "Mark Incomplete"
                            } else {
                                if (isFa) "تغییر به انجام شد" else "Mark Complete"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.deleteTask(task.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFa) "حذف" else "Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
