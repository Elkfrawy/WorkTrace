package com.example.mywork.data

import androidx.room.Embedded
import androidx.room.Relation

data class WorkOrderWithDetails(
    @Embedded
    val workOrder: WorkOrderEntity,

    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: CustomerEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "workOrderId"
    )
    val items: List<WorkItemEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "workOrderId"
    )
    val payments: List<PaymentLogEntity>
) {
    val totalCost: Double
        get() = items.sumOf { it.price * it.quantity }

    val totalPaid: Double
        get() = payments.sumOf { it.amount }

    val amountDue: Double
        get() = (totalCost - totalPaid).coerceAtLeast(0.0)

    val remainingItemsCount: Int
        get() = items.count { !it.isDone }

    val totalItemsCount: Int
        get() = items.size

    val isFullyPaid: Boolean
        get() = totalCost > 0 && totalPaid >= totalCost

    val isAllItemsDone: Boolean
        get() = items.isNotEmpty() && items.all { it.isDone }
}
