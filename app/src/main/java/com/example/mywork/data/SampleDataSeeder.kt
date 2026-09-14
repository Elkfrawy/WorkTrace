package com.example.mywork.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object SampleDataSeeder {

    suspend fun seedIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val customerDao = database.customerDao()
        val workOrderDao = database.workOrderDao()

        val existingCustomers = customerDao.getCustomerByIdDirect(1L)
        val existingOrders = workOrderDao.getWorkOrderEntityById(1L)

        if (existingCustomers != null || existingOrders != null) {
            return@withContext
        }

        // 1. Seed Customers
        val johnId = customerDao.insertCustomer(
            CustomerEntity(
                name = "John Smith",
                phone = "555-0123",
                address = "123 Oak Street, Suite 4",
                notes = "VIP Customer"
            )
        )

        val sarahId = customerDao.insertCustomer(
            CustomerEntity(
                name = "Sarah Connor",
                phone = "555-0199",
                address = "456 Pine Ave",
                notes = "Call before arrival"
            )
        )

        val apexId = customerDao.insertCustomer(
            CustomerEntity(
                name = "Apex Technologies",
                phone = "555-0888",
                address = "789 Tech Park Blvd",
                notes = "Billing contact: Mike"
            )
        )

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // 2 days ago (Overdue)
        val twoDaysAgo = cal.timeInMillis - (2 * 24 * 60 * 60 * 1000L)
        // Today
        val today = cal.timeInMillis
        // Tomorrow
        val tomorrow = cal.timeInMillis + (24 * 60 * 60 * 1000L)
        // 5 days later
        val fiveDaysLater = cal.timeInMillis + (5 * 24 * 60 * 60 * 1000L)
        // 10 days ago (Archived)
        val tenDaysAgo = cal.timeInMillis - (10 * 24 * 60 * 60 * 1000L)

        // 2. Work Order 1: Overdue
        val order1Id = workOrderDao.insertWorkOrder(
            WorkOrderEntity(
                customerId = johnId,
                title = "Emergency Pipe Repair",
                description = "Water leaking in main basement line",
                dueDate = twoDaysAgo,
                isArchived = false
            )
        )
        workOrderDao.insertWorkItem(
            WorkItemEntity(workOrderId = order1Id, name = "Replace Main Valve", price = 180.0, quantity = 1, isDone = false)
        )
        workOrderDao.insertWorkItem(
            WorkItemEntity(workOrderId = order1Id, name = "Piping Inspection", price = 75.0, quantity = 1, isDone = true)
        )
        workOrderDao.insertPaymentLog(
            PaymentLogEntity(workOrderId = order1Id, amount = 100.0, note = "Initial Deposit")
        )

        // 3. Work Order 2: Today
        val order2Id = workOrderDao.insertWorkOrder(
            WorkOrderEntity(
                customerId = sarahId,
                title = "HVAC Maintenance & Filter Change",
                description = "Annual AC checkup and filter replacement",
                dueDate = today,
                isArchived = false
            )
        )
        workOrderDao.insertWorkItem(
            WorkItemEntity(workOrderId = order2Id, name = "AC Unit Tune-up", price = 150.0, quantity = 1, isDone = false)
        )
        workOrderDao.insertWorkItem(
            WorkItemEntity(workOrderId = order2Id, name = "HEPA Air Filters", price = 45.0, quantity = 2, isDone = false)
        )

        // 4. Work Order 3: Tomorrow
        val order3Id = workOrderDao.insertWorkOrder(
            WorkOrderEntity(
                customerId = apexId,
                title = "Office Electrical Wiring Upgrade",
                description = "Upgrade breaker panel for server room",
                dueDate = tomorrow,
                isArchived = false
            )
        )
        workOrderDao.insertWorkItem(
            WorkItemEntity(workOrderId = order3Id, name = "Install Circuit Breaker Panel", price = 450.0, quantity = 1, isDone = false)
        )
        workOrderDao.insertWorkItem(
            WorkItemEntity(workOrderId = order3Id, name = "Conduit Wiring", price = 120.0, quantity = 3, isDone = false)
        )
        workOrderDao.insertPaymentLog(
            PaymentLogEntity(workOrderId = order3Id, amount = 300.0, note = "Advance Payment")
        )

        // 5. Work Order 4: Later
        val order4Id = workOrderDao.insertWorkOrder(
            WorkOrderEntity(
                customerId = johnId,
                title = "Roofing Leak Inspection",
                description = "Inspect roof tiles around chimney",
                dueDate = fiveDaysLater,
                isArchived = false
            )
        )
        workOrderDao.insertWorkItem(
            WorkItemEntity(workOrderId = order4Id, name = "Roof Sealant Application", price = 250.0, quantity = 1, isDone = false)
        )

        // 6. Work Order 5: Archived
        val order5Id = workOrderDao.insertWorkOrder(
            WorkOrderEntity(
                customerId = sarahId,
                title = "Kitchen Sink Replacement",
                description = "Completed and archived last week",
                dueDate = tenDaysAgo,
                isArchived = true
            )
        )
        workOrderDao.insertWorkItem(
            WorkItemEntity(workOrderId = order5Id, name = "Stainless Steel Sink", price = 220.0, quantity = 1, isDone = true)
        )
        workOrderDao.insertWorkItem(
            WorkItemEntity(workOrderId = order5Id, name = "Plumbing Labor", price = 130.0, quantity = 1, isDone = true)
        )
        workOrderDao.insertPaymentLog(
            PaymentLogEntity(workOrderId = order5Id, amount = 350.0, note = "Paid in full")
        )
    }
}
