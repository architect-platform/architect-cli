package io.github.architectplatform.cli.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ApiCommandResponse(
	val success: Boolean = false,
	val output: String?,
)