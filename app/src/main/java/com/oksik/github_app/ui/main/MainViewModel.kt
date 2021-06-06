package com.oksik.github_app.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.oksik.github_app.R
import com.oksik.github_app.model.CommitItem
import com.oksik.github_app.model.Repository
import com.oksik.github_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainViewModel(private val context: Application) : AndroidViewModel(context) {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _repository = MutableLiveData<Repository>()
    val repository: LiveData<Repository>
        get() = _repository

    private val _commits = MutableLiveData<List<CommitItem>>()
    val commits: LiveData<List<CommitItem>>
        get() = _commits

    private val _commitsInformationToShare = MutableLiveData<String?>()
    val commitsInformationToShare: LiveData<String?>
        get() = _commitsInformationToShare

    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?>
        get() = _snackbarMessage

    val ownerAndRepository = MutableLiveData<String>()

    private val owner
        get() = ownerAndRepository.value?.substringBefore("/")

    private val repositoryName
        get() = ownerAndRepository.value?.substringAfter("/")

    fun onItemClicked(item: CommitItem) {
        item.isSelected = !item.isSelected
    }

    fun search() {
        if (!isOwnerRepoFieldMatchesPattern()) {
            _snackbarMessage.postValue(context.getString(R.string.incorrect_userrepo))
            return
        }

        if (owner.isNullOrEmpty() || repositoryName.isNullOrEmpty()) {
            _snackbarMessage.value = context.getString(R.string.empty_owner_repo)
            return
        }

        getRepositoryAndCommits()
    }

    private fun getRepositoryAndCommits() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val repositoryCommits = getRepositoryCommits(owner!!, repositoryName!!)
                val sortedRepositoryCommits = sortRepositoryCommitsByDateDesc(repositoryCommits!!)
                _repository.postValue(getRepository(owner!!, repositoryName!!))
                _commits.postValue(sortedRepositoryCommits!!)
            } catch (e: HttpException) {
                if (e.code() == 404)
                    _snackbarMessage.postValue(context.getString(R.string.no_repositories))
                else if (e.code() == 504)
                    _snackbarMessage.postValue(context.getString(R.string.no_internet_connection))
            } catch (e: Exception) {
                _snackbarMessage.postValue(context.getString(R.string.fetching_data_error))
                Log.e(TAG, "mes: ", e)
            }
        }
    }

    private fun isOwnerRepoFieldMatchesPattern() =
        ownerAndRepository.value?.matches(Regex("[a-zA-Z0-9]*/[a-zA-Z0-9]*")) == true

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

    fun send() {
        val selectedCommits = _commits.value?.takeWhile { it.isSelected }
        if (selectedCommits.isNullOrEmpty()) {
            _snackbarMessage.value = context.getString(R.string.select_one_or_more)
        } else {
            _commitsInformationToShare.value = getSelectedCommitsInformation(selectedCommits)
        }
    }

    private fun getSelectedCommitsInformation(selectedCommits: List<CommitItem>): String {
        var commitsInformation = ""
        for (commitItem in selectedCommits) {
            commitsInformation += commitItem.sha.plus("\n")
                .plus(commitItem.commit.message).plus("\n")
                .plus(commitItem.commit.author.name).plus("\n")
        }
        return commitsInformation
    }

    fun commitInformationSent() {
        _commitsInformationToShare.value = null
    }

    fun snackbarMessageShowed() {
        _snackbarMessage.value = null
    }
}