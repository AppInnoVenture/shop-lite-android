package com.yashas.shoplite.domain.model

data class Review(
    val rating: Int,
    val comment: String,
    val date: String,
    val reviewerName: String
)
