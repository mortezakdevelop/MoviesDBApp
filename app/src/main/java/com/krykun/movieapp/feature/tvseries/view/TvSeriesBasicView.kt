package com.krykun.movieapp.feature.tvseries.view

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.krykun.domain.model.remote.tvcastdetails.Cast
import com.krykun.domain.model.remote.tvcastdetails.Crew
import com.krykun.movieapp.R
import com.krykun.movieapp.feature.addtoplaylist.presentation.PlaylistSelectViewModel
import com.krykun.movieapp.feature.addtoplaylist.view.PlaylistSelectedView
import com.krykun.movieapp.feature.tvseries.presentation.TvSeriesDetailsViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(
    ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun TvSeriesDetailsBasicView(
    navHostController: NavHostController,
    viewModel: TvSeriesDetailsViewModel,
    bottomSheetState: ModalBottomSheetState,
    playlistSelectViewModel: PlaylistSelectViewModel,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val scrollSate = rememberScrollState()
    val scaffoldState = rememberScaffoldState()
    val message = stringResource(R.string.tv_series_added_to_playlist)
    val movieData = viewModel.movieData
    val scope = rememberCoroutineScope()

    BackHandler {
        if (bottomSheetState.isVisible ||
            bottomSheetState.isAnimationRunning
        ) {
            scope.launch {
                bottomSheetState.hide()
            }
        } else {
            navHostController.popBackStack()
        }
    }

    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        ModalBottomSheetLayout(
            sheetContent = {
                PlaylistSelectedView(playlistSelectViewModel)
            },
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            sheetState = bottomSheetState
        ) {
            Scaffold(
                scaffoldState = scaffoldState,
                floatingActionButton = {
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(),
                        exit = scaleOut()
                    ) {
                        FloatingActionButton(
                            onClick = {
                                viewModel.updateMovieSelector()
                            },
                            shape = RoundedCornerShape(20.dp),
                            contentColor = colorResource(id = R.color.floating_button_color)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add, contentDescription = "",
                                tint = Color.White
                            )
                        }
                    }
                },
                backgroundColor = Color.Transparent
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    BackBtn(navHostController = navHostController)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollSate)
                    ) {
                        HeaderView(
                            backdropPath = movieData.value?.backdropPath ?: ""
                        )
                        RatingView(
                            isRatingVisible = viewModel.isRatingVisible,
                            screenWidth = screenWidth,
                            movieData = movieData
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp)) {
                                TitleView(movieData)
                            }
                            LazyRow {
                                items(count = movieData.value?.genres?.size ?: 0) { index ->
                                    Text(
                                        text = movieData.value?.genres?.get(index)?.name ?: "",
                                        modifier = Modifier
                                            .padding(
                                                start = if (index == 0) {
                                                    24.dp
                                                } else {
                                                    2.dp
                                                },
                                                end = 2.dp,
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = Color.LightGray,
                                                shape = CircleShape
                                            )
                                            .padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = 5.dp,
                                                bottom = 5.dp
                                            ),
                                        color = colorResource(id = R.color.white),

                                        )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp)) {
                                Spacer(modifier = Modifier.height(30.dp))
                                Text(
                                    text = stringResource(R.string.plot_summary),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = movieData.value?.overview ?: "",
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                        movieData.value?.seasons?.let {
                            SeasonsView(it)
                        }
                        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp)) {
                            Spacer(modifier = Modifier.height(30.dp))
                            Text(
                                text = stringResource(R.string.cast_and_crew),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        LazyRow {
                            itemsIndexed(
                                movieData.value?.cast?.castAndCrew ?: listOf()
                            ) { index, item ->
                                if (item is Cast) {
                                    CastView(castItem = item)
                                } else {
                                    CrewView(crewItem = item as Crew)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}