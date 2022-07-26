package com.krykun.movieapp.feature.playlist.main.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.krykun.domain.model.local.Playlist
import com.krykun.domain.usecase.local.GetAllPlaylistsUseCase
import com.krykun.domain.usecase.local.GetPlaylistMoviesByLimit
import com.krykun.movieapp.base.BaseViewModel
import com.krykun.movieapp.state.AppState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    appState: MutableStateFlow<AppState>,
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    private val getPlaylistMoviesByLimit: GetPlaylistMoviesByLimit
) : BaseViewModel<PlaylistSideEffects>(appState) {

    init {
        getAllPlaylists()
    }

    val playlistState = mutableStateOf(listOf<Playlist>())

    private fun getAllPlaylists() = intent {
        viewModelScope.launch {
            getAllPlaylistsUseCase.getAllPlaylistsFlow()
                .collect {
                    reduce {
                        state.value = state.value.copy(
                            playlistState = state.value.playlistState.copy(
                                playlists = it
                            )
                        )
                        state
                    }
                    if (isSomePlaylistsHaveItems(it)) {
                        postSideEffect(PlaylistSideEffects.UpdatePlaylist(it))
                    } else {
                        postSideEffect(PlaylistSideEffects.UpdatePlaylist(listOf()))
                    }
                }
        }
    }

    private fun isSomePlaylistsHaveItems(allPlaylists: List<Playlist>): Boolean {
        var isPlaylistNotEmpty = false
        run breaking@{
            allPlaylists.forEach {
                isPlaylistNotEmpty = it.movieList.isNotEmpty()
                if (isPlaylistNotEmpty) {
                    return@breaking
                }
            }
        }
        return isPlaylistNotEmpty
    }

    fun navigateToPlaylistDetails(playlistId: Long) = intent {
        reduce {
            state.value = state.value.copy(
                playlistState = state.value.playlistState.copy(
                    playlistDetailsState = state.value.playlistState.playlistDetailsState.copy(
                        playlistId = playlistId
                    )
                )
            )
            state
        }
        postSideEffect(PlaylistSideEffects.NavigateToPlaylistDetails)
    }

//    fun subscribeToState() =
//        container.stateFlow.value.takeWhenChanged {
//            it.playlistState
//        }.map {
//            if (isSomePlaylistsHaveItems(it.playlists)) {
//                it
//            } else {
//                it.copy(playlists = listOf())
//            }
//        }
}