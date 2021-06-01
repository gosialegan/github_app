package com.oksik.github_app.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oksik.github_app.model.CommitItem
import com.oksik.github_app.model.Repository
import com.oksik.github_app.network.GitHubApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainViewModel : ViewModel() {

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
                val repositoryCommits = getRepositoryCommits(owner!!, repositoryName!!)
                val sortedRepositoryCommits = sortRepositoryCommitsByDateDesc(repositoryCommits)
                _repository.postValue(getRepository(owner!!, repositoryName!!))
                _commits.postValue(sortedRepositoryCommits)
            }
        }
    }

    private suspend fun getRepository(owner: String, repositoryName: String): Repository {
        val requestRepository = GitHubApi.RETROFIT_SERVICE.getRepository(owner, repositoryName)
        return requestRepository.await()
    }

    private suspend fun getRepositoryCommits(owner: String, repositoryName: String): List<CommitItem> {
        val requestCommits = GitHubApi.RETROFIT_SERVICE.getRepositoryCommits(owner, repositoryName)
        return requestCommits.await()
    }

    private fun sortRepositoryCommitsByDateDesc(repositoryCommits: List<CommitItem>): List<CommitItem> {
        return repositoryCommits.sortedByDescending {
            LocalDateTime.parse(
                it.commit.author.date,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
        }
    }

    fun onItemClicked(sha: String) {
    }
}