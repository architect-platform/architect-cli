package io.github.architectplatform.cli

import io.github.architectplatform.api.interfaces.ApiCommandRequest
import io.micronaut.configuration.picocli.PicocliRunner
import jakarta.inject.Singleton
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

@Singleton
@Command(
	name = "architect",
	description = ["Architect CLI"],
)
class ArchitectLauncher(
	val engineCommandClient: EngineCommandClient
) : Runnable {

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
		if (command == null) {
			val commands = engineCommandClient.getAll().body()
			println("Available commands:")
			commands.forEach { command ->
				println(" - $command")
			}
			return
		}

		println("Running command: ${args.first()} with args: ${args.drop(1)}")
		val result = engineCommandClient.executeCommand(command!!, args)
		println("Success: ${result.body().success}")
		println("Output: ${result.body().output}")
	}

	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			PicocliRunner.run(ArchitectLauncher::class.java, *args)
		}
	}
}