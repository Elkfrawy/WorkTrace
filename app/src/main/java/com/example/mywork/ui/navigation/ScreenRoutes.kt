package com.example.mywork.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object Customers : Screen("customers_screen")
    object Settings : Screen("settings_screen")
    object WorkOrderDetail : Screen("work_order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "work_order_detail/$orderId"
    }
}
