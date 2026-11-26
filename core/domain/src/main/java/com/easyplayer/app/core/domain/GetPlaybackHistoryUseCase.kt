package com.easyplayer.app.core.domain

import com.easyplayer.app.core.common.Dispatcher
import com.easyplayer.app.core.common.NextDispatchers
import com.easyplayer.app.core.model.Video
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for retrieving videos in playback history, newest first.
 */
class GetPlaybackHistoryUseCase @Inject constructor(
    private val getSortedVideosUseCase: GetSortedVideosUseCase,
    @Dispatcher(NextDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {

    operator fun invoke(): Flow<List<Video>> {
        return getSortedVideosUseCase().map { videos ->
            videos
                .filter { it.lastPlayedAt != null }
                .sortedByDescending { it.lastPlayedAt?.time }
        }.flowOn(defaultDispatcher)
    }
}
