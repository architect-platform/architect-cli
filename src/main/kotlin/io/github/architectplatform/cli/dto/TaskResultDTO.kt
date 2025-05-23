package io.github.architectplatform.cli.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class TaskResultDTO(
    val success: Boolean,
    val message: String?,
)