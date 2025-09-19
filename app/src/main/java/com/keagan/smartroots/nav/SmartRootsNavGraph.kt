package com.keagan.smartroots.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keagan.smartroots.data.Prefs
import com.keagan.smartroots.model.AppState
import com.keagan.smartroots.screens.DetailScreen
import com.keagan.smartroots.screens.HomeScreen
import com.keagan.smartroots.screens.SplashScreen

const val ROUTE_SPLASH = "splash"
const val ROUTE_HOME = "home/{mode}"          // "veg" | "fodder"
const val ROUTE_DETAIL = "detail/{metric}"

@Composable
fun SmartRootsNavGraph(
    nav: NavHostController,
    prefs: Prefs,
    app: AppState                                  // ✅ receive shared state
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
                app = app,                              // ✅ pass to Home
                onBack = {
                    nav.navigate(ROUTE_SPLASH) {
                        popUpTo(ROUTE_HOME) { inclusive = true }
                    }
                },
                onOpenMetric = { key ->
                    nav.navigate("detail/$key")
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
    }
}
