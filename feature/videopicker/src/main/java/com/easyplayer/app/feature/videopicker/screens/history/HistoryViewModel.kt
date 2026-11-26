package com.easyplayer.app.feature.videopicker.screens.history

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyplayer.app.core.data.repository.MediaRepository
import com.easyplayer.app.core.data.repository.PreferencesRepository
import com.easyplayer.app.core.domain.GetPlaybackHistoryUseCase
import com.easyplayer.app.core.domain.GetSortedVideosUseCase
import com.easyplayer.app.core.model.MediaViewMode
import com.easyplayer.app.core.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getPlaybackHistoryUseCase: GetPlaybackHistoryUseCase,
    private val getSortedVideosUseCase: GetSortedVideosUseCase,
    private val mediaRepository: MediaRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val uiStateInternal = MutableStateFlow(HistoryUiState())
    val uiState = uiStateInternal.asStateFlow()

    private val eventsInternal = Channel<HistoryEvent>()
    val events = eventsInternal.receiveAsFlow()

    init {
        collectHistory()
    }

    private fun collectHistory() {
        viewModelScope.launch {
            getPlaybackHistoryUseCase().collect { videos ->
                uiStateInternal.update { it.copy(videos = videos) }
            }
        }
    }

    fun playVideo(video: Video) {
        viewModelScope.launch {
            val mediaViewMode = preferencesRepository.applicationPreferences.value.mediaViewMode
            val sourcePath = video.parentPath.takeIf { it.isNotBlank() }
            val videos = when (mediaViewMode) {
                MediaViewMode.VIDEOS -> getSortedVideosUseCase().first()
                MediaViewMode.FOLDERS -> getSortedVideosUseCase(sourcePath).first()
                    .filter { it.parentPath == video.parentPath }
                MediaViewMode.FOLDER_TREE -> getSortedVideosUseCase(sourcePath).first()
            }
            val currentUri = video.uriString.toUri()
            val uris = videos.map { it.uriString.toUri() }.let { playlist ->
                if (playlist.contains(currentUri)) playlist else listOf(currentUri) + playlist
            }
            eventsInternal.send(HistoryEvent.PlayVideos(uris))
        }
    }

    fun removeFromHistory(uri: String) {
        viewModelScope.launch {
            mediaRepository.removeFromPlaybackHistory(uri)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            mediaRepository.clearPlaybackHistory()
        }
    }
}

data class HistoryUiState(
    val videos: List<Video> = emptyList(),
)

sealed interface HistoryEvent {
    data class PlayVideos(val uris: List<Uri>) : HistoryEvent
}
