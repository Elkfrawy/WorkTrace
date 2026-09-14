package com.example.mywork.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywork.data.CustomerEntity
import com.example.mywork.data.PaymentLogEntity
import com.example.mywork.data.WorkItemEntity
import com.example.mywork.data.WorkOrderEntity
import com.example.mywork.data.WorkOrderWithDetails
import com.example.mywork.data.WorkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkOrderDetailViewModel(
    private val repository: WorkRepository
) : ViewModel() {

    val customers: StateFlow<List<CustomerEntity>> = repository.getAllCustomers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentWorkOrderDetails = MutableStateFlow<WorkOrderWithDetails?>(null)
    val currentWorkOrderDetails: StateFlow<WorkOrderWithDetails?> = _currentWorkOrderDetails.asStateFlow()

    fun loadWorkOrder(orderId: Long) {
        if (orderId <= 0) {
            _currentWorkOrderDetails.value = null
            return
        }
        viewModelScope.launch {
            repository.getWorkOrderWithDetailsById(orderId).collect { details ->
                _currentWorkOrderDetails.value = details
            }
        }
    }

    fun saveWorkOrder(
        orderId: Long,
        customerId: Long?,
        title: String,
        description: String?,
        dueDate: Long,
        onSaved: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val currentArchived = _currentWorkOrderDetails.value?.workOrder?.isArchived ?: false
            val entity = WorkOrderEntity(
                id = if (orderId > 0) orderId else 0,
                customerId = customerId,
                title = title.trim(),
                description = description?.trim()?.ifEmpty { null },
                dueDate = dueDate,
                isArchived = currentArchived
            )
            val savedId = if (orderId > 0) {
                repository.updateWorkOrder(entity)
                orderId
            } else {
                repository.insertWorkOrder(entity)
            }
            onSaved(savedId)
        }
    }

    fun setWorkOrderArchived(orderId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.setWorkOrderArchived(orderId, isArchived)
        }
    }

    fun addWorkItem(
        workOrderId: Long,
        name: String,
        price: Double,
        quantity: Int
    ) {
        if (workOrderId <= 0) return
        viewModelScope.launch {
            val item = WorkItemEntity(
                workOrderId = workOrderId,
                name = name.trim(),
                price = price,
                quantity = quantity,
                isDone = false
            )
            repository.insertWorkItem(item)
        }
    }

    fun toggleWorkItemDone(itemId: Long, isDone: Boolean) {
        viewModelScope.launch {
            repository.setWorkItemDone(itemId, isDone)
        }
    }

    fun deleteWorkItem(item: WorkItemEntity) {
        viewModelScope.launch {
            repository.deleteWorkItem(item)
        }
    }

    fun addPaymentLog(
        workOrderId: Long,
        amount: Double,
        date: Long,
        note: String?
    ) {
        if (workOrderId <= 0) return
        viewModelScope.launch {
            val payment = PaymentLogEntity(
                workOrderId = workOrderId,
                amount = amount,
                paymentDate = date,
                note = note?.trim()?.ifEmpty { null }
            )
            repository.insertPaymentLog(payment)
        }
    }

    fun deletePaymentLog(payment: PaymentLogEntity) {
        viewModelScope.launch {
            repository.deletePaymentLog(payment)
        }
    }

    fun deleteWorkOrder(workOrder: WorkOrderEntity, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteWorkOrder(workOrder)
            onDeleted()
        }
    }
}
