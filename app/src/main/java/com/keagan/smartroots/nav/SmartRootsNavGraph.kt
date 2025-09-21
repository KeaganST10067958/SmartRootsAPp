package com.keagan.smartroots.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keagan.smartroots.data.Prefs
import com.keagan.smartroots.model.AppState
import com.keagan.smartroots.screens.*

const val ROUTE_SPLASH = "splash"
const val ROUTE_HOME = "home/{mode}"
const val ROUTE_DETAIL = "detail/{metric}"

// NEW:
const val ROUTE_PLANNER = "planner/{tent}"   // "fodder" | "veg"
const val ROUTE_HARVEST = "harvest"

@Composable
fun SmartRootsNavGraph(
    nav: NavHostController,
    prefs: Prefs,
    app: AppState
) {
    NavHost(navController = nav, startDestination = ROUTE_SPLASH) {

        composable(ROUTE_SPLASH) {
            SplashScreen(
                prefs = prefs,
                onSelectMode = { mode ->
                    when (mode) {
                        "planner_fodder" -> nav.navigate("planner/fodder")
                        "planner_veg" -> nav.navigate("planner/veg")
                        "harvest" -> nav.navigate(ROUTE_HARVEST)
                        else -> {
                            nav.navigate("home/$mode") {
                                popUpTo(ROUTE_SPLASH) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(
            route = ROUTE_HOME,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "veg"
            HomeScreen(
                mode = mode,
                app = app,
                onBack = {
                    nav.navigate(ROUTE_SPLASH) {
                        popUpTo(ROUTE_HOME) { inclusive = true }
                    }
                },
                onOpenMetric = { key -> nav.navigate("detail/$key") }
            )
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("metric") { type = NavType.StringType })
        ) { backStackEntry ->
            val metric = backStackEntry.arguments?.getString("metric").orEmpty()
            DetailScreen(metricKey = metric, onBack = { nav.popBackStack() })
        }

        // NEW Planner
        composable(
            route = ROUTE_PLANNER,
            arguments = listOf(navArgument("tent") { type = NavType.StringType })
        ) { backStackEntry ->
            val tent = backStackEntry.arguments?.getString("tent") ?: "fodder"
            PlannerScreen(tent = tent, onBack = { nav.popBackStack() })
        }

        // NEW Harvest
        composable(ROUTE_HARVEST) {
            HarvestScreen(onBack = { nav.popBackStack() })
        }
    }
}
