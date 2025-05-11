package io.github.architectplatform.cli.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ApiCommandDTO(
    val name: String,
    val description: String = "",
    val usage: String = "",
    val subcommands: List<ApiCommandDTO> = emptyList()
)
