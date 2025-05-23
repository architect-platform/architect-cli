package io.github.architectplatform.cli.client

import io.github.architectplatform.cli.dto.ProjectDTO
import io.github.architectplatform.cli.dto.RegisterProjectRequest
import io.github.architectplatform.cli.dto.TaskDTO
import io.github.architectplatform.cli.dto.TaskResultDTO
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("engine", path = "/api/projects")
interface EngineCommandClient {

  @Get fun getAllProjects(): List<ProjectDTO>

  @Post fun registerProject(@Body request: RegisterProjectRequest): ProjectDTO

  @Get("/{name}") fun getProject(@PathVariable name: String): ProjectDTO?

  @Get("/{projectName}/tasks") fun getAllTasks(@PathVariable projectName: String): List<TaskDTO>

  @Get("/{projectName}/tasks/{taskName}")
  fun getTask(@PathVariable projectName: String, @PathVariable taskName: String): TaskDTO?

  @Post("/{projectName}/tasks/{taskName}")
  fun executeTask(
      @PathVariable projectName: String,
      @PathVariable taskName: String,
      @Body args: List<String>
  ): TaskResultDTO
}
