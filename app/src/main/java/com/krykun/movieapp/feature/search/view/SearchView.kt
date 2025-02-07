package com.krykun.movieapp.feature.search.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.krykun.domain.model.remote.search.SearchItem
import com.krykun.movieapp.R
import com.krykun.movieapp.custom.EmptyTextToolbar
import com.krykun.movieapp.ext.clearFocusOnKeyboardDismiss
import com.krykun.movieapp.ext.collectAndHandleState
import com.krykun.movieapp.feature.search.presentation.SearchSideEffects
import com.krykun.movieapp.feature.search.presentation.SearchViewModel
import com.krykun.movieapp.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchView(
    viewModel: SearchViewModel,
    navHostController: NavHostController,
    innerPadding: PaddingValues
) {

    var searchResults =
        viewModel.searchResults?.collectAndHandleState(viewModel::handleLoadSearchItemsState)

    val queryIsEmpty = remember { mutableStateOf(false) }
    val markIsModified = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    val query = remember { mutableStateOf("") }
    val scrollState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    if (markIsModified.value) {
        markIsModified.value = false
        searchResults =
            viewModel.searchResults?.collectAndHandleState(viewModel::handleLoadSearchItemsState)
    }
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    viewModel.collectSideEffect {
        handleSideEffects(
            sideEffects = it,
            markIsModified = markIsModified,
            searchResults = searchResults,
            isLoading = isLoading,
            navHostController = navHostController,
            query = query,
            scope = scope,
            scrollState = scrollState
        )
    }

    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                SearchBar(
                    queryIsEmpty = queryIsEmpty,
                    viewModel = viewModel,
                    query = query
                )
                if (isLoading.value) {
                    LoadingView()
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(minSize = 128.dp),
                        state = scrollState
                    ) {
                        items(count = searchResults?.itemCount ?: 0) { index ->
                            Box(modifier = Modifier.padding(8.dp)) {
                                searchResults?.get(index)?.let {
                                    SearchItemView(
                                        searchItem = it,
                                        viewModel = viewModel,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (searchResults?.itemCount == 0 && !isLoading.value) {
                Loader(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    //TODO remove this when HorizontalPager will remember scroll position when recomposing
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.setLastScrolledPage(scrollState.firstVisibleItemIndex)
                viewModel.setScrollOffset(scrollState.firstVisibleItemScrollOffset)
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = searchResults?.loadState?.refresh) {
        if (searchResults?.loadState?.refresh is LoadState.NotLoading) {
            viewModel.setIsLoading(false)
            viewModel.setSavedQuery()
            viewModel.getCurrentPageAndScrollOffset()
        } else if (searchResults?.loadState?.refresh is LoadState.Loading) {
            if (!queryIsEmpty.value) {
                viewModel.setIsLoading(true)
            }
        }
    }
}

@Composable
fun Loader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.search))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.make_search),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchBar(
    queryIsEmpty: MutableState<Boolean>,
    viewModel: SearchViewModel,
    query: MutableState<String>,
) {
    Card(
        modifier = Modifier.padding(
            start = 8.dp,
            end = 8.dp,
            top = 16.dp
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = 16.dp,
        contentColor = colorResource(id = R.color.light_gray_color)
    ) {
        Column {
            var showClearIcon by rememberSaveable { mutableStateOf(false) }
            if (query.value.isEmpty()) {
                showClearIcon = false
            } else if (query.value.isNotEmpty()) {
                showClearIcon = true
            }

            CompositionLocalProvider(
                LocalTextToolbar provides EmptyTextToolbar,
                LocalOverscrollConfiguration provides null
            ) {
                TextField(
                    value = query.value,
                    onValueChange = { onQueryChanged ->
                        query.value = onQueryChanged
                        queryIsEmpty.value = query.value.isEmpty()
                        viewModel.updateText(query.value)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            tint = MaterialTheme.colors.onBackground,
                            contentDescription = "Search icon"
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = showClearIcon,
                            enter = scaleIn(),
                            exit = scaleOut()
                        ) {
                            IconButton(onClick = {
                                query.value = ""
                                queryIsEmpty.value = query.value.isEmpty()
                                viewModel.updateText(query.value)
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    tint = MaterialTheme.colors.onBackground,
                                    contentDescription = "Clear icon"
                                )
                            }
                        }
                    },
                    maxLines = 1,
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.Black,
                        backgroundColor = Color.Transparent,
                        disabledTextColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(
                                R.string.hint_search_query,
                            ),
                            color = Color.Gray
                        )
                    },
                    textStyle = MaterialTheme.typography.subtitle1,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colors.background, shape = RectangleShape)
                        .clearFocusOnKeyboardDismiss()
                )
            }
        }
    }
}

private fun handleSideEffects(
    sideEffects: SearchSideEffects,
    markIsModified: MutableState<Boolean>,
    searchResults: LazyPagingItems<SearchItem>?,
    isLoading: MutableState<Boolean>,
    navHostController: NavHostController,
    query: MutableState<String>,
    scope: CoroutineScope,
    scrollState: LazyGridState
) {
    when (sideEffects) {
        is SearchSideEffects.TryReloadPage -> {
            searchResults?.retry()
        }
        is SearchSideEffects.UpdateSearchResult -> {
            markIsModified.value = true
        }
        is SearchSideEffects.SetIsLoading -> {
            isLoading.value = sideEffects.isLoading
        }
        is SearchSideEffects.NavigateToMovie -> {
            navHostController.navigate(Screen.MovieDetails().route)
        }
        is SearchSideEffects.NavigateToTvSeries -> {
            navHostController.navigate(Screen.TvSeriesDetails().route)
        }
        is SearchSideEffects.NavigateToPersonDetails -> {
            navHostController.navigate(Screen.PersonDetails().route)
        }
        is SearchSideEffects.SetSavedQuery -> {
            query.value = sideEffects.query
        }
        is SearchSideEffects.GetCurrentDiscoverPageAndScrollOffset -> {
            val currentPage = sideEffects.currentPageAndOffset
            scope.launch {
                scrollState.scrollToItem(currentPage, 0)
            }
        }
    }
}