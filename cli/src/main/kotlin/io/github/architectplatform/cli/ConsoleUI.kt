import io.github.architectplatform.cli.TerminalUI

class ConsoleUI(private val taskName: String) {

  companion object {
    private const val TOTAL_WIDTH = 100
    private const val LEFT_PANEL_WIDTH = 34
    private val RIGHT_PANEL_WIDTH = TOTAL_WIDTH - LEFT_PANEL_WIDTH - 3
    private val TASK_COL_WIDTH = 18
    private val EVENTS_COL_WIDTH = 8
    private val MESSAGE_COL_WIDTH =
        RIGHT_PANEL_WIDTH - TASK_COL_WIDTH - EVENTS_COL_WIDTH - 6 // 6 for padding & separators
  }

  private val ui = TerminalUI(TOTAL_WIDTH)

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

  private fun redraw() {
    print("\u001B[2J")
    print("\u001B[H")

    ui.clear()
    ui.drawLine('╔', null, '╗', '═')
    ui.addCenteredLine("🚀 Architect CLI")
    val statusLine = "Task: $taskName | Exec: $executionId | Status: ${lastMessage ?: "Running..."}"
    ui.addCenteredLine(statusLine)
    ui.drawLine('╠', null, '╣', '═')

    ui.addSplitLine(
        "Summary".padEnd(LEFT_PANEL_WIDTH - 2),
        "Logs".padEnd(RIGHT_PANEL_WIDTH - 1),
        LEFT_PANEL_WIDTH,
        RIGHT_PANEL_WIDTH)
    ui.drawLine('╠', null, '╣', '═')

    val summaryLines =
        listOf(
            "Tasks Seen  : ${seenTasks.size}",
            "Tasks Logged: ${taskEvents.size}",
            "Failures    : ${if (failed) 1 else 0}",
            "Progress    : ${taskEvents.size} / ${seenTasks.size} tasks")

    val headers = listOf("Task", "Events", "Message")

    val rows =
        taskEvents.entries.map { (taskId, icons) ->
          val label = if (taskId == "global") "🌐 Global" else "🔧 $taskId"
          val iconsString = icons.takeLast(EVENTS_COL_WIDTH / 2).joinToString(" ")
          val msg = taskMessages[taskId] ?: ""
          listOf(label, iconsString, msg)
        }

    val maxLines = maxOf(summaryLines.size, rows.size + 4)

    for (i in 0 until maxLines) {
      val left = if (i < summaryLines.size) summaryLines[i] else " ".repeat(LEFT_PANEL_WIDTH - 1)
      val right =
          when (i) {
            0 ->
                buildTableLine(
                    ui,
                    listOf("", "", ""),
                    listOf(TASK_COL_WIDTH, EVENTS_COL_WIDTH, MESSAGE_COL_WIDTH),
                    '┌',
                    '┬',
                    '┐',
                    '─')
            1 ->
                buildTableRow(
                    ui, headers, listOf(TASK_COL_WIDTH, EVENTS_COL_WIDTH, MESSAGE_COL_WIDTH))
            2 ->
                buildTableLine(
                    ui,
                    listOf("", "", ""),
                    listOf(TASK_COL_WIDTH, EVENTS_COL_WIDTH, MESSAGE_COL_WIDTH),
                    '├',
                    '┼',
                    '┤',
                    '─')
            in 3 until rows.size + 3 -> {
              val rowIndex = i - 3
              buildTableRow(
                  ui, rows[rowIndex], listOf(TASK_COL_WIDTH, EVENTS_COL_WIDTH, MESSAGE_COL_WIDTH))
            }
            rows.size + 3 -> {
              // Table bottom border line
              buildTableLine(
                  ui,
                  listOf("", "", ""),
                  listOf(TASK_COL_WIDTH, EVENTS_COL_WIDTH, MESSAGE_COL_WIDTH),
                  '└',
                  '┴',
                  '┘',
                  '─')
            }
            else -> " ".repeat(RIGHT_PANEL_WIDTH - 1)
          }

      ui.addSplitLine(left, right, LEFT_PANEL_WIDTH, RIGHT_PANEL_WIDTH)
    }

    ui.drawLine('╚', null, '╝', '═')

    println(ui.render())
  }

  private fun buildTableLine(
      ui: TerminalUI,
      cells: List<String>,
      widths: List<Int>,
      leftChar: Char,
      middleChar: Char,
      rightChar: Char,
      fillChar: Char
  ): String {
    val sb = StringBuilder()
    sb.append(leftChar)
    for (i in widths.indices) {
      sb.append(fillChar.toString().repeat(widths[i]))
      sb.append(if (i == widths.lastIndex) rightChar else middleChar)
    }
    return sb.toString()
  }

  private fun buildTableRow(ui: TerminalUI, cells: List<String>, widths: List<Int>): String {
    val sb = StringBuilder()
    sb.append('│')
    for (i in widths.indices) {
      val cellText = truncateOrPad(cells[i], widths[i])
      sb.append(cellText)
      sb.append(if (i == widths.lastIndex) '│' else '│')
    }
    return sb.toString()
  }

  private fun truncateOrPad(text: String, width: Int): String {
    return if (text.length > width) text.take(width - 1) + "…" else text.padEnd(width)
  }
}
