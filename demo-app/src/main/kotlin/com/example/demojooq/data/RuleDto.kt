package com.example.demojooq.data

data class RuleDto(
    val enabled: Boolean,
    val targets: Set<String>,
)
