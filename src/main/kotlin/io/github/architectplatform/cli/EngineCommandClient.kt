package io.github.architectplatform.cli

import io.github.architectplatform.cli.dto.ApiCommandDTO
import io.github.architectplatform.cli.dto.ApiCommandResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client(id = "engine", path = "/api")
interface EngineCommandClient {

	@Get("/commands")
	fun getAll(): HttpResponse<List<String>>

	@Get("/commands/{name}")
	fun getCommand(@PathVariable name: String): HttpResponse<ApiCommandDTO>

	@Post("/commands/{name}")
	fun executeCommand(@PathVariable name: String, @Body args: List<String>): HttpResponse<ApiCommandResponse>

}