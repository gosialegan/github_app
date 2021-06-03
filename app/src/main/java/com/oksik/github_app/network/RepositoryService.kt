package com.oksik.github_app.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.provider.SyncStateContract
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.oksik.github_app.model.CommitItem
import com.oksik.github_app.model.Repository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException


private const val BASE_URL = "https://api.github.com/"

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
    ): Deferred<List<CommitItem>?>?
}

class RetrofitClient {

    companion object {
        fun create(context: Context): GitHubApiService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(okHttp(context))
                .baseUrl(BASE_URL)
                .build()

            return retrofit.create(GitHubApiService::class.java)
        }

        private fun okHttp(context: Context): OkHttpClient {
            val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
            val cacheSize = (5 * 1024 * 1024).toLong()
            val myCache = Cache(context.cacheDir, cacheSize)
            return OkHttpClient.Builder()
                .cache(myCache)
                .addInterceptor { chain ->
                    var request = chain.request()
                    request = if (hasNetwork(context)!!)
                        request.newBuilder().header("Cache-Control", "public, max-age=" + 5).build()
                    else
                        request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .build()
        }

        private fun hasNetwork(context: Context): Boolean? {
            var isConnected: Boolean? = false // Initial Value
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            if (activeNetwork != null && activeNetwork.isConnected)
                isConnected = true
            return isConnected
        }
    }
}