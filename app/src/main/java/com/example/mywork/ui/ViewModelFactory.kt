package com.example.mywork.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mywork.data.WorkRepository
import com.example.mywork.ui.customer.CustomerViewModel
import com.example.mywork.ui.main.MainViewModel
import com.example.mywork.ui.order.WorkOrderDetailViewModel

class AppViewModelFactory(
    private val repository: WorkRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(repository) as T
            }
            modelClass.isAssignableFrom(CustomerViewModel::class.java) -> {
                CustomerViewModel(repository) as T
            }
            modelClass.isAssignableFrom(WorkOrderDetailViewModel::class.java) -> {
                WorkOrderDetailViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
