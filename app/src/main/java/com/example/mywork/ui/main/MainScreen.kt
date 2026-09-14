package com.example.mywork.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mywork.data.WorkOrderWithDetails
import com.example.mywork.ui.utils.formatCurrency
import com.example.mywork.ui.utils.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWorkOrder: (orderId: Long) -> Unit
) {
    val categorizedOrders by viewModel.categorizedWorkOrders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val totalOrdersCount = categorizedOrders.sumOf { it.orders.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WorkTrace") },
                actions = {
                    IconButton(onClick = onNavigateToCustomers) {
                        Icon(Icons.Default.People, contentDescription = "Customers")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToWorkOrder(0L) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Work Order")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search work orders or customers") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true
            )

            // Filter Chips: Active vs Archived
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == OrderFilter.ACTIVE,
                    onClick = { viewModel.setFilter(OrderFilter.ACTIVE) },
                    label = { Text("Active Orders") }
                )
                FilterChip(
                    selected = selectedFilter == OrderFilter.ARCHIVED,
                    onClick = { viewModel.setFilter(OrderFilter.ARCHIVED) },
                    label = { Text("Archived") }
                )
            }

            if (totalOrdersCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) {
                            if (selectedFilter == OrderFilter.ARCHIVED)
                                "No archived work orders."
                            else
                                "No work orders yet.\nTap + to create your first work order."
                        } else {
                            "No matching work orders found."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    categorizedOrders.forEach { categorized ->
                        if (categorized.orders.isNotEmpty() || searchQuery.isBlank()) {
                            item {
                                SectionHeader(
                                    title = categorized.title,
                                    count = categorized.orders.size,
                                    section = categorized.section
                                )
                            }

                            if (categorized.orders.isEmpty()) {
                                item {
                                    Text(
                                        text = "No work orders scheduled.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                    )
                                }
                            } else {
                                items(categorized.orders, key = { it.workOrder.id }) { detail ->
                                    WorkOrderCard(
                                        detail = detail,
                                        onClick = { onNavigateToWorkOrder(detail.workOrder.id) },
                                        onToggleItemDone = { itemId, isDone ->
                                            viewModel.toggleWorkItemDone(itemId, isDone)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int,
    section: DateSection
) {
    val (icon: ImageVector, iconTint) = when (section) {
        DateSection.OVERDUE -> Icons.Default.Warning to MaterialTheme.colorScheme.error
        DateSection.TODAY -> Icons.Default.Today to MaterialTheme.colorScheme.primary
        DateSection.TOMORROW -> Icons.Default.CalendarToday to MaterialTheme.colorScheme.tertiary
        DateSection.LATER -> Icons.Default.Event to MaterialTheme.colorScheme.secondary
        DateSection.ARCHIVED -> Icons.Default.Archive to MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (section == DateSection.OVERDUE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (count > 0)
                iconTint.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) iconTint else MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun WorkOrderCard(
    detail: WorkOrderWithDetails,
    onClick: () -> Unit,
    onToggleItemDone: (itemId: Long, isDone: Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val isOverdue = !detail.workOrder.isArchived &&
            detail.workOrder.dueDate < System.currentTimeMillis()

    val isCompleted = detail.remainingItemsCount == 0 && detail.amountDue == 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (detail.workOrder.isArchived || isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title & Date Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = detail.workOrder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Date Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        detail.workOrder.isArchived -> MaterialTheme.colorScheme.surfaceVariant
                        isCompleted -> MaterialTheme.colorScheme.secondaryContainer
                        isOverdue -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = when {
                                    detail.workOrder.isArchived -> MaterialTheme.colorScheme.onSurfaceVariant
                                    isCompleted -> MaterialTheme.colorScheme.onSecondaryContainer
                                    isOverdue -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = formatDate(detail.workOrder.dueDate),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    detail.workOrder.isArchived -> MaterialTheme.colorScheme.onSurfaceVariant
                                    isCompleted -> MaterialTheme.colorScheme.onSecondaryContainer
                                    isOverdue -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                    }
                }

            // Customer Row
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = detail.customer?.name ?: "No Customer Assigned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Key Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Remaining Due Amount
                Column {
                    Text(
                        text = "Amount Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(detail.amountDue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (detail.amountDue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                // Remaining Items Count
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Work Items",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (detail.remainingItemsCount == 0 && detail.totalItemsCount > 0)
                            "All Done ✓"
                        else
                            "${detail.remainingItemsCount} remaining",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (detail.remainingItemsCount == 0 && detail.totalItemsCount > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Quick toggle work items dropdown button
            if (detail.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Hide Work Items" else "Quick Check Items",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        detail.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = item.isDone,
                                        onCheckedChange = { isDone ->
                                            onToggleItemDone(item.id, isDone)
                                        }
                                    )
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (item.isDone) FontWeight.Normal else FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = formatCurrency(item.price * item.quantity),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
