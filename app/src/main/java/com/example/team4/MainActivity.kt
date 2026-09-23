package com.example.team4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.team4.ui.navigation.Screen
import com.example.team4.ui.screen.DashboardScreen
import com.example.team4.ui.screen.ExpenseScreen
import com.example.team4.ui.screen.ImportExportScreen
import com.example.team4.ui.screen.StudentDetailScreen
import com.example.team4.ui.screen.StudentListScreen
import com.example.team4.ui.theme.TEAM4Theme
import com.example.team4.util.ExportService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var exportService: ExportService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TEAM4Theme(dynamicColor = false) {
                MainScreen(exportService)
            }
        }
    }
}

@Composable
fun MainScreen(exportService: ExportService) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        NavigationItem("Home", Screen.Dashboard, Icons.Default.Home),
        NavigationItem("Students", Screen.Students, Icons.Default.Groups),
        NavigationItem("Expenses", Screen.Expenses, Icons.Default.Description),
        NavigationItem("Reports", Screen.ImportExport, Icons.AutoMirrored.Filled.FactCheck)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(item.screen::class) } == true,
                        onClick = {
                            navController.navigate(item.screen) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable<Screen.Dashboard> {
                DashboardScreen(hiltViewModel())
            }
            composable<Screen.Students> {
                StudentListScreen(
                    viewModel = hiltViewModel(),
                    onStudentClick = { id ->
                        navController.navigate(Screen.StudentDetail(id))
                    }
                )
            }
            composable<Screen.StudentDetail> { backStackEntry ->
                val route: Screen.StudentDetail = backStackEntry.toRoute()
                StudentDetailScreen(
                    studentId = route.studentId,
                    viewModel = hiltViewModel(),
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<Screen.Expenses> {
                ExpenseScreen(hiltViewModel())
            }
            composable<Screen.ImportExport> {
                ImportExportScreen(
                    studentViewModel = hiltViewModel(),
                    expenseViewModel = hiltViewModel(),
                    dashboardViewModel = hiltViewModel(),
                    exportService = exportService
                )
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val screen: Any,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
