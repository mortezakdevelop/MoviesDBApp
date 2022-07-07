package com.krykun.movieapp.feature.moviedetails.presentation

import androidx.lifecycle.ViewModel
import com.krykun.domain.usecase.GetCastDetailsUseCase
import com.krykun.domain.usecase.GetMovieDetailsUseCase
import com.krykun.movieapp.state.AppState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    appState: MutableStateFlow<AppState>,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val getCastDetailsUseCase: GetCastDetailsUseCase
) : ViewModel(),
    ContainerHost<MutableStateFlow<AppState>, MovieDetailsSideEffects> {
    override val container =
        container<MutableStateFlow<AppState>, MovieDetailsSideEffects>(appState)

    init {
        loadMovieDetails()
    }

    private fun loadMovieDetails() = intent {
        postSideEffect(MovieDetailsSideEffects.ShowLoadingState)
        val castResult = getCastDetailsUseCase.getCastDetails(state.value.movieDetailsState.movieId)
        val result = getMovieDetailsUseCase.getMovieDetail(state.value.movieDetailsState.movieId)
            .copy(cast = castResult)

//        if (result) {
//
//        } else {
//            postSideEffect(MovieDetailsSideEffects.ShowErrorState)
//        }
        reduce {
            state.value = state.value.copy(
                movieDetailsState = MovieDetailsState(
                    movieData = result
                )
            )
            state
        }
        postSideEffect(MovieDetailsSideEffects.ShowMovieData(result))
    }

}