package com.oksik.github_app.model

data class CommitItem(
    val sha: String,
    val commit: Commit,
    var isSelected: Boolean = false
)
