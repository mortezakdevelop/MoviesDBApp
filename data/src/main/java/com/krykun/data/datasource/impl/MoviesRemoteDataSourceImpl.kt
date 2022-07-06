package com.krykun.data.datasource.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.krykun.data.api.ApiService
import com.krykun.data.datasource.MoviesRemoteDataSource
import com.krykun.data.model.castdetails.CastDetailsResponse
import com.krykun.data.model.genre.Genre
import com.krykun.data.model.moviedetails.MovieDetailsResponse
import com.krykun.data.model.movielistitem.MovieItem
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class MoviesRemoteDataSourceImpl @Inject constructor(
    private val apiService: ApiService
) : MoviesRemoteDataSource {

    override fun getUpcomingMovies(
        country: String?,
        language: String?,
        category: String?
    ): Flow<PagingData<MovieItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                MoviesPagingSource(
                    apiService = apiService,
                )
            }
        ).flow
    }

    override suspend fun getGenres(): List<Genre> {
        return apiService.getGenres().genres ?: listOf()
    }

    override suspend fun getMovieDetails(movieId: Int): MovieDetailsResponse {
        return apiService.getMovieDetails(movieId)
    }

    override suspend fun getCastDetails(movieId: Int): CastDetailsResponse {
        return apiService.getCastDetails(movieId)
    }
}