package com.krykun.data.api

import com.krykun.data.model.remote.BasicMoviesResponse
import com.krykun.data.model.remote.moviecastdetails.CastDetailsResponse
import com.krykun.data.model.remote.genre.GenresResponse
import com.krykun.data.model.remote.moviedetails.MovieDetailsResponse
import com.krykun.data.model.remote.movielistitem.MovieItem
import com.krykun.data.model.remote.movierecommendations.MovieRecommendationResponse
import com.krykun.data.model.remote.movies.MovieItemResponse
import com.krykun.data.model.remote.personcombinedcredits.PersonCombinedCreditsResponse
import com.krykun.data.model.remote.persondetails.PersonDetailsResponse
import com.krykun.data.model.remote.search.SearchItem
import com.krykun.data.model.remote.tvcastdetails.TvCastDetailsResponse
import com.krykun.data.model.remote.tvdetails.TvDetailsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("discover/movie")
    suspend fun getDiscoverMovies(
        @Query("page") page: Int = 0,
        @Query("sort_by") sortBy: String? = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = true,
        @Query("include_video") includeVideo: Boolean = true,
    ): BasicMoviesResponse<MovieItem>

    @GET("movie/{id}/recommendations")
    suspend fun getRecommendationMovies(
        @Path("id") movieId: Int,
        @Query("page") page: Int = 0,
    ): BasicMoviesResponse<MovieRecommendationResponse>

    @GET("genre/movie/list")
    suspend fun getGenres(): Response<GenresResponse>

    @GET("genre/tv/list")
    suspend fun getTvGenres(): Response<GenresResponse>

    @GET("movie/{id}")
    suspend fun getMovieDetails(@Path("id") movieId: Int): Response<MovieDetailsResponse>

    @GET("movie/{id}/credits")
    suspend fun getMovieCastDetails(@Path("id") movieId: Int): Response<CastDetailsResponse>

    @GET("tv/{id}")
    suspend fun getTvDetails(@Path("id") movieId: Int): Response<TvDetailsResponse>

    @GET("tv/{id}/credits")
    suspend fun getTvCastDetails(@Path("id") movieId: Int): Response<TvCastDetailsResponse>

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("page") page: Int = 0,
    ): BasicMoviesResponse<MovieItemResponse>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 0,
    ): BasicMoviesResponse<MovieItemResponse>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 0,
    ): BasicMoviesResponse<MovieItemResponse>

    @GET("search/multi")
    suspend fun search(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("include_adult") includeAdult: Boolean = true,
    ): BasicMoviesResponse<SearchItem>

    @GET("person/{id}")
    suspend fun getPersonDetails(@Path("id") movieId: Int): Response<PersonDetailsResponse>

    @GET("person/{id}/combined_credits")
    suspend fun getPersonCombinedCredits(@Path("id") personId: Int): Response<PersonCombinedCreditsResponse>
}
