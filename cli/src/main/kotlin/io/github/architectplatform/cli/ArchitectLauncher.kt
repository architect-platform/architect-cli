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
      if (command != null && command == "engine") {
          handleEngineCommand()
          return
      }
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

    private fun handleEngineCommand() {
        val arg = args.getOrNull(1)
        if (arg == null) {
            println("No command provided for 'engine'. Available commands: register, list, execute")
            return
        }

        when (arg) {
            "install" -> {
                println("Installing Architect Engine...")
                val command = "curl -sSL https://raw.githubusercontent.com/architect-platform/architect-engine/main/.installers/bash | bash"
                execute(command)
            }
            "start" -> {
                println("Running Architect Engine...")
                val command = "architect-engine"
                execute(command, false)
            }
            "stop" -> {
                println("Stopping Architect Engine...")
                val command = "pkill -f architect-engine"
                execute(command)
            }
            "clean" -> {
                println("Cleaning Architect Engine...")
                val command = "rm -rf ~/.architect-engine"
                execute(command)
            }
            else -> {
                println("Unknown command for 'engine': $arg")
                println("Available commands: install, start, stop, clean")
            }
        }
    }

    private fun execute(command: String, wait: Boolean = true) {
        try {
            val process = Runtime.getRuntime().exec(command)
            if (wait) {
                process.waitFor()
                println("Command: $command executed successfully.")
            } else {
                println("Command: $command is running in the background.")
            }
        } catch (e: Exception) {
            println("Failed to execute command: $command - ${e.message}")
        }
    }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      PicocliRunner.run(ArchitectLauncher::class.java, *args)
    }
  }
}
