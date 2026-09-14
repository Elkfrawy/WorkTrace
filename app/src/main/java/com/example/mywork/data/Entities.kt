package com.example.mywork.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "work_orders",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("customerId")]
)
data class WorkOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long?,
    val title: String,
    val description: String? = null,
    val dueDate: Long,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "work_items",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["workOrderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workOrderId")]
)
data class WorkItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workOrderId: Long,
    val name: String,
    val price: Double,
    val quantity: Int = 1,
    val isDone: Boolean = false
)

@Entity(
    tableName = "payment_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["workOrderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workOrderId")]
)
data class PaymentLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workOrderId: Long,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val note: String? = null
)
