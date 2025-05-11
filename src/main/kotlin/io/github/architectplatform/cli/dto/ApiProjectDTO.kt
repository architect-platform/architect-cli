package io.github.architectplatform.cli.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ApiProjectDTO(
    val name: String,
    val path: String,
)
