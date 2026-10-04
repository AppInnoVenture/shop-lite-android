package com.yashas.shoplite.data.remote

import androidx.annotation.Keep

@Keep
data class ReviewDto(
    val rating: Int,
    val comment: String,
    val date: String,
    val reviewerName: String
)
