package com.krykun.movieapp.feature.moviedetails

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.krykun.domain.model.remote.MovieDiscoverItem
import com.krykun.movieapp.ext.collectAndHandleState
import com.krykun.movieapp.feature.moviedetails.presentation.MovieDetailsSideEffects
import com.krykun.movieapp.feature.moviedetails.presentation.MovieDetailsViewModel
import com.krykun.movieapp.feature.moviedetails.view.BaseMovieDetailsView
import com.krykun.movieapp.feature.moviedetails.view.ErrorView
import com.krykun.movieapp.feature.moviedetails.view.LoadingView
import com.krykun.movieapp.feature.playlistselect.presentation.PlaylistSelectViewModel
import com.krykun.movieapp.state.DetailsState
import com.krykun.movieapp.state.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalMotionApi
@Composable
fun MovieDetailsView(
    viewModel: MovieDetailsViewModel = hiltViewModel(),
    navHostController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val recommendedMovies =
        viewModel.getDiscoverMovies.collectAndHandleState(viewModel::handleLoadState)
    val lazyListState = rememberLazyListState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val playlistSelectViewModel: PlaylistSelectViewModel = hiltViewModel()

    Crossfade(targetState = viewModel.movieDetailsState.value) {
        when (it) {
            DetailsState.LOADING -> {
                LoadingView()
            }
            DetailsState.DEFAULT -> {
                BaseMovieDetailsView(
                    navHostController = navHostController,
                    viewModel = viewModel,
                    bottomSheetState = bottomSheetState,
                    playlistSelectViewModel = playlistSelectViewModel,
                    recommendedMovies = recommendedMovies,
                    lazyListState = lazyListState
                )
            }
            DetailsState.ERROR -> {
                ErrorView()
            }
        }
    }

    viewModel.collectSideEffect {
        handleSideEffects(
            sideEffects = it,
            scope = scope,
            bottomSheetState = bottomSheetState,
            movies = recommendedMovies,
            viewModel = viewModel
        )
    }

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.clearSelectState()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    //TODO remove this when HorizontalPager will remember scroll position when recomposing
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.setLastScrolledPage(lazyListState.firstVisibleItemIndex)
                viewModel.setScrollOffset(lazyListState.firstVisibleItemScrollOffset)
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    //TODO remove this when HorizontalPager will remember scroll position when recomposing
    LaunchedEffect(key1 = recommendedMovies.loadState.refresh) {
        if (recommendedMovies.loadState.refresh is LoadState.NotLoading) {
            viewModel.getCurrentPageAndScrollOffset()
            viewModel.setLoadingState(LoadingState.STATIONARY)
        } else if (recommendedMovies.loadState.refresh is LoadState.Loading) {
            viewModel.setLoadingState(LoadingState.LOADING)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun handleSideEffects(
    sideEffects: MovieDetailsSideEffects,
    scope: CoroutineScope,
    bottomSheetState: ModalBottomSheetState,
    movies: LazyPagingItems<MovieDiscoverItem>,
    viewModel: MovieDetailsViewModel,
) {
    when (sideEffects) {
        is MovieDetailsSideEffects.ShowLoadingState -> {
            viewModel.movieDetailsState.value =
                DetailsState.LOADING
        }
        is MovieDetailsSideEffects.ShowErrorState -> {
            viewModel.movieDetailsState.value =
                DetailsState.ERROR
        }
        is MovieDetailsSideEffects.ShowMovieData -> {
            viewModel.movieData.value = sideEffects.movieDetails
            viewModel.movieDetailsState.value = DetailsState.DEFAULT
            scope.launch {
                delay(300)
                viewModel.isRatingVisible.value = true
            }
        }
        is MovieDetailsSideEffects.OpenPlaylistSelector -> {
            scope.launch {
                bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
            }
        }
        is MovieDetailsSideEffects.TryReloadRecommendationsPage -> {
            movies.retry()
        }
    }
}
