package com.keagan.smartroots.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keagan.smartroots.data.Prefs
import com.keagan.smartroots.model.AppState
import com.keagan.smartroots.screens.CropPlannerScreen
import com.keagan.smartroots.screens.DetailScreen
import com.keagan.smartroots.screens.HarvestScreen
import com.keagan.smartroots.screens.HomeScreen
import com.keagan.smartroots.screens.SplashScreen

const val ROUTE_SPLASH = "splash"
const val ROUTE_HOME = "home/{mode}"          // "veg" | "fodder"
const val ROUTE_DETAIL = "detail/{metric}"
const val ROUTE_HARVEST = "harvest"
const val ROUTE_PLANNER = "planner/{mode}"

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
                    nav.navigate("home/$mode") {
                        popUpTo(ROUTE_SPLASH) { inclusive = true }
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
                onOpenMetric = { key ->
                    when (key) {
                        "harvest" -> nav.navigate(ROUTE_HARVEST)
                        "planner" -> nav.navigate("planner/$mode")
                        else -> nav.navigate("detail/$key")
                    }
                }
            )
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("metric") { type = NavType.StringType })
        ) { backStackEntry ->
            val metric = backStackEntry.arguments?.getString("metric").orEmpty()
            DetailScreen(
                metricKey = metric,
                onBack = { nav.popBackStack() }
            )
        }

        composable(ROUTE_HARVEST) {
            HarvestScreen(onBack = { nav.popBackStack() })
        }

        composable(
            route = ROUTE_PLANNER,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { bse ->
            CropPlannerScreen(
                mode = bse.arguments?.getString("mode") ?: "veg",
                onBack = { nav.popBackStack() }
            )
        }
    }
}
