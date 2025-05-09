package io.github.architectplatform.cli

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
	version = ["${'$'}{project.version}"],
)
class ArchitectLauncher : Runnable {

	override fun run() {
		CommandLine.usage(this, System.out)
	}

	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			PicocliRunner.run(ArchitectLauncher::class.java, *args)
		}
	}
}