package io.github.architectplatform.cli

import io.github.architectplatform.cli.client.EngineCommandClient
import io.github.architectplatform.cli.dto.ApiRegisterProjectRequest
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

    try {
      // Check if the project is registered
      val project = engineCommandClient.getProject(projectName)
      if (project == null) {
        println("Project not found. Registering project: $projectName")
        val request = ApiRegisterProjectRequest(name = projectName, path = projectPath)
        engineCommandClient.registerProject(request)
        println("Project registered successfully.")
      } else {
        println("Project already registered: $project")
      }
    } catch (e: Exception) {
      println("Error registering project: ${e.message}")
      return
    }

    args = args.drop(1) // Drop the first argument which is the command name

    if (command == null) {
      val commands = engineCommandClient.getAllCommands(projectName)
      println("Available commands:")
      commands.forEach { command -> println(" - $command") }
      return
    }

    println("Running command: $command with args: $args")
    val result = engineCommandClient.executeCommand(projectName, command!!, args)
    println("Result: $result")
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      PicocliRunner.run(ArchitectLauncher::class.java, *args)
    }
  }
}
