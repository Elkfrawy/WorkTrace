package com.example.mywork.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerByIdDirect(id: Long): CustomerEntity?
}

@Dao
interface WorkOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrder(workOrder: WorkOrderEntity): Long

    @Update
    suspend fun updateWorkOrder(workOrder: WorkOrderEntity)

    @Delete
    suspend fun deleteWorkOrder(workOrder: WorkOrderEntity)

    @Query("UPDATE work_orders SET isArchived = :isArchived WHERE id = :orderId")
    suspend fun updateWorkOrderArchiveStatus(orderId: Long, isArchived: Boolean)

    @Query("SELECT * FROM work_orders WHERE id = :id")
    suspend fun getWorkOrderEntityById(id: Long): WorkOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkItem(workItem: WorkItemEntity): Long

    @Update
    suspend fun updateWorkItem(workItem: WorkItemEntity)

    @Delete
    suspend fun deleteWorkItem(workItem: WorkItemEntity)

    @Query("UPDATE work_items SET isDone = :isDone WHERE id = :itemId")
    suspend fun updateWorkItemDoneStatus(itemId: Long, isDone: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentLog(paymentLog: PaymentLogEntity): Long

    @Delete
    suspend fun deletePaymentLog(paymentLog: PaymentLogEntity)

    @Transaction
    @Query("SELECT * FROM work_orders ORDER BY dueDate ASC")
    fun getAllWorkOrdersWithDetails(): Flow<List<WorkOrderWithDetails>>

    @Transaction
    @Query("SELECT * FROM work_orders WHERE id = :id")
    fun getWorkOrderWithDetailsById(id: Long): Flow<WorkOrderWithDetails?>

    @Transaction
    @Query("SELECT * FROM work_orders WHERE id = :id")
    suspend fun getWorkOrderWithDetailsByIdDirect(id: Long): WorkOrderWithDetails?
}
