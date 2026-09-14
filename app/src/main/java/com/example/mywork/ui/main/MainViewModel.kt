package com.example.mywork.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywork.data.WorkOrderWithDetails
import com.example.mywork.data.WorkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class OrderFilter {
    ACTIVE,
    ARCHIVED
}

enum class DateSection {
    OVERDUE,
    TODAY,
    TOMORROW,
    LATER,
    ARCHIVED
}

data class CategorizedWorkOrders(
    val section: DateSection,
    val title: String,
    val orders: List<WorkOrderWithDetails>
)

class MainViewModel(
    private val repository: WorkRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow(OrderFilter.ACTIVE)
    val selectedFilter: StateFlow<OrderFilter> = _selectedFilter

    val categorizedWorkOrders: StateFlow<List<CategorizedWorkOrders>> = combine(
        repository.getAllWorkOrdersWithDetails(),
        _searchQuery,
        _selectedFilter
    ) { orders, query, filter ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val todayStart = cal.timeInMillis
        val tomorrowStart = todayStart + (24 * 60 * 60 * 1000L)
        val dayAfterTomorrowStart = tomorrowStart + (24 * 60 * 60 * 1000L)

        val list = mutableListOf<CategorizedWorkOrders>()

        if (filter == OrderFilter.ARCHIVED) {
            val archived = orders
                .filter { it.workOrder.isArchived }
                .sortedByDescending { it.workOrder.dueDate }
                .filter { order ->
                    query.isBlank() ||
                            order.workOrder.title.contains(query, ignoreCase = true) ||
                            (order.customer?.name?.contains(query, ignoreCase = true) == true)
                }
            list.add(CategorizedWorkOrders(DateSection.ARCHIVED, "Archived Work Orders", archived))
        } else {
            val unarchived = orders
                .filter { !it.workOrder.isArchived }
                .sortedBy { it.workOrder.dueDate }
                .filter { order ->
                    query.isBlank() ||
                            order.workOrder.title.contains(query, ignoreCase = true) ||
                            (order.customer?.name?.contains(query, ignoreCase = true) == true)
                }

            val overdue = unarchived.filter {
                it.workOrder.dueDate < todayStart
            }
            val today = unarchived.filter {
                it.workOrder.dueDate in todayStart until tomorrowStart
            }
            val tomorrow = unarchived.filter {
                it.workOrder.dueDate in tomorrowStart until dayAfterTomorrowStart
            }
            val later = unarchived.filter {
                it.workOrder.dueDate >= dayAfterTomorrowStart
            }

            if (overdue.isNotEmpty()) {
                list.add(CategorizedWorkOrders(DateSection.OVERDUE, "Overdue Work", overdue))
            }
            list.add(CategorizedWorkOrders(DateSection.TODAY, "Today's Work", today))
            list.add(CategorizedWorkOrders(DateSection.TOMORROW, "Tomorrow's Work", tomorrow))
            list.add(CategorizedWorkOrders(DateSection.LATER, "Upcoming & Later Work", later))
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: OrderFilter) {
        _selectedFilter.value = filter
    }

    fun toggleWorkItemDone(itemId: Long, isDone: Boolean) {
        viewModelScope.launch {
            repository.setWorkItemDone(itemId, isDone)
        }
    }

    fun setWorkOrderArchived(orderId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.setWorkOrderArchived(orderId, isArchived)
        }
    }

    fun deleteWorkOrder(workOrder: WorkOrderWithDetails) {
        viewModelScope.launch {
            repository.deleteWorkOrder(workOrder.workOrder)
        }
    }
}
