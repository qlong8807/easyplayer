package com.easyplayer.app.feature.videopicker.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.easyplayer.app.feature.videopicker.screens.history.HistoryRoute
import kotlinx.serialization.Serializable

@Serializable
data object HistoryRoute

fun NavController.navigateToHistory(navOptions: NavOptions? = null) {
    this.navigate(HistoryRoute, navOptions)
}

fun NavGraphBuilder.historyScreen(
    onNavigateUp: () -> Unit,
    onPlayVideos: (uris: List<Uri>) -> Unit,
) {
    composable<HistoryRoute> {
        HistoryRoute(
            onNavigateUp = onNavigateUp,
            onPlayVideos = onPlayVideos,
        )
    }
}
