package com.example.mywork

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mywork.ui.AppViewModelFactory
import com.example.mywork.ui.customer.CustomerListScreen
import com.example.mywork.ui.customer.CustomerViewModel
import com.example.mywork.ui.main.MainScreen
import com.example.mywork.ui.main.MainViewModel
import com.example.mywork.ui.navigation.Screen
import com.example.mywork.ui.order.WorkOrderDetailScreen
import com.example.mywork.ui.order.WorkOrderDetailViewModel
import com.example.mywork.ui.settings.SettingsScreen
import com.example.mywork.ui.settings.SettingsViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext as MyWorkApplication
    val factory = AppViewModelFactory(context.repository)

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            val viewModel: MainViewModel = viewModel(factory = factory)
            MainScreen(
                viewModel = viewModel,
                onNavigateToCustomers = {
                    navController.navigate(Screen.Customers.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToWorkOrder = { orderId ->
                    navController.navigate(Screen.WorkOrderDetail.createRoute(orderId))
                }
            )
        }

        composable(Screen.Customers.route) {
            val viewModel: CustomerViewModel = viewModel(factory = factory)
            CustomerListScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.WorkOrderDetail.route,
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            val viewModel: WorkOrderDetailViewModel = viewModel(factory = factory)
            WorkOrderDetailScreen(
                orderId = orderId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
