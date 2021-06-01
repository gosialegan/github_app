package com.oksik.github_app.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.oksik.github_app.model.CommitItem
import com.oksik.github_app.model.Repository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


private const val BASE_URL = "https://api.github.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface GitHubApiService {
    @GET("repos/{owner}/{repository}")
    fun getRepository(
        @Path("owner") owner: String,
        @Path("repository") repository: String
    ): Deferred<Repository>

    @GET("repos/{owner}/{repository}/commits")
    fun getRepositoryCommits(
        @Path("owner") owner: String,
        @Path("repository") repository: String
    ): Deferred<List<CommitItem>>
}

object GitHubApi {
    val RETROFIT_SERVICE: GitHubApiService by lazy {
        retrofit.create(GitHubApiService::class.java)
    }
}