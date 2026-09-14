package com.example.mywork.ui.main

import com.example.mywork.data.CustomerDao
import com.example.mywork.data.CustomerEntity
import com.example.mywork.data.PaymentLogEntity
import com.example.mywork.data.WorkItemEntity
import com.example.mywork.data.WorkOrderDao
import com.example.mywork.data.WorkOrderEntity
import com.example.mywork.data.WorkOrderWithDetails
import com.example.mywork.data.WorkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeCustomerDao : CustomerDao {
        override suspend fun insertCustomer(customer: CustomerEntity): Long = 1L
        override suspend fun updateCustomer(customer: CustomerEntity) {}
        override suspend fun deleteCustomer(customer: CustomerEntity) {}
        override fun getAllCustomers(): Flow<List<CustomerEntity>> = MutableStateFlow(emptyList())
        override fun getCustomerById(id: Long): Flow<CustomerEntity?> = MutableStateFlow(null)
        override suspend fun getCustomerByIdDirect(id: Long): CustomerEntity? = null
    }

    private class FakeWorkOrderDao(
        val ordersFlow: MutableStateFlow<List<WorkOrderWithDetails>>
    ) : WorkOrderDao {
        override suspend fun insertWorkOrder(workOrder: WorkOrderEntity): Long = 1L
        override suspend fun updateWorkOrder(workOrder: WorkOrderEntity) {}
        override suspend fun deleteWorkOrder(workOrder: WorkOrderEntity) {}
        override suspend fun updateWorkOrderArchiveStatus(orderId: Long, isArchived: Boolean) {}
        override suspend fun getWorkOrderEntityById(id: Long): WorkOrderEntity? = null
        override suspend fun insertWorkItem(workItem: WorkItemEntity): Long = 1L
        override suspend fun updateWorkItem(workItem: WorkItemEntity) {}
        override suspend fun deleteWorkItem(workItem: WorkItemEntity) {}
        override suspend fun updateWorkItemDoneStatus(itemId: Long, isDone: Boolean) {}
        override suspend fun insertPaymentLog(paymentLog: PaymentLogEntity): Long = 1L
        override suspend fun deletePaymentLog(paymentLog: PaymentLogEntity) {}
        override fun getAllWorkOrdersWithDetails(): Flow<List<WorkOrderWithDetails>> = ordersFlow
        override fun getWorkOrderWithDetailsById(id: Long): Flow<WorkOrderWithDetails?> = MutableStateFlow(null)
        override suspend fun getWorkOrderWithDetailsByIdDirect(id: Long): WorkOrderWithDetails? = null
    }

    @Test
    fun testOverdueSectionIncludedWhenOverdueWorkExists() = runTest {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -2) // 2 days ago
        val pastDueDate = cal.timeInMillis

        val overdueOrder = WorkOrderWithDetails(
            workOrder = WorkOrderEntity(id = 1, customerId = null, title = "Overdue Repair", dueDate = pastDueDate, isArchived = false),
            customer = null,
            items = listOf(WorkItemEntity(id = 10, workOrderId = 1, name = "Fix leakage", price = 100.0, isDone = false)),
            payments = emptyList()
        )

        val ordersFlow = MutableStateFlow(listOf(overdueOrder))
        val repository = WorkRepository(FakeCustomerDao(), FakeWorkOrderDao(ordersFlow))
        val viewModel = MainViewModel(repository)

        backgroundScope.launch { viewModel.categorizedWorkOrders.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.categorizedWorkOrders.value

        assertTrue(result.any { it.section == DateSection.OVERDUE })
        val overdueSection = result.first { it.section == DateSection.OVERDUE }
        assertEquals(1, overdueSection.orders.size)
        assertEquals("Overdue Repair", overdueSection.orders.first().workOrder.title)
    }

    @Test
    fun testOverdueSectionHiddenWhenNoOverdueWorkExists() = runTest {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
        val futureDueDate = cal.timeInMillis

        val upcomingOrder = WorkOrderWithDetails(
            workOrder = WorkOrderEntity(id = 1, customerId = null, title = "Future Task", dueDate = futureDueDate, isArchived = false),
            customer = null,
            items = listOf(WorkItemEntity(id = 10, workOrderId = 1, name = "Inspect", price = 50.0, isDone = false)),
            payments = emptyList()
        )

        val ordersFlow = MutableStateFlow(listOf(upcomingOrder))
        val repository = WorkRepository(FakeCustomerDao(), FakeWorkOrderDao(ordersFlow))
        val viewModel = MainViewModel(repository)

        backgroundScope.launch { viewModel.categorizedWorkOrders.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.categorizedWorkOrders.value

        assertFalse(result.any { it.section == DateSection.OVERDUE })
    }

    @Test
    fun testArchivedOrdersHiddenInActiveView() = runTest {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -2) // 2 days ago
        val pastDueDate = cal.timeInMillis

        val archivedOrder = WorkOrderWithDetails(
            workOrder = WorkOrderEntity(id = 1, customerId = null, title = "Archived Repair", dueDate = pastDueDate, isArchived = true),
            customer = null,
            items = listOf(WorkItemEntity(id = 10, workOrderId = 1, name = "Fix leakage", price = 100.0, isDone = false)),
            payments = emptyList()
        )

        val ordersFlow = MutableStateFlow(listOf(archivedOrder))
        val repository = WorkRepository(FakeCustomerDao(), FakeWorkOrderDao(ordersFlow))
        val viewModel = MainViewModel(repository)

        backgroundScope.launch { viewModel.categorizedWorkOrders.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.categorizedWorkOrders.value

        assertFalse(result.any { it.section == DateSection.OVERDUE })
        assertTrue(result.all { section -> section.orders.none { it.workOrder.id == 1L } })
    }

    @Test
    fun testArchivedOrdersShownInArchivedView() = runTest {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -2) // 2 days ago
        val pastDueDate = cal.timeInMillis

        val archivedOrder = WorkOrderWithDetails(
            workOrder = WorkOrderEntity(id = 1, customerId = null, title = "Archived Repair", dueDate = pastDueDate, isArchived = true),
            customer = null,
            items = emptyList(),
            payments = emptyList()
        )

        val ordersFlow = MutableStateFlow(listOf(archivedOrder))
        val repository = WorkRepository(FakeCustomerDao(), FakeWorkOrderDao(ordersFlow))
        val viewModel = MainViewModel(repository)

        backgroundScope.launch { viewModel.categorizedWorkOrders.collect {} }
        viewModel.setFilter(OrderFilter.ARCHIVED)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.categorizedWorkOrders.value

        assertEquals(1, result.size)
        assertEquals(DateSection.ARCHIVED, result.first().section)
        assertEquals("Archived Repair", result.first().orders.first().workOrder.title)
    }
}
