package io.github.architectplatform.cli

import io.github.architectplatform.api.interfaces.ApiCommandRequest
import io.github.architectplatform.api.interfaces.ApiCommandResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.annotation.SerdeImport

@SerdeImport(ApiCommandRequest::class)
@SerdeImport(ApiCommandResponse::class)
@Client(id = "engine")
interface EngineClient {

	@Get("/api/health")
	fun health(): HttpResponse<String>

	@Post("/api/command")
	fun executeCommand(command: ApiCommandRequest): HttpResponse<ApiCommandResponse>
}