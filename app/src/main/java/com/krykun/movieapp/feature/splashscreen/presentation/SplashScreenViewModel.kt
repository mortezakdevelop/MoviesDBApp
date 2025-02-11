package com.krykun.movieapp.feature.splashscreen.presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.krykun.domain.model.local.Playlist
import com.krykun.domain.usecase.local.AddPlaylistUseCase
import com.krykun.domain.usecase.local.GetAllPlaylistsUseCase
import com.krykun.domain.usecase.remote.moviedetails.GetMovieGenresUseCase
import com.krykun.domain.usecase.remote.tvdetails.GetTvGenresUseCase
import com.krykun.movieapp.R
import com.krykun.movieapp.base.BaseViewModel
import com.krykun.movieapp.state.AppState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class SplashScreenViewModel @Inject constructor(
    private val context: Context,
    appState: MutableStateFlow<AppState>,
    private val getMovieGenresUseCase: GetMovieGenresUseCase,
    private val getTvGenresUseCase: GetTvGenresUseCase,
    private val addPlaylistUseCase: AddPlaylistUseCase,
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase
) : BaseViewModel<SplashScreenSideEffect>(appState) {

    /* It's a delay that is used to show the splash screen for 1 second. */
    private val splashDelay = 1000L

    /* It's a flag that is used to start the animation in the `SplashScreen` */
    val startAnimFlag = mutableStateOf(false)

    init {
        setScreenOpen()
        makeInitialDelay()
    }

    /**
     * > The function `setScreenOpen` is a private function that returns an intent that reduces the
     * state by copying the current state and setting the `isScreenOpen` property of the
     * `splashScreenState` property of the state to `true`
     */
    private fun setScreenOpen() = intent {
        reduce {
            state.value =
                state.value.copy(splashScreenState = state.value.splashScreenState.copy(isScreenOpen = true))
            state
        }
    }

    /**
     * It inserts the default playlists if they don't exist, and then calls the callback function
     *
     * @param callback SimpleSyntax<MutableStateFlow<AppState>, SplashScreenSideEffect>.() -> Unit
     */
    private fun insertDefaultPlaylist(callback: SimpleSyntax<MutableStateFlow<AppState>, SplashScreenSideEffect>.() -> Unit) =
        intent {
            var job: Job? = null
            job = viewModelScope.launch(Dispatchers.IO) {
                val allPlaylists = getAllPlaylistsUseCase.getAllPlaylists()
                insertPlaylist(allPlaylists, this@intent, PlaylistType.MOVIE)
                insertPlaylist(allPlaylists, this@intent, PlaylistType.TVSERIES)
                if (allPlaylists.isNotEmpty()) {
                    reduce {
                        state.value = state.value.copy(
                            playlistState = state.value.playlistState.copy(
                                playlists = state.value.playlistState.playlists + allPlaylists
                            )
                        )
                        state
                    }
                }
                callback(this@intent)
                job?.cancel()

            }
        }

    /* An enum class that has two values: MOVIE and TVSERIES. */
    private enum class PlaylistType {
        MOVIE,
        TVSERIES
    }

    /**
     * It checks if the favourite playlist exists, if it doesn't, it creates it and adds it to the
     * state
     *
     * @param it List<Playlist> - the list of playlists that are currently in the database
     * @param intent SimpleSyntax<MutableStateFlow<AppState>, SplashScreenSideEffect>
     * @param type PlaylistType
     */
    private suspend fun insertPlaylist(
        it: List<Playlist>,
        intent: SimpleSyntax<MutableStateFlow<AppState>, SplashScreenSideEffect>,
        type: PlaylistType
    ) {
        var playlist: Playlist? = null
        var playlistInsertResult: Long = 0

        if (it.find {
                when (type) {
                    PlaylistType.MOVIE -> it.name == context.getString(R.string.favourite_movies)
                    PlaylistType.TVSERIES -> it.name == context.getString(R.string.favourite_tv_series)
                }
            } == null) {
            playlist = Playlist(
                name = when (type) {
                    PlaylistType.MOVIE -> context.getString(R.string.favourite_movies)
                    PlaylistType.TVSERIES -> context.getString(R.string.favourite_tv_series)
                },
                movieList = listOf()
            )
            playlistInsertResult =
                addPlaylistUseCase.addPlaylist(playlist)
        }

        if (playlistInsertResult >= 1) {
            playlist?.copy(
                playlistId = playlistInsertResult
            )?.let {
                intent.reduce {
                    state.value = state.value.copy(
                        playlistState = state.value.playlistState.copy(
                            playlists = state.value.playlistState.playlists + it

                        )
                    )
                    state
                }
            }
        }
    }

    /**
     * We're making a network call to get the movie genres and tv genres, and if the response is
     * successful, we're updating the state with the genres and then moving to the next screen
     */
    private fun makeInitialDelay() {
        insertDefaultPlaylist {
            viewModelScope.launch(Dispatchers.IO) {
                val response = getMovieGenresUseCase.getMovieGenres()
                val tvResponse = getTvGenresUseCase.getTvGenres()
                if (response.isSuccess && tvResponse.isSuccess) {
                    reduce {
                        state.value = state.value.copy(
                            baseMoviesState = state.value.baseMoviesState.copy(
                                genres = (response.getOrNull() ?: listOf()) +
                                        (tvResponse.getOrNull() ?: listOf()),
                            )
                        )
                        state
                    }
                    delay(splashDelay)
                    postSideEffect(SplashScreenSideEffect.MoveToNextScreen)
                }
            }
        }
    }
}