package com.easyplayer.app.feature.videopicker.screens.history

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easyplayer.app.core.model.Video
import com.easyplayer.app.core.ui.R
import com.easyplayer.app.core.ui.components.NextDialogWithDoneAndCancelButtons
import com.easyplayer.app.core.ui.components.NextSegmentedListItem
import com.easyplayer.app.core.ui.components.NextTopAppBar
import com.easyplayer.app.core.ui.designsystem.NextIcons
import com.easyplayer.app.core.ui.extensions.plus
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel = hiltViewModel(),
    onPlayVideos: (uris: List<Uri>) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HistoryEvent.PlayVideos -> onPlayVideos(event.uris)
            }
        }
    }

    HistoryScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onVideoClick = viewModel::playVideo,
        onRemoveVideo = viewModel::removeFromHistory,
        onClearHistory = viewModel::clearHistory,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    onNavigateUp: () -> Unit = {},
    onVideoClick: (Video) -> Unit = {},
    onRemoveVideo: (uri: String) -> Unit = {},
    onClearHistory: () -> Unit = {},
) {
    var showClearConfirmation by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NextTopAppBar(
                title = stringResource(R.string.playback_history),
                navigationIcon = {
                    FilledTonalIconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = NextIcons.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_up),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClearConfirmation = true }) {
                        Icon(
                            imageVector = NextIcons.DeleteSweep,
                            contentDescription = stringResource(R.string.clear_playback_history),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp) + padding,
        ) {
            items(
                items = uiState.videos,
                key = { it.uriString },
            ) { video ->
                HistoryVideoItem(
                    video = video,
                    onClick = { onVideoClick(video) },
                    onRemove = { onRemoveVideo(video.uriString) },
                )
            }

            if (uiState.videos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = NextIcons.History,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.no_playback_history),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmation) {
        NextDialogWithDoneAndCancelButtons(
            title = stringResource(R.string.clear_playback_history),
            onDoneClick = {
                onClearHistory()
                showClearConfirmation = false
            },
            onDismissClick = { showClearConfirmation = false },
            content = {
                Text(text = stringResource(R.string.clear_playback_history_confirmation))
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HistoryVideoItem(
    video: Video,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val playedAt = remember(video.lastPlayedAt?.time) {
        video.lastPlayedAt?.let { date ->
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
        }
    }

    NextSegmentedListItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        onClick = onClick,
        leadingContent = {
            Icon(
                imageVector = NextIcons.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = NextIcons.Close,
                    contentDescription = stringResource(R.string.remove_from_playback_history),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        content = {
            Column {
                Text(
                    text = video.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = video.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (playedAt != null) {
                    Text(
                        text = playedAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}
