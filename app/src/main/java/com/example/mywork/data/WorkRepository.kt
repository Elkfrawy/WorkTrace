package com.example.mywork.data

import kotlinx.coroutines.flow.Flow

class WorkRepository(
    private val customerDao: CustomerDao,
    private val workOrderDao: WorkOrderDao
) {
    // Customer operations
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    fun getCustomerById(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)

    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    // Work Order operations
    fun getAllWorkOrdersWithDetails(): Flow<List<WorkOrderWithDetails>> =
        workOrderDao.getAllWorkOrdersWithDetails()

    fun getWorkOrderWithDetailsById(id: Long): Flow<WorkOrderWithDetails?> =
        workOrderDao.getWorkOrderWithDetailsById(id)

    suspend fun insertWorkOrder(workOrder: WorkOrderEntity): Long =
        workOrderDao.insertWorkOrder(workOrder)

    suspend fun updateWorkOrder(workOrder: WorkOrderEntity) =
        workOrderDao.updateWorkOrder(workOrder)

    suspend fun deleteWorkOrder(workOrder: WorkOrderEntity) =
        workOrderDao.deleteWorkOrder(workOrder)

    suspend fun setWorkOrderArchived(orderId: Long, isArchived: Boolean) =
        workOrderDao.updateWorkOrderArchiveStatus(orderId, isArchived)

    // Work Item operations
    suspend fun insertWorkItem(item: WorkItemEntity): Long =
        workOrderDao.insertWorkItem(item)

    suspend fun updateWorkItem(item: WorkItemEntity) =
        workOrderDao.updateWorkItem(item)

    suspend fun deleteWorkItem(item: WorkItemEntity) =
        workOrderDao.deleteWorkItem(item)

    suspend fun setWorkItemDone(itemId: Long, isDone: Boolean) =
        workOrderDao.updateWorkItemDoneStatus(itemId, isDone)

    // Payment Log operations
    suspend fun insertPaymentLog(payment: PaymentLogEntity): Long =
        workOrderDao.insertPaymentLog(payment)

    suspend fun deletePaymentLog(payment: PaymentLogEntity) =
        workOrderDao.deletePaymentLog(payment)
}
