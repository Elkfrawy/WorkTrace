package com.example.mywork

import com.example.mywork.data.CustomerEntity
import com.example.mywork.data.PaymentLogEntity
import com.example.mywork.data.WorkItemEntity
import com.example.mywork.data.WorkOrderEntity
import com.example.mywork.data.WorkOrderWithDetails
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkOrderCalculationsTest {

    @Test
    fun testWorkOrderTotalCostAndAmountDueCalculation() {
        val customer = CustomerEntity(id = 1, name = "John Doe", phone = "555-0199", address = "123 Main St")
        val order = WorkOrderEntity(id = 10, customerId = 1, title = "Plumbing Repair", dueDate = System.currentTimeMillis())

        val items = listOf(
            WorkItemEntity(id = 100, workOrderId = 10, name = "Replace Pipe", price = 150.0, quantity = 2, isDone = true),
            WorkItemEntity(id = 101, workOrderId = 10, name = "Fix Faucet", price = 75.5, quantity = 1, isDone = false)
        )

        val payments = listOf(
            PaymentLogEntity(id = 200, workOrderId = 10, amount = 100.0, note = "Deposit")
        )

        val details = WorkOrderWithDetails(
            workOrder = order,
            customer = customer,
            items = items,
            payments = payments
        )

        // Total Cost: (150.0 * 2) + (75.5 * 1) = 300.0 + 75.5 = 375.5
        assertEquals(375.5, details.totalCost, 0.001)

        // Total Paid: 100.0
        assertEquals(100.0, details.totalPaid, 0.001)

        // Amount Due: 375.5 - 100.0 = 275.5
        assertEquals(275.5, details.amountDue, 0.001)

        // Remaining items: 1 (Fix Faucet is not done)
        assertEquals(1, details.remainingItemsCount)
        assertEquals(2, details.totalItemsCount)
    }

    @Test
    fun testFullyPaidAndDoneStatus() {
        val order = WorkOrderEntity(id = 10, customerId = null, title = "Lawn Mowing", dueDate = System.currentTimeMillis())

        val items = listOf(
            WorkItemEntity(id = 100, workOrderId = 10, name = "Front Yard", price = 50.0, quantity = 1, isDone = true)
        )

        val payments = listOf(
            PaymentLogEntity(id = 200, workOrderId = 10, amount = 50.0)
        )

        val details = WorkOrderWithDetails(
            workOrder = order,
            customer = null,
            items = items,
            payments = payments
        )

        assertEquals(0.0, details.amountDue, 0.001)
        assertEquals(0, details.remainingItemsCount)
        assertEquals(true, details.isFullyPaid)
        assertEquals(true, details.isAllItemsDone)
    }
}
