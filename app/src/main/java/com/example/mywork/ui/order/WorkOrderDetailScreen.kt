package com.example.mywork.ui.order

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mywork.data.CustomerEntity
import com.example.mywork.data.PaymentLogEntity
import com.example.mywork.data.WorkItemEntity
import com.example.mywork.ui.utils.formatCurrency
import com.example.mywork.ui.utils.formatDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDetailScreen(
    orderId: Long,
    viewModel: WorkOrderDetailViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(orderId) {
        viewModel.loadWorkOrder(orderId)
    }

    val details by viewModel.currentWorkOrderDetails.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var dueDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(details) {
        if (details != null && !isInitialized) {
            val order = details!!.workOrder
            title = order.title
            description = order.description ?: ""
            selectedCustomerId = order.customerId
            dueDate = order.dueDate
            isInitialized = true
        }
    }

    var showAddWorkItemDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val activeOrderId = details?.workOrder?.id ?: orderId

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dueDate

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.timeInMillis = dueDate
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            dueDate = calendar.timeInMillis
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.timeInMillis = dueDate
            calendar.set(year, month, dayOfMonth)
            dueDate = calendar.timeInMillis
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (activeOrderId > 0) "Work Order #$activeOrderId" else "New Work Order") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (activeOrderId > 0 && details != null) {
                        val isArchived = details!!.workOrder.isArchived
                        IconButton(onClick = {
                            viewModel.setWorkOrderArchived(activeOrderId, !isArchived)
                        }) {
                            Icon(
                                imageVector = if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                contentDescription = if (isArchived) "Unarchive Order" else "Archive Order"
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Order",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.saveWorkOrder(
                                    orderId = activeOrderId,
                                    customerId = selectedCustomerId,
                                    title = title,
                                    description = description,
                                    dueDate = dueDate,
                                    onSaved = { savedId ->
                                        onNavigateBack()
                                    }
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save Order")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title & Description
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Order Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Customer Selector
            CustomerDropdownSelector(
                customers = customers,
                selectedCustomerId = selectedCustomerId,
                onCustomerSelected = { selectedCustomerId = it }
            )

            // Due Date Picker Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Due Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDate(dueDate),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                TextButton(onClick = { datePickerDialog.show() }) {
                    Text("Change")
                }
            }

            // Financial Summary Card
            details?.let { detail ->
                FinancialSummaryCard(
                    totalCost = detail.totalCost,
                    totalPaid = detail.totalPaid,
                    amountDue = detail.amountDue,
                    remainingItems = detail.remainingItemsCount,
                    totalItems = detail.totalItemsCount
                )
            }

            HorizontalDivider()

            // Work Items Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Work Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        if (activeOrderId == 0L && title.isNotBlank()) {
                            // Auto save order first to get an ID
                            viewModel.saveWorkOrder(
                                orderId = 0L,
                                customerId = selectedCustomerId,
                                title = title,
                                description = description,
                                dueDate = dueDate,
                                onSaved = { savedId ->
                                    viewModel.loadWorkOrder(savedId)
                                    showAddWorkItemDialog = true
                                }
                            )
                        } else {
                            showAddWorkItemDialog = true
                        }
                    },
                    enabled = title.isNotBlank() || activeOrderId > 0
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item")
                }
            }

            val items = details?.items ?: emptyList()
            if (items.isEmpty()) {
                Text(
                    text = "No work items added yet. Tap 'Add Item' to list items to charge for.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                items.forEach { item ->
                    WorkItemRow(
                        item = item,
                        onToggleDone = { isDone ->
                            viewModel.toggleWorkItemDone(item.id, isDone)
                        },
                        onDelete = {
                            viewModel.deleteWorkItem(item)
                        }
                    )
                }
            }

            HorizontalDivider()

            // Payment Logs Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = {
                        if (activeOrderId == 0L && title.isNotBlank()) {
                            viewModel.saveWorkOrder(
                                orderId = 0L,
                                customerId = selectedCustomerId,
                                title = title,
                                description = description,
                                dueDate = dueDate,
                                onSaved = { savedId ->
                                    viewModel.loadWorkOrder(savedId)
                                    showAddPaymentDialog = true
                                }
                            )
                        } else {
                            showAddPaymentDialog = true
                        }
                    },
                    enabled = title.isNotBlank() || activeOrderId > 0
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Payment")
                }
            }

            val payments = details?.payments ?: emptyList()
            if (payments.isEmpty()) {
                Text(
                    text = "No payments recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                payments.forEach { payment ->
                    PaymentLogRow(
                        payment = payment,
                        onDelete = {
                            viewModel.deletePaymentLog(payment)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddWorkItemDialog) {
        val currentOrderId = details?.workOrder?.id ?: orderId
        AddWorkItemDialog(
            onDismiss = { showAddWorkItemDialog = false },
            onSave = { name, price, quantity ->
                viewModel.addWorkItem(currentOrderId, name, price, quantity)
                showAddWorkItemDialog = false
            }
        )
    }

    if (showAddPaymentDialog) {
        val currentOrderId = details?.workOrder?.id ?: orderId
        AddPaymentDialog(
            onDismiss = { showAddPaymentDialog = false },
            onSave = { amount, date, note ->
                viewModel.addPaymentLog(currentOrderId, amount, date, note)
                showAddPaymentDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog && details != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Work Order") },
            text = { Text("Are you sure you want to delete work order '${details!!.workOrder.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWorkOrder(details!!.workOrder) {
                            showDeleteConfirmDialog = false
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDropdownSelector(
    customers: List<CustomerEntity>,
    selectedCustomerId: Long?,
    onCustomerSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCustomer = customers.find { it.id == selectedCustomerId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCustomer?.name ?: "Select Customer (Optional)",
            onValueChange = {},
            readOnly = true,
            label = { Text("Customer") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onCustomerSelected(null)
                    expanded = false
                }
            )
            customers.forEach { customer ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(customer.name, fontWeight = FontWeight.Bold)
                            if (customer.phone.isNotBlank()) {
                                Text(customer.phone, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    onClick = {
                        onCustomerSelected(customer.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FinancialSummaryCard(
    totalCost: Double,
    totalPaid: Double,
    amountDue: Double,
    remainingItems: Int,
    totalItems: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Work Items Cost:")
                Text(formatCurrency(totalCost), fontWeight = FontWeight.SemiBold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Paid:")
                Text(
                    formatCurrency(totalPaid),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Remaining Amount Due:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = formatCurrency(amountDue),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (amountDue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Incomplete Items:")
                Text(
                    text = "$remainingItems of $totalItems remaining",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun WorkItemRow(
    item: WorkItemEntity,
    onToggleDone: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDone)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isDone,
                    onCheckedChange = onToggleDone
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${formatCurrency(item.price)} x ${item.quantity} = ${formatCurrency(item.price * item.quantity)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun PaymentLogRow(
    payment: PaymentLogEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatCurrency(payment.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = formatDate(payment.paymentDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                if (!payment.note.isNullOrBlank()) {
                    Text(
                        text = payment.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete payment",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddWorkItemDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, price: Double, quantity: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Work Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price per item ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "Please enter valid item name and price",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    val quantity = quantityText.toIntOrNull() ?: 1
                    if (name.isBlank() || price == null || price < 0) {
                        isError = true
                    } else {
                        onSave(name, price, quantity)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddPaymentDialog(
    onDismiss: () -> Unit,
    onSave: (amount: Double, date: Long, note: String?) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var paymentDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = paymentDate

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.timeInMillis = paymentDate
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            paymentDate = calendar.timeInMillis
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.timeInMillis = paymentDate
            calendar.set(year, month, dayOfMonth)
            paymentDate = calendar.timeInMillis
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Customer Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Payment Date: ${formatDate(paymentDate)}")
                    TextButton(onClick = { datePickerDialog.show() }) {
                        Text("Change")
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Payment Method (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "Please enter a valid amount",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        isError = true
                    } else {
                        onSave(amount, paymentDate, note)
                    }
                }
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
