package io.github.architectplatform.cli

import io.github.architectplatform.cli.client.EngineCommandClient
import io.github.architectplatform.cli.dto.RegisterProjectRequest
import io.micronaut.configuration.picocli.PicocliRunner
import jakarta.inject.Singleton
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Singleton
@Command(
    name = "architect",
    description = ["Architect CLI"],
)
class ArchitectLauncher(private val engineCommandClient: EngineCommandClient) : Runnable {

  @Parameters(
      description = ["Command to execute"],
      arity = "0..*",
      paramLabel = "<command>",
  )
  var command: String? = null

  @Parameters(
      description = ["Arguments for the command"],
      arity = "0..*",
      paramLabel = "<args>",
  )
  var args: List<String> = emptyList()

  override fun run() {

    // Current working directory
    val projectPath = System.getProperty("user.dir")
    println("Current working directory: $projectPath")
    // Get the project name from the path
    val projectName = projectPath.substringAfterLast("/").substringBeforeLast(".")
    println("Project name: $projectName")

    println("Registering project: $projectName")
    val request = RegisterProjectRequest(name = projectName, path = projectPath)
    engineCommandClient.registerProject(request)

    args = args.drop(1) // Drop the first argument which is the command name

    if (command == null) {
      val commands = engineCommandClient.getAllTasks(projectName)
      println("Available commands:")
      commands.forEach { command -> println(" - $command") }
      return
    }

    println("Running command: $command with args: $args")
    val result = engineCommandClient.executeTask(projectName, command!!, args)
    println(result.toString())
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      PicocliRunner.run(ArchitectLauncher::class.java, *args)
    }
  }
}
