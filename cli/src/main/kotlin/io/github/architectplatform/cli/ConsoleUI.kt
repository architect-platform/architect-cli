package io.github.architectplatform.cli

class ConsoleUI(private val taskName: String) {

  companion object {
    private const val LABEL_WIDTH = 20
    private const val ICON_COLUMN_CAPACITY = 3
    private const val ICON_COLUMN_WIDTH = ICON_COLUMN_CAPACITY * 3
    private const val MESSAGE_WIDTH = 60
  }

  private val taskEvents = linkedMapOf<String, MutableList<String>>()
  private val taskMessages = linkedMapOf<String, String>()
  private val seenTasks = mutableSetOf<String>()
  private var executionId: String = "N/A"
  private var lastMessage: String? = null
  private var failed = false

  val hasFailed: Boolean
    get() = failed

  fun process(event: Map<String, Any>) {
    executionId = event["executionId"]?.toString() ?: executionId
    val taskId = event["taskId"]?.toString() ?: "global"
    val message = event["message"]?.toString() ?: "No message provided"
    val type = event["eventType"]?.toString()?.uppercase() ?: "INFO"

    seenTasks += taskId
    val icon =
        when (type) {
          "STARTED" -> "▶️"
          "COMPLETED" -> "✅"
          "FAILED" -> {
            failed = true
            "❌"
          }
          "SKIPPED" -> "⏭️"
          else -> "ℹ️"
        }

    taskEvents.computeIfAbsent(taskId) { mutableListOf() }.add(icon)
    taskMessages[taskId] = message

    redraw()
  }

  fun complete(finalMessage: String) {
    lastMessage = "✅ $finalMessage"
    redraw()
  }

  fun completeWithError(errorMessage: String) {
    lastMessage = "❌ Error: $errorMessage"
    failed = true
    redraw()
  }

  private fun drawLine(left: String, mid: String, right: String, fill: String): String {
    return left +
        fill.repeat(LABEL_WIDTH + 1) +
        mid +
        fill.repeat(ICON_COLUMN_WIDTH + 2) +
        mid +
        fill.repeat(MESSAGE_WIDTH) +
        right
  }

  private fun redraw() {
    print("\u001B[2J")
    print("\u001B[H")

    println("🚀 Architect CLI")
    println("🎯 Task: $taskName")
    println("🧩 Execution ID: $executionId")
    println("📊 Progress: ${taskEvents.size}/${seenTasks.size} tasks recorded")
    println("📦 Status: ${lastMessage ?: "Running..."}")
    println("\n📝 Logs:\n")

    println(drawLine("┌", "┬", "┐", "─"))
    println(
        "│ ${"Task".padEnd(LABEL_WIDTH - 1)} │ ${"Events".padEnd(ICON_COLUMN_WIDTH)} │ ${"Message".padEnd(MESSAGE_WIDTH - 1)}│")
    println(drawLine("├", "┼", "┤", "─"))

    taskEvents.forEach { (taskId, icons) ->
      val label = if (taskId == "global") "🌐 Global" else "🔧 $taskId"
      val paddedLabel = label.padEnd(LABEL_WIDTH)
      val iconList = icons.takeLast(ICON_COLUMN_CAPACITY)
      val iconsString = iconList.joinToString(" ").padEnd(ICON_COLUMN_WIDTH)
      val message = taskMessages[taskId]?.take(MESSAGE_WIDTH) ?: ""

      println("│ $paddedLabel│ $iconsString │ ${message.padEnd(MESSAGE_WIDTH - 1)}│")
    }

    println(drawLine("└", "┴", "┘", "─"))
  }
}
