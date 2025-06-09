import io.github.architectplatform.cli.TerminalUI

class ConsoleUI(private val taskName: String) {

  object AnsiColors {
    const val RESET = "\u001B[0m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"
    const val BOLD = "\u001B[1m"
    const val ORANGE = "\u001B[38;5;208m"
  }

  private val ansiRegex = Regex("\u001B\\[[0-9;]*m")

  private fun String.visibleLength(): Int = this.replace(ansiRegex, "").length

  companion object {
    private const val TOTAL_WIDTH = 150
    private const val LEFT_PANEL_WIDTH = 44
    private val RIGHT_PANEL_WIDTH = TOTAL_WIDTH - LEFT_PANEL_WIDTH - 3

    private const val INFO_COL_COUNT = 4
    private const val INFO_COL_GUTTERS = INFO_COL_COUNT + 1 // 4 columns + 5 separators
    private val INFO_COL_WIDTH = (TOTAL_WIDTH - INFO_COL_GUTTERS) / INFO_COL_COUNT

    private val TASK_COL_WIDTH = 20
    private val EVENTS_COL_WIDTH = 22
    private val MESSAGE_COL_WIDTH = RIGHT_PANEL_WIDTH - TASK_COL_WIDTH - EVENTS_COL_WIDTH - 6
  }

  private val ui = TerminalUI(TOTAL_WIDTH)

  private val taskEvents = linkedMapOf<String, MutableList<String>>()
  private val taskMessages = linkedMapOf<String, String>()
  private val seenTasks = mutableSetOf<String>()
  private var executionId: String = "N/A"
  private var lastMessage: String? = null
  private var failed = false
  private val failureReasons = mutableMapOf<String, MutableList<String>>()

  val hasFailed: Boolean
    get() = failed

