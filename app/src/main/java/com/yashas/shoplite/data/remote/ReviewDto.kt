package com.yashas.shoplite.data.remote

data class ReviewDto(
    val rating: Int,
    val comment: String,
    val date: String,
    val reviewerName: String
)
