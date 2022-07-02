package com.krykun.domain.usecase

import com.krykun.domain.model.Genre
import com.krykun.domain.repositories.MoviesRemoteRepo
import javax.inject.Inject

class GetUpcomingMoviesUseCase @Inject constructor(
    private val moviesRemoteRepo: MoviesRemoteRepo
) {

    fun getMovies(
        country: String? = null,
        language: String? = null,
        category: String? = null,
        genres: List<Genre>
    ) = moviesRemoteRepo.getUpcomingMovies(
        country = country,
        language = language,
        category = category,
        genres = genres
    )
}