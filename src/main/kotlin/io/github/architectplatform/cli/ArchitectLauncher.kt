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
	mixinStandardHelpOptions = true,
)
class ArchitectLauncher(
	val engineClient: EngineClient
) : Runnable {

	@Parameters
	var args: Array<String> = emptyArray()

	override fun run() {
		if (args.isEmpty()) {
			CommandLine.usage(this, System.out)
			return
		}

		try {
			engineClient.health()
		} catch (e: Exception) {
			println("Engine is not running. Please start the engine first.")
			return
		}

		println("Running command: ${args.first()} with args: ${args.drop(1)}")
		val command = ApiCommandRequest(args.first(), args.drop(1).toList())
		val result = engineClient.executeCommand(command)
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