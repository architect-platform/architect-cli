package io.github.architectplatform.cli

class TerminalUI(private val totalWidth: Int) {

  private val lines = mutableListOf<String>()

  fun clear() {
    lines.clear()
  }

  /**
   * Draw a horizontal line with given left, middle (optional), right chars and fill character. If
   * middle is null, the line is a solid line from left to right fill.
   */
  fun drawLine(left: Char, middle: Char?, right: Char, fill: Char) {
    val line = buildString {
      append(left)
      if (middle == null) {
        append(fill.toString().repeat(totalWidth - 2))
      } else {
        // Draw fill repeated totalWidth-3 times + middle + then right
        // Note: usually totalWidth - 3 fill + 1 middle + 1 right + 1 left = totalWidth
        append(fill.toString().repeat(totalWidth - 3))
        append(middle)
      }
      append(right)
    }
    lines.add(line)
  }

  /** Draw a full line consisting only of the given char. */
  fun drawFullLine(char: Char) {
    lines.add(char.toString().repeat(totalWidth))
  }

  /** Add a line with centered text inside borders. */
  fun addCenteredLine(text: String, leftBorder: Char = '║', rightBorder: Char = '║') {
    // Center extension inside this function scope
    fun String.center(width: Int): String {
      if (this.length >= width) return this
      val padding = width - this.length
      val padStart = padding / 2
      val padEnd = padding - padStart
      return " ".repeat(padStart) + this + " ".repeat(padEnd)
    }

    val padded = text.center(totalWidth - 2)
    lines.add("$leftBorder$padded$rightBorder")
  }

  /**
   * Add a line split into two sections with their own widths, separated by middleBorder. Text is
   * padded or truncated accordingly.
   */
  fun addSplitLine(
      leftText: String,
      rightText: String,
      leftWidth: Int,
      rightWidth: Int,
      leftBorder: Char = '║',
      middleBorder: Char = '│',
      rightBorder: Char = '║'
  ) {
    val leftPadded = truncateOrPad(leftText, leftWidth)
    val rightPadded = truncateOrPad(rightText, rightWidth)
    lines.add("$leftBorder$leftPadded$middleBorder$rightPadded$rightBorder")
  }

  /** Add an empty split line with given section widths. */
  fun addEmptySplitLine(
      leftWidth: Int,
      rightWidth: Int,
      leftBorder: Char = '║',
      middleBorder: Char = '│',
      rightBorder: Char = '║'
  ) {
    val leftSpace = " ".repeat(leftWidth)
    val rightSpace = " ".repeat(rightWidth)
    lines.add("$leftBorder$leftSpace$middleBorder$rightSpace$rightBorder")
  }

  /**
   * Draw a table with headers and rows inside specified widths. Borders can be customized via
   * TableBorders.
   */
  fun drawTable(
      headers: List<String>,
      rows: List<List<String>>,
      colWidths: List<Int>,
      borders: TableBorders = TableBorders()
  ) {
    // Header top border line
    lines.add(buildTableLine(colWidths, borders.headerTop))

    // Header titles
    lines.add(buildTableRow(headers, colWidths, borders.headerRow))

    // Header bottom border line
    lines.add(buildTableLine(colWidths, borders.headerBottom))

    // Table rows
    rows.forEach { row -> lines.add(buildTableRow(row, colWidths, borders.row)) }
  }

  private fun buildTableLine(colWidths: List<Int>, borderChars: TableBorders.BorderChars): String {
    val sb = StringBuilder()
    sb.append(borderChars.left)
    for ((i, width) in colWidths.withIndex()) {
      sb.append(borderChars.fill.toString().repeat(width))
      sb.append(if (i == colWidths.lastIndex) borderChars.right else borderChars.middle)
    }
    return sb.toString()
  }

  private fun buildTableRow(
      cells: List<String>,
      colWidths: List<Int>,
      borderChars: TableBorders.BorderChars
  ): String {
    val sb = StringBuilder()
    sb.append(borderChars.left)
    for ((i, width) in colWidths.withIndex()) {
      val cellText = truncateOrPad(cells.getOrNull(i) ?: "", width)
      sb.append(cellText)
      sb.append(if (i == colWidths.lastIndex) borderChars.right else borderChars.middle)
    }
    return sb.toString()
  }

  /** Truncate text if longer than width, appending ellipsis, or pad with spaces if shorter. */
  private fun truncateOrPad(text: String, width: Int): String {
    return if (text.length > width) {
      if (width <= 1) "…" else text.take(width - 1) + "…"
    } else {
      text.padEnd(width)
    }
  }

  /** Render all accumulated lines as a single string with newlines. */
  fun render(): String = lines.joinToString("\n")

  /** Configuration for table border characters. */
  data class TableBorders(
      val headerTop: BorderChars = BorderChars('┌', '┬', '┐', '─'),
      val headerRow: BorderChars = BorderChars('│', '│', '│', ' '),
      val headerBottom: BorderChars = BorderChars('├', '┼', '┤', '─'),
      val row: BorderChars = BorderChars('│', '│', '│', ' ')
  ) {
    data class BorderChars(val left: Char, val middle: Char, val right: Char, val fill: Char)
  }
}
