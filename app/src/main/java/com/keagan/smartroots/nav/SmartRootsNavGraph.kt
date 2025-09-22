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
const val ROUTE_PLANNER = "planner/{tent}"   // "fodder" | "veg"
const val ROUTE_HARVEST = "harvest"

// ✅ NEW routes
const val ROUTE_FACE_SCAN = "facescan?next={next}"
const val ROUTE_CHAT_FRED = "chatfred"
const val ROUTE_TALK_FRED = "talkfred"

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
                            // go to FaceScan first, then continue to the intended home
                            val next = "home/$mode"
                            nav.navigate("facescan?next=$next") {
                                popUpTo(ROUTE_SPLASH) { inclusive = false }
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
                onOpenMetric = { key ->
                    when (key) {
                        "chat_fred" -> nav.navigate(ROUTE_CHAT_FRED)
                        "talk_fred" -> nav.navigate(ROUTE_TALK_FRED)
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
            DetailScreen(metricKey = metric, onBack = { nav.popBackStack() })
        }

        composable(
            route = ROUTE_PLANNER,
            arguments = listOf(navArgument("tent") { type = NavType.StringType })
        ) { backStackEntry ->
            val tent = backStackEntry.arguments?.getString("tent") ?: "fodder"
            PlannerScreen(tent = tent, onBack = { nav.popBackStack() })
        }

        composable(ROUTE_HARVEST) {
            HarvestScreen(onBack = { nav.popBackStack() })
        }

        // ✅ NEW: Face scan mock
        composable(
            route = "facescan?next={next}",
            arguments = listOf(navArgument("next") { type = NavType.StringType; defaultValue = "home/veg" })
        ) { backStackEntry ->
            val next = backStackEntry.arguments?.getString("next") ?: "home/veg"
            FaceScanScreen(nextRoute = next) { go ->
                nav.navigate(go) {
                    popUpTo(ROUTE_SPLASH) { inclusive = true }
                }
            }
        }

        // ✅ NEW: Chat with Fred
        composable(ROUTE_CHAT_FRED) {
            ChatFredScreen(onBack = { nav.popBackStack() })
        }

        // ✅ NEW: Talk with Fred (voice prompt)
        composable(ROUTE_TALK_FRED) {
            TalkFredScreen(onBack = { nav.popBackStack() })
        }
    }
}
