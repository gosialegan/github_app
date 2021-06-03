package com.oksik.github_app.ui.main

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.oksik.github_app.model.CommitItem
import com.oksik.github_app.model.Repository
import com.oksik.github_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainViewModel(private val context: Application) : AndroidViewModel(context) {

    private val _repository = MutableLiveData<Repository>()
    val repository: LiveData<Repository>
        get() = _repository

    private val _commits = MutableLiveData<List<CommitItem>>()
    val commits: LiveData<List<CommitItem>>
        get() = _commits

    val ownerAndRepository = MutableLiveData<String>()

    private val owner
        get() = ownerAndRepository.value?.substringBefore("/")

    private val repositoryName
        get() = ownerAndRepository.value?.substringAfter("/")

    fun search() {
        if (!owner.isNullOrEmpty() || !repositoryName.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {

                try {
                    val repositoryCommits = getRepositoryCommits(owner!!, repositoryName!!)
                    val sortedRepositoryCommits =
                        sortRepositoryCommitsByDateDesc(repositoryCommits!!)
                    _repository.postValue(getRepository(owner!!, repositoryName!!))
                    _commits.postValue(sortedRepositoryCommits!!)
                } catch (e: Exception) {
                    Log.e("tutaj",  "mes: ", e)
                }
            }
        }
    }

    private suspend fun getRepository(owner: String, repositoryName: String): Repository {
        val requestRepository = RetrofitClient.create(context).getRepository(owner, repositoryName)
        return requestRepository.await()
    }

    private suspend fun getRepositoryCommits(owner: String, repositoryName: String): List<CommitItem>? {
        val requestCommits = RetrofitClient.create(context).getRepositoryCommits(owner, repositoryName)
        return requestCommits?.await()
    }

    private fun sortRepositoryCommitsByDateDesc(repositoryCommits: List<CommitItem>?): List<CommitItem>? {
        return repositoryCommits?.sortedByDescending {
            LocalDateTime.parse(
                it.commit.author.date,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
        }
    }

    fun onItemClicked(sha: String) {
    }
}