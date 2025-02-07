package com.krykun.movieapp.feature.moviedetails.presentation

import com.krykun.domain.model.remote.moviedetails.MovieDetails

sealed class MovieDetailsSideEffects {
    data class ShowMovieData(val movieDetails: MovieDetails?) : MovieDetailsSideEffects()
    object ShowLoadingState : MovieDetailsSideEffects()
    object ShowErrorState : MovieDetailsSideEffects()
    object OpenPlaylistSelector : MovieDetailsSideEffects()
    object TryReloadRecommendationsPage : MovieDetailsSideEffects()
    data class NavigateToMovie(val movieId: Int) : MovieDetailsSideEffects()
}