package io.github.architectplatform.cli

import io.github.architectplatform.cli.dto.ApiCommandDTO
import io.github.architectplatform.cli.dto.ApiCommandResponse
import io.github.architectplatform.cli.dto.ApiProjectDTO
import io.github.architectplatform.cli.dto.ApiRegisterProjectRequest
import io.github.architectplatform.cli.dto.ContextDTO
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client(id = "engine", path = "/api/projects")
interface EngineCommandClient {

  @Get fun getAll(): List<ApiProjectDTO>

  @Get("/{name}") fun getProject(@PathVariable name: String): ApiProjectDTO?

  @Post fun registerProject(@Body request: ApiRegisterProjectRequest): ApiProjectDTO

  @Get("/{projectName}/commands")
  fun getAllCommands(@PathVariable projectName: String): List<ApiCommandDTO>

  @Get("/{projectName}/commands/{commandName}")
  fun getCommand(
      @PathVariable projectName: String,
      @PathVariable commandName: String
  ): ApiCommandDTO?

  @Post("/{projectName}/commands/{commandName}")
  fun executeCommand(
      @PathVariable projectName: String,
      @PathVariable commandName: String,
      @Body args: List<String>
  ): ApiCommandResponse

  @Get("/{projectName}/context") fun getContext(@PathVariable projectName: String): ContextDTO
}