  fun process(event: Map<String, Any>) {
    executionId = event["executionId"]?.toString() ?: executionId
    val taskId = event["taskId"]?.toString() ?: "global"
    val message = event["message"]?.toString() ?: "No message provided"
    val type = event["eventType"]?.toString()?.uppercase() ?: "INFO"
    val reason = event["reason"]?.toString()

    seenTasks += taskId
    val icon =
        when (type) {
          "STARTED" -> "▶️"
          "COMPLETED" -> "✅"
          "FAILED" -> {
            failed = true
            if (reason != null)
                failureReasons.computeIfAbsent(taskId) { mutableListOf() }.add(reason)
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
    lastMessage = "✅  $finalMessage"
    redraw()
  }

  fun completeWithError(errorMessage: String) {
    lastMessage = "❌  Error: $errorMessage"
    failed = true
    redraw()
  }

  private fun redraw() {
    print("\u001B[2J\u001B[H") // Clear screen

    ui.clear()
    ui.drawLine('╔', null, '╗', '═')
    ui.addCenteredLine("${AnsiColors.BOLD}${AnsiColors.CYAN}🚀 Architect CLI${AnsiColors.RESET}")
    ui.drawLine('╠', null, '╣', '═')

    val projectName = System.getProperty("user.dir").substringAfterLast('/')
    val infoCells =
        listOf(
            "📦 ${AnsiColors.CYAN}Project:${AnsiColors.RESET} $projectName",
            "🧪 ${AnsiColors.CYAN}Task:${AnsiColors.RESET} $taskName",
            "🧾 ${AnsiColors.CYAN}Exec:${AnsiColors.RESET} $executionId",
            "📊 ${AnsiColors.CYAN}Status:${AnsiColors.RESET} ${lastMessage ?: "..."}")

    val cells = infoCells.map { truncateOrPadAnsi(it, INFO_COL_WIDTH) }
    val content = cells.joinToString("│")
    val visible = content.replace(Regex("\u001B\\[[0-9;]*m"), "").length
    val padding = " ".repeat((TOTAL_WIDTH - 2 - visible).coerceAtLeast(0)) // exclude ║ ║
    ui.addLine("║$content$padding║")
    ui.drawLine('╠', null, '╣', '═')

    ui.addSplitLine(
        boldYellow(" 📈 Summary").padEnd(LEFT_PANEL_WIDTH - 1),
        boldYellow(" 🗂️ Tasks"),
        LEFT_PANEL_WIDTH,
        RIGHT_PANEL_WIDTH)

    val summaryLines =
        listOf(
            " 🔍 Tasks Seen    : ${seenTasks.size}",
            " 📒 Tasks Logged  : ${taskEvents.size}",
            " 🚨 Failures      : ${if (failed) 1 else 0}",
            " 📊 Progress      : ${taskEvents.size} / ${seenTasks.size} tasks")

    val headers = listOf("🛠️ Task", "✉️ Events", "💬 Message")

    val rows =
        taskEvents.entries.map { (taskId, icons) ->
          val label = if (taskId == "global") "⚙️ Execution" else "🔧 $taskId"
          val iconsString = icons.takeLast(EVENTS_COL_WIDTH / 2).joinToString(" ")
          val msg = taskMessages[taskId] ?: ""
          listOf(label, iconsString, msg)
        }

    val maxLines = maxOf(summaryLines.size, rows.size + 4)

    for (i in 0 until maxLines) {
      val left = if (i < summaryLines.size) summaryLines[i] else " ".repeat(LEFT_PANEL_WIDTH - 1)
      val right =
          when (i) {
            0 -> tableLine('┌', '┬', '┐', '─')
            1 -> tableRow(headers)
            2 -> tableLine('├', '┼', '┤', '─')
            in 3 until rows.size + 3 -> tableRow(rows[i - 3])
            rows.size + 3 -> tableLine('└', '┴', '┘', '─')
            else -> " ".repeat(RIGHT_PANEL_WIDTH - 1)
          }

      ui.addSplitLine(left, right, LEFT_PANEL_WIDTH, RIGHT_PANEL_WIDTH)
    }

    if (failureReasons.isNotEmpty()) {
      ui.drawLine('╠', null, '╣', '═')
      ui.addCenteredLine("${AnsiColors.BOLD}${AnsiColors.RED}❌  Failure Details${AnsiColors.RESET}")
      ui.drawLine('╠', null, '╣', '═')

      for ((task, reasons) in failureReasons) {
        val taskHeader = "${AnsiColors.BOLD}${AnsiColors.YELLOW}🔧 $task${AnsiColors.RESET}"
        val strippedHeader = taskHeader.replace(Regex("\u001B\\[[0-9;]*m"), "")
        val padding = " ".repeat(TOTAL_WIDTH - 3 - strippedHeader.length)
        ui.addLine("║ $taskHeader$padding║")

        for (reason in reasons) {
          val reasonLines = wrapText(reason, TOTAL_WIDTH - 5)
          reasonLines.forEach { line ->
            val internalPadding = TOTAL_WIDTH - 5 - line.visibleLength()
            ui.addLine("║   $line${" ".repeat(internalPadding)}║")
          }
        }
      }
    }

    // Draw this only once at the very end
    ui.drawLine('╚', null, '╝', '═')
    println(ui.render())
  }

  private fun wrapText(text: String, width: Int): List<String> {
    val ansiRegex = Regex("\u001B\\[[0-9;]*m")
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()
    var visibleLength = 0

    for (word in words) {
      val stripped = ansiRegex.replace(word, "")
      val extraSpace = if (currentLine.isNotEmpty()) 1 else 0

      if (visibleLength + stripped.length + extraSpace > width) {
        lines += currentLine.toString()
        currentLine = StringBuilder()
        visibleLength = 0
      }

      if (currentLine.isNotEmpty()) {
        currentLine.append(" ")
        visibleLength += 1
      }

      currentLine.append(word)
      visibleLength += stripped.length
    }

    if (currentLine.isNotEmpty()) lines += currentLine.toString()
    return lines
  }

  private fun tableLine(left: Char, mid: Char, right: Char, fill: Char): String {
    val widths = listOf(TASK_COL_WIDTH, EVENTS_COL_WIDTH, MESSAGE_COL_WIDTH)
    return buildString {
      append(left)
      for ((i, w) in widths.withIndex()) {
        append(fill.toString().repeat(w))
        append(if (i == widths.lastIndex) right else mid)
      }
    }
  }

  private fun tableRow(cells: List<String>): String {
    val widths = listOf(TASK_COL_WIDTH, EVENTS_COL_WIDTH, MESSAGE_COL_WIDTH)
    return buildString {
      append('│')
      for ((i, cell) in cells.withIndex()) {
        append(truncateOrPadAnsi(cell, widths[i]))
        append('│')
      }
    }
  }

  private fun truncateOrPadAnsi(text: String, width: Int): String {
    val ansiRegex = Regex("\u001B\\[[0-9;]*m")
    val stripped = ansiRegex.replace(text, "")
    val visibleLength = stripped.length

    // Truncate if needed (ensure space for "…" and padding)
    val safeContent =
        if (visibleLength > width - 2) {
          stripped.take(width - 3) + "…"
        } else {
          stripped
        }

    val content = " $safeContent "
    val visibleWithPadding = content.length
    val padding = width - visibleWithPadding

    return content + " ".repeat(padding.coerceAtLeast(0))
  }

  private fun boldYellow(text: String): String =
      "${AnsiColors.BOLD}${AnsiColors.YELLOW}$text${AnsiColors.RESET}"
}
