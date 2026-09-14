package com.example.mywork.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywork.data.CustomerEntity
import com.example.mywork.data.WorkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val repository: WorkRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val customers: StateFlow<List<CustomerEntity>> = combine(
        repository.getAllCustomers(),
        _searchQuery
    ) { customerList, query ->
        if (query.isBlank()) {
            customerList
        } else {
            customerList.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                        customer.phone.contains(query) ||
                        customer.address.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveCustomer(
        id: Long = 0,
        name: String,
        phone: String,
        address: String,
        notes: String
    ) {
        viewModelScope.launch {
            val customer = CustomerEntity(
                id = id,
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                notes = notes.trim()
            )
            if (id == 0L) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }
}
