package com.bridge.androidtechnicaltest.di

data class ApiErrors(
    val title: String?,
    val status: Int?,
    val traceId: String?,
    val errors: Map<String, List<String>>? = null
)
